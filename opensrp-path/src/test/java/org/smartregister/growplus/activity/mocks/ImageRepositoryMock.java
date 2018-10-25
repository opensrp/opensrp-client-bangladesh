package org.smartregister.path.activity.mocks;

import org.smartregister.domain.ProfileImage;
import org.smartregister.repository.ImageRepository;

/**
 * Created by kaderchowdhury on 06/12/17.
 */

public class ImageRepositoryMock extends ImageRepository {
    @Override
    public ProfileImage findByEntityId(String entityId) {
        return null;
    }
}
