package util;

import org.smartregister.growplus.view.Field;

import java.util.ArrayList;
import java.util.List;

public class SortFilterUtil {

    public enum FieldType{
        SORT,
        FILTER
    }
    public enum SelectType{
        SINGLE_SELECT,
        MULTI_SELECT
    }
    private static List<Field> sortFields = new ArrayList<Field>();
    private static List<Field> filterFields = new ArrayList<Field>();
    private static SelectType sortFieldType = SelectType.SINGLE_SELECT;
    private static SelectType filterFieldType = SelectType.MULTI_SELECT;

    public static void init(){
        sortFields.clear();
        filterFields.clear();
        
        //adding sort fields
        sortFields.add(new Field("Household Name(A-Z)","first_name asc"));
        sortFields.add(new Field("Household Name(Z-A)","first_name desc"));
        sortFields.add(new Field("Last Visit Date(ASC)","last_interacted_with asc"));
        sortFields.add(new Field("Last Visit Date(DESC)","last_interacted_with desc"));
        sortFields.add(new Field("Member Number(ASC)","member_count asc"));
        sortFields.add(new Field("Member Number(DESC)","member_count desc"));
        
        //adding filterfields
//        filterFields.add(new Field("Gender"));
//        filterFields.add(new Field("Ward"));
//        filterFields.add(new Field("Block"));

    }

    public static void initFilterFields(String[] filters){
    for(String filter:filters){
        filterFields.add(new Field(filter));
    }
    }
    public static boolean isChecked(FieldType ft,int position){
        switch(ft){
            case SORT:
                return sortFields.get(position).isSelected();
            case FILTER:
                return filterFields.get(position).isSelected();
        }
        return false;
    }
    public static void setChecked(FieldType ft,int which){
        switch(ft){
            case SORT:
                if(sortFieldType==SelectType.SINGLE_SELECT) {
                    for (Field fd : sortFields) {
                        fd.setSelected(false);
                    }
                }
                sortFields.get(which).setSelected(!sortFields.get(which).isSelected());
                break;
            case FILTER:
                if(filterFieldType==SelectType.SINGLE_SELECT) {
                    for (Field fd : filterFields) {
                        fd.setSelected(false);
                    }
                }
                filterFields.get(which).setSelected(!filterFields.get(which).isSelected());
                break;
        }

    }
    public static int currentChecked(FieldType ft){
        switch(ft){
            case SORT:
                for (Field fd : sortFields) {
                    if(fd.isSelected()){
                        return sortFields.indexOf(fd);
                    }
                }
                break;
            case FILTER:

                break;
        }
        return -1;
    }
    private static final String deafult_sort_query = "HHID desc";
    public static String getSortQuery() {
        for (Field fd : sortFields) {
            if(fd.isSelected()){
                return fd.getSort_query();
            }
        }
        return deafult_sort_query;
    }

    public static int[] currentCheckedList(FieldType ft){
        int[]checked_list = new int[10];

        switch(ft){
            case SORT:

                break;
            case FILTER:
                checked_list = new int[filterFields.size()];
                for (int i=0;i<filterFields.size();i++) {
//                    checked_list[i] =
                }
                break;
        }
        return checked_list;
    }
    public static void setFieldType(FieldType ft,SelectType st) {
        switch(ft){
            case SORT:
                sortFieldType = st;
            case FILTER:
                filterFieldType = st;
        }
    }

    public static List<Field> getFields(FieldType ft) {
        switch(ft){
            case SORT:
                return sortFields;
            case FILTER:
                return filterFields;
        }
        return null;
    }


}
