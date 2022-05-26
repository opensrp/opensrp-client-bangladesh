package org.smartregister.cbhc.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.cbhc.BuildConfig;
import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.MeContract;
import org.smartregister.cbhc.troublshoot.ForceSyncActivity;
import org.smartregister.repository.AllSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeFragment extends Fragment implements MeContract.View {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        TextView username = view.findViewById(R.id.username);
        Button Logout = view.findViewById(R.id.logout);
        Button Force_Logout = view.findViewById(R.id.force_logout);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String anm_name = allSharedPreferences.fetchRegisteredANM();
        String provider_name = allSharedPreferences.getPreference(anm_name);
        String version_name = BuildConfig.VERSION_NAME;
        String build_date = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(BuildConfig.BUILD_TIMESTAMP));
        String version_text = "Version " + version_name + ", Built on: " + build_date;
        String user_details = anm_name;
        if (!StringUtils.isEmpty(provider_name)) {
            user_details = provider_name + "\n" + anm_name+"\n"+version_text;
        }

        username.setText(user_details);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AncApplication.getInstance().logoutUser();
            }
        });
        Force_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AncApplication.getInstance().forcelogoutCurrentUser();
            }
        });
        view.findViewById(R.id.troubleshoot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(getActivity(), ForceSyncActivity.class));
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
