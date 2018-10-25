package org.smartregister.growplus.activity;//package org.smartregister.path.activity;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.ListAdapter;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import junit.framework.Assert;
//
//import net.sqlcipher.Cursor;
//import net.sqlcipher.MatrixCursor;
//import net.sqlcipher.database.SQLiteDatabase;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.robolectric.Robolectric;
//import org.robolectric.RuntimeEnvironment;
//import org.robolectric.android.controller.ActivityController;
//import org.robolectric.annotation.Config;
//import org.robolectric.shadows.ShadowLooper;
//import org.smartregister.CoreLibrary;
//import org.smartregister.commonregistry.CommonPersonObject;
//import org.smartregister.commonregistry.CommonPersonObjectClient;
//import org.smartregister.commonregistry.CommonRepository;
//import org.smartregister.path.R;
//import org.smartregister.path.activity.mockactivity.HouseholdDetailActivityMock;
//import org.smartregister.path.activity.mockactivity.HouseholdSmartRegisterActivityMock;
//import org.smartregister.path.activity.shadow.CloudantDataHandlerShadow;
//import org.smartregister.path.activity.shadow.CommonRepositoryShadow;
//import org.smartregister.path.activity.shadow.SecuredActivityShadow;
//import org.smartregister.path.activity.shadow.SecuredFragmentShadow;
//import org.smartregister.path.activity.shadow.ShadowContextForRegistryActivity;
//import org.smartregister.path.activity.shadow.UniqueIDRepositoryShadow;
//import org.smartregister.path.activity.shadow.VaccinatorApplicationShadow;
//import org.smartregister.path.customshadow.LocationPickerViewShadow;
//import org.smartregister.path.customshadow.MyShadowAsyncTask;
//import org.smartregister.path.fragment.HouseholdMemberAddFragment;
//import org.smartregister.path.fragment.HouseholdSmartRegisterFragment;
//import org.smartregister.path.repository.PathRepository;
//import org.smartregister.repository.DetailsRepository;
//import org.smartregister.view.controller.ANMLocationController;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import shared.BaseUnitTest;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.isNull;
//import static org.mockito.Mockito.when;
//
///**
// * Created by kaderchowdhury on 18/12/17.
// */
//@Config(shadows = {SecuredActivityShadow.class, CloudantDataHandlerShadow.class, UniqueIDRepositoryShadow.class, LocationPickerViewShadow.class, VaccinatorApplicationShadow.class, MyShadowAsyncTask.class, CommonRepositoryShadow.class, SecuredFragmentShadow.class, ShadowContextForRegistryActivity.class})
//public class HouseholDetailActivityTest extends BaseUnitTest {
//
//    public String locationJson = "{\"locationsHierarchy\":{\"map\":{\"4d2b6b78-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d2b6b78-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d2b6b78-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4cff021b-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4ceded7f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Faridpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cff021b-9f95-11e6-a293-000c299c7c5d\"},\"4d490c27-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d490c27-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d490c27-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d0d6a3b-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf2aa1d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Dhopadanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d0d6a3b-9f95-11e6-a293-000c299c7c5d\"},\"4d3dce1e-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d3dce1e-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d3dce1e-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d08d133-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf13ffd-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Bamandanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d08d133-9f95-11e6-a293-000c299c7c5d\"},\"4d3e7c0d-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d3e7c0d-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d3e7c0d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d08d133-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf13ffd-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Bamandanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d08d133-9f95-11e6-a293-000c299c7c5d\"},\"4d202352-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d202352-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ka\",\"node\":{\"locationId\":\"4d202352-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ka\",\"parentLocation\":{\"locationId\":\"4cfa1085-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4cec07fe-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Laxmipur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cfa1085-9f95-11e6-a293-000c299c7c5d\"},\"4d37269c-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d37269c-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d37269c-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d049150-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cefd3e2-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Naldanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d049150-9f95-11e6-a293-000c299c7c5d\"},\"4d51b81c-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d51b81c-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d51b81c-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d11240c-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf40a29-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ramjiban\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d11240c-9f95-11e6-a293-000c299c7c5d\"},\"4d4fec87-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d4fec87-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ga\",\"node\":{\"locationId\":\"4d4fec87-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ga\",\"parentLocation\":{\"locationId\":\"4d0fde93-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4cf35774-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kanchibari\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d0fde93-9f95-11e6-a293-000c299c7c5d\"},\"4d1abb13-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d1abb13-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d1abb13-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4cf780cf-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4ceb63c3-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kuptala\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cf780cf-9f95-11e6-a293-000c299c7c5d\"},\"4d601aab-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d601aab-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d601aab-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d16e7d5-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf61dc1-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Sonaroy\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d16e7d5-9f95-11e6-a293-000c299c7c5d\"},\"4d52ec31-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d52ec31-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d52ec31-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d11240c-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf40a29-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ramjiban\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d11240c-9f95-11e6-a293-000c299c7c5d\"},\"4d4cc968-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d4cc968-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d4cc968-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d0f41a0-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf35774-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kanchibari\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d0f41a0-9f95-11e6-a293-000c299c7c5d\"},\"4d5bf39a-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d5bf39a-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d5bf39a-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d14f89b-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf56d3d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Sarbanonda\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d14f89b-9f95-11e6-a293-000c299c7c5d\"},\"4d2c0d10-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d2c0d10-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ka\",\"node\":{\"locationId\":\"4d2c0d10-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ka\",\"parentLocation\":{\"locationId\":\"4cffa005-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4ceded7f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Faridpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cffa005-9f95-11e6-a293-000c299c7c5d\"},\"4d4371b3-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d4371b3-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ga\",\"node\":{\"locationId\":\"4d4371b3-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ga\",\"parentLocation\":{\"locationId\":\"4d0b82ad-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf1f2d6-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Chaparhati\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d0b82ad-9f95-11e6-a293-000c299c7c5d\"},\"4d3ad2ec-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d3ad2ec-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d3ad2ec-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d06eedc-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf08264-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Rasulpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d06eedc-9f95-11e6-a293-000c299c7c5d\"},\"4d42d4d5-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d42d4d5-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d42d4d5-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d0b82ad-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf1f2d6-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Chaparhati\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d0b82ad-9f95-11e6-a293-000c299c7c5d\"},\"4d3f10d2-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d3f10d2-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ga\",\"node\":{\"locationId\":\"4d3f10d2-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ga\",\"parentLocation\":{\"locationId\":\"4d08d133-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf13ffd-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Bamandanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d08d133-9f95-11e6-a293-000c299c7c5d\"},\"4d5db7fe-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d5db7fe-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ga\",\"node\":{\"locationId\":\"4d5db7fe-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ga\",\"parentLocation\":{\"locationId\":\"4d15a150-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4cf56d3d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Sarbanonda\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d15a150-9f95-11e6-a293-000c299c7c5d\"},\"4d26eb3d-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d26eb3d-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d26eb3d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4cfd271f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4ced4440-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Damodar Pur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cfd271f-9f95-11e6-a293-000c299c7c5d\"},\"4d2ac47c-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d2ac47c-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d2ac47c-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4cff021b-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4ceded7f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Faridpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cff021b-9f95-11e6-a293-000c299c7c5d\"},\"4d1b59f3-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d1b59f3-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d1b59f3-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4cf780cf-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4ceb63c3-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kuptala\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cf780cf-9f95-11e6-a293-000c299c7c5d\"},\"4d49acde-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d49acde-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d49acde-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d0d6a3b-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf2aa1d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Dhopadanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d0d6a3b-9f95-11e6-a293-000c299c7c5d\"},\"4d2facfa-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d2facfa-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ka\",\"node\":{\"locationId\":\"4d2facfa-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ka\",\"parentLocation\":{\"locationId\":\"4d017353-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4cee8ec5-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Jamalpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d017353-9f95-11e6-a293-000c299c7c5d\"},\"4d30f754-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d30f754-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ga\",\"node\":{\"locationId\":\"4d30f754-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ga\",\"parentLocation\":{\"locationId\":\"4d017353-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4cee8ec5-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Jamalpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d017353-9f95-11e6-a293-000c299c7c5d\"},\"4d2f107c-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d2f107c-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d2f107c-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d00ddff-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cee8ec5-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Jamalpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d00ddff-9f95-11e6-a293-000c299c7c5d\"},\"4d5738a0-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d5738a0-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d5738a0-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d13046d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf4be41-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Shantiram\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d13046d-9f95-11e6-a293-000c299c7c5d\"},\"4d37be5d-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d37be5d-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ka\",\"node\":{\"locationId\":\"4d37be5d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ka\",\"parentLocation\":{\"locationId\":\"4d053aa8-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4cefd3e2-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Naldanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d053aa8-9f95-11e6-a293-000c299c7c5d\"},\"4d2e77a0-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d2e77a0-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d2e77a0-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d00ddff-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cee8ec5-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Jamalpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d00ddff-9f95-11e6-a293-000c299c7c5d\"},\"4d1eead7-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d1eead7-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d1eead7-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4cf9792f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cec07fe-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Laxmipur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cf9792f-9f95-11e6-a293-000c299c7c5d\"},\"4d5c86f4-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d5c86f4-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d5c86f4-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d14f89b-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf56d3d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Sarbanonda\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d14f89b-9f95-11e6-a293-000c299c7c5d\"},\"4d368df9-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d368df9-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d368df9-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d049150-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cefd3e2-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Naldanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d049150-9f95-11e6-a293-000c299c7c5d\"},\"4d278906-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d278906-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d278906-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4cfd271f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4ced4440-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Damodar Pur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cfd271f-9f95-11e6-a293-000c299c7c5d\"},\"4d246dd5-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d246dd5-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ka\",\"node\":{\"locationId\":\"4d246dd5-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ka\",\"parentLocation\":{\"locationId\":\"4cfbf623-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4ceca83f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Malibari\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cfbf623-9f95-11e6-a293-000c299c7c5d\"},\"4d1bf291-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d1bf291-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ka\",\"node\":{\"locationId\":\"4d1bf291-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ka\",\"parentLocation\":{\"locationId\":\"4cf8350a-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4ceb63c3-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kuptala\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cf8350a-9f95-11e6-a293-000c299c7c5d\"},\"4d32d4e3-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d32d4e3-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d32d4e3-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d02a3f7-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cef30df-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kamar Para\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d02a3f7-9f95-11e6-a293-000c299c7c5d\"},\"4d40dc58-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d40dc58-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ga\",\"node\":{\"locationId\":\"4d40dc58-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ga\",\"parentLocation\":{\"locationId\":\"4d096e7d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4cf13ffd-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Bamandanga\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d096e7d-9f95-11e6-a293-000c299c7c5d\"},\"4d58ff1d-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d58ff1d-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d58ff1d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d18e293-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf6c72e-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Sripur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d18e293-9f95-11e6-a293-000c299c7c5d\"},\"4d3a3748-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d3a3748-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d3a3748-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d06eedc-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf08264-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Rasulpur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d06eedc-9f95-11e6-a293-000c299c7c5d\"},\"4d6300a0-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d6300a0-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d6300a0-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d18e293-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf6c72e-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Sripur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d18e293-9f95-11e6-a293-000c299c7c5d\"},\"4d4e0aa6-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d4e0aa6-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d4e0aa6-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d0f41a0-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf35774-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kanchibari\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d0f41a0-9f95-11e6-a293-000c299c7c5d\"},\"4d336caf-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d336caf-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d336caf-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4d02a3f7-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cef30df-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kamar Para\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d02a3f7-9f95-11e6-a293-000c299c7c5d\"},\"4d569207-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d569207-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d569207-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d13046d-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf4be41-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Shantiram\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d13046d-9f95-11e6-a293-000c299c7c5d\"},\"4d3404d7-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d3404d7-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ka\",\"node\":{\"locationId\":\"4d3404d7-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ka\",\"parentLocation\":{\"locationId\":\"4d034013-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4cef30df-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Kamar Para\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d034013-9f95-11e6-a293-000c299c7c5d\"},\"4d2342cc-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d2342cc-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d2342cc-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4cfb54f1-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4ceca83f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Malibari\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cfb54f1-9f95-11e6-a293-000c299c7c5d\"},\"4d44321c-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d44321c-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Gha\",\"node\":{\"locationId\":\"4d44321c-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Gha\",\"parentLocation\":{\"locationId\":\"4d0b82ad-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf1f2d6-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Chaparhati\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d0b82ad-9f95-11e6-a293-000c299c7c5d\"},\"4d28238f-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d28238f-9f95-11e6-a293-000c299c7c5d\",\"label\":\"2-Ka\",\"node\":{\"locationId\":\"4d28238f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"2-Ka\",\"parentLocation\":{\"locationId\":\"4cfdca18-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"4ced4440-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Damodar Pur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cfdca18-9f95-11e6-a293-000c299c7c5d\"},\"4d22a675-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d22a675-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d22a675-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4cfb54f1-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4ceca83f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Malibari\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cfb54f1-9f95-11e6-a293-000c299c7c5d\"},\"4d5f87eb-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d5f87eb-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Ka\",\"node\":{\"locationId\":\"4d5f87eb-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Ka\",\"parentLocation\":{\"locationId\":\"4d16e7d5-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cf61dc1-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Sonaroy\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4d16e7d5-9f95-11e6-a293-000c299c7c5d\"},\"4d1f7f4a-9f95-11e6-a293-000c299c7c5d\":{\"id\":\"4d1f7f4a-9f95-11e6-a293-000c299c7c5d\",\"label\":\"1-Kha\",\"node\":{\"locationId\":\"4d1f7f4a-9f95-11e6-a293-000c299c7c5d\",\"name\":\"1-Kha\",\"parentLocation\":{\"locationId\":\"4cf9792f-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"4cec07fe-9f95-11e6-a293-000c299c7c5d\",\"name\":\"Laxmipur\",\"voided\":false},\"voided\":false},\"tags\":[\"Subunit\"],\"voided\":false},\"parent\":\"4cf9792f-9f95-11e6-a293-000c299c7c5d\"}},\"parentChildren\":{\"4cfb54f1-9f95-11e6-a293-000c299c7c5d\":[\"4d2342cc-9f95-11e6-a293-000c299c7c5d\",\"4d22a675-9f95-11e6-a293-000c299c7c5d\"],\"4d0f41a0-9f95-11e6-a293-000c299c7c5d\":[\"4d4e0aa6-9f95-11e6-a293-000c299c7c5d\",\"4d4cc968-9f95-11e6-a293-000c299c7c5d\"],\"4cfa1085-9f95-11e6-a293-000c299c7c5d\":[\"4d202352-9f95-11e6-a293-000c299c7c5d\"],\"4d08d133-9f95-11e6-a293-000c299c7c5d\":[\"4d3f10d2-9f95-11e6-a293-000c299c7c5d\",\"4d3dce1e-9f95-11e6-a293-000c299c7c5d\",\"4d3e7c0d-9f95-11e6-a293-000c299c7c5d\"],\"4d0b82ad-9f95-11e6-a293-000c299c7c5d\":[\"4d42d4d5-9f95-11e6-a293-000c299c7c5d\",\"4d44321c-9f95-11e6-a293-000c299c7c5d\",\"4d4371b3-9f95-11e6-a293-000c299c7c5d\"],\"4cff021b-9f95-11e6-a293-000c299c7c5d\":[\"4d2b6b78-9f95-11e6-a293-000c299c7c5d\",\"4d2ac47c-9f95-11e6-a293-000c299c7c5d\"],\"4d18e293-9f95-11e6-a293-000c299c7c5d\":[\"4d6300a0-9f95-11e6-a293-000c299c7c5d\",\"4d58ff1d-9f95-11e6-a293-000c299c7c5d\"],\"4d16e7d5-9f95-11e6-a293-000c299c7c5d\":[\"4d601aab-9f95-11e6-a293-000c299c7c5d\",\"4d5f87eb-9f95-11e6-a293-000c299c7c5d\"],\"4d00ddff-9f95-11e6-a293-000c299c7c5d\":[\"4d2e77a0-9f95-11e6-a293-000c299c7c5d\",\"4d2f107c-9f95-11e6-a293-000c299c7c5d\"],\"4cf780cf-9f95-11e6-a293-000c299c7c5d\":[\"4d1b59f3-9f95-11e6-a293-000c299c7c5d\",\"4d1abb13-9f95-11e6-a293-000c299c7c5d\"],\"4d15a150-9f95-11e6-a293-000c299c7c5d\":[\"4d5db7fe-9f95-11e6-a293-000c299c7c5d\"],\"4d11240c-9f95-11e6-a293-000c299c7c5d\":[\"4d51b81c-9f95-11e6-a293-000c299c7c5d\",\"4d52ec31-9f95-11e6-a293-000c299c7c5d\"],\"4d02a3f7-9f95-11e6-a293-000c299c7c5d\":[\"4d336caf-9f95-11e6-a293-000c299c7c5d\",\"4d32d4e3-9f95-11e6-a293-000c299c7c5d\"],\"4d0d6a3b-9f95-11e6-a293-000c299c7c5d\":[\"4d490c27-9f95-11e6-a293-000c299c7c5d\",\"4d49acde-9f95-11e6-a293-000c299c7c5d\"],\"4cffa005-9f95-11e6-a293-000c299c7c5d\":[\"4d2c0d10-9f95-11e6-a293-000c299c7c5d\"],\"4d14f89b-9f95-11e6-a293-000c299c7c5d\":[\"4d5c86f4-9f95-11e6-a293-000c299c7c5d\",\"4d5bf39a-9f95-11e6-a293-000c299c7c5d\"],\"4d017353-9f95-11e6-a293-000c299c7c5d\":[\"4d2facfa-9f95-11e6-a293-000c299c7c5d\",\"4d30f754-9f95-11e6-a293-000c299c7c5d\"],\"4cf9792f-9f95-11e6-a293-000c299c7c5d\":[\"4d1eead7-9f95-11e6-a293-000c299c7c5d\",\"4d1f7f4a-9f95-11e6-a293-000c299c7c5d\"],\"4cf8350a-9f95-11e6-a293-000c299c7c5d\":[\"4d1bf291-9f95-11e6-a293-000c299c7c5d\"],\"4d06eedc-9f95-11e6-a293-000c299c7c5d\":[\"4d3a3748-9f95-11e6-a293-000c299c7c5d\",\"4d3ad2ec-9f95-11e6-a293-000c299c7c5d\"],\"4cfdca18-9f95-11e6-a293-000c299c7c5d\":[\"4d28238f-9f95-11e6-a293-000c299c7c5d\"],\"4d0fde93-9f95-11e6-a293-000c299c7c5d\":[\"4d4fec87-9f95-11e6-a293-000c299c7c5d\"],\"4d034013-9f95-11e6-a293-000c299c7c5d\":[\"4d3404d7-9f95-11e6-a293-000c299c7c5d\"],\"4d096e7d-9f95-11e6-a293-000c299c7c5d\":[\"4d40dc58-9f95-11e6-a293-000c299c7c5d\"],\"4cfd271f-9f95-11e6-a293-000c299c7c5d\":[\"4d26eb3d-9f95-11e6-a293-000c299c7c5d\",\"4d278906-9f95-11e6-a293-000c299c7c5d\"],\"4d049150-9f95-11e6-a293-000c299c7c5d\":[\"4d37269c-9f95-11e6-a293-000c299c7c5d\",\"4d368df9-9f95-11e6-a293-000c299c7c5d\"],\"4d053aa8-9f95-11e6-a293-000c299c7c5d\":[\"4d37be5d-9f95-11e6-a293-000c299c7c5d\"],\"4cfbf623-9f95-11e6-a293-000c299c7c5d\":[\"4d246dd5-9f95-11e6-a293-000c299c7c5d\"],\"4d13046d-9f95-11e6-a293-000c299c7c5d\":[\"4d569207-9f95-11e6-a293-000c299c7c5d\",\"4d5738a0-9f95-11e6-a293-000c299c7c5d\"]}}}";
//
//    HouseholdDetailActivityMock activity;
//    private Map<String, String> details;
//    ActivityController<HouseholdDetailActivityMock> controller;
//
//    @Mock
//    private org.smartregister.Context context_;
//
//    @Mock
//    private SQLiteDatabase database;
//
//    @Mock
//    private Context applicationContext;
//
//    @Mock
//    private PathRepository pathRepository;
//
//    @Mock
//    private CommonRepository commonRepository;
//
//    @Mock
//    private CoreLibrary coreLibrary;
//
//    @Mock
//    private ANMLocationController anmLocationController;
//
//    @Mock
//    private org.smartregister.repository.AllSharedPreferences allSharedPreferences;
////    @Mock
////    CommonPersonObject personObject;
//
//    @Mock
//    private DetailsRepository detailsRepository;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        VaccinatorApplicationShadow.pathRepository = pathRepository;
//        details = new HashMap<>();
//        context_ = ShadowContextForRegistryActivity.getInstance();
//        SecuredFragmentShadow.mContext = context_;
//        ShadowContextForRegistryActivity.commonRepository = commonRepository;
//        String[] columns = new String[]{"_id", "relationalid", "first_name", "dob", "details", "HHID", "Date_Of_Reg", "address1"};
//        MatrixCursor matrixCursor = new MatrixCursor(columns);
//        matrixCursor.addRow(new Object[]{"1", "relationalid", "first_name", "dob", "details", "HHID", "Date_Of_Reg", "address1"});
//        matrixCursor.addRow(new Object[]{"2", "relationalid", "first_name", "dob", "details", "HHID", "Date_Of_Reg", "address1"});
//        for (int i = 3; i < 22; i++) {
//            matrixCursor.addRow(new Object[]{"" + i, "relationalid" + i, "first_name" + i, "dob" + i, "details+i", "HHID" + i, "Date_Of_Reg" + i, "address"+i});
//        }
//        CommonPersonObject personObject = new CommonPersonObject("caseID","relationalID",new HashMap<String, String>(),"type");
//        details.put("address4","1-Kha");
//        CommonPersonObjectClient client = new CommonPersonObjectClient(personObject.getCaseId(),personObject.getDetails(),"name");
//        client.setColumnmaps(details);
//        personObject.setColumnmaps(details);
//        HouseholdSmartRegisterActivityMock.setmContext(context_);
//        when(context_.updateApplicationContext(isNull(Context.class))).thenReturn(context_);
//        when(context_.updateApplicationContext(isNull(Context.class))).thenReturn(context_);
//        when(context_.updateApplicationContext(any(Context.class))).thenReturn(context_);
//        when(anmLocationController.get()).thenReturn(locationJson);
//        when(context_.IsUserLoggedOut()).thenReturn(false);
//        when(context_.applicationContext()).thenReturn(applicationContext);
//        when(context_.anmLocationController()).thenReturn(anmLocationController);
//        when(context_.allSharedPreferences()).thenReturn(allSharedPreferences);
//        when(allSharedPreferences.fetchRegisteredANM()).thenReturn("Test User");
//        when(allSharedPreferences.getANMPreferredName(anyString())).thenReturn("Test User");
//        when(context_.commonrepository(anyString())).thenReturn(commonRepository);
//        when(context_.detailsRepository()).thenReturn(getDetailsRepository());
//        when(commonRepository.rawCustomQueryForAdapter(anyString())).thenReturn(matrixCursor);
//        when(commonRepository.readAllcommonforCursorAdapter(any(Cursor.class))).thenReturn(personObject);
//        CoreLibrary.init(context_);
//        when(pathRepository.getReadableDatabase()).thenReturn(database);
//        when(pathRepository.getWritableDatabase()).thenReturn(database);
//        when(database.rawQuery(anyString(),any(String[].class))).thenReturn(matrixCursor);
//        Intent intent = new Intent(RuntimeEnvironment.application,HouseholdDetailActivityMock.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("household_details", client);
//        intent.putExtras(bundle);
//        controller = Robolectric.buildActivity(HouseholdDetailActivityMock.class,intent);
//        activity = controller.create()
//                .start()
//                .resume()
//                .visible()
//                .get();
////controller.create();
//    }
//
//
//
//    @Test
//    public void assertActivityNotNull() {
//        Assert.assertNotNull(activity);
//        tearDown();
//    }
//
//    @Test
//    public void householdDetailsShowList() {
//        final ListView list = (ListView) activity.findViewById(R.id.household_list);
//        ListAdapter  adapter = tryGetAdapter(list);
//        assertTrue(adapter.getCount()>0);
//        tearDown();
////        assertTrue(PathJsonFormActivity.isLaunched);
////        assertTrue( instanceof PathJsonFormActivity);
//    }
//
//
//
//
//
//    private ListAdapter tryGetAdapter(final ListView list) {
//        ListAdapter adapter = list.getAdapter();
//        while (adapter.getCount() == 0) {
//            ShadowLooper.idleMainLooper(1000);
//            adapter = list.getAdapter();
//        }
//        return adapter;
//    }
//
//
//    @After
//    public void tearDown() {
//        destroyController();
//        activity = null;
//        controller = null;
//    }
//
//    private void destroyController() {
//        try {
//            activity.finish();
//            controller.pause().stop().destroy(); //destroy controller if we can
//
//        } catch (Exception e) {
//            Log.e(getClass().getCanonicalName(), e.getMessage());
//        }
//
//        System.gc();
//    }
//
//    private DetailsRepository getDetailsRepository() {
//
//        return new DetailsRepositoryLocal();
//    }
//
//    class DetailsRepositoryLocal extends DetailsRepository {
//
//        @Override
//        public Map<String, String> getAllDetailsForClient(String baseEntityId) {
//            return details;
//        }
//    }
//}
