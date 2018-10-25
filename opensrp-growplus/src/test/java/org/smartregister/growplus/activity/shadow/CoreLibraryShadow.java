package org.smartregister.growplus.activity.shadow;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.CoreLibrary;

/**
 * Created by kaderchowdhury on 14/12/17.
 */
@Implements(CoreLibrary.class)
public class CoreLibraryShadow extends Shadow {
    @Implementation
    public static CoreLibrary getInstance() {
        CoreLibrary coreLibrary = Mockito.mock(CoreLibrary.class);
//        org.smartregister.Context context = Mockito.mock(org.smartregister.Context.class);
//        ImageRepository imageRepository = Mockito.mock(ImageRepository.class);
//        Mockito.when(coreLibrary.context()).thenReturn(context);
//        Mockito.when(context.imageRepository()).thenReturn(imageRepository);
        return coreLibrary;
    }
}
