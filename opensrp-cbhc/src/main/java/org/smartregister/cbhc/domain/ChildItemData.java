package org.smartregister.cbhc.domain;

public class ChildItemData {
    String baseEntityId,firstName,lastName,dob,gender,weight;

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

    public ChildItemData(String baseEntityId, String firstName, String lastName, String dob, String gender, String weight) {
        this.baseEntityId = baseEntityId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.weight= weight;
    }
}
