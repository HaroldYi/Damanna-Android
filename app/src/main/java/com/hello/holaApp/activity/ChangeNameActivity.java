package com.hello.holaApp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.holaApp.R;
import com.hello.holaApp.common.BaseApplication;
import com.hello.holaApp.common.CommonFunction;
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

        CommonFunction.sendMsg(getApplicationContext());

        this.userName = (EditText) findViewById(R.id.userName);
        this.userName.setOnKeyListener((v, keyCode, event) -> {
            //Enter key Action
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                //Enter키눌렀을떄 처리
                return true;
            }
            return false;
        });

        this.userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
        title.setText(getResources().getString(R.string.change_name));

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);

        Button saveBtn = (Button) actionView.findViewById(R.id.saveBtn);
        saveBtn.setText(getResources().getString(R.string.confirm_btn));
        saveBtn.setOnClickListener(this);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        this.userName.setText(name);
        this.userName.setSelection(this.userName.getText().length());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.saveBtn :
                String userName = this.userName.getText().toString().replace("\n", "");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build();

                new Handler().postDelayed(() -> {
                    BaseApplication.getInstance().progressON(this, getResources().getString(R.string.saving));
                }, 500);

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

                                                BaseApplication.getInstance().progressOFF();
                                                finish();
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error writing document", e);
                                            Crashlytics.logException(e);
                                        });
                            }
                        });
                break;
            case R.id.backBtn :
                finish();
                break;
        }
    }
}
