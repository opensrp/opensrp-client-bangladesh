package org.smartregister.cbhc.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.cbhc.util.GrowthUtil;
import org.smartregister.util.DatePickerUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.Calendar;
import java.util.Date;

import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

public class RecordMUACDialogFragment extends DialogFragment {


    private Date dateOfBirth;

    public static final String WRAPPER_TAG = "tag";
    public static final String DATE_OF_BIRTH_TAG = "dob";

    public static RecordMUACDialogFragment newInstance() {
        RecordMUACDialogFragment recordMUACDialogFragment = new RecordMUACDialogFragment();
        return recordMUACDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        dateOfBirth = GrowthUtil.dateOfBirth();

        ViewGroup dialogView = (ViewGroup) inflater.inflate(org.smartregister.growthmonitoring.R.layout.record_weight_dialog_view, container, false);

        final EditText editMUAC = (EditText) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.edit_weight);
        CustomFontTextView titleView = dialogView.findViewById(org.smartregister.growthmonitoring.R.id.record_weight);
        TextView value_unit_symbol = dialogView.findViewById(org.smartregister.growthmonitoring.R.id.value_unit_symbol);
        value_unit_symbol.setText("cm");
        titleView.setText("Record MUAC");
        //formatEditMUACView(editMUAC, "");

        final DatePicker earlierDatePicker = (DatePicker) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.earlier_date_picker);
        earlierDatePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());
        if (dateOfBirth != null) {
            earlierDatePicker.setMinDate(dateOfBirth.getTime());
        }

        String firstName = org.smartregister.util.Utils.getValue(GrowthUtil.childDetails.getColumnmaps(), "first_name", true);
        String lastName = Utils.getValue(GrowthUtil.childDetails.getColumnmaps(), "last_name", true);
        String childName = getName(firstName, lastName).trim();
        TextView nameView = (TextView) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.child_name);
        nameView.setText(childName);

        String zeirId = getValue(GrowthUtil.childDetails.getColumnmaps(), "zeir_id", false);
        TextView numberView = (TextView) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.child_zeir_id);
        numberView.setText(String.format("%s: %s", getString(org.smartregister.growthmonitoring.R.string.label_zeir), zeirId));

        String duration = "";
        String dobString = getValue(GrowthUtil.childDetails.getColumnmaps(), "dob", false);
        if (StringUtils.isNotBlank(dobString)) {
            DateTime dateTime = new DateTime(getValue(GrowthUtil.childDetails.getColumnmaps(), "dob", false));
            duration = DateUtil.getDuration(dateTime);
        }
        TextView ageView = (TextView) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.child_age);
        if (StringUtils.isNotBlank(duration)) {
            ageView.setText(String.format("%s: %s", getString(org.smartregister.growthmonitoring.R.string.age), duration));
        } else {
            ageView.setText("");
        }


        final Button set = (Button) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.set);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String heightString = editMUAC.getText().toString();
                if (StringUtils.isBlank(heightString) || Float.valueOf(heightString) <= 0f) {
                    return;
                }

                dismiss();

                int day = earlierDatePicker.getDayOfMonth();
                int month = earlierDatePicker.getMonth();
                int year = earlierDatePicker.getYear();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);



            }
        });

        final Button heightTakenToday = (Button) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.weight_taken_today);
        heightTakenToday.setText("MUAC taken today");
        heightTakenToday.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String heightString = editMUAC.getText().toString();
                if (StringUtils.isBlank(heightString) || Float.valueOf(heightString) <= 0f) {
                    return;
                }

                dismiss();

                Calendar calendar = Calendar.getInstance();


                Float height = Float.valueOf(heightString);

            }
        });

        final Button heightTakenEarlier = (Button) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.weight_taken_earlier);
        heightTakenEarlier.setText("MUAC taken earlier");
        heightTakenEarlier.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                heightTakenEarlier.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                earlierDatePicker.setVisibility(View.VISIBLE);
                earlierDatePicker.requestFocus();
                set.setVisibility(View.VISIBLE);

                DatePickerUtils.themeDatePicker(earlierDatePicker, new char[]{'d', 'm', 'y'});
            }
        });

        Button cancel = (Button) dialogView.findViewById(org.smartregister.growthmonitoring.R.id.cancel);
        cancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return dialogView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface

    }

    private void formatEditMUACView(EditText editMUAC, String userInput) {
        StringBuilder stringBuilder = new StringBuilder(userInput);

        while (stringBuilder.length() > 2 && stringBuilder.charAt(0) == '0') {
            stringBuilder.deleteCharAt(0);
        }
        while (stringBuilder.length() < 2) {
            stringBuilder.insert(0, '0');
        }
        stringBuilder.insert(stringBuilder.length() - 1, '.');

        editMUAC.setText(stringBuilder.toString());
        // keeps the cursor always to the right
        Selection.setSelection(editMUAC.getText(), stringBuilder.toString().length());
    }

    @Override
    public void onStart() {
        super.onStart();
        // without a handler, the window size itself correctly
        // but the keyboard does not show up
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Window window = null;
                if (getDialog() != null) {
                    window = getDialog().getWindow();
                }

                if (window == null) {
                    return;
                }
                window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

            }

        });

    }
}
