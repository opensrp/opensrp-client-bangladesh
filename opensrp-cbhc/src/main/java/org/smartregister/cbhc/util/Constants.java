package org.smartregister.cbhc.util;

import org.smartregister.cbhc.BuildConfig;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public abstract class Constants {
    public static String EC_CLIENT_FIELDS = "ec_client_fields.json";
    public static final String SQLITE_DATE_TIME_FORMAT = "yyyy-MM-dd";
    public static final int OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_SOURCE = BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;

    public static final String IS_REMOTE_LOGIN = "is_remote_login";
    public static final long MAX_SERVER_TIME_DIFFERENCE = BuildConfig.MAX_SERVER_TIME_DIFFERENCE;
    public static final String VIEW_CONFIGURATION_PREFIX = "ViewConfiguration_";

    public static final boolean TIME_CHECK = BuildConfig.TIME_CHECK;
    public static final String CURRENT_LOCATION_ID = "CURRENT_LOCATION_ID";

    public static final String LAST_SYNC_TIMESTAMP = "LAST_SYNC_TIMESTAMP";
    public static final String LAST_CHECK_TIMESTAMP = "LAST_SYNC_CHECK_TIMESTAMP";

    public static final String GLOBAL_IDENTIFIER = "identifier";
    public static final String ANC_ID = "ANC_ID";
    public static String DEFALT_DATE_REMOVED_DATE = "01-01-1001";
    public static class CONFIGURATION {
        public static final String LOGIN = "login";
        public static final String HOME_REGISTER = "home_register";
    }
    public static final class CMED_KEY{
        public static final String USER_NAME = "user_name";
        public static final String USER_PASSWORD = "password_string";
        public static final String CMED_ACTION = "android.intent.action.CMED";
        public static boolean IS_FROM_CMED = false;
    }
    public static final class EventType {
        public static final String HouseholdREGISTRATION = "Household Registration";
        public static final String UPDATE_Household_REGISTRATION = "Update Household Registration";
        public static final String MemberREGISTRATION = "Member Registration";
        public static final String WomanMemberREGISTRATION = "Woman Member Registration";
        public static final String Child_REGISTRATION = "Child Registration";
        public static final String REGISTRATION = "ANC Registration";
        public static final String UPDATE_REGISTRATION = "Update ANC Registration";
        public static final String QUICK_CHECK = "Quick Check";
        public static final String CLOSE = "ANC Close";

        public static final String PREGNANT_STATUS = "PregnancyStatus";
        public static final String MARITAL_STATUS = "Marital Status";
    }

    public static class JSON_FORM {
        public static final String Household_REGISTER = "household_registration";
        public static final String MEMBER_REGISTER = "member_registration";

        public static final String ANC_REGISTER = "anc_register";
        public static final String ANC_CLOSE = "anc_close";
        public static final String FOLLOW_UP = "member_followup";

    }

    public static class FOLLOWUP_FORM {

        //DS Forms
        public static final String Followup_Form_MHV_DS_Female = "followup/mhv/DS/Followup_Form_MHV_DS_Female";
        public static final String Followup_Form_MHV_DS_Male = "followup/mhv/DS/Followup_Form_MHV_DS_Male";
        public static final String Followup_Form_MHV_DS_NewBorn = "followup/mhv/DS/Followup_Form_MHV_DS_NewBorn";
        public static final String Followup_Form_MHV_DS_Toddler = "followup/mhv/DS/Followup_Form_MHV_DS_Toddler";

        //Others
        public static final String Followup_Form_MHV_ANC = "followup/mhv/Followup_Form_MHV_ANC";
        public static final String Followup_Form_MHV_Death = "followup/mhv/Followup_Form_MHV_Death";

        public static final String Followup_Form_MHV_Transfer = "followup/mhv/Followup_Form_MHV_HH_Transfer";
        public static final String Followup_Form_MHV_Delivery = "followup/mhv/Followup_Form_MHV_Delivery";
        public static final String Followup_Form_MHV_DS = "followup/mhv/Followup_Form_MHV_DS";
        public static final String Followup_Form_MHV_FP = "followup/mhv/Followup_Form_MHV_FP";
        public static final String Followup_Form_MHV_Marital_M = "followup/mhv/Followup_Form_MHV_Marital_M";
        public static final String Followup_Form_MHV_Marital_F = "followup/mhv/Followup_Form_MHV_Marital_F";
        public static final String Followup_Form_MHV_Mobile_no = "followup/mhv/Followup_Form_MHV_Mobile_no";
        public static final String Followup_Form_MHV_Pregnant = "followup/mhv/Followup_Form_MHV_Pregnant";
        public static final String Followup_Form_MHV_PNC = "followup/mhv/Followup_Form_MHV_PNC";
        public static final String Followup_Form_MHV_Risky_Habit = "followup/mhv/Followup_Form_MHV_Risky_Habit";

        public static ArrayList<FOLLOWUPFORMS> followup_forms;
        public static ArrayList<FOLLOWUPFORMS> getFollowup_forms(){
            followup_forms = new ArrayList<>();
            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_DS_Female,"সাধারণ রোগ মহিলা "));
            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_DS_Male,"সাধারণ রোগ পুরুষ"));
            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_DS_NewBorn,"শিশু (০-২ মাস)"));
            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_DS_Toddler,"শিশু (২ মাস-৫ বছর)"));

            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_ANC,"প্রসব পূর্ব "));
            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_Death,"মৃত্যু "));

            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_Delivery,"প্রসব"));

            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_FP,"পরিবার পরিকল্পনা"));
//            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_Marital,"বৈবাহিক অবস্থা"));
            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_Mobile_no,"মোবাইল নম্বর"));

            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_PNC,"প্রসব পরবর্তী "));
            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_Risky_Habit,"ঝুকিপূর্ন অভ্যাস"));

            followup_forms.add(new FOLLOWUPFORMS(Followup_Form_MHV_Pregnant,"গর্ভাবস্থা"));

            return followup_forms;
        }
        public static class FOLLOWUPFORMS {
            String form_name;
            String display_name;
            public FOLLOWUPFORMS(String form_name,String display_name){
                this.form_name = form_name;
                this.display_name = display_name;
            }

            public String getForm_name(){
                return form_name;
            }
            public String getDisplay_name(){
                return display_name;
            }
        }



    }

    public static class JSON_FORM_KEY {
        public static final String ENTITY_ID = "entity_id";
        public static final String OPTIONS = "options";
        public static final String ENCOUNTER_LOCATION = "encounter_location";
        public static final String ATTRIBUTES = "attributes";
        public static final String DEATH_DATE = "deathdate";
        public static final String DEATH_DATE_APPROX = "deathdateApprox";
        public static final String ANC_CLOSE_REASON = "anc_close_reason";
    }

    public static final class ServiceType {
        public static final int AUTO_SYNC = 1;
        public static final int PULL_UNIQUE_IDS = 4;
        public static final int IMAGE_UPLOAD = 8;
        public static final int PULL_VIEW_CONFIGURATIONS = 9;

    }

    public static final class KEY {
        public static final String SEX = "Sex";
        public static final String KEY = "key";
        public static final String VALUE = "value";
        public static final String TREE = "tree";
        public static final String DEFAULT = "default";
        public static final String PHOTO = "photo";

    }

    public static final class INTENT_KEY {
        public static final String BASE_ENTITY_ID = "base_entity_id";
        public static final String JSON = "json";
        public static final String WHO_ANC_ID = "who_anc_id";
        public static final String TO_RESCHEDULE = "to_reschedule";
    }

    public static class OPENMRS {
        public static final String ENTITY = "openmrs_entity";
        public static final String ENTITY_ID = "openmrs_entity_id";
    }

    public static class ENTITY {
        public static final String PERSON = "person";
    }

    public static class BOOLEAN_INT {
        public static final int TRUE = 1;
    }

    public static final class SyncFilters {

        public static final String FILTER_TEAM_ID = "teamId";
        public static final String PROVIDER_ID = "providerId";
        public static final String LOCATION_ID = "locationId";
    }
}