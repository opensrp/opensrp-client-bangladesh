package org.smartregister.cbhc.repository;

public class EventLog {
    private String BaseEntityId;
    private String ClientJson;
    private String EventJson;
    private String FormJson;
    private String EventId;
    private String EventType;
    private String FamilyId;
    private String EventDate;

    public String getClientJson() {
        return ClientJson;
    }

    public void setClientJson(String clientJson) {
        ClientJson = clientJson;
    }

    public String getFormJson() {
        return FormJson;
    }

    public void setFormJson(String formJson) {
        FormJson = formJson;
    }

    public String getEventId() {
        return EventId;
    }

    public void setEventId(String EventId) {
        this.EventId = EventId;
    }

    public String getEventType() {
        return EventType;
    }

    public void setEventType(String EventType) {
        this.EventType = EventType;
    }

    public String getBaseEntityId() {
        return BaseEntityId;
    }

    public void setBaseEntityId(String BaseEntityId) {
        this.BaseEntityId = BaseEntityId;
    }

    public String getFamilyId() {
        return FamilyId;
    }

    public void setFamilyId(String FamilyId) {
        this.FamilyId = FamilyId;
    }

    public String getEventDate() {
        return EventDate;
    }

    public void setEventDate(String EventDate) {
        this.EventDate = EventDate;
    }

    public String getEventJson() {
        return EventJson;
    }

    public void setEventJson(String EventJson) {
        this.EventJson = EventJson;
    }


}
