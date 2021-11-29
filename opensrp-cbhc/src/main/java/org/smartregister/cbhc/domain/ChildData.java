package org.smartregister.cbhc.domain;

public class ChildData {
    String child_height,child_weight,child_muac,child_status,has_edema;

    public ChildData(String child_height, String child_weight, String child_muac, String child_status,String has_edema) {
        this.child_height = child_height;
        this.child_weight = child_weight;
        this.child_muac = child_muac;
        this.child_status = child_status;
        this.has_edema = has_edema;
    }

    public String getChild_height() {
        return child_height;
    }

    public String getChild_weight() {
        return child_weight;
    }

    public String getChild_muac() {
        return child_muac;
    }

    public String getChild_status() {
        return child_status;
    }

    public String getHas_edema() {
        return has_edema;
    }
}
