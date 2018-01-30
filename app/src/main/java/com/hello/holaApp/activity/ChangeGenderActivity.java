package com.hello.holaApp.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lji5317 on 08/01/2018.
 */

public class ChangeGenderActivity extends AppCompatActivity {

    private String genderStr = "";
    private Calendar calendar;
    private EditText dateOfBirth;

    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_gender);

        CommonFunction.sendMsg(getApplicationContext());

        this.calendar = Calendar.getInstance();

        RadioButton male = (RadioButton) findViewById(R.id.male);
        RadioButton female = (RadioButton) findViewById(R.id.female);
        RadioGroup genderGroup = (RadioGroup) findViewById(R.id.gender);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.hide();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        RadiusNetworkImageView imageView = findViewById(R.id.user_profile_photo);
        imageView.setRadius(150f);
        imageView.setImageUrl(user.getPhotoUrl().toString(), VolleySingleton.getInstance(this).getImageLoader());

        TextView textView = (TextView) findViewById(R.id.user_name);
        textView.setText(user.getDisplayName());

        Button saveBtn = (Button) findViewById(R.id.saveBtn);

        dateOfBirth = (EditText) findViewById(R.id.dateOfBirth);
        dateOfBirth.setInputType(0);

        int year = this.calendar.get(Calendar.YEAR);
        int month = (this.calendar.get(Calendar.MONTH)+1);
        int dayOfMonth = this.calendar.get(Calendar.DAY_OF_MONTH);

        dateOfBirth.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(this, listener, year, month - 1, dayOfMonth);
            dialog.show();
        });

        TextView finalDateOfBirth = dateOfBirth;
        dateOfBirth.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    if(finalDateOfBirth.getText().toString() != null && !finalDateOfBirth.getText().toString().isEmpty()) {
                        saveBtn.setBackgroundColor(Color.GREEN);
                    } else {
                        saveBtn.setBackgroundColor(Color.GRAY);
                    }
                    break;
            }
            return true;
        });

        if(this.genderStr != null && !this.genderStr.isEmpty()) {
            saveBtn.setBackgroundColor(Color.GRAY);
        } else {
            TextView finalDateOfBirth1 = dateOfBirth;
            saveBtn.setOnClickListener(view -> {

                RadioButton genderRadioBtn = (RadioButton) findViewById(genderGroup.getCheckedRadioButtonId());
                this.genderStr = genderRadioBtn.getText().toString();

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("gender", (this.genderStr.equals("남성") ? "male" : "female"));
                userInfo.put("dateOfBirth", new Date(this.calendar.getTimeInMillis()));

                FirebaseFirestore.getInstance().collection("member").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update(userInfo)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            startActivity(new Intent(this, SelectCountryActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            });
        }

        /*actionBar.setCustomView(actionView);*/

        /*Intent intent = getIntent();
        this.genderStr = intent.getStringExtra("gender");

        if(this.genderStr.equals("male")) {
            male.setChecked(true);
            female.setChecked(false);
        } else {
            male.setChecked(false);
            female.setChecked(true);
        }*/
    }

    private DatePickerDialog.OnDateSetListener listener = (view, year, monthOfYear, dayOfMonth) -> {

        this.calendar.set(year, monthOfYear, dayOfMonth);

        String month = ((monthOfYear + 1) < 10 ? "0" + (monthOfYear+1) : ""+(monthOfYear+1));
        String date = ((dayOfMonth + 1) < 10 ? "0" + dayOfMonth : ""+dayOfMonth);

        this.dateOfBirth.setText(String.format("%s.%s.%s", year, month, date));
    };
}
