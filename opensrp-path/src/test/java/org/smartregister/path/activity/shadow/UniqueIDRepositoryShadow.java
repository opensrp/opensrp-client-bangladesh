package org.smartregister.path.activity.shadow;

import android.database.Cursor;
import android.util.Log;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.path.domain.UniqueId;
import org.smartregister.path.repository.UniqueIdRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by kaderchowdhury on 17/12/17.
 */
@Implements(UniqueIdRepository.class)
public class UniqueIDRepositoryShadow extends Shadow {
    @Implementation
    public UniqueId getNextUniqueId() {
        UniqueId uniqueId = null;
        uniqueId = new UniqueId("id","openmrsID", "unused", "usedby", new Date());
        return uniqueId;
    }
}
