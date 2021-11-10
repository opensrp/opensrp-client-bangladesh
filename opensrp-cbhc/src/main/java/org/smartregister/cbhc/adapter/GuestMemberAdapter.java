package org.smartregister.cbhc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.joda.time.DateTime;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.domain.GuestMemberData;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;

public class GuestMemberAdapter extends RecyclerView.Adapter<GuestMemberAdapter.GuestMemberViewHolder> {
    private ArrayList<GuestMemberData> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public GuestMemberAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }
    public static class GuestMemberViewHolder extends RecyclerView.ViewHolder{
        public CustomFontTextView textViewForumDate,textViewName,textViewAge,textViewGender;
        public ImageView profileImage,smallImage;

        public GuestMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.name_tv);
            textViewAge = itemView.findViewById(R.id.age_tv);
            textViewGender = itemView.findViewById(R.id.gender_id);

            profileImage= itemView.findViewById(R.id.profile_image);
            smallImage = itemView.findViewById(R.id.small_image);
        }
    }

    public void setData(ArrayList<GuestMemberData> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public GuestMemberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new GuestMemberViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_guest_member, null));

    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull final GuestMemberViewHolder viewHolder, int position) {
        final GuestMemberData content = contentList.get(position);
        viewHolder.profileImage.setVisibility(View.VISIBLE);
        viewHolder.smallImage.setVisibility(View.VISIBLE);

        viewHolder.textViewName.setText(context.getString(R.string.name2,(content.getfName()+" "+content.getlName())));
        viewHolder.textViewGender.setText(context.getString(R.string.gender,getGender(content.getGender())));

       if(content.getAge().equals("")){
           String dobString = Utils.getDuration(content.getDob());
           viewHolder.textViewAge.setText(context.getString(R.string.age2,dobString));
       }else{
           viewHolder.textViewAge.setText(context.getString(R.string.age2,content.getAge()));
       }

       if(content.getGender().equalsIgnoreCase("f")) {
           viewHolder.profileImage.setImageResource(R.drawable.child_girl_infant);
           viewHolder.smallImage.setImageResource(R.drawable.female_child_cbhc);
       }else{
           viewHolder.profileImage.setImageResource(R.drawable.child_boy_infant);
           viewHolder.smallImage.setImageResource(R.drawable.male_child_cbhc);
       }

        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(position, content));
    }

    public static String getGender(String value){
        if(value.equalsIgnoreCase("F")){
            //return "মহিলা";
            return "Female";
        }
        else if(value.equalsIgnoreCase("M")){
            //return "পুরুষ";
            return "Male";
        }
        return "";
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, GuestMemberData content);
    }


}
