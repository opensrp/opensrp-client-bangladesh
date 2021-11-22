package org.smartregister.cbhc.domain;

import org.smartregister.commonregistry.CommonPersonObjectClient;

public class GuestMemberData {
    String baseEntity,fName,lName,dob,gender,age;
    CommonPersonObjectClient pClient;

    public GuestMemberData(String baseEntity,String fName, String lName, String dob, String gender, String age,CommonPersonObjectClient pClient) {
        this.baseEntity = baseEntity;
        this.fName = fName;
        this.lName = lName;
        this.dob = dob;
        this.gender = gender;
        this.age = age;
        this.pClient = pClient;
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

    public CommonPersonObjectClient getpClient() {
        return pClient;
    }
}
