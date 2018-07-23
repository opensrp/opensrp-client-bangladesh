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

package org.smartregister.growplus.activity;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.growplus.R;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import util.PathConstants;

import static org.smartregister.growplus.activity.LoginActivity.getOpenSRPContext;
import static org.smartregister.growplus.provider.ChildSmartClientsProvider.checkForWeightGainCalc;
import static org.smartregister.util.Utils.getValue;

/**
 * This shows how to place markers on a map.
 */
public class GrowthGeoMapActivity extends AppCompatActivity implements
        OnMarkerClickListener,
        OnInfoWindowClickListener,
        OnMarkerDragListener,
        OnSeekBarChangeListener,
        OnInfoWindowLongClickListener,
        OnInfoWindowCloseListener,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {

   /** Demonstrates customizing the info window and/or its contents. */

    private GoogleMap mMap;



    /**
     * Keeps track of the last selected marker (though it may no longer be selected).  This is
     * useful for refreshing the info window.
     */
    private Marker mLastSelectedMarker;



    private final Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_geo_map);



        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        new OnMapAndViewReadyListener(mapFragment, this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Add lots of markers to the map.
        List <geoChildWeightHolder> geoChildWeightHolders = addMarkersToMap();

        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
//        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnInfoWindowLongClickListener(this);

        // Override the default content description on the view, for accessibility mode.
        // Ideally this string would be localised.
        mMap.setContentDescription("Map with lots of markers.");

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i =0;i<geoChildWeightHolders.size();i++){
            builder.include(geoChildWeightHolders.get(i).position);
        }
        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
    }

    private ArrayList<geoChildWeightHolder> addMarkersToMap() {
        ////////////////////////////////queries//////////////////////////////////////////
        WeightRepository weightRepository = VaccinatorApplication.getInstance().weightRepository();

        List<CommonPersonObject> allcommonschild = VaccinatorApplication.getInstance().context().commonrepository("ec_child").allcommon();
        ArrayList<geoChildWeightHolder> allgeoChildWeightHolders = new ArrayList<geoChildWeightHolder>();
        for(int i = 0;i<allcommonschild.size();i++) {
            Map<String,String> details =VaccinatorApplication.getInstance().context().detailsRepository().getAllDetailsForClient(allcommonschild.get(i).getCaseId());
            allcommonschild.get(i).getColumnmaps().putAll(details);
            CommonPersonObjectClient pc = new CommonPersonObjectClient(allcommonschild.get(i).getCaseId(),allcommonschild.get(i).getDetails(),"");
            pc.setColumnmaps(allcommonschild.get(i).getColumnmaps());
            String geopoint = details.get("gps");
                if(geopoint!=null){
                    geoChildWeightHolder geoChildWeightHolder =
                            new geoChildWeightHolder(pc.getCaseId(),pc.getColumnmaps().get("first_name"));
                    geoChildWeightHolder.setPosition(new LatLng(Double.parseDouble(geopoint.split(" ")[0]),Double.parseDouble(geopoint.split(" ")[1])));
                    DateTime birthDateTime = null;
                    String dobString = getValue(pc.getColumnmaps(), PathConstants.KEY.DOB, false);
                    String durationString = "";
                    if (StringUtils.isNotBlank(dobString)) {
                        try {
                            birthDateTime = new DateTime(dobString);
                            String duration = DateUtil.getDuration(birthDateTime);
                            if (duration != null) {
                                durationString = duration;
                            }
                        } catch (Exception e) {
                            Log.e(getClass().getName(), e.toString(), e);
                        }
                    }
                    String gender = getValue(pc.getColumnmaps(), PathConstants.KEY.GENDER, true);

                    List<Weight> weightlist = weightRepository.findLast5(pc.getCaseId());
                    if(weightlist.size() >= 1) {
                        boolean adequate = checkForWeightGainCalc(birthDateTime.toDate(), Gender.valueOf(gender.toUpperCase()), weightlist.get(0), pc, getOpenSRPContext().detailsRepository());
                        geoChildWeightHolder.setChild_growth_rate(adequate);
                    }
                        allgeoChildWeightHolders.add(geoChildWeightHolder);
                }
        }
        /////////////////////////////////////////////////////////////////////////

        // Uses a colored icon.
//        mBrisbane = mMap.addMarker(new MarkerOptions()
//                .position(BRISBANE)
//                .title("Brisbane")
//                .snippet("Population: 2,074,200")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//

        for (int i = 0; i < allgeoChildWeightHolders.size(); i++) {
            if(allgeoChildWeightHolders.get(i).isChild_growth_rate()) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(allgeoChildWeightHolders.get(i).position)
                        .title(allgeoChildWeightHolders.get(i).childname)
                        .icon(vectorToBitmap(R.drawable.ic_cross, getResources().getColor(R.color.alert_complete_green))));
            }else if(!allgeoChildWeightHolders.get(i).isChild_growth_rate()){
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(allgeoChildWeightHolders.get(i).position)
                        .title(allgeoChildWeightHolders.get(i).childname)
                        .icon(vectorToBitmap(R.drawable.ic_cross, getResources().getColor(R.color.alert_urgent_red))));
            }
        }
        return allgeoChildWeightHolders;
    }

    /**
     * Demonstrates converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     */
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, "map not ready", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /** Called when the Clear button is clicked. */
    public void onClearMap(View view) {
        if (!checkReady()) {
            return;
        }
        mMap.clear();
    }

    /** Called when the Reset button is clicked. */
    public void onResetMap(View view) {
        if (!checkReady()) {
            return;
        }
        // Clear the map because we don't want duplicates of the markers.
        mMap.clear();
        addMarkersToMap();
    }

    /** Called when the Reset button is clicked. */
    public void onToggleFlat(View view) {
        if (!checkReady()) {
            return;
        }


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!checkReady()) {
            return;
        }
        float rotation = seekBar.getProgress();

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Do nothing.
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Do nothing.
    }

    //
    // Marker related listeners.
    //

    @Override
    public boolean onMarkerClick(final Marker marker) {


        // Markers have a z-index that is settable and gettable.
//        float zIndex = marker.getZIndex() + 1.0f;
//        marker.setZIndex(zIndex);
//        Toast.makeText(this, marker.getTitle() + " z-index set to " + zIndex,
//                Toast.LENGTH_SHORT).show();

        mLastSelectedMarker = marker;
        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Click Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        //Toast.makeText(this, "Close Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        Toast.makeText(this, "Info Window long click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

//        mTopText.setText("onMarkerDragStart");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
//        mTopText.setText("onMarkerDrag.  Current Position: " + marker.getPosition());
    }

    class geoChildWeightHolder{
        String childcaseID;
        String childname;
        boolean child_growth_rate;
        LatLng position;

        public boolean isChild_growth_rate() {
            return child_growth_rate;
        }

        public void setChild_growth_rate(boolean child_growth_rate) {
            this.child_growth_rate = child_growth_rate;
        }

        public LatLng getPosition() {
            return position;
        }

        public void setPosition(LatLng position) {
            this.position = position;
        }

        public geoChildWeightHolder(String childcaseID, String childname) {
            this.childcaseID = childcaseID;
            this.childname = childname;
        }
    }

}
