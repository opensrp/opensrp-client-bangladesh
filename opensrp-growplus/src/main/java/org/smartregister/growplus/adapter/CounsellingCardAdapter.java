package org.smartregister.growplus.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.SnackBar;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.domain.Photo;
import org.smartregister.growplus.R;
import org.smartregister.growplus.domain.Counselling;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.ImageUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.immunization.view.VaccineCard;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.OnClick;

import static org.smartregister.immunization.util.VaccinatorUtils.generateScheduleList;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by Jason Rogena - jrogena@ona.io on 22/02/2017.
 */
public class CounsellingCardAdapter extends BaseAdapter {
    private static final String TAG = "CounsellingCardAdapter";
    private final Context context;


    private List<Counselling> counsellingList;


    public CounsellingCardAdapter(Context context,List<Counselling> vaccineList) {
        this.context = context;

        this.counsellingList = vaccineList;

    }

    @Override
    public int getCount() {
        return counsellingList.size();
    }

    @Override
    public Object getItem(int position) {
        return counsellingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 231231 + position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout counsellingcard = (RelativeLayout) layoutInflater.inflate(R.layout.view_counselling_card, null, true);
            counsellingcard.setBackgroundResource(org.smartregister.immunization.R.drawable.vaccine_card_background_white);
            TextView counsellingdate = (TextView)counsellingcard.findViewById(R.id.name_tv);
            counsellingdate.setText(Utils.convertDateFormat(new DateTime(counsellingList.get(position).getDate().getTime())));
            counsellingcard.findViewById(R.id.undo_b).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            return counsellingcard;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public void update(ArrayList<Counselling> counsellingList) {
      this.counsellingList = counsellingList;
    }





    public List<Counselling> getCounsellingList() {
        return counsellingList;
    }



}
