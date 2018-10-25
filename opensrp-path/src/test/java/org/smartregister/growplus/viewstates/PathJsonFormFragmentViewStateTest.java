package org.smartregister.path.viewstates;

import android.os.Parcel;
import android.os.Parcelable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import shared.BaseUnitTest;

/**
 * Created by kaderchowdhury on 10/12/17.
 */

public class PathJsonFormFragmentViewStateTest extends BaseUnitTest {

    PathJsonFormFragmentViewState pathJsonFormFragmentViewState;
    @Mock
    Parcel parcel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        pathJsonFormFragmentViewState = new PathJsonFormFragmentViewState();
        pathJsonFormFragmentViewState.writeToParcel(parcel,1);
        Parcelable.Creator<PathJsonFormFragmentViewState> creator = PathJsonFormFragmentViewState.CREATOR;
        creator.createFromParcel(parcel);
        creator.newArray(0);
    }

    @Test
    public void mockRunnable() {

    }
}
