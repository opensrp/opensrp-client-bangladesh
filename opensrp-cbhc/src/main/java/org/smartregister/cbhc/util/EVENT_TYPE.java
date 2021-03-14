package org.smartregister.cbhc.util;

public enum EVENT_TYPE {
    Household_Registration("Household Registration","household_registration"),
    Update_Household_Registration("Update Household Registration","household_registration"),
    Woman_Member_Registration("Woman Member Registration","member_registration"),
    Member_Registration("Member Registration","member_registration"),
    Child_Registration("Child Registration","member_registration"),

    Followup_ANC("Followup ANC","followup/mhv/Followup_Form_MHV_ANC"),
    Followup_Death_Status("Followup Death Status","followup/mhv/Followup_Form_MHV_Death"),
    Followup_Delivery("Followup Delivery","followup/mhv/Followup_Form_MHV_Delivery"),
    Followup_Family_Planning("Followup Family Planning","followup/mhv/Followup_Form_MHV_FP"),
    Followup_HH_Transfer("Followup HH Transfer","followup/mhv/Followup_Form_MHV_HH_Transfer"),
    Followup_Marital_Status_Female("Followup Marital Status Female","followup/mhv/Followup_Form_MHV_Marital_F"),
    Followup_Marital_Status_Male("Followup Marital Status Male","followup/mhv/Followup_Form_MHV_Marital_M"),
    Followup_Member_Transfer("Followup Member Transfer","followup/mhv/Followup_Form_MHV_Member_Transfer"),
    Followup_Mobile_Number("Followup Mobile Number","followup/mhv/Followup_Form_MHV_Mobile_no"),
    Followup_PNC("Followup PNC","followup/mhv/Followup_Form_MHV_PNC"),
    Followup_Pregnant_Status("Followup Pregnant Status","followup/mhv/Followup_Form_MHV_Pregnant"),
    Followup_Risky_Habit("Followup Risky Habit","followup/mhv/Followup_Form_MHV_Risky_Habit"),

    Followup_Disease_Female("Followup Disease Female","followup/mhv/DS/Followup_Form_MHV_DS_Female"),
    Followup_Disease_Male("Followup Disease Male","followup/mhv/DS/Followup_Form_MHV_DS_Male"),
    Followup_Disease_Child("Followup Disease Child","followup/mhv/DS/Followup_Form_MHV_DS_NewBorn"),
    Followup_Disease_Toddler("Followup Disease Toddler","followup/mhv/DS/Followup_Form_MHV_DS_Toddler");

    public String form_name;
    public String encounter_type;
    EVENT_TYPE(String encounter_type,String form_name){
        this.form_name = form_name;
        this.encounter_type = encounter_type;
    }

}
