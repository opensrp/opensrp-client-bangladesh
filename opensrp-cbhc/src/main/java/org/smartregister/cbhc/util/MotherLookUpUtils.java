package org.smartregister.cbhc.util;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.Context;
import org.smartregister.cbhc.domain.EntityLookUp;
import org.smartregister.cbhc.util.DBConstants;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.event.Listener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;
import static android.view.View.combineMeasuredStates;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.smartregister.util.Utils.getValue;
import static org.smartregister.util.Utils.startAsyncTask;

/**
 * Created by keyman on 26/01/2017.
 */
public class MotherLookUpUtils {
    private static final String TAG = MotherLookUpUtils.class.getName();

    public static final String firstName = "first_name";
    public static final String lastName = "last_name";
    public static final String birthDate = "date_birth";
    public static final String dob = "dob";
    public static final String baseEntityId = "base_entity_id";

    public static HashMap<String,String> lookUpTableHash = new HashMap<String,String>();
    public static String Name = "name";
    public static String Place = "place";

    static {
        lookUpTableHash.put("Father_Guardian_First_Name_english",DBConstants.MEMBER_TABLE_NAME);
        lookUpTableHash.put("Mother_Guardian_First_Name_english",DBConstants.WOMAN_TABLE_NAME);
        lookUpTableHash.put("spouseName_english",DBConstants.MEMBER_TABLE_NAME+","+DBConstants.WOMAN_TABLE_NAME);
        lookUpTableHash.put("birthPlace",DBConstants.MEMBER_TABLE_NAME+","+DBConstants.WOMAN_TABLE_NAME);

        //        lookUpTableHash.put(,DBConstants.CHILD_TABLE_NAME);
    }

    public static void motherLookUp(final Context context, final EntityLookUp entityLookUp, final Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> listener, final ProgressBar progressBar, final String householdID, final String lookuptype) {

        startAsyncTask(new AsyncTask<Void, Void, HashMap<CommonPersonObject, List<CommonPersonObject>>>() {
            @Override
            protected HashMap<CommonPersonObject, List<CommonPersonObject>> doInBackground(Void... params) {
                publishProgress();
                return lookUp(context, entityLookUp,householdID, lookuptype);
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                if (progressBar != null) {
                    progressBar.setVisibility(VISIBLE);
                }
            }

            @Override
            protected void onPostExecute(HashMap<CommonPersonObject, List<CommonPersonObject>> result) {
                listener.onEvent(result);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, null);
    }

    private static HashMap<CommonPersonObject, List<CommonPersonObject>> lookUp(Context context, EntityLookUp entityLookUp,String householdid, String lookuptype) {
        if(lookuptype.equals("birth_place")){
            return Jilla.getResults(entityLookUp.getMap().get("birth_place"));
        }
        HashMap<CommonPersonObject, List<CommonPersonObject>> results = new HashMap<>();
        if (context == null) {
            return results;
        }


        if (entityLookUp.isEmpty()) {
            return results;
        }

        String tableName = lookUpTableHash.get(lookuptype);
        String childTableName = DBConstants.CHILD_TABLE_NAME;


        List<String> ids = new ArrayList<>();
        List<CommonPersonObject> motherList = new ArrayList<CommonPersonObject>();

        CommonRepository commonRepository = context.commonrepository(DBConstants.HOUSEHOLD_TABLE_NAME);
        String query = lookUpQuery(entityLookUp.getMap(), tableName,householdid);

        Cursor cursor = null;
        try {

            cursor = commonRepository.rawCustomQueryForAdapter(query);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    CommonPersonObject commonPersonObject = commonRepository.readAllcommonforCursorAdapter(cursor);
                    motherList.add(commonPersonObject);


                    ids.add(commonPersonObject.getCaseId());
                    cursor.moveToNext();
                }
            }


        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (motherList.isEmpty()) {
            return results;
        }

        CommonRepository childRepository = context.commonrepository(childTableName);
        List<CommonPersonObject> childList = childRepository.findByRelational_IDs(ids.toArray(new String[ids.size()]));

        for (CommonPersonObject mother : motherList) {
            results.put(mother, findChildren(childList, mother.getCaseId()));
        }


        return results;

    }

    private static List<CommonPersonObject> findChildren(List<CommonPersonObject> childList, String motherBaseEnityId) {
        List<CommonPersonObject> foundChildren = new ArrayList<>();
        for (CommonPersonObject child : childList) {
            String relationalID = getValue(child.getColumnmaps(), "relational_id", false);
            if (!foundChildren.contains(child) && relationalID.equals(motherBaseEnityId)) {
                foundChildren.add(child);
            }
        }

        return foundChildren;

    }

    private static String lookUpQuery(Map<String, String> entityMap, String tableName , String relationalid) {
        if(!tableName.contains(",")){
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, new String[]

                        {
                                tableName + ".relationalid",
                                tableName + ".details",
                                tableName + ".first_name",
                                tableName + "." + DBConstants.KEY.LAST_NAME,
                                tableName + ".dob"
                        }

        );
        queryBUilder.mainCondition(getMainConditionString(entityMap));
        if(!isBlank(relationalid)){
            queryBUilder.addCondition("and relational_id = '"+relationalid+"'");
        }
        String query = queryBUilder.getSelectquery();
        return queryBUilder.Endquery(query);
        }else{
            SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
            queryBUilder.SelectInitiateMainTable(tableName.split(",")[0], new String[]{
                    tableName.split(",")[0] + ".relationalid",
                    tableName.split(",")[0] + ".details",
                    tableName.split(",")[0] + ".first_name",
                    tableName.split(",")[0] + "." + DBConstants.KEY.LAST_NAME,
                    tableName.split(",")[0] + ".dob"
            });
            if(!isBlank(relationalid)){
                queryBUilder.addCondition("where "+tableName.split(",")[0]+".relational_id = '"+relationalid+"'");
            }
            String currentquery = queryBUilder.getSelectquery().concat(" Union all ");
            SmartRegisterQueryBuilder queryBUilder2 = new SmartRegisterQueryBuilder();
            queryBUilder2.SelectInitiateMainTable(tableName.split(",")[1], new String[]{
                    tableName.split(",")[1] + ".relationalid",
                    tableName.split(",")[1] + ".details",
                    tableName.split(",")[1] + ".first_name",
                    tableName.split(",")[1] + "." + DBConstants.KEY.LAST_NAME,
                    tableName.split(",")[1] + ".dob"
            });
            if(!isBlank(relationalid)){
                queryBUilder2.addCondition("where "+tableName.split(",")[1]+".relational_id = '"+relationalid+"'");
            }
            currentquery = currentquery.concat(queryBUilder2.getSelectquery());
            return queryBUilder.Endquery(currentquery);
        }
    }


    private static String getMainConditionString(Map<String, String> entityMap) {

        String mainConditionString = "";
        for (Map.Entry<String, String> entry : entityMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (StringUtils.containsIgnoreCase(key, firstName)) {
                key = firstName;
            }

            if (StringUtils.containsIgnoreCase(key, lastName)) {
                key = lastName;
            }

            if (StringUtils.containsIgnoreCase(key, birthDate)) {
                if (!isDate(value)) {
                    continue;
                }
                key = dob;
            }

            if (!key.equals(dob)) {
                if (isBlank(mainConditionString)) {
                    mainConditionString += " " + key + " Like '%" + value + "%'";
                } else {
                    mainConditionString += " AND " + key + " Like '%" + value + "%'";

                }
            } else {
                if (isBlank(mainConditionString)) {
                    mainConditionString += " cast(" + key + " as date) " + " =  cast('" + value + "'as date) ";
                } else {
                    mainConditionString += " AND cast(" + key + " as date) " + " =  cast('" + value + "'as date) ";

                }
            }
        }

        return mainConditionString;

    }

    private static boolean isDate(String dobString) {
        try {
            DateUtil.yyyyMMdd.parse(dobString);
            return true;
        } catch (ParseException e) {
            return false;
        }

    }
}
