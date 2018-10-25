package org.smartregister.growplus.activity.shadow;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.clientandeventmodel.Gender;

import java.util.HashMap;
import java.util.Map;

import util.PathConstants;

/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Implements(org.smartregister.Context.class)
public class ContextShadow extends  Shadow {

    @Implementation
    public Boolean IsUserLoggedOut() {
        return false;
    }

//    @Implementation
//    public static org.smartregister.Context getInstance() {
//        org.smartregister.Context context = Mockito.mock(org.smartregister.Context.class);
//        org.smartregister.repository.DetailsRepository detailsRepository = Mockito.mock(org.smartregister.repository.DetailsRepository.class);
//        Mockito.doReturn(detailsRepository).when(context).detailsRepository();
//        Map<String, String> details = new HashMap<>();
//        details.put("dob","1985-07-24T00:00:00.000Z");
//        details.put("gender", Gender.FEMALE.name());
//        details.put(PathConstants.KEY.BIRTH_WEIGHT,"100.0");
//        Mockito.doReturn(details).when(detailsRepository).getAllDetailsForClient(Mockito.anyString());
//        return context;
//    }

}
