package com.hello.holaApp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lji5317 on 15/12/2017.
 */

public class PopupActivity extends AppCompatActivity {

    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_activity);

        CommonFunction.sendMsg(getApplicationContext());

        //UI 객체생성
        EditText content = (EditText)findViewById(R.id.content);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View view = getLayoutInflater().inflate(R.layout.new_say_action_bar, null);
        actionBar.setCustomView(view);

        Toolbar parent = (Toolbar)view.getParent();
        parent.setContentInsetsAbsolute(0,0);

        ImageButton backBtn = (ImageButton) view.findViewById(R.id.backBtn);

        backBtn.setOnClickListener(view1 -> {
            finish();
        });

        Button saveBtn = (Button) view.findViewById(R.id.saveBtn);

        //확인 버튼 클릭
        saveBtn.setOnClickListener(view1 -> {

            String contentStr = content.getText().toString();
            if(contentStr != null && !contentStr.isEmpty()) {
                //데이터 전달하기
                /*setResult(RESULT_OK, intent);*/

                Map<String, Object> stringMap = new HashMap<>();

                stringMap.put("member_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                stringMap.put("content", contentStr);
                stringMap.put("location", new GeoPoint(CommonFunction.getLatitude(), CommonFunction.getLongitude()));
                stringMap.put("reg_dt", new Date());

                /*DocumentReference memberReference = this.db.collection("member").document(this.user.getUid());
                stringMap.put("member", memberReference);*/

                DocumentReference sayReference = FirebaseFirestore.getInstance().collection("say").document();
                stringMap.put("id", sayReference.getId());

                sayReference.set(stringMap)
                        .addOnSuccessListener(documentReference -> {
                            //액티비티(팝업) 닫기
                            MainActivity.tabIndex = 0;
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            //
                            Crashlytics.logException(e);
                            Log.w(TAG, "Error adding document", e);
                        });
            } else {
                Toast toast = Toast.makeText(this, getResources().getString(R.string.require_content), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }
}
