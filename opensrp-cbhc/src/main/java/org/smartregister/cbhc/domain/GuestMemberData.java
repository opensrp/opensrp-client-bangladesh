package org.smartregister.cbhc.domain;

public class GuestMemberData {
    String baseEntity,fName,lName,dob,gender,age;

    public GuestMemberData(String baseEntity,String fName, String lName, String dob, String gender, String age) {
        this.baseEntity = baseEntity;
        this.fName = fName;
        this.lName = lName;
        this.dob = dob;
        this.gender = gender;
        this.age = age;
    }

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getBaseEntity() {
        return baseEntity;
    }
}
