package org.smartregister.path.activity.shadow;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.commonregistry.CommonRepository;

/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Implements(CommonRepository.class)
public class CommonRepositoryShadow extends Shadow {
    @Implementation
    public android.database.Cursor rawCustomQueryForAdapter(String query) {
        return Mockito.mock(android.database.Cursor.class);
    }
}
