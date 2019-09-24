package org.smartregister.cbhc.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.smartregister.cbhc.application.AncApplication;

/**
 * Created by ndegwamartin on 15/03/2018.
 */

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getName();

    public static boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) AncApplication
                    .getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        } catch (Exception e) {
            Utils.appendLog(NetworkUtils.class.getName(), e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return false;
    }
}
