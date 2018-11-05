package org.smartregister.path.fragment;

import android.content.Intent;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.CoreLibrary;
import org.smartregister.growplus.customshadow.MyShadowAsyncTask;
import org.smartregister.growplus.fragment.mocks.FragmentMockActivity;
import org.smartregister.growplus.fragment.mocks.ShadowBaseRegisterActivity;
import org.smartregister.growplus.fragment.mocks.ShadowOpensrpSSLHelper;
import org.smartregister.ssl.OpensrpSSLHelper;

import shared.BaseUnitTest;
import shared.customshadows.FontTextViewShadow;

/**
 * Created by kaderchowdhury on 06/12/17.
 */
@PowerMockIgnore({"javax.xml.*", "org.xml.sax.*", "org.w3c.dom.*", "org.springframework.context.*", "org.apache.log4j.*"})
@PrepareForTest({CoreLibrary.class, OpensrpSSLHelper.class,AdvancedSearchFragment.class})
@Config(shadows = {FontTextViewShadow.class, ShadowOpensrpSSLHelper.class, MyShadowAsyncTask.class})
public class FragmentTest extends BaseUnitTest {
    @Mock
    private CoreLibrary coreLibrary;
    @Mock
    private org.smartregister.Context context_;
    private FragmentMockActivity activity;
    private ActivityController<FragmentMockActivity>controller;
    @Before
    public void setUp() throws Exception {
        org.mockito.MockitoAnnotations.initMocks(this);

        Intent intent = new Intent(RuntimeEnvironment.application, FragmentMockActivity.class);
        controller = Robolectric.buildActivity(FragmentMockActivity.class, intent);
        activity = controller.get();

        CoreLibrary.init(context_);
        controller.setup();

    }

    @After
    public void tearDown() {
        destroyController();
        activity = null;
        controller = null;

    }

    private void destroyController() {
        try {
            activity.finish();
            controller.pause().stop().destroy(); //destroy controller if we can

        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }

        System.gc();
    }

    @Test
    public void AdvancedSearchFragmentTest() {
//        activity.startAdvancedSearchFragment();
    }


    @Test
    public void BaseSmartRegisterFragmentTest() {
//        activity.startBaseSmartRegisterFragment();
    }//BaseSmartRegisterFragment

    @Test
    public void DraftMonthlyFragmentTest() {
//        activity.startDraftMonthlyFragment();
    }
}
