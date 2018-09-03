package org.smartregister.path.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

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
//        org.smartregister.org.smartregister.cbhc.repository.DetailsRepository detailsRepository = Mockito.mock(org.smartregister.org.smartregister.cbhc.repository.DetailsRepository.class);
//        Mockito.doReturn(detailsRepository).when(context).detailsRepository();
//        Map<String, String> details = new HashMap<>();
//        details.put("dob","1985-07-24T00:00:00.000Z");
//        details.put("gender", Gender.FEMALE.name());
//        details.put(PathConstants.KEY.BIRTH_WEIGHT,"100.0");
//        Mockito.doReturn(details).when(detailsRepository).getAllDetailsForClient(Mockito.anyString());
//        return context;
//    }

}
