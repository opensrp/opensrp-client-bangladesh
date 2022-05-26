package org.smartregister.cbhc.troublshoot;


import static org.smartregister.cbhc.troublshoot.InvalidDataDisplayActivity.TYPE_CLIENT;
import static org.smartregister.cbhc.troublshoot.InvalidDataDisplayActivity.TYPE_EVENT;

import android.annotation.SuppressLint;
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

public class InvalidDataAdapter extends RecyclerView.Adapter<InvalidDataAdapter.InvalidDataViewHolder> {
    private ArrayList<InvalidDataModel> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;
    private int type;

    public InvalidDataAdapter(Context context, OnClickAdapter onClickAdapter, int type) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
        this.type = type;
    }

    public void setData(ArrayList<InvalidDataModel> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public InvalidDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(type == TYPE_CLIENT){
            return new InvalidDataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invalid_client_view_content, null));
        }else if(type == TYPE_EVENT){
            return new InvalidDataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invalid_event_view_content, null));

        }
        return null;

    }

    @Override
    public void onBindViewHolder(@NonNull final InvalidDataViewHolder viewHolder, int position) {

        final InvalidDataModel content = contentList.get(position);
        try{
            viewHolder.textViewDate.setText(content.date.getYear()+"-"+content.date.getMonthOfYear()+"-"+content.date.getDayOfMonth());
        }catch (Exception e){

        }
        if(content.needToDelete){
            viewHolder.imageViewDelete.setVisibility(View.VISIBLE);
        }else {
            viewHolder.imageViewDelete.setVisibility(View.GONE);
        }
        if(type==TYPE_CLIENT){
            viewHolder.textViewName.setText(content.firstName);
            viewHolder.textViewUniqueId.setText(content.unique_id);
            viewHolder.textViewAddress.setText(content.address);
            viewHolder.textViewBaseEntityId.setText(content.baseEntityId);
        }else{
            viewHolder.textViewBaseEntityId.setText(content.formSubmissionId);
        }
        viewHolder.textViewEventName.setText(content.eventType);
        viewHolder.textViewErrorCause.setText(content.errorCause);
        viewHolder.textViewAction.setText(content.action);

        viewHolder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAdapter.onDelete(position,content);
            }
        });
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(position, content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, InvalidDataModel content);
        void onDelete(int position, InvalidDataModel content);
    }
    public static class InvalidDataViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewDate,textViewName,textViewEventName,textViewErrorCause,textViewUniqueId,
                textViewAddress,textViewBaseEntityId,textViewAction;
        public ImageView imageViewDelete;

        public InvalidDataViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.date_tv);
            textViewName = itemView.findViewById(R.id.first_name);
            textViewEventName = itemView.findViewById(R.id.event_type);
            textViewErrorCause = itemView.findViewById(R.id.error_cause);
            textViewUniqueId = itemView.findViewById(R.id.unique_id_tv);
            textViewAddress = itemView.findViewById(R.id.address_tv);
            textViewBaseEntityId = itemView.findViewById(R.id.base_entity_id);
            textViewAction = itemView.findViewById(R.id.action_tv);
            imageViewDelete = itemView.findViewById(R.id.delete_btn);
        }
    }
}
