package com.hello.TrevelMeetUp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.TrevelMeetUp.R;
import com.sendbird.android.SendBird;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lji5317 on 08/01/2018.
 */

public class ChangeNameActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText userName;
    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.change_name);

        this.userName = (EditText) findViewById(R.id.userName);

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
        title.setText("이름 변경");

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);

        Button saveBtn = (Button) actionView.findViewById(R.id.saveBtn);
        saveBtn.setText("확인");
        saveBtn.setOnClickListener(this);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        this.userName.setText(name);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.saveBtn :
                String userName = this.userName.getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                Map<String, Object> userInfo = new HashMap<>();
                                userInfo.put("name", userName);

                                FirebaseFirestore.getInstance().collection("member").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .update(userInfo)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");

                                            SendBird.updateCurrentUserInfo(userName, user.getPhotoUrl().toString(), e12 -> {
                                                if (e12 != null) {
                                                    // Error.
                                                    /*Crashlytics.logException(e12);*/
                                                    return;
                                                }

                                                finish();
                                            });
                                        })
                                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                            }
                        });
                break;
            case R.id.backBtn :
                finish();
                break;
        }
    }
}
