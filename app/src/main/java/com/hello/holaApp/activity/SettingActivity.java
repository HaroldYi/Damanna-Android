package com.hello.holaApp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;

import org.mortbay.jetty.Main;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lji5317 on 08/01/2018.
 */

public class SettingActivity extends AppCompatActivity {

    static MainActivity mainActivity = (MainActivity)MainActivity.activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonFunction.sendMsg(getApplicationContext());

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.new_say_action_bar, null);
        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText("Setting");

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);

        backBtn.setOnClickListener(view1 -> {
            finish();
        });

        Button saveBtn = (Button) actionView.findViewById(R.id.saveBtn);
        saveBtn.setVisibility(View.GONE);

        actionBar.setCustomView(actionView);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content,
                        new MyPreferenceFragment()).commit();
    }

    // PreferenceFragment 클래스 사용
    public static class MyPreferenceFragment extends PreferenceFragment {

        private FirebaseAuth mAuth;
        private FirebaseFirestore db;
        private FirebaseUser currentUser;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.setting);
        }

        @Override
        public void onResume() {
            super.onResume();

            this.mAuth = FirebaseAuth.getInstance();
            this.currentUser = this.mAuth.getCurrentUser();
            this.db = FirebaseFirestore.getInstance();

            SwitchPreference notification = (SwitchPreference) findPreference("notification");
            SwitchPreference bell = (SwitchPreference) findPreference("bell");
            SwitchPreference vibration = (SwitchPreference) findPreference("vibration");

            SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);

            notification.setChecked(pref.getBoolean("notification", true));
            bell.setChecked(pref.getBoolean("bell", true));
            vibration.setChecked(pref.getBoolean("vibration", true));

            Preference name = findPreference("name");
            Preference age = findPreference("age");
            Preference nation = findPreference("nation");
            Preference identity = findPreference("identity");
            Preference question = findPreference("question");
            Preference signOut = findPreference("signOut");

            Preference gender = findPreference("gender");

            notification.setOnPreferenceClickListener(preference -> {

                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("notification", notification.isChecked());
                editor.putBoolean("bell", bell.isChecked());
                editor.putBoolean("vibration", vibration.isChecked());
                editor.commit();

                return false;
            });

            bell.setOnPreferenceClickListener(preference -> {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("bell", bell.isChecked());
                editor.commit();
                return false;
            });

            vibration.setOnPreferenceClickListener(preference -> {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("vibration", vibration.isChecked());
                editor.commit();
                return false;
            });

            /*gender.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), ChangeGenderActivity.class));
                return false;
            });*/

            question.setOnPreferenceClickListener(preference -> {
                Uri uri = Uri.parse("mailto:hellostudioteam@naver.com");
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(it);

                return false;
            });

            signOut.setOnPreferenceClickListener(preference -> {
                FirebaseAuth.getInstance().signOut();
                MainActivity.tabIndex = 0;
                startActivity(new Intent(getActivity(), SplashActivity.class));
                mainActivity.finish();
                getActivity().finish();
                return false;
            });

            DocumentReference docRef = this.db.collection("member").document(this.currentUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {

                        String nameStr = document.getData().get("name").toString();
                        String nationStr = document.getData().get("nation").toString();
                        String identityStr = document.getData().get("identity").toString();

                        long dateOfBirth = ((Date) document.getData().get("dateOfBirth")).getTime();
                        long now = System.currentTimeMillis();

                        Calendar birthCalendar = Calendar.getInstance();
                        birthCalendar.setTimeInMillis(dateOfBirth);

                        int yearOfBirth = birthCalendar.get(Calendar.YEAR);

                        Calendar nowCalender = Calendar.getInstance();
                        nowCalender.setTimeInMillis(now);

                        int nowYear = nowCalender.get(Calendar.YEAR);

                        int koreanAge = nowYear - yearOfBirth + 1;

                        name.setSummary(nameStr);
                        age.setSummary(String.format("%d세", koreanAge));
                        nation.setSummary(nationStr);
                        identity.setSummary(identityStr);

                        name.setOnPreferenceClickListener(preference -> {
                            Intent intent= new Intent(getActivity(), ChangeNameActivity.class);
                            intent.putExtra("name", nameStr);

                            startActivity(intent);
                            return false;
                        });

                        age.setOnPreferenceClickListener(preference -> {
                            Intent intent= new Intent(getActivity(), ChangeDateOfBirth.class);
                            intent.putExtra("dateOfBirth", String.format("%d", dateOfBirth));

                            startActivity(intent);
                            return false;
                        });

                        nation.setOnPreferenceClickListener(preference -> {
                            Intent intent= new Intent(getActivity(), ChangeCountry.class);
                            intent.putExtra("nation", nationStr);

                            startActivity(intent);
                            return false;
                        });

                        identity.setOnPreferenceClickListener(preference -> {
                            Intent intent= new Intent(getActivity(), ChangeIdentityActivity.class);
                            intent.putExtra("identity", identityStr);

                            startActivity(intent);
                            return false;
                        });
                    }
                } else {
                    Crashlytics.logException(task.getException());
                }
            });
        }
    }
}
