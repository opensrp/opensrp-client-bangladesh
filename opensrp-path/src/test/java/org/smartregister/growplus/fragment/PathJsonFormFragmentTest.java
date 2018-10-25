package org.smartregister.growplus.fragment;

import org.junit.Before;
import org.junit.Test;
import org.smartregister.path.fragment.*;

import shared.BaseUnitTest;

import junit.framework.Assert;

/**
 * Created by ona on 28/08/2017.
 */
public class PathJsonFormFragmentTest extends BaseUnitTest {

    protected org.smartregister.path.fragment.PathJsonFormFragment pathJsonFormFragment;

    @Before
    public void setUp() {
        pathJsonFormFragment = org.smartregister.path.fragment.PathJsonFormFragment.getFormFragment("testStep");
    }

    @Test
    public void setPathJsonFormFragmentNotNullOnInstantiation() throws Exception {
        Assert.assertNotNull(pathJsonFormFragment);

    }

    @Test
    public void motherLookUpListenerIsNotNullOnFragmentInstantiation() throws Exception {
        Assert.assertNotNull(pathJsonFormFragment.motherLookUpListener());

    }

    @Test
    public void contextNotNullOnFragmentInstantiation() throws Exception {
        Assert.assertNotNull(pathJsonFormFragment.context());

    }

}
