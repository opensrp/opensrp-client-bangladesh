package org.smartregister.cbhc.troublshoot;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.cbhc.R;

import java.util.ArrayList;

public class ForceSynItemAdapter extends RecyclerView.Adapter<ForceSynItemAdapter.ForceItemViewHolder> {
    public interface OnClickAdapter{
        void onClickItem(int position);
    }
    private ArrayList<ForceSyncModel> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public ForceSynItemAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<ForceSyncModel> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public ForceItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ForceItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_forse_sync_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ForceItemViewHolder paymentViewHolder, int position) {
        ForceSyncModel content = contentList.get(position);
        paymentViewHolder.checkBox.setTag(position);
        paymentViewHolder.itemText.setText(content.title);
        if(content.isSelected){
            paymentViewHolder.checkBox.setImageResource(R.drawable.ic_checked_f);

        }else{
            paymentViewHolder.checkBox.setImageResource(R.drawable.ic_unchecked_f);
        }
        paymentViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.isSelected = !content.isSelected;
                notifyItemChanged(position);
            }
        });
        paymentViewHolder.itemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.isSelected = !content.isSelected;
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    //call this method from confirm button

    public String getSelectedServiceQuery(){
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<contentList.size();i++){
            if(contentList.get(i).isSelected){
                if(builder.toString().isEmpty()){
                    builder.append(" eventType = '"+contentList.get(i).eventType+"'");
                }else{
                    builder.append(" OR ");
                    builder.append(" eventType = '"+contentList.get(i).eventType+"'");
                }
            }
        }
        return builder.toString();

    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override public int getItemViewType(int position) {
        return position;
    }
    public class ForceItemViewHolder extends RecyclerView.ViewHolder{

        public ImageView checkBox;
        public TextView itemText;
        public ForceItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checked_image);
            itemText =  itemView.findViewById(R.id.service_name);
        }
    }

}
