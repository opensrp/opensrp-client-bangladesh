package org.smartregister.growplus.view;

public class Field {

    private String field_name;
    private String field_type;
    private String display_name;
    private boolean isSelected;
    private String sort_query;

    public Field(String display_name){
        this.display_name = display_name;
    }
    public Field(String display_name, String sort_query){
        this.display_name = display_name;
        this.sort_query = sort_query;
    }

    public String getFilterQuery(){
        return " ward = '"+display_name+"' ";
    }

    public String getBlockFilterQuery(){
        return " block like '%"+display_name+"%' ";
    }
    public String getSort_query() {
        return sort_query;
    }

    public void setSort_query(String sort_query) {
        this.sort_query = sort_query;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Field(String field_name, String field_type, String display_name) {
        this.field_name = field_name;
        this.field_type = field_type;
        this.display_name = display_name;
    }

    public String getFieldName() {
        return field_name;
    }

    public void setFieldName(String field_name) {
        this.field_name = field_name;
    }

    public String getFieldType() {
        return field_type;
    }

    public void setFieldType(String field_type) {
        this.field_type = field_type;
    }

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String display_name) {
        this.display_name = display_name;
    }
}
