package org.smartregister.cbhc.domain;

/**
 * Created by raihan on 12/20/18.
 */


public class draft_form_object{
    String ID_COLUMN,  FormNAME, DATE, draft_STATUS,DraftFormJson, UPDATED_AT_COLUMN;
    String household_BASE_ENTITY_ID = "";

    public String getDraftFormJson() {
        return DraftFormJson;
    }

    public void setDraftFormJson(String draftFormJson) {
        DraftFormJson = draftFormJson;
    }

    public String getID_COLUMN() {
        return ID_COLUMN;
    }

    public void setID_COLUMN(String ID_COLUMN) {
        this.ID_COLUMN = ID_COLUMN;
    }

    public String getHousehold_BASE_ENTITY_ID() {
        return household_BASE_ENTITY_ID;
    }

    public void setHousehold_BASE_ENTITY_ID(String household_BASE_ENTITY_ID) {
        this.household_BASE_ENTITY_ID = household_BASE_ENTITY_ID;
    }

    public String getFormNAME() {
        return FormNAME;
    }

    public void setFormNAME(String formNAME) {
        FormNAME = formNAME;
    }

    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public String getDraft_STATUS() {
        return draft_STATUS;
    }

    public void setDraft_STATUS(String draft_STATUS) {
        this.draft_STATUS = draft_STATUS;
    }

    public String getUPDATED_AT_COLUMN() {
        return UPDATED_AT_COLUMN;
    }

    public void setUPDATED_AT_COLUMN(String UPDATED_AT_COLUMN) {
        this.UPDATED_AT_COLUMN = UPDATED_AT_COLUMN;
    }
}