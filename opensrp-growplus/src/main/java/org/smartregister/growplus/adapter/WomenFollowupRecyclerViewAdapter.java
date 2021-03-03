package org.smartregister.growplus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.growplus.R;

import java.util.List;
import java.util.Map;

public class WomenFollowupRecyclerViewAdapter extends RecyclerView.Adapter<WomenFollowupRecyclerViewAdapter.ViewHolder> {

    private Map<String, String> mData;
    private String[] keys;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public WomenFollowupRecyclerViewAdapter(Context context, Map<String, String> data, String[] keys) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.keys = keys;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.content_women_followup_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String key = keys[position];
        holder.myTextViewKey.setText(key);
        holder.myTextViewValue.setText(mData.get(key));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextViewKey;
        TextView myTextViewValue;

        ViewHolder(View itemView) {
            super(itemView);
            myTextViewKey = itemView.findViewById(R.id.women_followup_key);
            myTextViewValue = itemView.findViewById(R.id.women_followup_value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}