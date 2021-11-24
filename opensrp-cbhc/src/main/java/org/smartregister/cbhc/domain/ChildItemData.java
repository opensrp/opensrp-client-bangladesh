package org.smartregister.cbhc.domain;

import org.smartregister.commonregistry.CommonPersonObjectClient;

public class ChildItemData {
    String baseEntityId,firstName,lastName,dob,gender,weight,child_status,height,vaccineName,vaccineDate;
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

    public String getHeight() {
        return height;
    }

    public String getVaccineDate() {
        return vaccineDate;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public String getChild_status() {
        return child_status;
    }

    public ChildItemData(String baseEntityId, String firstName, String lastName, String dob, String gender, String weight,String height,String vaccineName, String vaccineDate, String child_status, CommonPersonObjectClient pClient) {
        this.baseEntityId = baseEntityId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.weight= weight;
        this.height = height;
        this.vaccineName = vaccineName;
        this.vaccineDate = vaccineDate;
        this.pClient = pClient;
        this.child_status = child_status;
    }
}
