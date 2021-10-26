package org.smartregister.growplus.domain;

import java.io.Serializable;
import java.util.Date;

public class CampaignForm implements Serializable {
    String name,type,baseEntityId;
    Date updatedDate,createdDate,targetDate;

    public CampaignForm(String name, String type,String baseEntityId, Date targetDate,Date updatedDate,Date createdDate) {
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

    public Date getTargetDate() {
        return targetDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }
}
