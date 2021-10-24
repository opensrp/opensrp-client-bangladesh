package org.smartregister.cbhc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.domain.CampaignForm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CampaignListRvAdapter extends RecyclerView.Adapter<CampaignListRvAdapter.Holder>{
    private Context context;
    ArrayList<CampaignForm> campaignFormList;
    private OnClickAdapter onClickAdapter;

    public CampaignListRvAdapter(Context context, ArrayList<CampaignForm> campaignFormList, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.campaignFormList = campaignFormList;
        this.onClickAdapter = onClickAdapter;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.card_item_campaign,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        CampaignForm campaignForm = campaignFormList.get(i);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yy");
        holder.name_tv.setText(campaignForm.getName());
        holder.type_tv.setText(campaignForm.getType());
        holder.date_tv.setText(simpleDateFormat.format(campaignForm.getTargetDate()));
        holder.itemView.setOnClickListener(v -> onClickAdapter.onClick(i, campaignForm));

    }

    public interface OnClickAdapter {
        void onClick(int position, CampaignForm content);
    }

    @Override
    public int getItemCount() {
        return campaignFormList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView name_tv,type_tv,date_tv;
        public Holder(@NonNull View itemView) {
            super(itemView);
            name_tv = itemView.findViewById(R.id.name_tv);
            type_tv = itemView.findViewById(R.id.type_tv);
            date_tv =itemView.findViewById(R.id.date_tv);
        }
    }
}
