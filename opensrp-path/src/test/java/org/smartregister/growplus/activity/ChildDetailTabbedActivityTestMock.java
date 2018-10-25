package org.smartregister.path.activity;

import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.annotation.Config;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.growplus.R;
import org.smartregister.growplus.activity.mockactivity.ChildDetailTabbedActivityTestVersion;
import org.smartregister.growplus.activity.mocks.MenuItemTestVersion;
import org.smartregister.growplus.application.VaccinatorApplication;

import org.smartregister.growplus.customshadow.LocationPickerViewShadow;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.util.FormUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import shared.BaseUnitTest;
import shared.customshadows.FontTextViewShadow;
import shared.customshadows.ImmunizationRowAdapterShadow;
import shared.customshadows.ImmunizationRowCardShadow;
import util.JsonFormUtils;

/**
 * Created by kaderchowdhury on 03/12/17.
 */
@Ignore
@PrepareForTest({FormUtils.class, JsonFormUtils.class})
@Config(shadows = {ImmunizationRowAdapterShadow.class, ImmunizationRowCardShadow.class, LocationPickerViewShadow.class, FontTextViewShadow.class})
public class ChildDetailTabbedActivityTestMock extends BaseUnitTest {

    private ChildDetailTabbedActivityTestVersion activity;

    @Mock
    private FormUtils formUtils;

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private Map<String, String> details;

    @Mock
    Context context;

    @Mock
    android.content.Context context_;

    @Mock
    VaccinatorApplication vaccinatorApplication;

//    MockContext mockContext;
    @Mock
    private CommonPersonObjectClient childDetails;

    @Mock
    Resources resources;

    @Mock
    AssetManager assetManager;

    @Mock
    DisplayMetrics displayMetrics;

    @Mock
    Configuration configuration;

    @Mock
    ApplicationInfo applicationInfo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        details = new HashMap<>();
        activity = Mockito.spy(new ChildDetailTabbedActivityTestVersion());
        Mockito.when(context_.getResources()).thenReturn(resources);
        Mockito.when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

        JSONObject child_enrollment = getJSONFromFile(getFileFromPath(this,"www/form/child_enrollment/form.json"));
        PowerMockito.mockStatic(FormUtils.class);
        PowerMockito.doReturn(context_).when(activity).getApplicationContext();
        Mockito.when(context_.getApplicationInfo()).thenReturn(applicationInfo);
        PowerMockito.when(FormUtils.getInstance(Mockito.any(android.content.Context.class))).thenReturn(formUtils);

        PowerMockito.when(formUtils.getFormJson(Mockito.anyString())).thenReturn(child_enrollment);

        PowerMockito.when(vaccinatorApplication.context()).thenReturn(context);
        PowerMockito.doReturn(context).when(activity).getOpenSRPContext();
        CoreLibrary.init(context);
        PowerMockito.mockStatic(JsonFormUtils.class);
//        Mockito.doNothing().when(JsonFormUtils.addChildRegLocHierarchyQuestions(Mockito.any(JSONObject.class),Mockito.any(Context.class)));
        activity.detailsRepository = getDetailsRepository();
        Field field = MemberModifier.field(activity.getClass(), "childDetails");
        field.setAccessible(true);
        field.set(activity, childDetails);

        PowerMockito.when(activity,"getmetaDataForEditForm").thenReturn("");
    }

    @Test
    public void assertConstrustorNotNull() throws Exception {
        Assert.assertNotNull(activity);


        MenuItemTestVersion menuItem = new MenuItemTestVersion();
        menuItem.setItemId(R.id.registration_data);
//        activity.onOptionsItemSelected(menuItem);
    }

    public JSONObject getJSONFromFile(File f) throws Exception {
        InputStream inputStream = new FileInputStream(f);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"));
        String jsonString;
        StringBuilder stringBuilder = new StringBuilder();

        while ((jsonString = reader.readLine()) != null) {
            stringBuilder.append(jsonString);
        }
        inputStream.close();

        return new JSONObject(stringBuilder.toString());


    }

    private static File getFileFromPath(Object obj, String fileName) {
        ClassLoader classLoader = obj.getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return new File(resource.getPath());
    }
    private DetailsRepository getDetailsRepository() {

        return new DetailsRepositoryLocal();
    }

    class DetailsRepositoryLocal extends DetailsRepository {

        @Override
        public Map<String, String> getAllDetailsForClient(String baseEntityId) {
            return details;
        }
    }


}
