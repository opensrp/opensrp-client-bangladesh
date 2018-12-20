package org.smartregister.cbhc.util;

import org.smartregister.cbhc.domain.EntityLookUp;
import org.smartregister.commonregistry.CommonPersonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Jilla {

    static HashMap<CommonPersonObject, List<CommonPersonObject>> results = new HashMap<>();
    static CommonPersonObject po = new CommonPersonObject("","",new HashMap<String, String>(),"");
    public static HashMap<CommonPersonObject, List<CommonPersonObject>> getResults(String lookup_str){

        results.clear();
        HashMap<String,String>details = new HashMap<String, String>();
        details.put("first_name","birth_place");
        details.put("last_name","NA");
        po = new CommonPersonObject("","",details,"");
        po.setColumnmaps(details);

        for(int i=0;i<jilla_list.length;i++){
            if(lookup_str!=null&&jilla_list[i].toLowerCase().contains(lookup_str.toLowerCase())){
                HashMap<String,String>d = new HashMap<String, String>();
                d.put("first_name",jilla_list[i]);
                d.put("last_name","");
                d.put("dtype","loc");
                CommonPersonObject newc = new CommonPersonObject("","",d,"");
                newc.setColumnmaps(d);
                results.put(newc,new ArrayList<CommonPersonObject>());
            }

        //    results.get(po).add(newc);
        }


        return results;
    }
    private static final String [] jilla_list = {
            "DHAKA",
            "FARIDPUR",
            "GAZIPUR",
            "GOPALGANJ",
            "JAMALPUR",
            "KISHOREGONJ",
            "MADARIPUR",
            "MANIKGANJ",
            "MUNSHIGANJ",
            "MYMENSINGH",
            "NARAYANGANJ",
            "NARSINGDI",
            "NETRAKONA",
            "RAJBARI",
            "SHARIATPUR",
            "SHERPUR",
            "TANGAIL",
            "BOGRA",
            "JOYPURHAT",
            "NAOGAON",
            "NATORE",
            "CHAPAI NABABGANJ",
            "PABNA",
            "RAJSHAHI",
            "SIRAJGANJ",
            "DINAJPUR",
            "GAIBANDHA",
            "KURIGRAM",
            "LALMONIRHAT",
            "NILPHAMARI ZILA",
            "PANCHAGARH",
            "RANGPUR",
            "THAKURGAON",
            "HABIGANJ",
            "MAULVIBAZAR",
            "SUNAMGANJ",
            "SYLHET",
            "BAGERHAT",
            "CHUADANGA",
            "JESSORE",
            "JHENAIDAH",
            "KHULNA",
            "KUSHTIA",
            "MAGURA",
            "MEHERPUR",
            "NARAIL",
            "SATKHIRA",
            "BARGUNA",
            "BARISAL",
            "BHOLA",
            "JHALOKATI",
            "PATUAKHALI",
            "PIROJPUR",
            "BANDARBAN",
            "BRAHMANBARIA",
            "CHANDPUR",
            "CHITTAGONG",
            "COMILLA",
            "COX'S BAZAR",
            "FENI",
            "KHAGRACHHARI",
            "LAKSHMIPUR",
            "NOAKHALI",
            "RANGAMATI"

};
}
