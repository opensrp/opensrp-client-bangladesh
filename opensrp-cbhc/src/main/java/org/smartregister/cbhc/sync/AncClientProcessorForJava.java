package org.smartregister.cbhc.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.CoreLibrary;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.helper.ECSyncHelper;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.db.Address;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.db.Obs;
import org.smartregister.domain.jsonmapping.ClassificationRule;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.ClientField;
import org.smartregister.domain.jsonmapping.Column;
import org.smartregister.domain.jsonmapping.ColumnType;
import org.smartregister.domain.jsonmapping.JsonMapping;
import org.smartregister.domain.jsonmapping.Rule;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.util.AssetHandler;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 15/03/2018.
 */

public class AncClientProcessorForJava {

    private static final String TAG = AncClientProcessorForJava.class.getCanonicalName();
    private static AncClientProcessorForJava instance;
    private Map<String, Object> jsonMap = new HashMap<>();
    private Context mContext;
    protected static final String VALUES_KEY = "values";
    public AncClientProcessorForJava(Context context) {
        mContext = context;
    }

    public static AncClientProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new AncClientProcessorForJava(context);
        }

        return instance;
    }
    public void saveClientDetails(String baseEntityId, Map<String, String> values, Long timestamp) {

    }
    protected <T> T assetJsonToJava(String fileName, Class<T> clazz) {
        return AssetHandler.assetJsonToJava(jsonMap, mContext, fileName, clazz);
    }
    public void updateClientDetailsTable(Event event, Client client) {

    }

    public void processClient(List<EventClient> eventClients) throws Exception {

        ClientClassification clientClassification = assetJsonToJava("ec_client_classification.json", ClientClassification.class);

        if (!eventClients.isEmpty()) {
            List<Event> unsyncEvents = new ArrayList<>();
            for (EventClient eventClient : eventClients) {
                Event event = eventClient.getEvent();
                if (event == null) {
                    return;
                }

                String eventType = event.getEventType();
                if (eventType == null) {
                    continue;
                }

                if (eventType.equals(Constants.EventType.CLOSE)) {
                    unsyncEvents.add(event);
                } else if (eventType.equals(Constants.EventType.HouseholdREGISTRATION)
                        || eventType.equals(Constants.EventType.UPDATE_Household_REGISTRATION)
                        || eventType.equals(Constants.EventType.MemberREGISTRATION)
                        || eventType.equals(Constants.EventType.WomanMemberREGISTRATION)
                        || eventType.equals(Constants.EventType.Child_REGISTRATION)
                        || eventType.equals(Constants.EventType.UPDATE_REGISTRATION)
                        || !Utils.notFollowUp(eventType)
                ) {
                    if (clientClassification == null) {
                        continue;
                    }

                    Client client = eventClient.getClient();

                    //iterate through the events
                    if (client != null && event != null && clientClassification != null) {
                        if (!eventType.contains("Household")) {
                            if (client.getRelationships() != null) {
                                processEvent(event, client, clientClassification);
                            }
                        } else {
                            processEvent(event, client, clientClassification);
                        }


                    }
                }
            }

            // Unsync events that are should not be in this device
            if (!unsyncEvents.isEmpty()) {
                unSync(unsyncEvents);
            }
        }
    }
    /**
     * Call this method to flag the event as processed in the local repository.
     * All events valid or otherwise must be flagged to avoid re-processing
     * @param event
     */
    public void completeProcessing(Event event) {
        if (event == null)
            return;

        CoreLibrary.getInstance().context()
                .getEventClientRepository().markEventAsProcessed(event.getFormSubmissionId());
    }
    public Boolean processField(org.smartregister.domain.jsonmapping.Field field, Event event, Client client) {
        try {
            if (field == null) {
                return false;
            }

            // keep checking if the event data matches the values expected by each rule, break the
            // moment the rule fails
            String dataSegment = null;
            String fieldName = field.field;
            String fieldValue = field.field_value;
            String responseKey = null;

            if (fieldName != null && fieldName.contains(".")) {
                String fieldNameArray[] = fieldName.split("\\.");
                dataSegment = fieldNameArray[0];
                fieldName = fieldNameArray[1];
                String concept = field.concept;

                if (concept != null) {
                    fieldValue = concept;
                    responseKey = VALUES_KEY;
                }
            }

            List<String> createsCase = field.creates_case;
            List<String> closesCase = field.closes_case;

            // some fields are in the main doc e.g event_type so fetch them from the main doc
            if (StringUtils.isNotBlank(dataSegment)) {
                List<String> responseValues = field.values;
                Object dataSegmentObject = getValue(event, dataSegment);
                if (dataSegmentObject != null) {
                    if (dataSegmentObject instanceof List) {

                        List dataSegmentList = (List) dataSegmentObject;
                        // Iterate in the segment e.g obs segment
                        for (Object segment : dataSegmentList) {
                            // let's discuss this further, to get the real value in the doc we've to
                            // use the keys 'fieldcode' and 'value'
                            Object value = getValue(segment, fieldName);
                            String docSegmentFieldValue = value != null ? value.toString() : "";
                            Object values = getValue(segment, responseKey);
                            List<String> docSegmentResponseValues = new ArrayList<>();
                            if (values instanceof List) {
                                docSegmentResponseValues = getValues((List) value);
                            }

                            if (docSegmentFieldValue.equalsIgnoreCase(fieldValue) && (!Collections
                                    .disjoint(responseValues, docSegmentResponseValues))) {
                                // this is the event obs we're interested in put it in the respective
                                // bucket specified by type variable
                                processCaseModel(event, client, createsCase);
                                closeCase(client, closesCase);
                            }

                        }
                    } else if (dataSegmentObject instanceof Map) {
                        Map map = (Map) dataSegmentObject;
                        // This means field_value and response_key are null so pick the
                        // value from the json object for the field_name
                        if (map.containsKey(fieldName)) {
                            Object objectValue = map.get(fieldName);
                            if (objectValue != null && objectValue instanceof String) {
                                String docSegmentFieldValue = objectValue.toString();
                                if (docSegmentFieldValue.equalsIgnoreCase(fieldValue)) {
                                    processCaseModel(event, client, createsCase);
                                    closeCase(client, closesCase);
                                }
                            }
                        }
                    }
                }

            } else {
                //fetch from the main doc
                Object value = getValue(event, fieldName);
                String docSegmentFieldValue = value != null ? value.toString() : "";
                if (docSegmentFieldValue.equalsIgnoreCase(fieldValue)) {
                    processCaseModel(event, client, createsCase);
                    closeCase(client, closesCase);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }
    protected List<String> getValues(List list) {
        List<String> values = new ArrayList<String>();
        if (list == null) {
            return values;
        }
        for (Object o : list) {
            if (o != null) {
                values.add(o.toString());
            }
        }
        return values;
    }
    private Field getField(Class clazz, String fieldName) {
        if (clazz == null || StringUtils.isBlank(fieldName)) {
            return null;
        }

        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // No need to log this, log will be to big
        }
        if (field != null) {
            return field;
        }

        return getField(clazz.getSuperclass(), fieldName);
    }
    protected Object getValue(Object instance, String fieldName) {
        if (instance == null || StringUtils.isBlank(fieldName)) {
            return null;
        }
        try {
            Field field = getField(instance.getClass(), fieldName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
    public void closeCase(String tableName, String baseEntityId) {
        CommonRepository cr = org.smartregister.CoreLibrary.getInstance().context().commonrepository(tableName);
        cr.closeCase(baseEntityId, tableName);
    }
    public Boolean closeCase(Client client, List<String> closesCase) {
        try {
            if (closesCase == null || closesCase.isEmpty()) {
                return false;
            }

            String baseEntityId = client.getBaseEntityId();

            for (String tableName : closesCase) {
                closeCase(tableName, baseEntityId);
                updateFTSsearch(tableName, baseEntityId, null);
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }
    public Table getColumnMappings(String registerName) {
        try {
            ClientField clientField = assetJsonToJava(CoreLibrary.getInstance().getEcClientFieldsFile(), ClientField.class);
            if (clientField == null) {
                return null;
            }
            List<Table> bindObjects = clientField.bindobjects;
            for (Table bindObject : bindObjects) {
                if (bindObject.name.equalsIgnoreCase(registerName)) {
                    return bindObject;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }
    /**
     * Reformat the data to be persisted in the database.
     * This function will reformat dates with supplied types for storage in the DB
     * @param column
     * @param columnValue
     * @return
     */
    protected String getFormattedValue(Column column, String columnValue) {
        // covert the column if its a formatted column with both

        String dataType = StringUtils.isNotBlank(column.dataType) ? column.dataType : "";
        switch (dataType) {
            case ColumnType.Date:
                if (StringUtils.isNotBlank(column.saveFormat) && StringUtils.isNotBlank(column.sourceFormat)) {
                    try {
                        Date sourceDate = new SimpleDateFormat(column.sourceFormat, Locale.getDefault()).parse(columnValue);
                        return new SimpleDateFormat(column.saveFormat, Locale.getDefault()).format(sourceDate);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            case ColumnType.String:
                if (StringUtils.isNotBlank(column.saveFormat)) {
                    return String.format(column.saveFormat, columnValue);
                }
                break;
            default:
                return columnValue;
        }

        return columnValue;
    }
    private List<Field> getFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        if (instance == null) {
            return new ArrayList<>();
        }


        Class current = clazz;
        while (current != null) { // we don't want to process Object.class
            // do something with current's fields
            Field[] fieldArray = current.getDeclaredFields();
            if (fieldArray != null) {
                fields.addAll(Arrays.asList(fieldArray));
            }

            current = current.getSuperclass();
        }

        return fields;
    }
    public Map<String, String> getClientAddressAsMap(Client client) {
        Map<String, String> addressMap = new HashMap<String, String>();
        if (client == null) {
            return addressMap;
        }
        try {
            final String addressFieldsKey = "addressFields";

            List<Address> addressList = client.getAddresses();
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                Map<String, String> addressFieldMap = address.getAddressFields();
                if (addressFieldMap != null) {
                    for (Map.Entry<String, String> entry : addressFieldMap.entrySet()) {
                        addressMap.put(entry.getKey(), entry.getValue());
                    }
                }

                List<Field> fields = getFields(address.getClass());
                for (Field classField : fields) {
                    String fieldName = classField.getName();
                    if (!fieldName.equals(addressFieldsKey)) {
                        String value = getValueAsString(address, classField.getName());
                        if (value != null) {
                            addressMap.put(classField.getName(), value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return addressMap;
    }

    public void processCaseModel(Event event, Client client, Column column, ContentValues contentValues) {
        try {
            String expectedEncounterType = event.getEventType();
            String docType = column.type;
            String columnName = column.column_name;
            JsonMapping jsonMapping = column.json_mapping;
            String dataSegment = null;
            String fieldName = jsonMapping.field;
            String fieldValue = null;
            String responseKey = null;

            String valueField = jsonMapping.value_field;

            if (fieldName != null && fieldName.contains(".")) {
                String fieldNameArray[] = fieldName.split("\\.");
                dataSegment = fieldNameArray[0];
                fieldName = fieldNameArray[1];
                fieldValue = StringUtils.isNotBlank(jsonMapping.concept) ? jsonMapping.concept
                        : (StringUtils.isNotBlank(jsonMapping.formSubmissionField) ? jsonMapping
                        .formSubmissionField : null);
                if (fieldValue != null) {
                    responseKey = VALUES_KEY;
                }
            }

            Object document = docType == null ? event : docType.equalsIgnoreCase("Event") ? event : client;

            Object docSegment;

            if (StringUtils.isNotBlank(dataSegment)) {
                // pick data from a specific section of the doc
                docSegment = getValue(document, dataSegment);
            } else {
                // else the use the main doc as the doc segment
                docSegment = document;
            }

            // special handler needed to process address,
            if (dataSegment != null && dataSegment.equalsIgnoreCase("addresses")) {
                Map<String, String> addressMap = getClientAddressAsMap(client);
                if (addressMap.containsKey(fieldName)) {
                    contentValues.put(columnName, addressMap.get(fieldName));
                }
                return;
            }

            // special handler for relationalid
            if (dataSegment != null && dataSegment.equalsIgnoreCase("relationships") && document instanceof Client) {
                Map<String, List<String>> relationshipMap = client.getRelationships();

                List<String> relationShipIds = relationshipMap.get(fieldName);
                if (relationShipIds != null && !relationShipIds.isEmpty()) {
                    contentValues.put(columnName, relationShipIds.get(0));
                }

                return;
            }

            String encounterType = jsonMapping.event_type;

            if (docSegment instanceof List) {

                List docSegmentList = (List) docSegment;

                for (Object segment : docSegmentList) {
                    String columnValue = null;

                    if (fieldValue == null) {
                        // This means field_value and response_key are null so pick the
                        // value from the json object for the field_name
                        columnValue = getValueAsString(segment, fieldName);
                    } else {
                        // this means field_value and response_key are not null e.g when
                        // retrieving some value in the events obs section
                        String expectedFieldValue = getValueAsString(segment, fieldName);
                        // some events can only be differentiated by the event_type value
                        // eg pnc1,pnc2, anc1,anc2
                        // check if encountertype (the one in ec_client_fields.json) is
                        // null or it matches the encounter type from the ec doc we're
                        // processing
                        boolean encounterTypeMatches =
                                (encounterType == null) || (encounterType
                                        .equalsIgnoreCase(expectedEncounterType));

                        if (encounterTypeMatches && expectedFieldValue
                                .equalsIgnoreCase(fieldValue)) {

                            if (StringUtils.isNotBlank(valueField)) {
                                columnValue = getValueAsString(segment, valueField);
                            }

                            if (columnValue == null) {
                                Object values = getValue(segment, responseKey);
                                if (values instanceof List) {
                                    List<String> li = getValues((List) values);
                                    if (!li.isEmpty()) {
                                        columnValue = li.get(0);
                                    }
                                }
                            }
                        }
                    }

                    // after successfully retrieving the column name and value store it
                    // in Content value
                    if (columnValue != null) {
                        columnValue = getHumanReadableConceptResponse(columnValue, segment);
                        String formattedValue = getFormattedValue(column, columnValue);
                        contentValues.put(columnName, formattedValue);
                    }
                }

            } else if (docSegment instanceof Map) {
                Map map = (Map) docSegment;
                // This means field_value and response_key are null so pick the
                // value from the json object for the field_name
                if (fieldValue == null && map.containsKey(fieldName)) {
                    Object mapValue = map.get(fieldName);
                    if (mapValue != null) {
                        if (mapValue instanceof String) {
                            String columnValue = getHumanReadableConceptResponse(mapValue.toString(), docSegment);
                            contentValues.put(columnName, columnValue);
                        } else {
                            contentValues.put(columnName, String.valueOf(mapValue));
                        }
                    }
                }
            } else {
                //e.g client attributes section
                String columnValue = getValueAsString(docSegment, fieldName);

                // after successfully retrieving the column name and value store it in
                // Content value
                if (columnValue != null) {
                    columnValue = getHumanReadableConceptResponse(columnValue,
                            docSegment);
                    contentValues.put(columnName, columnValue);
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }
    /**
     * Get human readable values from the json doc humanreadablevalues key if the key is empty
     * return value
     *
     * @param value
     * @param object
     * @return
     * @throws Exception
     */
    protected String getHumanReadableConceptResponse(String value, Object object) {
        try {
            if (StringUtils.isBlank(value) || (object != null && !(object instanceof Obs))) {
                return value;
            }

            final String HUMAN_READABLE_VALUES = "humanReadableValues";
            List humanReadableValues = new ArrayList();
            Object humanReadableObject = getValue(object, HUMAN_READABLE_VALUES);
            if (humanReadableObject != null && humanReadableObject instanceof List) {
                humanReadableValues = (List) humanReadableObject;
            }

            if (object == null || humanReadableValues.isEmpty()) {
                String humanReadableValue = org.smartregister.CoreLibrary.getInstance().context().
                        customHumanReadableConceptResponse().get(value);

                if (StringUtils.isNotBlank(humanReadableValue)) {
                    return humanReadableValue;
                }

                return value;
            }

            return humanReadableValues.size() == 1 ? humanReadableValues.get(0).toString()
                    : humanReadableValues.toString();
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return value;
    }
    protected String getValueAsString(Object instance, String fieldName) {
        Object object = getValue(instance, fieldName);
        if (object != null) {
            return object.toString();
        }
        return null;
    }
    public Boolean processCaseModel(Event event, Client client, List<String> createsCase) {
        try {

            if (createsCase == null || createsCase.isEmpty()) {
                return false;
            }
            for (String clientType : createsCase) {
                Table table = getColumnMappings(clientType);
                List<Column> columns = table.columns;
                String baseEntityId = client != null ? client.getBaseEntityId() : event != null ? event.getBaseEntityId() : null;

                ContentValues contentValues = new ContentValues();
                //Add the base_entity_id
                contentValues.put("base_entity_id", baseEntityId);
                contentValues.put("is_closed", 0);

                for (Column colObject : columns) {
                    processCaseModel(event, client, colObject, contentValues);
                }

                // Modify openmrs generated identifier, Remove hyphen if it exists
                updateIdenitifier(contentValues);

                // save the values to db
                executeInsertStatement(contentValues, clientType);

                updateFTSsearch(clientType, baseEntityId, contentValues);
                Long timestamp = getEventDate(event.getEventDate());
                //addContentValuesToDetailsTable(contentValues, timestamp);
                //updateClientDetailsTable(event, client);
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);

            return null;
        }
    }
    private long getEventDate(DateTime eventDate) {
        if (eventDate == null) {
            return new Date().getTime();
        } else {
            return eventDate.getMillis();
        }
    }
    /**
     * Insert the a new record to the database and returns its id
     **/
    public Long executeInsertStatement(ContentValues values, String tableName) {
        if("ec_details".equalsIgnoreCase(tableName))return -1l;
        CommonRepository cr = org.smartregister.CoreLibrary.getInstance().context().commonrepository(tableName);
        return cr.executeInsertStatement(values, tableName);
    }
    /**
     * Update given OPENMRS identifier, removes hyphen
     *
     * @param values
     */
    private void updateIdenitifier(ContentValues values) {
        try {
            for (String identifier : getOpenmrsGenIds()) {
                Object value = values.get(identifier); //TODO
                if (value != null) {
                    String sValue = value.toString();
                    if (value instanceof String && StringUtils.isNotBlank(sValue)) {
                        values.remove(identifier);
                        values.put(identifier, sValue.replace("-", ""));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }
    public Boolean processClientClass(ClassificationRule clientClass, Event event, Client client) {
        try {
            if (clientClass == null) {
                return false;
            }

            if (event == null) {
                return false;
            }

            if (client == null) {
                return false;
            }

            Rule rule = clientClass.rule;
            List<org.smartregister.domain.jsonmapping.Field> fields = rule.fields;

            for (org.smartregister.domain.jsonmapping.Field field : fields) {
                processField(field, event, client);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return null;
        }
    }
    public Boolean processEvent(Event event, Client client, ClientClassification clientClassification) throws Exception {
        try {
            // mark event as processed regardless of any errors
            completeProcessing(event);

            if (event.getCreator() != null) {
                Log.i(TAG, "EVENT from openmrs");
            }
            // For data integrity check if a client exists, if not pull one from cloudant and
            // insert in drishti sqlite db

            if (client == null) {
                return false;
            }

            // Get the client type classification
            List<ClassificationRule> clientClasses = clientClassification.case_classification_rules;
            if (clientClasses == null || clientClasses.isEmpty()) {
                return false;
            }

            // Check if child is deceased and skip
            if (client.getDeathdate() != null) {
                return false;
            }

            for (ClassificationRule clientClass : clientClasses) {
                processClientClass(clientClass, event, client);
            }

            // Incase the details have not been updated
//            String updatedString = event.getDetails() != null ? event.getDetails().get(detailsUpdated) : null;
//            if (StringUtils.isBlank(updatedString) || !Boolean.TRUE.toString().equals(updatedString)) {
//                updateClientDetailsTable(event, client);
//            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }
    /*
        private Integer parseInt(String string) {
            try {
                return Integer.valueOf(string);
            } catch (NumberFormatException e) {
Utils.appendLog(getClass().getName(),e);
                Log.e(TAG, e.toString(), e);
            }
            return null;
        }

        private ContentValues processCaseModel(EventClient eventClient, Table table) {
            try {
                List<Column> columns = table.columns;
                ContentValues contentValues = new ContentValues();

                for (Column column : columns) {
                    processCaseModel(eventClient.getEvent(), eventClient.getClient(), column, contentValues);
                }

                return contentValues;
            } catch (Exception e) {
Utils.appendLog(getClass().getName(),e);
                Log.e(TAG, e.toString(), e);
            }
            return null;
        }

        private Date getDate(String eventDateStr) {
            Date date = null;
            if (StringUtils.isNotBlank(eventDateStr)) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
                    date = dateFormat.parse(eventDateStr);
                } catch (ParseException e) {
Utils.appendLog(getClass().getName(),e);
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        date = dateFormat.parse(eventDateStr);
                    } catch (ParseException pe) {
                        try {
                            date = DateUtil.parseDate(eventDateStr);
                        } catch (ParseException pee) {
                            Log.e(TAG, pee.toString(), pee);
                        }
                    }
                }
            }
            return date;
        }

    */
    private boolean unSync(ECSyncHelper ecSyncHelper, DetailsRepository detailsRepository, List<Table> bindObjects, Event event, String registeredAnm) {
        try {
            String baseEntityId = event.getBaseEntityId();
            String providerId = event.getProviderId();

            if (providerId.equals(registeredAnm)) {
                boolean eventDeleted = ecSyncHelper.deleteEventsByBaseEntityId(baseEntityId);
                boolean clientDeleted = ecSyncHelper.deleteClient(baseEntityId);
                Log.d(getClass().getName(), "EVENT_DELETED: " + eventDeleted);
                Log.d(getClass().getName(), "ClIENT_DELETED: " + clientDeleted);

//                boolean detailsDeleted = detailsRepository.deleteDetails(baseEntityId);
//                Log.d(getClass().getName(), "DETAILS_DELETED: " + detailsDeleted);

                for (Table bindObject : bindObjects) {
                    String tableName = bindObject.name;

                    boolean caseDeleted = deleteCase(tableName, baseEntityId);
                    Log.d(getClass().getName(), "CASE_DELETED: " + caseDeleted);
                }

                return true;
            }
        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.toString(), e);
        }
        return false;
    }
    public boolean deleteCase(String tableName, String baseEntityId) {
        CommonRepository cr = org.smartregister.CoreLibrary.getInstance().context().commonrepository(tableName);
        return cr.deleteCase(baseEntityId, tableName);
    }
    private boolean unSync(List<Event> events) {
        try {

            if (events == null || events.isEmpty()) {
                return false;
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            String registeredAnm = allSharedPreferences.fetchRegisteredANM();

            ClientField clientField = assetJsonToJava("ec_client_fields.json", ClientField.class);
            if (clientField == null) {
                return false;
            }

            List<Table> bindObjects = clientField.bindobjects;
//            DetailsRepository detailsRepository = AncApplication.getInstance().getContext().detailsRepository();
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(getContext());

            for (Event event : events) {
//                unSync(ecUpdater, detailsRepository, bindObjects, event, registeredAnm);
                unSync(ecUpdater, null, bindObjects, event, registeredAnm);
            }

            return true;

        } catch (Exception e) {
            Utils.appendLog(getClass().getName(), e);
            Log.e(TAG, e.toString(), e);
        }

        return false;
    }

    public Context getContext() {
        return mContext;
    }



    public String[] getOpenmrsGenIds() {
        return new String[]{DBConstants.KEY.ANC_ID};
    }


    public void updateFTSsearch(String tableName, String entityId, ContentValues contentValues) {

        Log.i(TAG, "Starting updateFTSsearch table: " + tableName);

        AllCommonsRepository allCommonsRepository = org.smartregister.CoreLibrary.getInstance().context().
                allCommonsRepositoryobjects(tableName);

        if (allCommonsRepository != null) {
            allCommonsRepository.updateSearch(entityId);
        }

        Log.i(TAG, "Finished updateFTSsearch table: " + tableName);
    }
}
