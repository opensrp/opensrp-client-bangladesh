package org.smartregister.cbhc.domain;

import java.util.Date;

public class ScheduleData {
    String campaignBaseEntityId,syncStatus;
    Date targetDate,updatedDate,createdDate;


    public ScheduleData(String campaignBaseEntityId, Date targetDate, Date updatedDate, Date createdDate,String syncStatus) {
        this.campaignBaseEntityId = campaignBaseEntityId;
        this.targetDate = targetDate;
        this.updatedDate = updatedDate;
        this.createdDate = createdDate;
        this.syncStatus = syncStatus;
    }


    public String getSyncStatus() {
        return syncStatus;
    }

    public String getCampaignBaseEntityId() {
        return campaignBaseEntityId;
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
