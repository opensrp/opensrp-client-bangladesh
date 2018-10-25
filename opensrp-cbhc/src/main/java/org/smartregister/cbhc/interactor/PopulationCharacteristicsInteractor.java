package org.smartregister.cbhc.interactor;

import org.smartregister.cbhc.contract.PopulationCharacteristicsContract;
import org.smartregister.cbhc.task.FetchPopulationCharacteristicsTask;

/**
 * Created by ndegwamartin on 28/08/2018.
 */
public class PopulationCharacteristicsInteractor implements PopulationCharacteristicsContract.Interactor {
    private PopulationCharacteristicsContract.Presenter presenter;

    public PopulationCharacteristicsInteractor(PopulationCharacteristicsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {

        if (!isChangingConfiguration) {
            presenter = null;
        }
    }

    @Override
    public void fetchPopulationCharacteristics() {
        new FetchPopulationCharacteristicsTask(presenter).execute();
    }

}
