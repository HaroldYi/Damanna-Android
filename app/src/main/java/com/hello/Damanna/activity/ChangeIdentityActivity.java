package com.hello.Damanna.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.Damanna.R;
import com.hello.Damanna.common.BaseApplication;
import com.sendbird.android.SendBird;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lji5317 on 08/01/2018.
 */

public class ChangeIdentityActivity extends AppCompatActivity {

    public static String identity = "";
    public Button saveBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.new_say_action_bar, null);
        actionBar.setCustomView(actionView);

        Toolbar parent = (Toolbar)actionView.getParent();
        parent.setContentInsetsAbsolute(0,0);

        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText("신분 변경");

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);

        backBtn.setOnClickListener(view1 -> {
            finish();
        });

        Intent intent = getIntent();
        this.identity = intent.getStringExtra("identity");
        this.saveBtn = (Button) findViewById(R.id.saveBtn);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content,
                        new ChangeIdentityActivity.MyPreferenceFragment()).commit();

        this.saveBtn.setText("확인");
        this.saveBtn.setOnClickListener(view -> {
            String identity = ChangeIdentityActivity.MyPreferenceFragment.getValue();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("identity", identity);

            FirebaseFirestore.getInstance().collection("member").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .update(userInfo)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot successfully written!");

                        SendBird.updateCurrentUserInfo(String.format("%s(%s)", this.user.getDisplayName(), identity), this.user.getPhotoUrl().toString(), e12 -> {
                            if (e12 != null) {
                                // Error.
                                /*Crashlytics.logException(e12);*/
                                return;
                            }

                            BaseApplication.getInstance().progressOFF();
                            finish();
                        });

                        finish();
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        });

    }

    // PreferenceFragment 클래스 사용
    public static class MyPreferenceFragment extends PreferenceFragment {
        private FirebaseAuth mAuth;

        private static CheckBoxPreference student;
        private static CheckBoxPreference wh;
        private static CheckBoxPreference traveler;
        private static CheckBoxPreference resident;
        private static CheckBoxPreference etc;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.change_identity);
        }

        @Override
        public void onResume() {
            super.onResume();

            this.mAuth = FirebaseAuth.getInstance();

            this.student = (CheckBoxPreference) findPreference("student");
            this.wh = (CheckBoxPreference) findPreference("wh");
            this.traveler = (CheckBoxPreference) findPreference("traveler");
            this.resident = (CheckBoxPreference) findPreference("resident");
            this.etc = (CheckBoxPreference) findPreference("etc");

            if(ChangeIdentityActivity.identity.equals("유학생")) {
                this.student.setChecked(true);
                this.wh.setChecked(false);
                this.traveler.setChecked(false);
                this.resident.setChecked(false);
                this.etc.setChecked(false);
            } else if(ChangeIdentityActivity.identity.equals("워킹홀리데이")) {
                this.student.setChecked(false);
                this.wh.setChecked(true);
                this.traveler.setChecked(false);
                this.resident.setChecked(false);
                this.etc.setChecked(false);
            } else if(ChangeIdentityActivity.identity.equals("여행자")) {
                this.student.setChecked(false);
                this.wh.setChecked(false);
                this.traveler.setChecked(true);
                this.resident.setChecked(false);
                this.etc.setChecked(false);
            } else if(ChangeIdentityActivity.identity.equals("교포")) {
                this.student.setChecked(false);
                this.wh.setChecked(false);
                this.traveler.setChecked(false);
                this.resident.setChecked(true);
                this.etc.setChecked(false);
            } else {
                this.student.setChecked(false);
                this.wh.setChecked(false);
                this.traveler.setChecked(false);
                this.resident.setChecked(false);
                this.etc.setChecked(true);
            }

            this.student.setOnPreferenceClickListener(preference -> {
                if(this.student.isChecked()) {
                    this.wh.setChecked(false);
                    this.traveler.setChecked(false);
                    this.resident.setChecked(false);
                    this.etc.setChecked(false);
                }
                return false;
            });

            this.wh.setOnPreferenceClickListener(preference -> {
                if(this.wh.isChecked()) {
                    this.student.setChecked(false);
                    this.traveler.setChecked(false);
                    this.resident.setChecked(false);
                    this.etc.setChecked(false);
                }
                return false;
            });

            this.traveler.setOnPreferenceClickListener(preference -> {
                if(this.traveler.isChecked()) {
                    this.student.setChecked(false);
                    this.wh.setChecked(false);
                    this.resident.setChecked(false);
                    this.etc.setChecked(false);
                }
                return false;
            });

            this.resident.setOnPreferenceClickListener(preference -> {
                if(this.resident.isChecked()) {
                    this.student.setChecked(false);
                    this.wh.setChecked(false);
                    this.traveler.setChecked(false);
                    this.etc.setChecked(false);
                }
                return false;
            });

            this.etc.setOnPreferenceClickListener(preference -> {
                if(this.etc.isChecked()) {
                    this.student.setChecked(false);
                    this.wh.setChecked(false);
                    this.traveler.setChecked(false);
                    this.resident.setChecked(false);
                }
                return false;
            });
        }

        public static String getValue() {
            String value = "";

            if(student.isChecked()) {
                value = "유학생";
            } else if(wh.isChecked()) {
                value = "워킹홀리데이";
            } else if(traveler.isChecked()) {
                value = "여행자";
            } else if(resident.isChecked()) {
                value = "교포";
            } else {
                value = "기타";
            }

            return value;
        }
    }
}
