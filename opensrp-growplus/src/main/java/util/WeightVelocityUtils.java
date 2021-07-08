package util;

import android.database.Cursor;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.growplus.application.VaccinatorApplication;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.repository.DetailsRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.vijay.jsonwizard.utils.FormUtils.DATE_FORMAT;
import static org.smartregister.growplus.activity.LoginActivity.getOpenSRPContext;
import static org.smartregister.growplus.fragment.GrowthFalteringTrendReportFragment.readAllWeights;
import static org.smartregister.util.Utils.getValue;

public class WeightVelocityUtils {
    static Map<String,Integer> weightMapBoy = new HashMap<String, Integer>()
    {

        {
            //key="interval_age"
            put("1_1", 805);put("1_2", 992);put("2_2", 1890);put("1_3", 658);put("2_3", 1701);
            put("3_3", 2608);put("1_4", 476);put("2_4", 1202);put("3_4", 2214);put("4_4", 3204);
            put("1_5", 383);put("2_5", 930);put("3_5", 1702);put("4_5", 2706);put("1_6", 287);
            put("2_6", 738);put("3_6", 1307);put("4_6", 2038);put("6_6", 4072);put("1_7", 223);
            put("2_7", 585);put("3_7", 1038);put("4_7", 1602);put("6_7", 3406);put("1_8", 181);
            put("2_8", 486);put("3_8", 859);put("4_8", 1312);put("6_8", 2662);put("1_9", 148);
            put("2_9", 417);put("3_9", 733);put("4_9", 1097);put("6_9", 2141);put("1_10", 120);
            put("2_10", 360);put("3_10", 639);put("4_10", 945);put("6_10", 1789);put("1_11", 100);
            put("2_11", 315);put("3_11", 567);put("4_11", 832);put("6_11", 1524);put("1_12", 91);
            put("2_12", 286);put("3_12", 509);put("4_12", 757);put("6_12", 1346);put("2_13", 260);
            put("3_13", 465);put("4_13", 700);put("6_13", 1212);put("2_14", 236);put("3_14", 430);
            put("4_14", 648);put("6_14", 1106);put("2_15", 212);put("3_15", 404);put("4_15", 598);
            put("6_15", 1024);put("2_16", 197);put("3_16", 385);put("4_16", 564);put("6_16", 970);
            put("2_17", 193);put("3_17", 372);put("4_17", 550);put("6_17", 937);put("2_18", 192);
            put("3_18", 362);put("4_18", 543);put("6_18", 914);put("2_19", 188);put("3_19", 355);
            put("4_19", 537);put("6_19", 898);put("2_20", 182);put("3_20", 351);put("4_20", 529);
            put("6_20", 887);put("2_21", 176);put("3_21", 347);put("4_21", 519);put("6_21", 880);
            put("2_22", 171);put("3_22", 342);put("4_22", 508);put("6_22", 872);put("2_23", 167);
            put("3_23", 334);put("4_23", 501);put("6_23", 863);put("2_24", 164);put("3_24", 322);
            put("4_24", 497);put("6_24", 855);

        }
    };
    static Map<String,Integer> weightMapGirl = new HashMap<String, Integer>()
    {
        {
            //key="interval_age"
            put("1_1", 697);put("1_2", 829);put("2_2", 1604);put("1_3", 571);put("2_3", 1450);
            put("3_3", 2247);put("1_4", 448);put("2_4", 1088);put("3_4", 1941);put("4_4", 2806);
            put("1_5", 355);put("2_5", 874);put("3_5", 1545);put("4_5", 2379);put("1_6", 271);
            put("2_6", 695);put("3_6", 1229);put("4_6", 1883);put("6_6", 3620);put("1_7", 214);
            put("2_7", 560);put("3_7", 995);put("4_7", 1522);put("6_7", 3033);put("1_8", 178);
            put("2_8", 469);put("3_8", 825);put("4_8", 1248);put("6_8", 2480);put("1_9", 139);
            put("2_9", 399);put("3_9", 697);put("4_9", 1042);put("6_9", 2030);put("1_10", 110);
            put("2_10", 336);put("3_10", 598);put("4_10", 891);put("6_10", 1692);put("1_11", 95);
            put("2_11", 297);put("3_11", 528);put("4_11", 785);put("6_11", 1446);put("1_12", 88);
            put("2_12", 274);put("3_12", 481);put("4_12", 712);put("6_12", 1271);put("2_13", 254);
            put("3_13", 451);put("4_13", 659);put("6_13", 1147);put("2_14", 238);put("3_14", 429);
            put("4_14", 624);put("6_14", 1063);put("2_15", 227);put("3_15", 413);put("4_15", 601);
            put("6_15", 1009);put("2_16", 220);put("3_16", 403);put("4_16", 586);put("6_16", 975);
            put("2_17", 216);put("3_17", 397);put("4_17", 578);put("6_17", 956);put("2_18", 212);
            put("3_18", 392);put("4_18", 572);put("6_18", 942);put("2_19", 205);put("3_19", 385);
            put("4_19", 565);put("6_19", 931);put("2_20", 196);put("3_20", 376);put("4_20", 556);
            put("6_20", 920);put("2_21", 188);put("3_21", 366);put("4_21", 546);put("6_21", 908);
            put("2_22", 178);put("3_22", 354);put("4_22", 532);put("6_22", 894);put("2_23", 164);
            put("3_23", 340);put("4_23", 517);put("6_23", 876);put("2_24", 150);put("3_24", 326);
            put("4_24", 503);put("6_24", 857);

        }
    };
//    public static Boolean checkForWeightGainCalc(Date dob, Weight weight, Weight previousWeight, CommonPersonObject childDetails, DetailsRepository detailsRepository) {
//        String dobString = "";
//        String formattedAge = "";
//        String formattedDob = "";
//        int monthLastWeightTaken = 0;
//        Map<String, String> detailsMap = detailsRepository.getAllDetailsForClient(childDetails.getCaseId());
//        childDetails.getColumnmaps().putAll(detailsMap);
//        Gender gender =   Gender.valueOf(getValue(childDetails.getColumnmaps(), PathConstants.KEY.GENDER, true).toUpperCase());
//
//
//        formattedDob = DATE_FORMAT.format(dob);
//        long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();
//
//        int age_in_months = (int) Math.floor((float) timeDiff /
//                TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
//        DateTime tempweighttime = null;
//        if(previousWeight == null) {
//            Float birthweight = new Float(getValue(detailsMap, "Birth_Weight", true));
//            previousWeight = new Weight();
//            previousWeight.setKg(birthweight);
//            previousWeight.setDate(dob);
//            monthLastWeightTaken = 0;
//        }else{
//            long timeDiffwhenLastWeightwastaken =  previousWeight.getDate().getTime() - dob.getTime();
//            monthLastWeightTaken = (int) Math.round((float) timeDiffwhenLastWeightwastaken /
//                    TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
//        }
//
//        long timeDiffwhenWeightwastaken =  weight.getDate().getTime() - dob.getTime();
//
//        int age_when_weight_taken = (int) Math.round((float) timeDiffwhenWeightwastaken /
//                TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
//        String name=getValue(detailsMap,"first_name",true);
//
//        Boolean check = WeightVelocityUtils.isWeightIncrement(name,weight.getKg(),previousWeight.getKg(),age_when_weight_taken,(age_when_weight_taken-monthLastWeightTaken),gender);
//        return check;
////        net.sqlcipher.database.SQLiteDatabase db = wp.getPathRepository().getReadableDatabase();
//
//
//    }
public static Boolean processWeightForGrowthChart(Weight weight,CommonPersonObject child) {

    DateTime birthDateTime = null;
    if(child!=null) {
        String dobString = getValue(child.getColumnmaps(), PathConstants.KEY.DOB, false);
        if (StringUtils.isNotBlank(dobString)) {
            try {
                birthDateTime = new DateTime(dobString);

            } catch (Exception e) {
            }
        }
        String gender = getValue(child.getColumnmaps(), PathConstants.KEY.GENDER, true);
        Boolean adequate = WeightVelocityUtils.checkForWeightGainCalc(birthDateTime.toDate(), Gender.valueOf(gender.toUpperCase()), weight, child.getCaseId(), getOpenSRPContext().detailsRepository());

        return adequate;
    }else{
        return false;
    }
}
    public static Boolean checkForWeightGainCalc(Date dob, Gender gender, Weight weight, String entityId, DetailsRepository detailsRepository) {

        Map<String, String> detailsMap = detailsRepository.getAllDetailsForClient(entityId);
        Float birthweight = new Float(getValue(detailsMap, "Birth_Weight", true));
        Weight previouseWeight = new Weight();
        previouseWeight.setKg(birthweight);
        previouseWeight.setDate(dob);
        int monthLastWeightTaken = 0;


        CommonRepository commonRepository = VaccinatorApplication.getInstance().context().commonrepository("ec_child");
        Cursor cursor =  commonRepository.rawCustomQueryForAdapter("SELECT * FROM weights where date( date / 1000, 'unixepoch') < date( "+weight.getDate().getTime()+"/1000,'unixepoch') and base_entity_id = '"+weight.getBaseEntityId()+"' group by base_entity_id having date = max(date)");
        List <Weight> previousweightlist = readAllWeights(cursor);

        if(previousweightlist.size()>0){
            previouseWeight = previousweightlist.get(0);
            long timeDiffwhenLastWeightwastaken =  previouseWeight.getDate().getTime() - dob.getTime();
            monthLastWeightTaken = (int) Math.round((float) timeDiffwhenLastWeightwastaken /
                    TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));

        }
        long timeDiffwhenWeightwastaken =  weight.getDate().getTime() - dob.getTime();

        /////////////////////////month last weight was taken calculation needs fixing /////////////////

        int age_when_weight_taken = (int) Math.round((float) timeDiffwhenWeightwastaken /
                TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
        String name=getValue(detailsMap,"first_name",true);
        Boolean check = WeightVelocityUtils.isWeightIncrement(name,weight.getKg(),previouseWeight.getKg(),age_when_weight_taken,(age_when_weight_taken-monthLastWeightTaken),gender);
        return check;


    }
    public static Boolean isWeightIncrement(String name,float presentWeight,float previousWeight,int monthWeightTaken,int interval,Gender gender){

        float weightDiffGrams=(presentWeight -previousWeight) * 1000;
        int getValueFromMap=0;

        switch (gender){
            case MALE:
                try{
                    getValueFromMap=weightMapBoy.get(interval+"_"+monthWeightTaken);
                }catch (Exception e){
                    //if value not exist;
                    Log.v("WEIGHT_CHECK","isCheck MALE:name:"+name+">>>"+interval+","+monthWeightTaken+" value not found return null");
                    return null;
                }
                if(weightDiffGrams<getValueFromMap){
                    Log.v("WEIGHT_CHECK","isCheck MALE:name:"+name+">>>"+interval+","+monthWeightTaken+","+weightDiffGrams+">>"+getValueFromMap+":false");

                    return false;
                }else{
                    Log.v("WEIGHT_CHECK","isCheck MALE:name:"+name+">>>"+interval+","+monthWeightTaken+","+weightDiffGrams+">>"+getValueFromMap+":true");

                    return true;
                }
            case FEMALE:
                try{
                    getValueFromMap=weightMapGirl.get(interval+"_"+monthWeightTaken);
                }catch (Exception e){
                    //if value not exist;
                    Log.v("WEIGHT_CHECK","isCheck FEMALE:name:"+name+">>>"+interval+","+monthWeightTaken+" value not found");

                    return null;
                }
                if(weightDiffGrams<getValueFromMap){
                    Log.v("WEIGHT_CHECK","isCheck FEMALE:name:"+name+">>>"+interval+","+monthWeightTaken+","+weightDiffGrams+">>"+getValueFromMap+":false");

                    return false;
                }else{
                    Log.v("WEIGHT_CHECK","isCheck FEMALE:name:"+name+">>>"+interval+","+monthWeightTaken+","+weightDiffGrams+">>"+getValueFromMap+":true");

                    return true;
                }
                default:
                    Log.v("WEIGHT_CHECK","isCheck name:"+name+" gender not found");
                    return null;
        }
    }
}
