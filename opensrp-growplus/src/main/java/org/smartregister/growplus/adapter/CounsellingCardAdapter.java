package org.smartregister.growplus.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.smartregister.growplus.R;
import org.smartregister.growplus.domain.Counselling;
import org.smartregister.growplus.listener.ActivityListener;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.smartregister.immunization.util.VaccinatorUtils.generateScheduleList;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by Jason Rogena - jrogena@ona.io on 22/02/2017.
 */
public class CounsellingCardAdapter extends BaseAdapter {
    private static final String TAG = "CounsellingCardAdapter";
    private final Context context;

    private List<Counselling> counsellingList;
    private String[] mWomenFollowupKeys;
    private HashMap<String, String> mWomenFollowupData;
    private ActivityListener mActivityListener;


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
                    Log.e(TAG, "TODO Call"); // TODO Counselling
                    if(mActivityListener == null) return;
                    mActivityListener.onCallbackActivity(counsellingList.get(position));
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

    public void setActivityListener(ActivityListener activityListener){
        mActivityListener = activityListener;
    }


}
