package org.smartregister.cbhc.domain;

import org.smartregister.commonregistry.CommonPersonObjectClient;

public class GuestMemberData {
    String baseEntity,fName,lName,dob,gender,age,weight,height,muac,edema,vaccName,vacDate;
    CommonPersonObjectClient pClient;

    public GuestMemberData(String baseEntity,String fName, String lName, String dob, String gender,
                           String age,CommonPersonObjectClient pClient,String weight,String height,
                           String muac,String edema,String vacDate,String vaccName) {
        this.baseEntity = baseEntity;
        this.fName = fName;
        this.lName = lName;
        this.dob = dob;
        this.gender = gender;
        this.age = age;
        this.pClient = pClient;

        this.weight = weight;
        this.height = height;
        this.muac = muac;
        this.edema = edema;
        this.vaccName = vaccName;
        this.vacDate = vacDate;
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

    public String getWeight() {
        return weight;
    }

    public String getHeight() {
        return height;
    }

    public String getMuac() {
        return muac;
    }

    public String getEdema() {
        return edema;
    }

    public String getVaccName() {
        return vaccName;
    }

    public String getVacDate() {
        return vacDate;
    }
}
