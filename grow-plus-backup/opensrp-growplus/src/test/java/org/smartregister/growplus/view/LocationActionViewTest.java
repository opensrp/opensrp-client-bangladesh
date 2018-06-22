package org.smartregister.growplus.view;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.smartregister.growplus.application.VaccinatorApplication;
import shared.BaseUnitTest;
import shared.customshadows.FontTextViewShadow;
import shared.customshadows.LocationPickerViewShadow;
import util.JsonFormUtils;

/**
 * Created by kaderchowdhury on 10/12/17.
 */
@PrepareForTest(JsonFormUtils.class)
@Config(shadows = {LocationPickerViewShadow.class, FontTextViewShadow.class})
public class LocationActionViewTest extends BaseUnitTest {

//    @Rule
//    public PowerMockRule rule = new PowerMockRule();

    LocationActionView locationActionView;

    android.content.Context context;

    org.smartregister.Context opensrpContext;
//    @Mock
//    LocationPickerView item;
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
//        PowerMockito.mockStatic(JsonFormUtils.class);
//        PowerMockito.when(JsonFormUtils.getOpenMrsReadableName(Mockito.anyString())).thenReturn("");
        context = RuntimeEnvironment.application;
        opensrpContext = VaccinatorApplication.getInstance().context();
//        locationActionView = new LocationActionView(context,opensrpContext);

//        locationActionView = new LocationActionView(context,opensrpContext,ViewAttributes.attrs);
//        locationActionView = new LocationActionView(context,opensrpContext,ViewAttributes.attrs,0);
//        locationActionView = new LocationActionView(context,opensrpContext,ViewAttributes.attrs,0,0);
    }

    @Test
    public void mockRunnable(){

    }

}
