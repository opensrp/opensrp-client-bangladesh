package org.smartregister.growplus.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
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
import org.smartregister.Context;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.CursorCommonObjectFilterOption;
import org.smartregister.cursoradapter.CursorCommonObjectSort;
import org.smartregister.cursoradapter.CursorSortOption;
import org.smartregister.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.growplus.R;

import org.smartregister.growplus.activity.HouseholdDetailActivity;
import org.smartregister.growplus.activity.HouseholdSmartRegisterActivity;
import org.smartregister.growplus.activity.LoginActivity;
import org.smartregister.growplus.activity.OnMapAndViewReadyListener;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growplus.domain.RegisterClickables;
import org.smartregister.growplus.option.BasicSearchOption;
import org.smartregister.growplus.option.DateSort;
import org.smartregister.growplus.option.StatusSort;
import org.smartregister.growplus.provider.HouseholdSmartClientsProvider;
import org.smartregister.growplus.servicemode.VaccinationServiceModeOption;
import org.smartregister.growplus.view.LocationPickerView;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.DateUtil;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;
import org.smartregister.view.dialog.DialogOption;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import util.PathConstants;
import util.WeightVelocityUtils;

import static android.view.View.INVISIBLE;
import static org.smartregister.growplus.activity.LoginActivity.getOpenSRPContext;
import static org.smartregister.util.Utils.getValue;

public class ReportGeoMapFragment extends Fragment implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerDragListener,
        SeekBar.OnSeekBarChangeListener,
        GoogleMap.OnInfoWindowLongClickListener,
        GoogleMap.OnInfoWindowCloseListener,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener{

    /** Demonstrates customizing the info window and/or its contents. */

    private GoogleMap mMap;



    /**
     * Keeps track of the last selected marker (though it may no longer be selected).  This is
     * useful for refreshing the info window.
     */
    private Marker mLastSelectedMarker;



    private final Random mRandom = new Random();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private View rootView;

    protected View getFragmentView() {
        View view = getView();
        if (view == null) {
            view = rootView;
        }
        return view;
    }

    View returnview;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = getFragmentView();
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
        }

        try {
            this.rootView = inflater.inflate(R.layout.marker_geo_map, container, false);
        } catch (InflateException ex) {
            Log.e("exception", "Inflate Exception in onCreateView; Caused by map fragment", ex);
        }
        return getFragmentView();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//        new OnMapAndViewReadyListener(mapFragment, this);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            // called here
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            new OnMapAndViewReadyListener(mapFragment, this);

        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        try {
            (new AsyncTask() {
                ProgressDialog dialog;
                List<geoChildWeightHolder> geoChildWeightHolders;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    dialog = ProgressDialog.show(getActivity(), "processing", "please Wait");
                    dialog.show();
                }

                @Override
                protected Object doInBackground(Object[] objects) {

                    geoChildWeightHolders = generateMarkersToMap();


                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    mMap = map;

                    // Hide the zoom controls as the button panel will cover it.
                    mMap.getUiSettings().setZoomControlsEnabled(false);

                    // Add lots of markers to the map.
                    addMarkersToMap(geoChildWeightHolders);
                    // Setting an info window adapter allows us to change the both the contents and look of the
                    // info window.
//        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

                    // Set listeners for marker events.  See the bottom of this class for their behavior.
                    mMap.setOnMarkerClickListener(ReportGeoMapFragment.this);
                    mMap.setOnInfoWindowClickListener(ReportGeoMapFragment.this);
                    mMap.setOnMarkerDragListener(ReportGeoMapFragment.this);
                    mMap.setOnInfoWindowCloseListener(ReportGeoMapFragment.this);
                    mMap.setOnInfoWindowLongClickListener(ReportGeoMapFragment.this);

                    // Override the default content description on the view, for accessibility mode.
                    // Ideally this string would be localised.
                    if(geoChildWeightHolders.size()>0) {
                        mMap.setContentDescription("Map with lots of markers.");

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (int i = 0; i < geoChildWeightHolders.size(); i++) {
                            builder.include(geoChildWeightHolders.get(i).position);
                        }
                        LatLngBounds bounds = builder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                    dialog.dismiss();
                }
            }).execute();

        }catch (Exception e){

        }

            }

    private void addMarkersToMap(List<geoChildWeightHolder> allgeoChildWeightHolders) {
        for (int i = 0; i < allgeoChildWeightHolders.size(); i++) {
            if(allgeoChildWeightHolders.get(i).isChild_growth_rate()==null){
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(allgeoChildWeightHolders.get(i).position)
                                .title(allgeoChildWeightHolders.get(i).childname)
                                .icon(vectorToBitmap(R.drawable.mapmarker, getResources().getColor(R.color.status_bar_text_almost_white))));

             }
            else if(allgeoChildWeightHolders.get(i).isChild_growth_rate()) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(allgeoChildWeightHolders.get(i).position)
                        .title(allgeoChildWeightHolders.get(i).childname)
                        .icon(vectorToBitmap(R.drawable.mapmarker, getResources().getColor(R.color.alert_complete_green))));
            }else if(!allgeoChildWeightHolders.get(i).isChild_growth_rate()){
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(allgeoChildWeightHolders.get(i).position)
                        .title(allgeoChildWeightHolders.get(i).childname)
                        .icon(vectorToBitmap(R.drawable.mapmarker, getResources().getColor(R.color.alert_urgent_red))));
            }
        }
    }

    private ArrayList<geoChildWeightHolder> generateMarkersToMap() {
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
                String[] str=geopoint.split(" ");
                if(str.length>0){
                    geoChildWeightHolder.setPosition(new LatLng(Double.parseDouble(geopoint.split(" ")[0]),Double.parseDouble(geopoint.split(" ")[1])));
                }else{
                    //allcommonschild.remove(i);
                    geoChildWeightHolder.setPosition(new LatLng(0.0,0.0));
                }
                DateTime birthDateTime = null;
                String dobString = getValue(pc.getColumnmaps(), PathConstants.KEY.DOB, false);
                if (StringUtils.isNotBlank(dobString)) {
                    try {
                        birthDateTime = new DateTime(dobString);

                    } catch (Exception e) {
                        Log.e(getClass().getName(), e.toString(), e);
                    }
                }
                String gender = getValue(pc.getColumnmaps(), PathConstants.KEY.GENDER, true);

                List<Weight> weightlist = weightRepository.findLast5(pc.getCaseId());
                if(weightlist.size() >= 1) {
                    Boolean adequate = WeightVelocityUtils.checkForWeightGainCalc(birthDateTime.toDate(), Gender.valueOf(gender.toUpperCase()), weightlist.get(0), pc.entityId(), getOpenSRPContext().detailsRepository());
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
            Toast.makeText(getActivity(), "map not ready", Toast.LENGTH_SHORT).show();
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
//        addMarkersToMap();
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
        Toast.makeText(getActivity(), "Click Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        //Toast.makeText(this, "Close Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        Toast.makeText(getActivity(), "Info Window long click", Toast.LENGTH_SHORT).show();
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
        Boolean child_growth_rate;
        LatLng position;

        public Boolean isChild_growth_rate() {
            return child_growth_rate;
        }

        public void setChild_growth_rate(Boolean child_growth_rate) {
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
