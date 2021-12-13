package org.smartregister.cbhc.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.domain.FormLocation;
import org.smartregister.cbhc.event.BaseEvent;
import org.smartregister.cbhc.helper.LocationHelper;
import org.smartregister.cbhc.model.UnsendData;
import org.smartregister.cbhc.repository.AncRepository;
import org.smartregister.cbhc.repository.UnSendDataRepository;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.DateUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static org.smartregister.util.Log.logError;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Utils {

    public static void showDialog(Context context,String title, String text){
        final Dialog dialog = new Dialog(context, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.new_notification_view);
        TextView textViewTitle = dialog.findViewById(R.id.stock_new_text);
        TextView titleTxt = dialog.findViewById(R.id.textview_detail_two);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static ArrayList<UnsendData> getCmedDataFromRepo(AncRepository repo){
        UnSendDataRepository followupFormRepository = new UnSendDataRepository(repo);
        return followupFormRepository.getAllUnsendData();

//        ArrayList<UnsendData> unsendDataArrayList = new ArrayList<>();
//        unsendDataArrayList.add(new UnsendData("4c7faea1-d1b0-451c-bb5d-abdafaf58de7","HH"));
//        unsendDataArrayList.add(new UnsendData("f1e2a271-7f17-4382-a643-0401431838cb","HH"));
//        unsendDataArrayList.add(new UnsendData("efc8bd85-6f3c-4515-bfff-2e9d428c6cea","MM"));
//        unsendDataArrayList.add(new UnsendData("7ebacc2f-b8af-40e0-afc8-f7e9840a60ae","MM"));
//        return unsendDataArrayList;
    }
    public static Intent passToMHVAPP(ArrayList<String> hhList, ArrayList<String> mmList, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String userName = allSharedPreferences.getPreference(Constants.CMED_KEY.USER_NAME);
        String passwordText = allSharedPreferences.getPreference(Constants.CMED_KEY.USER_PASSWORD);
        Intent intent = new Intent();
        intent.setAction(Constants.CMED_KEY.MPOWER_ACTION);
        //intent.setClassName("com.example.myapplication", "com.example.myapplication.MainActivity");
        intent.setComponent(new ComponentName("com.cmed.mhv", "com.cmed.mhv.home.view.MainActivity_"));

        intent.putExtra(Constants.CMED_KEY.HH_LIST, hhList);
        intent.putExtra(Constants.CMED_KEY.MM_LIST,  mmList);
        intent.putExtra(Constants.CMED_KEY.USER_NAME,userName);
        intent.putExtra(Constants.CMED_KEY.USER_PASSWORD,passwordText);
        return intent;
    }
    public static Intent passMemberFromReferlToMHVAPp(JSONObject jsonObject,ArrayList<String> hhList, ArrayList<String> mmList, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String userName = allSharedPreferences.getPreference(Constants.CMED_KEY.USER_NAME);
        String passwordText = allSharedPreferences.getPreference(Constants.CMED_KEY.USER_PASSWORD);
        Intent intent = new Intent();
        intent.setAction(Constants.CMED_KEY.MPOWER_ACTION);
//        intent.setClassName("com.example.testapplication", "com.example.myapplication.MainActivity");
        intent.setComponent(new ComponentName("com.cmed.mhv", "com.cmed.mhv.home.view.MainActivity_"));
        intent.putExtra(Constants.CMED_KEY.MEMBER_REFERL, jsonObject.toString());
        intent.putExtra(Constants.CMED_KEY.HH_LIST, hhList);
        intent.putExtra(Constants.CMED_KEY.MM_LIST,  mmList);
        intent.putExtra(Constants.CMED_KEY.USER_NAME,userName);
        intent.putExtra(Constants.CMED_KEY.USER_PASSWORD,passwordText);
        return intent;
    }
    public static Intent callingPrimaAppLoginActivity(String userName, String passwordText){
        Intent intent = new Intent();
        intent.setAction(Constants.CMED_KEY.CMED_ACTION);
        intent.setClassName("org.smartregister.cbhc", "org.smartregister.cbhc.activity.LoginActivity");
        intent.putExtra(Constants.CMED_KEY.USER_NAME,userName);
        intent.putExtra(Constants.CMED_KEY.USER_PASSWORD,passwordText);
        return intent;
    }

    public static final int NOFILTER = 8888;
    public static final String DEFAULT_IDENTIFIER = "88888888";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
    private static final String TAG = Utils.class.getCanonicalName();

    private static final SimpleDateFormat DB_DF = new SimpleDateFormat(Constants.SQLITE_DATE_TIME_FORMAT);
    private static final DateTimeFormatter SQLITE_DATE_DF = DateTimeFormat.forPattern(Constants.SQLITE_DATE_TIME_FORMAT);
    public static boolean VIEWREFRESH = false;

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    public static void showShortToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void saveLanguage(String language) {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(AncApplication.getInstance().getApplicationContext()));
        allSharedPreferences.saveLanguagePreference(language);
        setLocale(new Locale(language));
    }

    public static String getLanguage() {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(AncApplication.getInstance().getApplicationContext()));
        return allSharedPreferences.fetchLanguagePreference();
    }

    public static void setLocale(Locale locale) {
        Resources resources = AncApplication.getInstance().getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            AncApplication.getInstance().getApplicationContext().createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, displayMetrics);
        }
    }

    public static void postEvent(BaseEvent event) {
        EventBus.getDefault().post(event);
    }

    public static void postStickyEvent(BaseEvent event) {//Each Sticky event must be manually cleaned by calling Utils.removeStickyEvent after handling
        EventBus.getDefault().postSticky(event);
    }

    public static void removeStickyEvent(BaseEvent event) {
        EventBus.getDefault().removeStickyEvent(event);

    }
    public static String getLocationTree(){
        ArrayList<String> healthFacilities = new ArrayList<>();
        healthFacilities.add("Country");
        healthFacilities.add("Division");
        healthFacilities.add("District");
        healthFacilities.add("Upazilla");
        healthFacilities.add("Union");
        healthFacilities.add("Ward");
        healthFacilities.add("Block");
        healthFacilities.add("Subunit");
        healthFacilities.add("EPI center");
        List<FormLocation> upToFacilities = LocationHelper.getInstance().generateLocationHierarchyTree(false, healthFacilities);

        String location = "";
        ArrayList<String>locations = new ArrayList<>();

            while(getNodeName(upToFacilities)!=null){
                FormLocation loc = upToFacilities.get(0);
                locations.add(loc.name);
                upToFacilities = loc.nodes;

            }
            for(int i = locations.size()-2;i>=0;i--){
                location += locations.get(i) + " ";
            }



        return location.trim();
    }
    public static String getNodeName(List<FormLocation> loc){
        if(loc==null)return null;
        return loc.get(0).name;
    }
    public static int getOccupationIndex(String profession){
        String openmrs_choice_ids[]= {
                    "ইঞ্জিনিয়ার : Engineer",
                    "বিজ্ঞানী : Physical scientist and relevant technicians",
                    "অর্থনীতিবিদ : Economist",
                    "পরিসংখ্যানবিদ, গণিতবিদ, সিস্টেম এনালিস্ট এবং এতদসম্পকিত কর্মী : Statistician, Mathematician, System Analyst and Relevant",
                    "লেখক, সাংবাদিক এবং এতদসম্পর্কিত কর্মী : Writer, Journalist and Relevant",
                    "বিমান এবং জাহাজের কর্মকর্তা : Officer of Airplane and Ship",
                    "চিকিৎসক, দন্ত চিকিৎসক ও প্রাণী চিকিৎসক : Medical doctor, Dentist & Veterinary",
                    "নার্স এবং চিকিৎসা-সংক্রান্ত অন্যান্য কর্মী :  Nurse and treatment related other stuffs",
                    "অভিনয়, কণ্ঠশিল্পী এবং নৃত্য শিল্পী : Acting, Singing and Dancing",
                    "খেলোয়াড় এবং এতদসম্পর্কিত কর্মী : Player and Relevant",
                    "হিসাব রক্ষক : Accountant",
                    "বিচারক : Judge",
                    "শিক্ষক : Teacher",
                    "আইনজীবী : Lawyer",
                    "ম্যানেজার : Manager",
                    "সরকারী কর্মকর্তা : Government Executive Officer",
                    "কৃষি খামার ব্যবস্থাপক ও তত্বাবধায়ক : Agricultural Farm Manager and Supervisor",
                    "করণীক (কেরাণী) : Clerk",
                    "টাইপিস্ট/স্টেনোগ্রাফার/কম্পিউটার অপারেটর : Typist, Stenographer & Computer Operator",
                    "রেকর্ড কিপার,ক্যাশিয়ার এবং এতদসম্পর্কিত কর্মী : Record Keeper, Cashier & Relevant",
                    "কম্পিউটার সম্পর্কিত কর্মী : Computer Relevant Worker",
                    "পেশাগত, কারিগরি এবং অন্যান্য অশ্রেণীভুক্ত এতদসম্পর্কিত কর্মী : Occupational, Technical and other non categorized worker",
                    "চিঠিপত্র বিলিকারী : Postman",
                    "টেলিফোন ও টেলিগ্রাফ অপারেটর : Telephone and Telegraph Operator",
                    "অশ্রেণীভুক্ত দাপ্তরিক কাজ : Non-categorized Clerical Worker",
                    "ভ্রমণ সংক্রান্ত কাজে নিয়োজিত কর্মী : Travelling Related Worker",
                    "উৎপাদন তত্ত্বাবধায়ক এবং ফোরম্যান : Production Supervisor & Foreman",
                    "ধর্মীয় কর্মী : Religious Worker",
                    "অশ্রেণীভুক্ত সেবা কর্মী : Non-categorized Service Provider",
                    "বনকর্মী : Forester",
                    "বৈদ্যুতিক কর্মী : Electrician",
                    "বৈদ্যুতিক ব্যতীত অন্যান্য মেশিন কর্মী : Machine Worker Other Then Electric",
                    "স্বর্ণকার : Goldsmith",
                    "গাড়ির চালক : Driver",
                    "বাবুর্চি, হোটেল বয় এবং এতদসম্পর্কিত সম্পর্কিত কর্মী : Cook, Waiter & Relevant Worker",
                    "অশ্রেণীভুক্ত বিক্রয় কর্মী : Non-categorized Sales Worker",
                    "যানবাহন ও যোগাযোগ তত্ত্বাবধায়ক : Vehicle & Communication Supervisor",
                    "গাড়ির কন্ডাক্টর : Car conductor",
                    "শব্দ প্রচারকর্মী ও চলচ্চিত্র প্রদর্শনকারী : Announcer & Cinema Exhibitor",
                    "নিরাপত্তা কর্মী : Guard",
                    "দোকানদার : Shopkeeper",
                    "ধোপার কাজ : Fuller",
                    "খনন কর্মী ও খননকারী : Excavation and Digging",
                    "ধাতু প্রক্রিয়াকারী : Metal Processing",
                    "রাসায়নিক দ্রব্য প্রক্রিয়াকারী : Chemical Material Processing",
                    "খাদ্য ও পানীয় প্রক্রিয়া কারী : Food & Drink Processing",
                    "তামাক প্রক্রিয়াকারী : Tobacco Processing",
                    "চামড়া প্রক্রিয়াকরণ :  Leather Processing",
                    "জুতা ও চামড়াজাত দ্রব্য প্রস্তুতকারী : Shoe & Lather Product Producer",
                    "রাবার ও প্লাস্টিক দ্রব্য প্রস্তুতকারী : Rubber & Plastic Material Producer",
                    "কাগজ ও কাগজের বোর্ড প্রস্তুতকারী : Paper & Paper Board Producer",
                    "মুদ্রণ কাজ : Printing",
                    "কর্মকার, ঢালাই কর্মী ও যন্ত্রাংশ প্রস্তুতকারী : Smith, Welding Worker & Machinery Parts Producer",
                    "পাথর কাটা ও প্রক্রিয়কারী : Mason",
                    "পানি ও পয়ঃনিষ্কাশন কাঠামো নির্মাণকারী : Plumber",
                    "বজ্য ব্যবস্থাপনা কর্মী : Corporate management staff",
                    "ধাতু ঝালাইকারী : Metal Welder",
                    "অকৃষি শ্রমিক : Non-agricultural Worker",
                    "কৃষি শ্রমিক : Agricultural Worker",
                    "কাঠ মিস্ত্রি : Carpenter",
                    "দর্জি ও অন্যান্য সেলাই কর্মী : Tailor & Relevant",
                    "গ্লাস ও মাটির জিনিস প্রস্তুতকারী : Glass Maker & Potter",
                    "তাঁতি কাপড় বোনা ও রং করা : Weaving & Painting",
                    "জেলে, শিকারী এবং এতদসম্পর্কিত কর্মী : Fisherman, Hunter and Relevant",
                    "ফেরিওয়ালা : Hawker",
                    "পাইকারী ও খুচরা ব্যবসা : Retail & Wholesale",
                    "বীমা, রিয়েল এস্টেট ব্যবসা এবং এতদসম্পর্কিত সেবা বিক্রেতা : Insurance, Real State and Relevant Service Seller",
                    "হোটেল মালিক : Hotel Owner",
                    "পোল্ট্রি মালিক : Poultry owner",
                    "অশ্রেণীভুক্ত গৃহপরিচারিকা : Non-categorized Housemaid",
                    "বাড়ির কেয়ারটেকার, ঝাড়ুদার এবং এতদসম্পর্কিত কর্মী : House Caretaker, Sweeper & Relevant Worker"
        };
        int index = -1;
        if(!StringUtils.isEmpty(profession)){
            for(int i=0;i<openmrs_choice_ids.length;i++){
                if(openmrs_choice_ids[i].contains(profession)){
                    index = i;
                    break;
                }
            }
        }

        return index;
    }
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void writePrefString(Context context, final String key, final String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDuration(String date) {
        DateTime duration;
        if (StringUtils.isNotBlank(date)) {
            try {
                duration = new DateTime(date);
                return DateUtil.getDuration(duration);
            } catch (Exception e) {
                Utils.appendLog(Utils.class.getName(),e);
                Log.e(TAG, e.toString(), e);
            }
        }
        return "";
    }

    public static String getDob(int age) {

        Calendar cal = Calendar.getInstance();
        if (age > 0)
            cal.add(Calendar.YEAR, -age);
//        cal.set(Calendar.DAY_OF_MONTH, 1);
//        cal.set(Calendar.MONTH, 0);

        return DatePickerFactory.DATE_FORMAT.format(cal.getTime());
    }

    public static int convertDpToPx(Context context, int dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return Math.round(px);
    }

    public static String convertDateFormat(Date date, SimpleDateFormat formatter) {

        return formatter.format(date);
    }

    public static boolean isEmptyMap(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmptyCollection(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static void hideKeyboard(Context context, View view) {
        try {

            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            Utils.appendLog(Utils.class.getName(),e);
            logError("Error encountered while hiding keyboard " + e);
        }
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            hideKeyboard(activity, view);
        }
    }

    public static Date dobStringToDate(String dobString) {
        DateTime dateTime = dobStringToDateTime(dobString);
        if (dateTime != null) {
            return dateTime.toDate();
        }
        return null;
    }

    public static DateTime dobStringToDateTime(String dobString) {
        try {
            if (StringUtils.isBlank(dobString)) {
                return null;
            }
            return new DateTime(dobString);

        } catch (Exception e) {
            Utils.appendLog(Utils.class.getName(),e);
            return null;
        }
    }

    public static int getAgeFromDate(String dateOfBirth) {
        try {
            DateTime date = DateTime.parse(dateOfBirth);

            Years age;
            if (date == null) {
                age = Years.yearsBetween(LocalDate.now(), LocalDate.now());
            } else {
                age = Years.yearsBetween(date.toLocalDate(), LocalDate.now());
            }

            return age.getYears();
        } catch (Exception e) {
            Utils.appendLog(Utils.class.getName(),e);

        }
        return 0;
    }

    public static String getTodaysDate() {
        return convertDateFormat(Calendar.getInstance().getTime(), DB_DF);
    }

    @Nullable
    public static int getAttributeDrawableResource(
            Context context,
            int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return typedValue.resourceId;
    }

    public static int getGestationAgeFromDate(String expectedDeliveryDate) {
        LocalDate date = SQLITE_DATE_DF.withOffsetParsed().parseLocalDate(expectedDeliveryDate);
        Weeks weeks = Weeks.weeksBetween(LocalDate.now(), date);
        return weeks.getWeeks();
    }

    public static CharSequence getDateByFormat(Date date) {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return DateFormat.format("dd-MM-yyyy", new java.util.Date());

    }

    public static boolean notFollowUp(String encounterType) {
        return !encounterType.startsWith("Followup");
    }

    public static void appendLog(String TAG, Exception e) {

//        StringWriter sw = new StringWriter();
//        e.printStackTrace(new PrintWriter(sw));
//        String exceptionAsString = sw.toString();
//
//        String text = TAG + " >>> "+ exceptionAsString;
//
//        File f = new File(Environment.getExternalStorageDirectory() + "/cbhc_log/error/");
//        if (!f.exists()) {
//            f.mkdirs();
//        }
//        File logFile = new File(Environment.getExternalStorageDirectory() + "/cbhc_log/error/"+"log.file");
//        if (!logFile.exists()) {
//            try {
//                logFile.createNewFile();
//            } catch (IOException ee) {
//                Log.e(TAG, ee.getMessage());
//            }
//        }
//        try {
//            //BufferedWriter for performance, true to set append to file flag
//            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
//            buf.append(text);
//            buf.newLine();
//            buf.close();
//        } catch (IOException ee) {
//            Log.e(TAG, e.getMessage(), e);
//        }
    }

}