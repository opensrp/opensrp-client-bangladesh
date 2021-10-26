package org.smartregister.growplus.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.smartregister.growplus.R;
import org.smartregister.growplus.domain.ScheduleData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ScheduleListTvAdapter extends RecyclerView.Adapter<ScheduleListTvAdapter.Holder> {
    private Context context;
    ArrayList<ScheduleData> scheduleDataArrayList;
    private OnClickAdapter onClickAdapter;

    public ScheduleListTvAdapter(Context context, ArrayList<ScheduleData> scheduleDataArrayList, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.scheduleDataArrayList = scheduleDataArrayList;
        this.onClickAdapter = onClickAdapter;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.card_item_schedule,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        ScheduleData scheduleData = scheduleDataArrayList.get(i);
        holder.schedule_title_tv.setText("Schedule "+(i+1));
        holder.target_date_tv.setText(new SimpleDateFormat("dd/MM/yyyy").format(scheduleData.getTargetDate()));
        holder.imageView_edit.setOnClickListener(v -> onClickAdapter.onClick(i, scheduleData));
    }
    public interface OnClickAdapter {
        void onClick(int position, ScheduleData content);
    }

    @Override
    public int getItemCount() {
        return scheduleDataArrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView schedule_title_tv,target_date_tv;
        ImageView imageView_edit;
        public Holder(@NonNull View itemView) {
            super(itemView);
            schedule_title_tv = itemView.findViewById(R.id.schedule_title_tv);
            target_date_tv = itemView.findViewById(R.id.target_date_tv);
            imageView_edit = itemView.findViewById(R.id.imageView_edit);
        }
    }
}
