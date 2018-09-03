package org.smartregister.cbhc.task;

import android.os.AsyncTask;

import org.smartregister.cbhc.contract.PopulationCharacteristicsContract;
import org.smartregister.cbhc.domain.Characteristic;
import org.smartregister.cbhc.helper.CharacteristicsHelper;

import java.util.List;

/**
 * Created by ndegwamartin on 28/08/2018.
 */
public class FetchPopulationCharacteristicsTask extends AsyncTask<Void, Void, List<Characteristic>> {

    private PopulationCharacteristicsContract.Presenter presenter;

    public FetchPopulationCharacteristicsTask(PopulationCharacteristicsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected List<Characteristic> doInBackground(final Void... params) {
        CharacteristicsHelper helper = new CharacteristicsHelper();
        return helper.getPopulationCharacteristics();
    }

    @Override
    protected void onPostExecute(final List<Characteristic> result) {
        presenter.renderView(result);
    }
}
