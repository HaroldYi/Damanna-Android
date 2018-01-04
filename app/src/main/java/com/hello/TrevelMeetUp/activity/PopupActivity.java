package com.hello.TrevelMeetUp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.hello.TrevelMeetUp.R;

/**
 * Created by lji5317 on 15/12/2017.
 */

public class PopupActivity extends BaseActivity {

    private EditText editText;
    private Button saveBtn;
    private Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity);

        //UI 객체생성
        this.editText = (EditText)findViewById(R.id.txtText);

        this.saveBtn = (Button) findViewById(R.id.saveBtn);
        this.cancelBtn = (Button) findViewById(R.id.cancelBtn);

        //확인 버튼 클릭
        this.saveBtn.setOnClickListener(view -> {
            //데이터 전달하기
            Intent intent = new Intent();
            intent.putExtra("sayContent", this.editText.getText().toString());
            setResult(RESULT_OK, intent);

            //액티비티(팝업) 닫기
            finish();
        });

        this.cancelBtn.setOnClickListener(view -> {
            //액티비티(팝업) 닫기
            finish();
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

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
