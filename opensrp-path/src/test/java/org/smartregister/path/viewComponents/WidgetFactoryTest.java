package org.smartregister.path.viewComponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import com.rey.material.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.path.R;

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
    public void mockcreateTableRow() {
        TableRow rows = new TableRow(RuntimeEnvironment.application);
        TextView label = new TextView(RuntimeEnvironment.application);
        TextView value = new TextView(RuntimeEnvironment.application);
        label.setId(R.id.label);
        value.setId(R.id.value);
        rows.addView(label);
        rows.addView(value);
        Mockito.doReturn(rows).when(layoutInflater).inflate(R.layout.tablerows, container, false);
//        rows.add
        widgetFactory.createTableRow(layoutInflater,container,"","");

    }

}
