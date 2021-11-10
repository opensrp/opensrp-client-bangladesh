package org.smartregister.cbhc.domain;

import org.smartregister.commonregistry.CommonPersonObjectClient;

public class MembersData {
    String  baseEntityId,fName,lName,dob,age,relation,gender;
    CommonPersonObjectClient pClient;

    public MembersData(String baseEntityId, String fName, String lName, String dob, String age, String relation,String gender,CommonPersonObjectClient pClient) {
        this.baseEntityId = baseEntityId;
        this.fName = fName;
        this.lName = lName;
        this.dob = dob;
        this.age = age;
        this.relation = relation;
        this.gender = gender;
        this.pClient = pClient;
    }


    public String getBaseEntityId() {
        return baseEntityId;
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

    public String getAge() {
        return age;
    }

    public String getRelation() {
        return relation;
    }

    public String getGender() {
        return gender;
    }

    public CommonPersonObjectClient getpClient() {
        return pClient;
    }
}
