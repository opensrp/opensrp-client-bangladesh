package org.smartregister.cbhc.notification;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.cbhc.R;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder>{
    private ArrayList<NotificationDTO> contentList;
    private Context context;
    private NotificationAdapter.OnClickAdapter onClickAdapter;

    public NotificationAdapter(Context context, NotificationAdapter.OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<NotificationDTO> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new NotificationViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_notification, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder viewHolder, int position) {
        final NotificationDTO content = contentList.get(position);
        viewHolder.textViewDate.setText(content.getDate()+"");
        viewHolder.textViewTitle.setText(content.getText()+"");
        viewHolder.imageViewAppIcon.setImageResource(R.drawable.ic_notification);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAdapter.onClick(viewHolder.getAdapterPosition(), content);
            }
        });
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, NotificationDTO content);
    }
}
