package com.hello.TrevelMeetUp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.TrevelMeetUp.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lji5317 on 08/01/2018.
 */

public class ChangeGenderActivity extends AppCompatActivity {

    private String genderStr = "";

    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_gender);

        RadioButton male = (RadioButton) findViewById(R.id.male);
        RadioButton female = (RadioButton) findViewById(R.id.female);
        RadioGroup genderGroup = (RadioGroup) findViewById(R.id.gender);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.new_say_action_bar, null);
        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText("신분 변경");

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);

        backBtn.setOnClickListener(view1 -> {
            finish();
        });

        Button saveBtn = (Button) actionView.findViewById(R.id.saveBtn);
        saveBtn.setText("확인");
        saveBtn.setOnClickListener(view -> {

            RadioButton genderRadioBtn = (RadioButton) findViewById(genderGroup.getCheckedRadioButtonId());
            this.genderStr = genderRadioBtn.getText().toString();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("gender", (this.genderStr.equals("남성") ? "male" : "female"));

            FirebaseFirestore.getInstance().collection("member").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .update(userInfo)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        finish();
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        });

        actionBar.setCustomView(actionView);

        Intent intent = getIntent();
        this.genderStr = intent.getStringExtra("gender");

        if(this.genderStr.equals("male")) {
            male.setChecked(true);
            female.setChecked(false);
        } else {
            male.setChecked(false);
            female.setChecked(true);
        }
    }
}
