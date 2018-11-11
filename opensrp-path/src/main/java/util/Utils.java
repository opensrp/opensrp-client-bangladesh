/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.domain.EditWrapper;
import org.smartregister.path.helper.LocationHelper;
import org.smartregister.path.sync.ECSyncUpdater;
import org.smartregister.path.sync.PathClientProcessor;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.repository.EventClientRepository;

import java.util.Collection;
import java.util.Date;
import java.util.Map;


/**
 * @author Maimoona
 *         Class containing some static utility methods.
 */
public class Utils {

    private Utils() {
    }

    public static TableRow getDataRow(Context context, String label, String value, TableRow row) {
        TableRow tr = row;
        if (row == null) {
            tr = new TableRow(context);
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tr.setLayoutParams(trlp);
            tr.setPadding(10, 5, 10, 5);
        }

        TextView l = new TextView(context);
        l.setText(label + ": ");
        l.setPadding(20, 2, 20, 2);
        l.setTextColor(Color.BLACK);
        l.setTextSize(14);
        l.setBackgroundColor(Color.WHITE);
        tr.addView(l);

        TextView v = new TextView(context);
        v.setText(value);
        v.setPadding(20, 2, 20, 2);
        v.setTextColor(Color.BLACK);
        v.setTextSize(14);
        v.setBackgroundColor(Color.WHITE);
        tr.addView(v);

        return tr;
    }

    public static TableRow getDataRow(Context context, String label, String value, String field, TableRow row) {
        TableRow tr = row;
        if (row == null) {
            tr = new TableRow(context);
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tr.setLayoutParams(trlp);
            tr.setPadding(10, 5, 10, 5);
        }

        TextView l = new TextView(context);
        l.setText(label + ": ");
        l.setPadding(20, 2, 20, 2);
        l.setTextColor(Color.BLACK);
        l.setTextSize(14);
        l.setBackgroundColor(Color.WHITE);
        tr.addView(l);

        EditWrapper editWrapper = new EditWrapper();
        editWrapper.setCurrentValue(value);
        editWrapper.setField(field);

        EditText e = new EditText(context);
        e.setTag(editWrapper);
        e.setText(value);
        e.setPadding(20, 2, 20, 2);
        e.setTextColor(Color.BLACK);
        e.setTextSize(14);
        e.setBackgroundColor(Color.WHITE);
        e.setInputType(InputType.TYPE_NULL);
        tr.addView(e);

        return tr;
    }

    public static TableRow getDataRow(Context context) {
        TableRow tr = new TableRow(context);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(trlp);
        tr.setPadding(0, 0, 0, 0);
        // tr.setBackgroundColor(Color.BLUE);
        return tr;
    }

    public static int addAsInts(boolean ignoreEmpty, String... vals) {
        int i = 0;
        for (String v : vals) {
            i += ignoreEmpty && StringUtils.isBlank(v) ? 0 : Integer.parseInt(v);
        }
        return i;
    }
    public static boolean isEmptyMap(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmptyCollection(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static TableRow addToRow(Context context, String value, TableRow row) {
        return addToRow(context, value, row, false, 1);
    }

    public static TableRow addToRow(Context context, String value, TableRow row, int weight) {
        return addToRow(context, value, row, false, weight);
    }

    public static TableRow addToRow(Context context, String value, TableRow row, boolean compact) {
        return addToRow(context, value, row, compact, 1);
    }

    private static TableRow addToRow(Context context, String value, TableRow row, boolean compact, int weight) {
        return addToRow(context, Html.fromHtml(value), row, compact, weight);
    }

    private static TableRow addToRow(Context context, Spanned value, TableRow row, boolean compact, int weight) {
        TextView v = new TextView(context);
        v.setText(value);
        if (compact) {
            v.setPadding(15, 4, 1, 1);
        } else {
            v.setPadding(2, 15, 2, 15);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT, weight
        );
        params.setMargins(0, 0, 1, 0);
        v.setLayoutParams(params);
        v.setTextColor(Color.BLACK);
        v.setTextSize(14);
        v.setBackgroundColor(Color.WHITE);
        row.addView(v);

        return row;
    }

    public static void putAll(Map<String, String> map, Map<String, String> extend) {
        Collection<String> values = extend.values();
        while (true) {
            if (!(values.remove(null))) break;
        }
        map.putAll(extend);
    }

    public static Date dobStringToDate(String dobString) {
        DateTime dateTime = dobStringToDateTime(dobString);
        if (dateTime != null) {
            return dateTime.toDate();
        }
        return null;
    }

    public static DateTime dobStringToDateTime(String dobString) {
        try {
            if (StringUtils.isBlank(dobString)) {
                return null;
            }
            return new DateTime(dobString);

        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> updateClientAttribute(Context context, CommonPersonObjectClient childDetails, String attributeName, Object attributeValue) throws Exception {

        org.smartregister.Context openSRPContext = VaccinatorApplication.getInstance().context();

        Date date = new Date();
        EventClientRepository db = VaccinatorApplication.getInstance().eventClientRepository();
        ECSyncUpdater ecUpdater = ECSyncUpdater.getInstance(context);

        JSONObject client = db.getClientByBaseEntityId(childDetails.entityId());
        JSONObject attributes = client.getJSONObject(JsonFormUtils.attributes);
        attributes.put(attributeName, attributeValue);
        client.remove(JsonFormUtils.attributes);
        client.put(JsonFormUtils.attributes, attributes);
        db.addorUpdateClient(childDetails.entityId(), client);


        DetailsRepository detailsRepository = openSRPContext.detailsRepository();
        detailsRepository.add(childDetails.entityId(), attributeName, attributeValue.toString(), new Date().getTime());
        ContentValues contentValues = new ContentValues();
        //Add the base_entity_id
        contentValues.put(attributeName.toLowerCase(), attributeValue.toString());
        db.getWritableDatabase().update(PathConstants.CHILD_TABLE_NAME, contentValues, "base_entity_id" + "=?", new String[]{childDetails.entityId()});

        AllSharedPreferences allSharedPreferences = openSRPContext.allSharedPreferences();
        String locationName = allSharedPreferences.fetchCurrentLocality();
        if (StringUtils.isBlank(locationName)) {
            locationName = LocationHelper.getInstance().getDefaultLocation();
        }

        Event event = (Event) new Event()
                .withBaseEntityId(childDetails.entityId())
                .withEventDate(new Date())
                .withEventType(JsonFormUtils.encounterType)
                .withLocationId(LocationHelper.getInstance().getOpenMrsLocationId(locationName))
                .withProviderId(allSharedPreferences.fetchRegisteredANM())
                .withEntityType(PathConstants.EntityType.CHILD)
                .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                .withDateCreated(new Date());

        JsonFormUtils.addMetaData(context, event, date);
        JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(event));
        db.addEvent(childDetails.entityId(), eventJson);
        long lastSyncTimeStamp = allSharedPreferences.fetchLastUpdatedAtDate(0);
        Date lastSyncDate = new Date(lastSyncTimeStamp);
        PathClientProcessor.getInstance(context).processClient(ecUpdater.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
        allSharedPreferences.saveLastUpdatedAtDate(lastSyncDate.getTime());

        //update details
        Map<String, String> detailsMap = detailsRepository.getAllDetailsForClient(childDetails.entityId());
        if (childDetails.getColumnmaps().containsKey(attributeName)) {
            childDetails.getColumnmaps().put(attributeName, attributeValue.toString());
        }
        util.Utils.putAll(detailsMap, childDetails.getColumnmaps());

        return detailsMap;
    }
    public static int convertDpToPx(Context context, int dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return Math.round(px);
    }

}
