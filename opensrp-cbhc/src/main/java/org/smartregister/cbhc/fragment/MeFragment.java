package org.smartregister.cbhc.fragment;

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

import org.smartregister.cbhc.R;
import org.smartregister.cbhc.application.AncApplication;
import org.smartregister.cbhc.contract.MeContract;
import org.smartregister.repository.AllSharedPreferences;

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
		TextView username = (TextView)view.findViewById(R.id.username);
		Button Logout = (Button)view.findViewById(R.id.logout);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String anm_name = allSharedPreferences.fetchRegisteredANM();
        username.setText(anm_name);
		Logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AncApplication.getInstance().logoutCurrentUser();
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
