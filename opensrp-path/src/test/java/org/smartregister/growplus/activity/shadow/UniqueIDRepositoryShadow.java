package org.smartregister.path.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.growplus.domain.UniqueId;
import org.smartregister.growplus.repository.UniqueIdRepository;

import java.util.Date;

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
