package org.smartregister.cbhc.model;

public class UnsendData {
    String baseEntityId;
    String type;
    long lastInteractedDate;
    boolean isSend;

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getLastInteractedDate() {
        return lastInteractedDate;
    }

    public void setLastInteractedDate(long lastInteractedDate) {
        this.lastInteractedDate = lastInteractedDate;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }
}
