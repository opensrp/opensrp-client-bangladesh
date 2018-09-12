package org.smartregister.cbhc.util;

import org.opensrp.api.constants.Gender;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.domain.Photo;
import org.smartregister.domain.ProfileImage;

/**
 * Created by ndegwamartin on 12/04/2018.
 */

public class ImageUtils {
    public static int getProfileImageResourceIDentifier() {
        return R.drawable.ic_woman_with_baby;
    }
    public static int profileImageResourceByGender(Gender gender) {
        if (gender != null) {
            if (gender.equals(Gender.MALE)) {
                return R.drawable.child_boy_infant;
            } else if (gender.equals(Gender.FEMALE)) {
                return R.drawable.child_girl_infant;
            }
        }
        return R.drawable.child_boy_infant;
    }
    public static Photo profilePhotoByClientID(String clientEntityId) {
        Photo photo = new Photo();
        ProfileImage profileImage = AncApplication.getInstance().getContext().imageRepository().findByEntityId(clientEntityId);
        if (profileImage != null) {
            photo.setFilePath(profileImage.getFilepath());
        } else {
            photo.setResourceId(getProfileImageResourceIDentifier());
        }
        return photo;
    }
}
