package org.smartregister.cbhc.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.event.BaseEvent;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.DateUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static org.smartregister.util.Log.logError;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Utils {

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

     /*   StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        String text = TAG + " >>> "+ exceptionAsString;

        File f = new File(Environment.getExternalStorageDirectory() + "/cbhc_log/error/");
        if (!f.exists()) {
            f.mkdirs();
        }
        File logFile = new File(Environment.getExternalStorageDirectory() + "/cbhc_log/error/"+"log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException ee) {
                Log.e(TAG, ee.getMessage());
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException ee) {
            Log.e(TAG, e.getMessage(), e);
        }*/
    }

}
