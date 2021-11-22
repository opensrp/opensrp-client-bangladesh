package org.smartregister.cbhc.domain;

import org.smartregister.commonregistry.CommonPersonObjectClient;

public class ChildItemData {
    String baseEntityId,firstName,lastName,dob,gender,weight,child_status;
    CommonPersonObjectClient pClient;

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getWeight() {
        return weight;
    }

    public CommonPersonObjectClient getpClient() {
        return pClient;
    }

    public String getChild_status() {
        return child_status;
    }

    public ChildItemData(String baseEntityId, String firstName, String lastName, String dob, String gender, String weight, String child_status, CommonPersonObjectClient pClient) {
        this.baseEntityId = baseEntityId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.weight= weight;
        this.pClient = pClient;
        this.child_status = child_status;
    }
}
