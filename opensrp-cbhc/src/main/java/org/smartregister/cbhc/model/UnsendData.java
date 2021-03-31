package org.smartregister.cbhc.model;

public class UnsendData {
    String baseEntityId;
    String type;
    public UnsendData(String baseEntityId, String type){
        this.baseEntityId = baseEntityId;
        this.type = type;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
