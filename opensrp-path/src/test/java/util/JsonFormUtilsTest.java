package util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.smartregister.path.activity.mocks.SQLiteDatabaseMock;
import org.smartregister.view.controller.ANMController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 09/12/17.
 */
public class JsonFormUtilsTest extends BaseUnitTest {

    private JsonFormUtils jsonFormUtils;

    @Mock
    private SQLiteDatabaseMock sqLiteDatabase;
    @Mock
    android.content.Context context;
    @Mock
    org.smartregister.Context opensrpContext;
    String providerId = "1";


    ANMControllerMock anmLocationController;
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jsonFormUtils = new JsonFormUtils();
        anmLocationController = new ANMControllerMock(null,null,null);

    }

    @Test
    public void mockTest() throws Exception {
        PowerMockito.doReturn(anmLocationController).when(opensrpContext).anmController();
//        PowerMockito.doReturn(locationString).when(anmLocationController).get();
        String household_json = getStringFromFile(getFileFromPath(this,"json_form/household_registration.json"));
        jsonFormUtils.saveForm(context,opensrpContext,household_json,providerId);

        String catchment_json = getStringFromFile(getFileFromPath(this,"json_form/out_of_catchment_service.json"));
        jsonFormUtils.saveForm(context,opensrpContext,catchment_json,providerId);

        String birth_json = getStringFromFile(getFileFromPath(this,"json_form/child_enrollment.json"));
        jsonFormUtils.saveForm(context,opensrpContext,birth_json,providerId);

        String women_json = getStringFromFile(getFileFromPath(this,"json_form/woman_member_registration.json"));
        jsonFormUtils.saveForm(context,opensrpContext,women_json,providerId);

        String women_json_e = getStringFromFile(getFileFromPath(this,"json_form/woman_member_registration_e.json"));
        jsonFormUtils.saveForm(context,opensrpContext,women_json_e,providerId);

        String women_json_lmp = getStringFromFile(getFileFromPath(this,"json_form/woman_member_registration_lmp.json"));
        jsonFormUtils.saveForm(context,opensrpContext,women_json_lmp,providerId);
    }

    public String getStringFromFile(File f) throws Exception {
        InputStream inputStream = new FileInputStream(f);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"));
        String jsonString;
        StringBuilder stringBuilder = new StringBuilder();

        while ((jsonString = reader.readLine()) != null) {
            stringBuilder.append(jsonString);
        }
        inputStream.close();

        return stringBuilder.toString();


    }

    private static File getFileFromPath(Object obj, String fileName) {
        ClassLoader classLoader = obj.getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return new File(resource.getPath());
    }
}
