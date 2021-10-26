package org.smartregister.growplus.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.rey.material.widget.CheckBox;
import com.rey.material.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Response;
import org.smartregister.growplus.R;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.domain.jsonmapping.FormLocation;
import org.smartregister.growplus.helper.LocationHelper;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import util.Constants;


public class BlocksDialog extends Activity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {

    private static final String WARD_SUBMIT_URL = "";
    private HTTPAgent httpAgent;
    ViewGroup blocklist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.blockselectiondialog);
        blocklist = findViewById(R.id.blockslist);
        findViewById(R.id.submitblocks).setOnClickListener(this);
//        getActionBar().setTitle("");
//        getActionBar().setDisplayHomeAsUpEnabled(false);
        httpAgent = VaccinatorApplication.getInstance().getContext().getHttpAgent();

        //get ward list from settings table
//        getWardList();
        //send ward list to server
        //receive block list

        Utils.startAsyncTask(new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getBlockList();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                setupViews(blocklist);
            }
        },null);
        //static returned block list
//        getLocationForm(this);
        //show block list to ui

    }
    public void getBlockList() {
        String baseUrl = VaccinatorApplication.getInstance().getContext().configuration().dristhiBaseURL();
        if (baseUrl.endsWith(getString(R.string.url_separator))) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(this.getString(R.string.url_separator)));
        }
//        baseUrl = baseUrl + "/rest/event/ward/block?wards=";
        baseUrl = baseUrl + "/rest/event/user/ward/block?username="+VaccinatorApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
//        192.168.19.167:8080/opensrp/rest/event/user/ward/block?username=nroy1978@gmail.com
//        for(int i=0;i<wardelist.size();i++){
//            baseUrl = baseUrl + wardelist.get(i).name + ",";
//        }
//        baseUrl = baseUrl.substring(0,baseUrl.length()-1);
        // create request body
//        JSONObject request = new JSONObject();
//        baseUrl = baseUrl.replaceAll(" ","%20");
//        try {
//            System.out.println(URLEncoder.encode("Hello World", "UTF-8").replace("+", "%20"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        Response response = httpAgent.fetch(baseUrl);
        if (response.isFailure()) {
            Log.e(getClass().getName(), "Events sync failed.");
            return;
        }
        try {
            JSONArray jsonObject = new JSONArray((String) response.payload());
            getBlockList(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void submit(String selectedLocation) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString(Constants.LOCATION_UPDATED,selectedLocation);
    }

    public void getBlockList(JSONArray map) {
        try{
            nodelist = new ArrayList<>();
//            JSONObject form = FormUtils.getInstance(context).getFormJson(Constants.LOCATIONFORM);
//            JSONArray map = form.getJSONArray("map");
            for(int i=0;i<map.length();i++){
                JSONObject ward = map.getJSONObject(i);
                Node node = new Node();
                node.name = ward.getString("name");
//                node.id = ward.getString("id");

                JSONArray nodes = ward.getJSONArray("node");
                for(int k=0;k<nodes.length();k++){
                    JSONObject block = nodes.getJSONObject(k);
                    Node childNode = new Node();
                    childNode.name = block.getString("name");
//                    childNode.id = block.getString("id");
                    childNode.nodes = null;
                    childNode.parent = node;
                    childNode.vaild = block.getBoolean("assigned");
                    node.nodes.add(childNode);
                }
                nodelist.add(node);
            }
        }catch(Exception e){
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    HashMap<View,Node>nodeView = new HashMap<>();
    //ward list to be sent to the server
    ArrayList<Node>wardelist = new ArrayList<>();
    ArrayList<Node>nodelist = new ArrayList<>();
    public void setupViews(ViewGroup view) {
        for(int i=0;i<nodelist.size();i++){
            TextView wardView = new TextView(this);
            wardView.setTypeface(Typeface.DEFAULT_BOLD);
            wardView.setTextColor(getResources().getColor(android.R.color.black));
            wardView.setText(nodelist.get(i).name);
            view.addView(wardView);
            nodeView.put(wardView,nodelist.get(i));
            ArrayList<Node>blocks = nodelist.get(i).nodes;
            for(int k=0;k<blocks.size();k++){
                CheckBox block = new CheckBox(this);
                block.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                block.setText(blocks.get(k).name);
                block.setChecked(blocks.get(k).vaild);
                block.setOnCheckedChangeListener(this);
                view.addView(block);
                nodeView.put(block,blocks.get(k));
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        nodeView.get(view).vaild = isChecked;
    }

    class Node {
        String name;
        String id;
        Node parent;
        ArrayList<Node>nodes = new ArrayList<>();
        Boolean vaild = false;
        public Node(){

        }
        public Node(String name, String id, ArrayList<Node> nodes, Boolean vaild) {
            this.name = name;
            this.id = id;
            this.nodes = nodes;
            this.vaild = vaild;
        }
    }
    public void submitBlocks(String BLOCKS){
        String baseUrl = VaccinatorApplication.getInstance().getContext().configuration().dristhiBaseURL();
        if (baseUrl.endsWith(this.getString(R.string.url_separator))) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(this.getString(R.string.url_separator)));
        }
        baseUrl = baseUrl + "/save/user/block";


        // create request body
//        JSONObject request = new JSONObject();

        baseUrl = baseUrl + "?username="+VaccinatorApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        baseUrl = baseUrl + "&password="+VaccinatorApplication.getInstance().getContext().allSettings().fetchANMPassword();
        baseUrl = baseUrl + "&blocks="+BLOCKS;

        //final String basicAuth = "Basic " + Base64.encodeToString((allSharedPreferences.fetchRegisteredANM() +
        //                    ":" + settings.fetchANMPassword()).getBytes(), Base64.NO_WRAP);

//        try {
//            System.out.println(URLEncoder.encode("Hello World", "UTF-8").replace("+", "%20"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        Response response = httpAgent.fetch(baseUrl);
        if (response.isFailure()) {
            Log.e(getClass().getName(), "Events sync failed.");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject((String) response.payload());
//            getBlockList(jsonObject);
            updateSetttingsTable(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void submitSelection(){
        String BLOCKS = "";
        //make wards valid if block in a ward is valid
        for(int i=0;i<nodelist.size();i++) {
            Node ward = nodelist.get(i);
            ward.vaild = false;
        }
        for(int i=0;i<nodelist.size();i++){
            Node ward = nodelist.get(i);
            for(int k=0;k<ward.nodes.size();k++){
                if(ward.nodes.get(k).vaild){
                    BLOCKS = BLOCKS + ward.nodes.get(k).name + ",";
//                    ward.vaild = ward.nodes.get(k).vaild;
                }
            }
        }
        BLOCKS = BLOCKS.substring(0,BLOCKS.length()-1);

        final String fblocks = BLOCKS.replaceAll(" ","%20");
        Utils.startAsyncTask(new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                submitBlocks(fblocks);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                finish();
//                setupViews(blocklist);
            }
        },null);

//        JSONArray map = new JSONArray();
//        try {
        //make json to be sent to the server

//        for(int i=0;i<nodelist.size();i++){
//            Node ward = nodelist.get(i);
//            if(ward.vaild){
//                JSONObject wobject = new JSONObject();
//
//                wobject.put("name",ward.name);
//                wobject.put("id",ward.id);
//                JSONArray nodes = new JSONArray();
//                for(int k=0;k<ward.nodes.size();k++){
//                    if(ward.nodes.get(k).vaild){
//                        JSONObject bobject = new JSONObject();
//
//                        bobject.put("name",ward.nodes.get(k).name);
//                        bobject.put("id",ward.nodes.get(k).id);
//                        nodes.put(bobject);
//                    }
//                }
//                wobject.put("node",nodes);
//                map.put(wobject);
//            }
//
//        }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return BLOCKS;
    }

    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public void updateSetttingsTable(JSONObject jsonObject) {
        try {

            ContentValues values = new ContentValues();
            values.put("value", jsonObject.toString());

            VaccinatorApplication.getInstance().getRepository().getWritableDatabase()
                    .update("settings",values,"key = ?",new String[]{"anmLocation"});
            // a case here would be if an event comes from openmrs
          //  AncApplication.getInstance().getRepository().getWritableDatabase().insert(EventClientRepository.Table.event.name(), null, values);

            VaccinatorApplication.getInstance().getRepository().getWritableDatabase().close();
        } catch (Exception e) {
            //Timber.e(e);
        }
    }
    public void updateLocationTable(String baseEntityId, JSONObject jsonObject, String syncStatus) {
        try {

            ContentValues values = new ContentValues();
            values.put("value", jsonObject.toString());

            VaccinatorApplication.getInstance().getRepository().getWritableDatabase()
                    .update("settings",values,"key = ?",new String[]{"anmLocation"});
// a case here would be if an event comes from openmrs
            //  AncApplication.getInstance().getRepository().getWritableDatabase().insert(EventClientRepository.Table.event.name(), null, values);


        } catch (Exception e) {
           // Timber.e(e);
        }
    }

    public FormLocation getWardList(){
        ArrayList<String> healthFacilities = new ArrayList<>();
        healthFacilities.add("Country");
        healthFacilities.add("Division");
        healthFacilities.add("District");
        healthFacilities.add("Upazilla");
        healthFacilities.add("Union");
        healthFacilities.add("Ward");
        healthFacilities.add("Block");
        healthFacilities.add("Subunit");
        healthFacilities.add("EPI center");

        //locationHelper getlocationtree
        List<String> defaultFacility = LocationHelper.getInstance().generateDefaultLocationHierarchy(healthFacilities);
        List<FormLocation> upToFacilities = LocationHelper.getInstance().generateLocationHierarchyTree(false, healthFacilities);

        String location = "";
        ArrayList<String> locations = new ArrayList<>();
//        FormLocation loc = null;
//        for(int i=0;i<upToFacilities.size();i++){
//            loc = upToFacilities.get(i);
//            Set<String> levels = loc.level;
//
//            if(loc.nodes!=null&&loc.nodes.get(0)!=null&&loc.nodes.get(0).nodes.get(0)==null){
//                break;
//            }
//        }
//        System.out.println(loc);
        getNodeName(upToFacilities);
//        while(getNodeName(upToFacilities)!=null){
//            FormLocation loc = upToFacilities.get(0);
//            locations.add(loc.name);
//            upToFacilities = loc.nodes;
//
//        }
//        for(int i = locations.size()-2;i>=0;i--){
//            location += locations.get(i) + " ";
//        }
        //        FormLocation locationTree = getLocationTree(upToFacilities);
//        System.out.println(locationTree);
        return null;
    }
    public void getNodeName(List<FormLocation> loc){

        if(loc==null)return;
        for(int i=0;i<loc.size();i++){
            FormLocation formLocation = loc.get(i);
            Set<String>levels = Collections.singleton(formLocation.level);
            for(String level : levels){
                if("ward".equalsIgnoreCase(level)){
                    wardelist.add(new Node(formLocation.key,formLocation.id,null,null));
                }
            }
            getNodeName(loc.get(i).nodes);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submitblocks:
                submitSelection();

                break;
        }
    }

}
