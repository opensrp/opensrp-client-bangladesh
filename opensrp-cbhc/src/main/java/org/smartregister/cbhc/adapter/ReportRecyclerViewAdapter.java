package org.smartregister.cbhc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.domain.ReportData;

import java.util.ArrayList;

public class ReportRecyclerViewAdapter extends RecyclerView.Adapter<ReportRecyclerViewAdapter.Holder> {
    Context context;
    ArrayList<ReportData> arrayList;

    public ReportRecyclerViewAdapter(Context context,ArrayList<ReportData> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ReportRecyclerViewAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.key_achievement_card,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportRecyclerViewAdapter.Holder holder, int i) {
        ReportData reportData = arrayList.get(i);

        holder.thumbnail.setColorFilter(ContextCompat.getColor(context, reportData.getColor()));

        holder.title.setText(reportData.getTitle());
        holder.subtitle.setText(reportData.getSubTitle());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        public TextView subtitle, title;
        public ImageView thumbnail, overflow;
        public Holder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.count);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }
}
