package org.smartregister.cbhc.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.contract.LoginContract;
import org.smartregister.cbhc.domain.FormLocation;
import org.smartregister.cbhc.event.ViewConfigurationSyncCompleteEvent;
import org.smartregister.cbhc.helper.LocationHelper;
import org.smartregister.cbhc.presenter.LoginPresenter;
import org.smartregister.cbhc.task.SaveTeamLocationsTask;
import org.smartregister.cbhc.util.Constants;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.smartregister.AllConstants.DRISHTI_BASE_URL;
import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.AsyncTask;

/**
 * Created by ndegwamartin on 21/06/2018.
 */
public class LoginActivity extends AppCompatActivity implements LoginContract.View, TextView.OnEditorActionListener, View.OnClickListener {

    LoginActivity mActivity;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private CheckBox showPasswordCheckBox;
    private ProgressDialog progressDialog;
    private Button loginButton;
    private TextView buildDetailsView;
    private LoginContract.Presenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.customAppThemeBlue)));
        mActivity = this;
        mLoginPresenter = new LoginPresenter(this);
        mLoginPresenter.setLanguage();
        setupViews(mLoginPresenter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String baseurl = allSharedPreferences.fetchBaseURL("");

        if (baseurl.isEmpty()) {

            allSharedPreferences.updateUrl(getString(R.string.opensrp_url));
            preferences.edit().putString(DRISHTI_BASE_URL, getString(R.string.opensrp_url)).apply();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Settings");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase("Settings")) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mActivity = this;
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
        mActivity = this;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String anm_name = allSharedPreferences.fetchRegisteredANM();
        if(!StringUtils.isEmpty(anm_name)){
            userNameEditText.setText(anm_name);
        }
        app_version_status();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivity = null;
        mLoginPresenter.onDestroy(isChangingConfigurations());
    }

    private void setupViews(LoginContract.Presenter presenter) {
        presenter.positionViews();
        initializeLoginChildViews();
        initializeProgressDialog();
        initializeBuildDetails();
        setListenerOnShowPasswordCheckbox();

    }

    private void initializeLoginChildViews() {
        userNameEditText = findViewById(R.id.login_user_name_edit_text);
        passwordEditText = findViewById(R.id.login_password_edit_text);
        showPasswordCheckBox = findViewById(R.id.login_show_password_checkbox);
        passwordEditText.setOnEditorActionListener(this);
        buildDetailsView = findViewById(R.id.login_build_text_view);
        loginButton = findViewById(R.id.login_login_btn);
        loginButton.setOnClickListener(this);
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(org.smartregister.R.string.loggin_in_dialog_title));
        progressDialog.setMessage(getString(org.smartregister.R.string.loggin_in_dialog_message));
    }

    private void initializeBuildDetails() {
        try {
            buildDetailsView.setText("Version " + getVersion() + ", Built on: " + mLoginPresenter.getBuildDate());

        } catch (Exception e) {
            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
            logError("Error fetching build details: " + e);
        }
    }

    private void setListenerOnShowPasswordCheckbox() {
        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    @Override
    public void goToHome(boolean remote) {
//        org.smartregister.Context.getInstance().allSharedPreferences().saveForceRemoteLogin(false);
        if (remote) {
            Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }
        Intent intent = new Intent(this, HomeRegisterActivity.class);
        intent.putExtra(Constants.IS_REMOTE_LOGIN, remote);
        startActivity(intent);

        finish();
    }


    @Override
    public void showErrorDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(org.smartregister.R.string.login_failed_dialog_title))
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        alertDialog.show();

    }

    public void showProgress(final boolean show) {
        if (show) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    @Override
    public void hideKeyboard() {
        Log.i(getClass().getName(), "Hiding Keyboard " + DateTime.now().toString());
        org.smartregister.cbhc.util.Utils.hideKeyboard(this);
    }

    @Override
    public void enableLoginButton(boolean isClickable) {
        loginButton.setClickable(isClickable);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == R.integer.login || actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
            String username = userNameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            mLoginPresenter.attemptLogin(username, password);

            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.login_login_btn:
                String username = userNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
//                username = "haiphn";
//                password = "ha123";
//                username = "testmhv2";
//                username = "maxii";
//                username = "teliya1@cc.com";
//                username = "sabab@ahmed.com";
//                password = "Pain2set";

                //anc repository object for raw query
//                AncRepository repo = (AncRepository) AncApplication.getInstance().getRepository();
//                SQLiteDatabase db = repo.getReadableDatabase();
//                Cursor cursor = db.rawQuery("sql",new String[]{});
//                cursor.close();
//                username = "robinuthappa@gmail.com";
//                username = "nroy1978@gmail.com";
//                password = "N01915910723@";
//                deleteKey(username);
//                userNameEditText.setText(username);
//                passwordEditText.setText(password);
                //copy username password to clipboard
//                Object clipboardService = getSystemService(CLIPBOARD_SERVICE);
//                final ClipboardManager clipboardManager = (ClipboardManager)clipboardService;
//                ClipData clipData = ClipData.newPlainText("Source Text", username + ":"+password);
//                // Set it as primary clip data to copy text to system clipboard.
//                clipboardManager.setPrimaryClip(clipData);

                mLoginPresenter.attemptLogin(username, password);
                break;
            default:
                break;
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        mActivity = null;
    }

    @Override
    public void setUsernameError(int resourceId) {
        userNameEditText.setError(getString(resourceId));
        userNameEditText.requestFocus();
        showErrorDialog(getResources().getString(R.string.unauthorized));
    }

    @Override
    public void resetUsernameError() {
        userNameEditText.setError(null);
    }

    @Override
    public void setPasswordError(int resourceId) {
        passwordEditText.setError(getString(resourceId));
        passwordEditText.requestFocus();
        showErrorDialog(getResources().getString(R.string.unauthorized));
    }

    @Override
    public void resetPaswordError() {
        passwordEditText.setError(null);
    }

    private String getVersion() throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        return packageInfo.versionName;
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void refreshViews(ViewConfigurationSyncCompleteEvent syncCompleteEvent) {
        if (syncCompleteEvent != null) {
            logInfo("Refreshing Login View...");
            mLoginPresenter.processViewCustomizations();

        }
    }
    public void app_version_status() {
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {
            String version_code = "";
            String version = "";

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
                    String baseurl = allSharedPreferences.fetchBaseURL("");
                    // Create a URL for the desired page
                    String base_url = getString(R.string.opensrp_url).replace("opensrp/", "");
                    if (!StringUtils.isEmpty(baseurl) && baseurl.contains("opensrp")) {
                        base_url = baseurl.replace("opensrp/", "");
                    }
                    URL url = new URL(base_url + "opt/multimedia/app-version.txt");

                    // Read all the text returned by the server
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String str;
                    str = "";
                    while ((str = in.readLine()) != null) {
                        // str is one line of text; readLine() strips the newline character(s)
                        version_code += str;
                    }
                    in.close();
                } catch (MalformedURLException e) {
                    org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
                } catch (IOException e) {
                    org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                try {
                    PackageInfo pInfo = LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                    version = pInfo.versionName;
                    if (!version_code.trim().isEmpty() && !version.equalsIgnoreCase(version_code.trim())) {
                        alertDialog = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this).create();
                        alertDialog.setTitle("New version available");
                        alertDialog.setCancelable(false);
                        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "UPDATE",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }


                                        } catch (Exception e) {
                                            org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        if (mActivity != null && alertDialog != null)
                            alertDialog.show();
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    org.smartregister.cbhc.util.Utils.appendLog(getClass().getName(), e);
                    e.printStackTrace();
                }
            }
        }, null);
    }
    @Override
    public Activity getActivityContext() {
        return this;

    }
    android.support.v7.app.AlertDialog alertDialog;


}
