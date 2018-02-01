package util;

import android.database.Cursor;
import android.widget.ProgressBar;

import net.sqlcipher.MatrixCursor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.event.Listener;
import org.smartregister.path.customshadow.MyShadowAsyncTask;
import org.smartregister.path.domain.EntityLookUp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 11/12/17.
 */
@Config(shadows = MyShadowAsyncTask.class)
public class MotherLookUpUtilsTest extends BaseUnitTest {
    MotherLookUpUtils motherLookUpUtils;
    EntityLookUp entityLookUp;
    Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> listener;
    ProgressBar progressBar;
    @Mock
    org.smartregister.Context context;
    @Mock
    CommonRepository commonRepository;

    MatrixCursor cursor;
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        motherLookUpUtils = new MotherLookUpUtils();
        entityLookUp = new EntityLookUp();
        listener = new Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>>(){

            @Override
            public void onEvent(HashMap<CommonPersonObject, List<CommonPersonObject>> commonPersonObjectListHashMap) {

            }
        };
        progressBar = new ProgressBar(RuntimeEnvironment.application);
        //context = RuntimeEnvironment.application;
        motherLookUpUtils.motherLookUp(context,entityLookUp,listener,progressBar);//returns result
        motherLookUpUtils.motherLookUp(null,entityLookUp,listener,progressBar);//returns result
        entityLookUp.put(firstName,firstName);
        entityLookUp.put(lastName,lastName);
        entityLookUp.put(birthDate,"10-10-2010");
        cursor = new MatrixCursor(new String[]{"id",firstName,lastName,birthDate,baseEntityId});
        cursor.addRow(new String[]{"1","","","10-10-1010","1"});

        Mockito.doReturn(commonRepository).when(context).commonrepository(Mockito.anyString());
        Mockito.doReturn(cursor).when(commonRepository).rawCustomQueryForAdapter(Mockito.anyString());
        CommonPersonObject commonPersonObject = new CommonPersonObject("1","",new HashMap<String,String>(),"");
        Mockito.doReturn(commonPersonObject).when(commonRepository).readAllcommonforCursorAdapter(cursor);
        ArrayList<CommonPersonObject>list = new ArrayList<CommonPersonObject>();
        list.add(commonPersonObject);
        Mockito.doReturn(list).when(commonRepository).findByRelational_IDs(Mockito.any(String[].class));
        motherLookUpUtils.motherLookUp(context,entityLookUp,listener,progressBar);//returns result
    }
    public static final String firstName = "first_name";
    public static final String lastName = "last_name";
    public static final String birthDate = "date_birth";
    public static final String dob = "dob";
    public static final String baseEntityId = "base_entity_id";
    @Test
    public void mockrunnable(){

    }
}
