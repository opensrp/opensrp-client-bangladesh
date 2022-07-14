package org.smartregister.cbhc.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.load.engine.Resource;

import org.joda.time.DateTime;
import org.smartregister.Context;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.activity.MemberProfileActivity;
import org.smartregister.cbhc.domain.GuestMemberData;
import org.smartregister.cbhc.domain.MembersData;
import org.smartregister.cbhc.fragment.ProfileOverviewFragment;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.cbhc.util.JsonFormUtils;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.Holder> {
    private android.content.Context context;
    private ArrayList<MembersData> membersDataArrayList;
    private OnItemClick onItemClick;
    private OnEditClick onEditClick;
    private OnBirthClick onBirthClick;
    String clientype="";

    public MembersAdapter(android.content.Context context,ArrayList<MembersData> membersDataArrayList,
                          OnItemClick onItemClick,OnEditClick onEditClick,OnBirthClick onBirthClick) {
        this.context= context;
        this.membersDataArrayList = membersDataArrayList;
        this.onItemClick = onItemClick;
        this.onEditClick = onEditClick;
        this.onBirthClick = onBirthClick;
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_guest_member, null));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, @SuppressLint("RecyclerView") int i) {
        final MembersData content = membersDataArrayList.get(i);
        String age = "0";
        holder.editLay.setVisibility(View.VISIBLE);
        holder.textViewName.setText((content.getfName()+" "+content.getlName()));
        holder.textViewGender.setText(content.getRelation());

        if(content.getAge().equals("")){
            String dobString = Utils.getDuration(content.getDob());
           // if(content.getDob().length()>10){
                age = String.valueOf(Utils.getAgeFromDate(content.getDob()));

            holder.textViewAge.setText(Utils.getDuration(content.getDob()));
           // }

        }else{
            age = content.getAge();
            holder.textViewAge.setText(age+"y");
        }


        //total birth button visible invisible logic
        if (!content.getTasks().equals("0") && !content.getTasks().isEmpty()) {
            try {
                int tasks_count = Integer.valueOf(content.getTasks());
                if (tasks_count > 0) {
                    holder.total_birth_btn.setText(context.getString(R.string.total_unregister_child, tasks_count + ""));
                    holder.total_birth_btn.setVisibility(View.VISIBLE);
                } else {
                    holder.total_birth_btn.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Utils.appendLog(ProfileOverviewFragment.class.getName(), e);

            }

        } else {
            try{
                int tasks_count = Integer.valueOf(content.getTasks());
                if(content.getMaritalStatus().equalsIgnoreCase("Married") && content.getGender().equalsIgnoreCase("f")){
                    holder.total_birth_btn.setText("+Child");
                    holder.total_birth_btn.setVisibility(View.VISIBLE);
                }else {
                    holder.total_birth_btn.setVisibility(View.GONE);
                }
            }catch (Exception e){
                Utils.appendLog(ProfileOverviewFragment.class.getName(), e);
            }
        }

        //member type logic
        try{
            if (Integer.parseInt(age) < 5) {
                if (!TextUtils.isEmpty(content.getGender()) && content.getGender().equalsIgnoreCase("m")) {
                    if (content.getpClient().entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        holder.profileImage.setTag(org.smartregister.R.id.entity_id, content.getpClient().entityId());

                        DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(content.getpClient().entityId(), OpenSRPImageLoader.getStaticImageListener(holder.profileImage, R.drawable.child_boy_infant, R.drawable.child_boy_infant));

                        holder.smallImg.setVisibility(View.VISIBLE);
                        holder.smallImg.setImageResource(R.drawable.male_child_cbhc);
                    }
                } else if (!TextUtils.isEmpty(content.getGender()) && content.getGender().equalsIgnoreCase("f")) {
                    if (content.getpClient().entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        holder.profileImage.setTag(org.smartregister.R.id.entity_id, content.getpClient().entityId());

                        DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(content.getpClient().entityId(), OpenSRPImageLoader.getStaticImageListener(holder.profileImage, R.drawable.child_girl_infant, R.drawable.child_girl_infant));

                        holder.smallImg.setVisibility(View.VISIBLE);
                        holder.smallImg.setImageResource(R.drawable.female_child_cbhc);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(content.getGender()) && content.getGender().equalsIgnoreCase("m")) {
                    if (content.getpClient().entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        holder.profileImage.setTag(org.smartregister.R.id.entity_id, content.getpClient().entityId());

                        DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(content.getpClient().entityId(), OpenSRPImageLoader.getStaticImageListener(holder.profileImage, R.drawable.male_cbhc_placeholder, R.drawable.male_cbhc_placeholder));

                        holder.smallImg.setVisibility(View.INVISIBLE);
                    }
                } else if (!TextUtils.isEmpty(content.getGender()) && content.getGender().equalsIgnoreCase("f")) {
                    if (content.getpClient().entityId() != null) {//image already in local storage most likey ):
                        //set profile image by passing the client id.If the image doesn't exist in the image org.smartregister.cbhc.repository then download and save locally
                        holder.profileImage.setTag(org.smartregister.R.id.entity_id, content.getpClient().entityId());
                        if(content.getPregnancyStatus().equalsIgnoreCase("Antenatal Period")){
                            holder.smallImg.setVisibility(View.VISIBLE);
                            holder.smallImg.setImageResource(R.drawable.pregnant_woman);
                        }else{
                            holder.smallImg.setVisibility(View.INVISIBLE);
                        }
                        DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(content.getpClient().entityId(), OpenSRPImageLoader.getStaticImageListener( holder.profileImage, R.drawable.women_cbhc_placeholder, R.drawable.women_cbhc_placeholder));

                    }
                }
            }

        }catch (Exception e){}

        //click listeners
        holder.itemView.setOnClickListener(view -> onItemClick.onClick(i,content,getClientType(content)));
        holder.editLay.setOnClickListener(view -> onEditClick.onClick(i,content,getClientType(content)));
        holder.total_birth_btn.setOnClickListener(view -> onBirthClick.onClick(i,content));
    }

    /**
     * getting user type here
     * @param content
     * @return
     */
    String getClientType(MembersData content) {
        String age = "0";
        if(content.getAge().equals("")){
            String dobString = Utils.getDuration(content.getDob());
            if(content.getDob().length()>10){
                age = String.valueOf(getAge((new DateTime(content.getDob()))));
            }

        }else{
            age = content.getAge();
        }

        try {
            if (Integer.parseInt(age) < 5) {
                if (!TextUtils.isEmpty(content.getGender()) && content.getGender().equalsIgnoreCase("m")) {
                    if (content.getpClient().entityId() != null) {//image already in local storage most likey ):
                        return "malechild";
                    }
                } else if (!TextUtils.isEmpty(content.getGender()) && content.getGender().equalsIgnoreCase("f")) {
                    if (content.getpClient().entityId() != null) {//image already in local storage most likey ):
                        return "femalechild";
                    }
                }
            } else {
                if (!TextUtils.isEmpty(content.getGender()) && content.getGender().equalsIgnoreCase("m")) {
                    if (content.getpClient().entityId() != null) {//image already in local storage most likey ):
                        return "member";
                    }
                } else if (!TextUtils.isEmpty(content.getGender()) && content.getGender().equalsIgnoreCase("f")) {
                    if (content.getpClient().entityId() != null) {//image already in local storage most likey ):
                        return "woman";
                    }
                }
            }
        }catch (Exception e){}
        return "";
    }

    @Override
    public int getItemCount() {
        return membersDataArrayList.size();
    }

    public static class Holder extends RecyclerView.ViewHolder{
        public CustomFontTextView textViewForumDate,textViewName,textViewAge,textViewGender;
        LinearLayout editLay;
        ImageView profileImage,smallImg;
        Button total_birth_btn;
        public Holder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.name_tv);
            textViewAge = itemView.findViewById(R.id.age_tv);
            textViewGender = itemView.findViewById(R.id.gender_id);

            editLay = itemView.findViewById(R.id.editLay);
            profileImage = itemView.findViewById(R.id.profile_image);
            smallImg = itemView.findViewById(R.id.small_image);

            total_birth_btn = itemView.findViewById(R.id.total_birth_btn);
        }
    }


    /**
     * age calculation
     * @param dateOfBirth
     * @return
     */
    public static int getAge(DateTime dateOfBirth) {

        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        int age = 0;

        SimpleDateFormat dateFormat = JsonFormUtils.DATE_FORMAT;
        Date convertedDate = new Date();
        try {
            convertedDate = dateOfBirth.toDate();
        } catch (Exception e) {
            Utils.appendLog(ProfileOverviewFragment.class.getName(), e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        birthDate.setTime(convertedDate);
        if (birthDate.after(today)) {
            throw new IllegalArgumentException("Can't be born in the future");
        }

        age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // If birth date is greater than todays date (after 2 days adjustment of
        // leap year) then decrement age one year
        if ((birthDate.get(Calendar.DAY_OF_YEAR)
                - today.get(Calendar.DAY_OF_YEAR) > 3)
                || (birthDate.get(Calendar.MONTH) > today.get(Calendar.MONTH))) {
            age--;

            // If birth date and todays date are of same month and birth day of
            // month is greater than todays day of month then decrement age
        } else if ((birthDate.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                && (birthDate.get(Calendar.DAY_OF_MONTH) > today
                .get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }

    /**
     * view on item click listeners
     */
    public interface OnItemClick{
        void onClick(int position, MembersData membersData,String clientType);
    }

    /**
     * edit click listeners
     */
    public interface OnEditClick{
        void onClick(int position, MembersData membersData,String clientType);
    }

    /**
     * total birth listeners
     */
    public interface OnBirthClick{
        void onClick(int position, MembersData membersData);
    }
}
