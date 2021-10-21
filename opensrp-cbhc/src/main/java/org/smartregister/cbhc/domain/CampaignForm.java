package org.smartregister.cbhc.domain;

import java.util.Date;

public class CampaignForm {
    String name,type,baseEntityId,targetDate;
    Date updatedDate,createdDate;

    public CampaignForm(String name, String type,String baseEntityId, String targetDate,Date updatedDate,Date createdDate) {
        this.name = name;
        this.type = type;
        this.baseEntityId = baseEntityId;
        this.targetDate = targetDate;
        this.updatedDate = updatedDate;
        this.createdDate = createdDate;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }
}
