package org.smartregister.cbhc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.ChildItemData;
import org.smartregister.cbhc.util.Utils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;

public class ChildListAdapter extends RecyclerView.Adapter<ChildListAdapter.Holder> {
    private Context context;
    private ArrayList<ChildItemData> childItemDataArrayList;
    private ArrayList<ChildItemData> tempChildItemDataArrayList;
    private TotalChild totalChild;
    private OnClickHandler onClickHandler;


    public ChildListAdapter(Context context, ArrayList<ChildItemData> childItemDataArrayList,TotalChild totalChild,OnClickHandler onClickHandler) {
        this.context = context;
        this.childItemDataArrayList = childItemDataArrayList;
        this.tempChildItemDataArrayList= childItemDataArrayList;
        this.totalChild = totalChild;
        this.onClickHandler = onClickHandler;
        totalChild.onChange(tempChildItemDataArrayList.size()+" Child's");
    }

    @NonNull
    @Override
    public ChildListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.card_item_child,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChildListAdapter.Holder holder, @SuppressLint("RecyclerView") int i) {
        ChildItemData childItemData = tempChildItemDataArrayList.get(i);
        holder.nameTv.setText(childItemData.getFirstName()+" "+childItemData.getLastName());
        holder.ageTv.setText(Utils.getDuration(childItemData.getDob()));
//        holder.weightTv.setText(childItemData.getWeight());
        if(childItemData.getGender().equals("F")) {
            holder.profileImage.setImageResource(R.drawable.child_girl_infant);
            holder.childImageSmall.setImageResource(R.drawable.female_child_cbhc);
        }
        else {
            holder.profileImage.setImageResource(R.drawable.child_boy_infant);
            holder.childImageSmall.setImageResource(R.drawable.male_child_cbhc);
        }

        
        holder.itemView.setOnClickListener(view -> onClickHandler.onClick(i,childItemData));


    }

    @Override
    public int getItemCount() {
        return tempChildItemDataArrayList.size();
    }

    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                tempChildItemDataArrayList = (ArrayList<ChildItemData>) results.values;
                int totalCh = tempChildItemDataArrayList.size();
                totalChild.onChange(totalCh>1?totalCh+" Child's":totalCh+" Child");
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<ChildItemData> filteredResults = null;
                if (constraint.length() == 0) {
                    filteredResults = childItemDataArrayList;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }

    protected ArrayList<ChildItemData> getFilteredResults(String constraint) {
        ArrayList<ChildItemData> results = new ArrayList<>();

        for (ChildItemData item : childItemDataArrayList) {
            if ((item.getFirstName().toLowerCase()+" "+item.getLastName().toLowerCase()).contains(constraint)) {
                results.add(item);
            }
        }
        return results;
    }

    public void setData( ArrayList<ChildItemData> childItemDataArrayList) {
        this.childItemDataArrayList = childItemDataArrayList;
        this.tempChildItemDataArrayList= childItemDataArrayList;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {
        CustomFontTextView nameTv,ageTv,weightTv;
        ImageView profileImage,childImageSmall;
        public Holder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.name_tv);
            ageTv = itemView.findViewById(R.id.age_tv);
            //weightTv= itemView.findViewById(R.id.weight_tv);

            profileImage = itemView.findViewById(R.id.profile_image_iv);
            childImageSmall= itemView.findViewById(R.id.childSmallImageView);
        }
    }

    public interface TotalChild{
        void onChange(String totalStr);
    }

    public interface OnClickHandler{
        void onClick(int position,ChildItemData childItemData);
    }
}
