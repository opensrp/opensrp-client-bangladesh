package org.smartregister.growplus.wrapper;

import android.database.Cursor;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.smartregister.view.contract.SmartRegisterClient;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 10/12/17.
 */

public class BaseViewRecordUpdateWrapperTest extends BaseUnitTest {
    BaseViewRecordUpdateWrapper baseViewRecordUpdateWrapper;
    @Mock
    View convertView;
    String lostToFollowUp = "";
    String inactive = "";
    @Mock
    Cursor cursor;
    @Mock
    SmartRegisterClient client;
    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        baseViewRecordUpdateWrapper = new BaseViewRecordUpdateWrapper();
    }
    @Test
    public void getConvertView() {
        baseViewRecordUpdateWrapper.getConvertView();
    }

    @Test
    public void setConvertView() {
        baseViewRecordUpdateWrapper.setConvertView(convertView);
    }

    @Test
    public void getLostToFollowUp() {
        baseViewRecordUpdateWrapper.getLostToFollowUp();
    }

    @Test
    public void setLostToFollowUp() {
        baseViewRecordUpdateWrapper.setLostToFollowUp(lostToFollowUp);
    }

    @Test
    public void getInactive() {
        baseViewRecordUpdateWrapper.getInactive();
    }

    @Test
    public void setInactive() {
        baseViewRecordUpdateWrapper.setInactive(inactive);
    }

    @Test
    public void getClient() {
        baseViewRecordUpdateWrapper.getClient();
    }

    @Test
    public void setClient() {
        baseViewRecordUpdateWrapper.setClient(client);
    }

    @Test
    public void getCursor() {
        baseViewRecordUpdateWrapper.getCursor();
    }

    @Test
    public void setCursor() {
        baseViewRecordUpdateWrapper.setCursor(cursor);
    }
}
