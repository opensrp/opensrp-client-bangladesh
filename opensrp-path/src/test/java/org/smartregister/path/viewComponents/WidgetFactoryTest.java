package org.smartregister.path.viewComponents;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 11/12/17.
 */

public class WidgetFactoryTest extends BaseUnitTest {

    WidgetFactory widgetFactory;

    @Mock
    LayoutInflater layoutInflater;

    @Mock
    ViewGroup container;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        widgetFactory = new WidgetFactory();
    }

    @Test
    public void mockRunnable() {

    }

}
