package org.smartregister.cbhc.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.common.reflect.TypeToken;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.activity.AncJsonFormActivity;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.FormLocation;
import org.smartregister.cbhc.domain.QuickCheck;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.helper.LocationHelper;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.view.LocationPickerView;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.FormEntityConstants;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.configurableviews.model.Field;
import org.smartregister.domain.Photo;
import org.smartregister.domain.ProfileImage;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by keyman on 27/06/2018.
 */
public class JsonFormUtils extends org.smartregister.util.JsonFormUtils {
    private static final String TAG = JsonFormUtils.class.getCanonicalName();

    public static final String METADATA = "metadata";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    public static final String CURRENT_OPENSRP_ID = "current_opensrp_id";
    public static final String ANC_ID = "ANC_ID";
    public static final String READ_ONLY = "read_only";

    private static final String FORM_SUBMISSION_FIELD = "formsubmissionField";
    private static final String TEXT_DATA_TYPE = "text";
    private static final String SELECT_ONE_DATA_TYPE = "select one";
    private static final String SELECT_MULTIPLE_DATA_TYPE = "select multiple";

    public static final int REQUEST_CODE_GET_JSON = 3432;

    public static JSONObject getFormAsJson(JSONObject form,
                                           String formName, String id,
                                           String currentLocationId,String HouseholdEnitityID) throws Exception {
        if (form == null) {
            return null;
        }

        String entityId = id;
        form.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);

        if (Constants.JSON_FORM.ANC_REGISTER.equals(formName)) {
            if (StringUtils.isNotBlank(entityId)) {
                entityId = entityId.replace("-", "");
            }

            // Inject opensrp id into the form
            JSONArray field = fields(form);
            JSONObject ancId = getFieldJSONObject(field, DBConstants.KEY.ANC_ID);
            if (ancId != null) {
                ancId.remove(JsonFormUtils.VALUE);
                ancId.put(JsonFormUtils.VALUE, entityId);
            }

        } else if (Constants.JSON_FORM.ANC_CLOSE.equals(formName)) {
            if (StringUtils.isNotBlank(entityId)) {
                // Inject entity id into the remove form
                form.remove(JsonFormUtils.ENTITY_ID);
                form.put(JsonFormUtils.ENTITY_ID, entityId);
            }
        }else if (Constants.JSON_FORM.MEMBER_REGISTER.equals(formName)) {


            if (StringUtils.isNotBlank(entityId)) {
                entityId = entityId.replace("-", "");
            }

            // Inject opensrp id into the form
            JSONArray field = fields(form);
            JSONObject ancId = getFieldJSONObject(field,"Patient_Identifier");
            if (ancId != null) {
                ancId.remove(JsonFormUtils.VALUE);
                ancId.put(JsonFormUtils.VALUE, entityId);
            }
            JSONObject metaDataJson = form.getJSONObject("metadata");
            JSONObject lookup = metaDataJson.getJSONObject("look_up");
            lookup.put("entity_id", "household");
            lookup.put("value", HouseholdEnitityID);

        } else {
            Log.w(TAG, "Unsupported form requested for launch " + formName);
            if (StringUtils.isNotBlank(entityId)) {
                entityId = entityId.replace("-", "");
            }

            // Inject opensrp id into the form
            JSONArray field = fields(form);
            JSONObject ancId = getFieldJSONObject(field, "Patient_Identifier");
            if (ancId != null) {
                ancId.remove(JsonFormUtils.VALUE);
                ancId.put(JsonFormUtils.VALUE, entityId);
            }
            JsonFormUtils.addWomanRegisterHierarchyQuestions(form);

        }
        Log.d(TAG, "form is " + form.toString());
        return form;
    }

    protected static Triple<Boolean, JSONObject, JSONArray> validateParameters(String jsonString) {

        JSONObject jsonForm = toJSONObject(jsonString);
        JSONArray fields = fields(jsonForm);

        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = Triple.of(jsonForm != null && fields != null, jsonForm, fields);
        return registrationFormParams;
    }

    public static Pair<Client, Event> processRegistrationForm(AllSharedPreferences allSharedPreferences, String jsonString) {

        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }

            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();

            String entityId = getString(jsonForm, ENTITY_ID);
            if (StringUtils.isBlank(entityId)) {
                entityId = generateRandomUUIDString();
            }

            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            JSONObject metadata = getJSONObject(jsonForm, METADATA);

            // String lastLocationName = null;
            // String lastLocationId = null;
            // TODO Replace values for location questions with their corresponding location IDs


            JSONObject lastInteractedWith = new JSONObject();
            lastInteractedWith.put(Constants.KEY.KEY, DBConstants.KEY.LAST_INTERACTED_WITH);
            lastInteractedWith.put(Constants.KEY.VALUE, Calendar.getInstance().getTimeInMillis());
            fields.put(lastInteractedWith);

            JSONObject dobUnknownObject = getFieldJSONObject(fields, DBConstants.KEY.DOB_UNKNOWN);
            JSONArray options = getJSONArray(dobUnknownObject, Constants.JSON_FORM_KEY.OPTIONS);
            JSONObject option = getJSONObject(options, 0);
            String dobUnKnownString = option != null ? option.getString(VALUE) : null;
            if (StringUtils.isNotBlank(dobUnKnownString) && Boolean.valueOf(dobUnKnownString)) {

                String ageString = getFieldValue(fields, DBConstants.KEY.AGE);
                if (StringUtils.isNotBlank(ageString) && NumberUtils.isNumber(ageString)) {
                    int age = Integer.valueOf(ageString);
                    JSONObject dobJSONObject = getFieldJSONObject(fields, DBConstants.KEY.DOB);
                    dobJSONObject.put(VALUE, Utils.getDob(age));

                    //Mark the birth date as an approximation
                    JSONObject isBirthdateApproximate = new JSONObject();
                    isBirthdateApproximate.put(Constants.KEY.KEY, FormEntityConstants.Person.birthdate_estimated);
                    isBirthdateApproximate.put(Constants.KEY.VALUE, Constants.BOOLEAN_INT.TRUE);
                    isBirthdateApproximate.put(Constants.OPENMRS.ENTITY, Constants.ENTITY.PERSON);//Required for value to be processed
                    isBirthdateApproximate.put(Constants.OPENMRS.ENTITY_ID, FormEntityConstants.Person.birthdate_estimated);
                    fields.put(isBirthdateApproximate);

                }
            }

            FormTag formTag = new FormTag();
            formTag.providerId = allSharedPreferences.fetchRegisteredANM();
            formTag.appVersion = BuildConfig.VERSION_CODE;
            formTag.databaseVersion = BuildConfig.DATABASE_VERSION;



            ArrayList<Address> adresses = new ArrayList<Address>();
            Address address1 = new Address();
            try{
                for (int i = 0; i < fields.length(); i++) {
                    String key = fields.getJSONObject(i).getString("key");

                    if (key.equals("HIE_FACILITIES")) {
                        if (!TextUtils.isEmpty(fields.getJSONObject(i).getString("value"))) {
                            String address = fields.getJSONObject(i).getString("value");
                            address = address.replace("[", "").replace("]", "");
                            String[] addressStringArray = address.split(",");
                            if (addressStringArray.length > 0) {
                                address1.setAddressType("usual_residence");
                                address1.addAddressField("country", addressStringArray[0].replaceAll("^\"|\"$", ""));
                                address1.addAddressField("stateProvince", addressStringArray[1].replaceAll("^\"|\"$", ""));
                                address1.addAddressField("countyDistrict", addressStringArray[2].replaceAll("^\"|\"$", ""));
                                address1.addAddressField("cityVillage", addressStringArray[3].replaceAll("^\"|\"$", ""));
                                address1.addAddressField("address1", addressStringArray[4].replaceAll("^\"|\"$", ""));
                                address1.addAddressField("address2", addressStringArray[5].replaceAll("^\"|\"$", ""));
                                address1.addAddressField("address3", addressStringArray[6].replaceAll("^\"|\"$", ""));
                                address1.addAddressField("address4", addressStringArray[7].replaceAll("^\"|\"$", ""));
                            }
                            Log.v("address", address);

                        }
                    }
                }
            }catch (Exception e){

            }



            Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields, formTag, entityId);

            if(baseClient.getGender()==null) {
                baseClient.setGender("m");
            }

            adresses.add(address1);
            baseClient.setAddresses(adresses);


            JSONObject lookUpJSONObject = getJSONObject(metadata, "look_up");
            String lookUpEntityId = "";
            String lookUpBaseEntityId = "";
            if (lookUpJSONObject != null) {
                lookUpEntityId = getString(lookUpJSONObject, "entity_id");
                lookUpBaseEntityId = getString(lookUpJSONObject, "value");
            }

            if (lookUpEntityId.equals("household") && StringUtils.isNotBlank(lookUpBaseEntityId)) {
                Client ss = new Client(lookUpBaseEntityId);
                Context context = AncApplication.getInstance().getContext().applicationContext();
                addRelationship(context, ss, baseClient);
                SQLiteDatabase db = AncApplication.getInstance().getRepository().getReadableDatabase();
                AncRepository pathRepository = new AncRepository(context,AncApplication.getInstance().getContext());
                EventClientRepository eventClientRepository = new EventClientRepository(pathRepository);
                JSONObject clientjson = eventClientRepository.getClient(db, lookUpBaseEntityId);
                baseClient.setAddresses(getAddressFromClientJson(clientjson));
            }

            Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, metadata, formTag, entityId, encounterType, DBConstants.WOMAN_TABLE_NAME);

            JsonFormUtils.tagSyncMetadata(allSharedPreferences, baseEvent);// tag docs

            return Pair.create(baseClient, baseEvent);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    private static ArrayList<Address> getAddressFromClientJson(JSONObject clientjson) {
        ArrayList<Address> addresses = new ArrayList<Address>();
        try {
            JSONArray addressArray = clientjson.getJSONArray("addresses");
            for(int i = 0 ;i<addressArray.length();i++){
                Address address = new Address();
                address.setAddressType(addressArray.getJSONObject(i).getString("addressType"));
                JSONObject addressfields = addressArray.getJSONObject(i).getJSONObject("addressFields");

                Iterator<?> keys = addressfields.keys();

                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    if ( addressfields.get(key) instanceof String ) {
                        address.addAddressField(key,addressfields.getString(key));
                    }
                }
                addresses.add(address);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    private static void addRelationship(Context context, Client parent, Client child) {
        try {
            String relationships = AssetHandler.readFileFromAssetsFolder(FormUtils.ecClientRelationships, context);
            JSONArray jsonArray = null;

            jsonArray = new JSONArray(relationships);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject rObject = jsonArray.getJSONObject(i);
                if (rObject.has("field") && getString(rObject, "field").equals(ENTITY_ID)) {
                    child.addRelationship(rObject.getString("client_relationship"), parent.getBaseEntityId());
                } /* else {
                    //TODO how to add other kind of relationships
                  } */
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    public static void mergeAndSaveClient(ECSyncHelper ecUpdater, Client baseClient) throws Exception {
        JSONObject updatedClientJson = new JSONObject(org.smartregister.util.JsonFormUtils.gson.toJson(baseClient));

        JSONObject originalClientJsonObject = ecUpdater.getClient(baseClient.getBaseEntityId());

        JSONObject mergedJson = org.smartregister.util.JsonFormUtils.merge(originalClientJsonObject, updatedClientJson);

        //TODO Save edit log ?

        ecUpdater.addClient(baseClient.getBaseEntityId(), mergedJson);
    }

    public static void saveImage(String providerId, String entityId, String imageLocation) {
        if (StringUtils.isBlank(imageLocation)) {
            return;
        }

        File file = new File(imageLocation);

        if (!file.exists()) {
            return;
        }

        Bitmap compressedImageFile = AncApplication.getInstance().getCompressor().compressToBitmap(file);
        saveStaticImageToDisk(compressedImageFile, providerId, entityId);

    }

    private static void saveStaticImageToDisk(Bitmap image, String providerId, String entityId) {
        if (image == null || StringUtils.isBlank(providerId) || StringUtils.isBlank(entityId)) {
            return;
        }
        OutputStream os = null;
        try {

            if (entityId != null && !entityId.isEmpty()) {
                final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId + ".JPEG";

                File outputFile = new File(absoluteFileName);
                os = new FileOutputStream(outputFile);
                Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                if (compressFormat != null) {
                    image.compress(compressFormat, 100, os);
                } else {
                    throw new IllegalArgumentException("Failed to save static image, could not retrieve image compression format from name "
                            + absoluteFileName);
                }
                // insert into the db
                ProfileImage profileImage = new ProfileImage();
                profileImage.setImageid(UUID.randomUUID().toString());
                profileImage.setAnmId(providerId);
                profileImage.setEntityID(entityId);
                profileImage.setFilepath(absoluteFileName);
                profileImage.setFilecategory("profilepic");
                profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
                ImageRepository imageRepo = AncApplication.getInstance().getContext().imageRepository();
                imageRepo.add(profileImage);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to save static image to disk");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close static images output stream after attempting to write image");
                }
            }
        }

    }

    public static String getString(String jsonString, String field) {
        return getString(toJSONObject(jsonString), field);
    }

    public static String getFieldValue(String jsonString, String key) {
        JSONObject jsonForm = toJSONObject(jsonString);
        if (jsonForm == null) {
            return null;
        }

        JSONArray fields = fields(jsonForm);
        if (fields == null) {
            return null;
        }

        return getFieldValue(fields, key);

    }

    public static JSONObject getFieldJSONObject(JSONArray jsonArray, String key) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = getJSONObject(jsonArray, i);
            String keyVal = getString(jsonObject, KEY);
            if (keyVal != null && keyVal.equals(key)) {
                return jsonObject;
            }
        }
        return null;
    }

    public static String getAutoPopulatedJsonEditFormString(Context context, Map<String, String> womanClient) {
        try {
            JSONObject form = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.ANC_REGISTER);
            LocationPickerView lpv = new LocationPickerView(context);
            lpv.init();
            JsonFormUtils.addWomanRegisterHierarchyQuestions(form);
            Log.d(TAG, "Form is " + form.toString());
            if (form != null) {
                form.put(JsonFormUtils.ENTITY_ID, womanClient.get(DBConstants.KEY.BASE_ENTITY_ID));
                form.put(JsonFormUtils.ENCOUNTER_TYPE, Constants.EventType.UPDATE_REGISTRATION);

                JSONObject metadata = form.getJSONObject(JsonFormUtils.METADATA);
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

                metadata.put(JsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

                form.put(JsonFormUtils.CURRENT_OPENSRP_ID, womanClient.get(DBConstants.KEY.ANC_ID).replace("-", ""));

                //inject opensrp id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    processPopulatableFields(womanClient, jsonObject);

                }

                return form.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return "";
    }

    protected static void processPopulatableFields(Map<String, String> womanClient, JSONObject jsonObject) throws JSONException {


        if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB) && !Boolean.valueOf(womanClient.get(DBConstants.KEY.DOB_UNKNOWN))) {

            String dobString = womanClient.get(DBConstants.KEY.DOB);
            Date dob = Utils.dobStringToDate(dobString);
            if (dob != null) {
                jsonObject.put(JsonFormUtils.VALUE, DATE_FORMAT.format(dob));
            }

        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.HOME_ADDRESS)) {

            String homeAddress = womanClient.get(DBConstants.KEY.HOME_ADDRESS);
            jsonObject.put(JsonFormUtils.VALUE, homeAddress);
            jsonObject.toString();

            List<String> healthFacilityHierarchy = new ArrayList<>();
            String address5 = womanClient.get(DBConstants.KEY.HOME_ADDRESS);
            healthFacilityHierarchy.add(address5);

            String schoolFacilityHierarchyString = AssetHandler.javaToJsonString(healthFacilityHierarchy, new TypeToken<List<String>>() {
            }.getType());

            if (StringUtils.isNotBlank(schoolFacilityHierarchyString)) {
                jsonObject.put(JsonFormUtils.VALUE, schoolFacilityHierarchyString);
            }

        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.KEY.PHOTO)) {

            Photo photo = ImageUtils.profilePhotoByClientID(womanClient.get(DBConstants.KEY.BASE_ENTITY_ID));

            if (StringUtils.isNotBlank(photo.getFilePath())) {

                jsonObject.put(JsonFormUtils.VALUE, photo.getFilePath());

            }
        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB_UNKNOWN)) {

            jsonObject.put(JsonFormUtils.READ_ONLY, false);
            JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
            optionsObject.put(JsonFormUtils.VALUE, womanClient.get(DBConstants.KEY.DOB_UNKNOWN));

        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.AGE)) {

            jsonObject.put(JsonFormUtils.READ_ONLY, false);
            jsonObject.put(JsonFormUtils.VALUE, Utils.getAgeFromDate(womanClient.get(DBConstants.KEY.DOB)));

        } else if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.ANC_ID)) {

            jsonObject.put(JsonFormUtils.VALUE, womanClient.get(DBConstants.KEY.ANC_ID).replace("-", ""));

        } else if (womanClient.containsKey(jsonObject.getString(JsonFormUtils.KEY))) {

            jsonObject.put(JsonFormUtils.READ_ONLY, false);
            jsonObject.put(JsonFormUtils.VALUE, womanClient.get(jsonObject.getString(JsonFormUtils.KEY)));
        } else {
            Log.e(TAG, "ERROR:: Unprocessed Form Object Key " + jsonObject.getString(JsonFormUtils.KEY));
        }
    }

    public static void addWomanRegisterHierarchyQuestions(JSONObject form) {
        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            ArrayList<String> allLevels = new ArrayList<>();
            allLevels.add("Country");
            allLevels.add("Division");
            allLevels.add("District");
            allLevels.add("Upazilla");
            allLevels.add("Union");
            allLevels.add("Ward");
            allLevels.add("Subunit");
            allLevels.add("EPI center");


            ArrayList<String> healthFacilities = new ArrayList<>();
            healthFacilities.add("Country");
            healthFacilities.add("Division");
            healthFacilities.add("District");
            healthFacilities.add("Upazilla");
            healthFacilities.add("Union");
            healthFacilities.add("Ward");
            healthFacilities.add("Subunit");
            healthFacilities.add("EPI center");


            ArrayList<String> defaultFacilities = new ArrayList<>();
            healthFacilities.add("Country");
            healthFacilities.add("Division");
            healthFacilities.add("District");
            healthFacilities.add("Upazilla");
            healthFacilities.add("Union");
            healthFacilities.add("Ward");
            healthFacilities.add("Subunit");
            healthFacilities.add("EPI center");


            List<String> defaultFacility = LocationHelper.getInstance().generateDefaultLocationHierarchy(healthFacilities);
            List<FormLocation> upToFacilities = LocationHelper.getInstance().generateLocationHierarchyTree(false, healthFacilities);

            String defaultFacilityString = AssetHandler.javaToJsonString(defaultFacility,
                    new TypeToken<List<String>>() {
                    }.getType());

            String upToFacilitiesString = AssetHandler.javaToJsonString(upToFacilities,
                    new TypeToken<List<FormLocation>>() {
                    }.getType());

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString(Constants.KEY.KEY).equalsIgnoreCase("HIE_FACILITIES")) {
                    if (StringUtils.isNotBlank(upToFacilitiesString)) {
                        questions.getJSONObject(i).put(Constants.KEY.TREE, new JSONArray(upToFacilitiesString));
                    }
                    if (StringUtils.isNotBlank(defaultFacilityString)) {
                        questions.getJSONObject(i).put(Constants.KEY.DEFAULT, defaultFacilityString);
                    }
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    public static void startFormForEdit(Activity context, int jsonFormActivityRequestCode, String metaData) {
        Intent intent = new Intent(context, AncJsonFormActivity.class);
        intent.putExtra(Constants.INTENT_KEY.JSON, metaData);

        Log.d(TAG, "form is " + metaData);

        context.startActivityForResult(intent, jsonFormActivityRequestCode);

    }

    public static Triple<Boolean, Event, Event> saveRemovedFromANCRegister(
            AllSharedPreferences allSharedPreferences,
            String jsonString, String providerId) {

        try {

            boolean isDeath = false;
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }

            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();

            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            JSONObject metadata = getJSONObject(jsonForm, METADATA);

            String encounterLocation = null;

            try {
                encounterLocation = metadata.getString(Constants.JSON_FORM_KEY.ENCOUNTER_LOCATION);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            Date encounterDate = new Date();
            String entityId = getString(jsonForm, ENTITY_ID);

            Event event = (Event) new Event()
                    .withBaseEntityId(entityId) //should be different for main and subform
                    .withEventDate(encounterDate)
                    .withEventType(encounterType)
                    .withLocationId(encounterLocation)
                    .withProviderId(providerId)
                    .withEntityType(DBConstants.WOMAN_TABLE_NAME)
                    .withFormSubmissionId(generateRandomUUIDString())
                    .withDateCreated(new Date());
            JsonFormUtils.tagSyncMetadata(allSharedPreferences, event);

            for (int i = 0; i < fields.length(); i++) {
                JSONObject jsonObject = getJSONObject(fields, i);

                String value = getString(jsonObject, VALUE);
                if (StringUtils.isNotBlank(value)) {
                    addObservation(event, jsonObject);
                    if (jsonObject.get(JsonFormUtils.KEY).equals(Constants.JSON_FORM_KEY.ANC_CLOSE_REASON)) {
                        isDeath = "Woman Died".equalsIgnoreCase(value);
                    }
                }
            }

            if (metadata != null) {
                Iterator<?> keys = metadata.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    JSONObject jsonObject = getJSONObject(metadata, key);
                    String value = getString(jsonObject, VALUE);
                    if (StringUtils.isNotBlank(value)) {
                        String entityVal = getString(jsonObject, OPENMRS_ENTITY);
                        if (entityVal != null) {
                            if (entityVal.equals(CONCEPT)) {
                                addToJSONObject(jsonObject, KEY, key);
                                addObservation(event, jsonObject);

                            } else if (entityVal.equals(ENCOUNTER)) {
                                String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
                                if (entityIdVal.equals(FormEntityConstants.Encounter.encounter_date.name())) {
                                    Date eDate = formatDate(value, false);
                                    if (eDate != null) {
                                        event.setEventDate(eDate);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Update Child Entity to include death date
            Event updateChildDetailsEvent = (Event) new Event()
                    .withBaseEntityId(entityId) //should be different for main and subform
                    .withEventDate(encounterDate)
                    .withEventType(Constants.EventType.UPDATE_REGISTRATION)
                    .withLocationId(encounterLocation)
                    .withProviderId(providerId)
                    .withEntityType(DBConstants.WOMAN_TABLE_NAME)
                    .withFormSubmissionId(generateRandomUUIDString())
                    .withDateCreated(new Date());
            JsonFormUtils.tagSyncMetadata(allSharedPreferences, updateChildDetailsEvent);

            return Triple.of(isDeath, event, updateChildDetailsEvent);

        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public static Event createQuickCheckEvent(AllSharedPreferences allSharedPreferences, QuickCheck quickCheck, String baseEntityId) {

        try {

            Field selectedReason = quickCheck.getSelectedReason();
            Set<Field> selectedComplaints = quickCheck.getSpecificComplaints();
            Set<Field> selectedDangerSigns = quickCheck.getSelectedDangerSigns();
            String specify = quickCheck.getOtherSpecify();


            Event event = (Event) new Event()
                    .withBaseEntityId(baseEntityId)
                    .withEventDate(new Date())
                    .withEventType(Constants.EventType.QUICK_CHECK)
                    .withEntityType(DBConstants.WOMAN_TABLE_NAME)
                    .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                    .withDateCreated(new Date());

            if (selectedReason != null) {
                event.addObs(createObs("contact_reason", SELECT_ONE_DATA_TYPE, selectedReason.getDisplayName()));
            }

            if (selectedComplaints != null && !selectedComplaints.isEmpty()) {
                event.addObs(createObs("specific_complaint", SELECT_MULTIPLE_DATA_TYPE, selectedComplaints));
            }

            if (StringUtils.isNotBlank(specify)) {
                event.addObs(createObs("specific_complaint_other", TEXT_DATA_TYPE, specify));
            }

            if (selectedDangerSigns != null && !selectedDangerSigns.isEmpty()) {
                event.addObs(createObs("danger_signs", SELECT_MULTIPLE_DATA_TYPE, selectedDangerSigns));
            }

            if (quickCheck.getHasDangerSigns()) {
                String value = quickCheck.getProceedRefer() ? quickCheck.getProceedToContact() : quickCheck.getReferAndCloseContact();
                event.addObs(createObs("danger_signs_proceed", SELECT_ONE_DATA_TYPE, value));

                if (!quickCheck.getProceedRefer()) {
                    value = quickCheck.getTreat() ? quickCheck.getYes() : quickCheck.getNo();
                    event.addObs(createObs("danger_signs_treat", SELECT_ONE_DATA_TYPE, value));
                }
            }

            JsonFormUtils.tagSyncMetadata(allSharedPreferences, event);

            return event;

        } catch (
                Exception e)

        {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }

    }

    private static Obs createObs(String formSubmissionField, String dataType, Set<Field> fieldList) {
        List<Object> vall = new ArrayList<>();
        for (Field field : fieldList) {
            vall.add(field.getDisplayName());
        }
        return new Obs(FORM_SUBMISSION_FIELD, dataType, formSubmissionField,
                "", vall, new ArrayList<>(), null, formSubmissionField);
    }

    private static Obs createObs(String formSubmissionField, String dataType, String value) {
        List<Object> vall = new ArrayList<>();
        vall.add(value);
        return new Obs(FORM_SUBMISSION_FIELD, dataType, formSubmissionField,
                "", vall, new ArrayList<>(), null, formSubmissionField);
    }

    private static Event tagSyncMetadata(AllSharedPreferences allSharedPreferences, Event event) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(allSharedPreferences.fetchDefaultLocalityId(providerId));
        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));
        return event;
    }

    public static void launchANCCloseForm(Activity activity) {
        try {
            Intent intent = new Intent(activity, AncJsonFormActivity.class);

            JSONObject form = FormUtils.getInstance(activity).getFormJson(Constants.JSON_FORM.ANC_CLOSE);
            if (form != null) {
                form.put(Constants.JSON_FORM_KEY.ENTITY_ID, activity.getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID));
                intent.putExtra(Constants.INTENT_KEY.JSON, form.toString());
                activity.startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
