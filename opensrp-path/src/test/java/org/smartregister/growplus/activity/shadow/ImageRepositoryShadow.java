package org.smartregister.growplus.activity.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.domain.ProfileImage;
import org.smartregister.repository.ImageRepository;

/**
 * Created by kaderchowdhury on 14/12/17.
 */
@Implements(ImageRepository.class)
public class ImageRepositoryShadow extends Shadow {
    @Implementation
    public ProfileImage findByEntityId(String entityId) {
        return new ProfileImage();
    }
}
