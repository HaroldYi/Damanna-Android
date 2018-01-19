package com.hello.Damanna.activity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.Damanna.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lji5317 on 08/01/2018.
 */

public class ChangeDateOfBirth extends AppCompatActivity {

    private EditText dateOfBirth;
    private Calendar calendar;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_date_of_birth);

        this.calendar = Calendar.getInstance();
        this.db = FirebaseFirestore.getInstance();
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
        title.setText("생년월일 변경");

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view1 -> {
            finish();
        });

        Button saveBtn = (Button) actionView.findViewById(R.id.saveBtn);
        saveBtn.setText("확인");
        saveBtn.setOnClickListener(view -> {
            this.db.collection("member").document(this.user.getUid())
                    .update("dateOfBirth", new Date(this.calendar.getTimeInMillis()))
                    .addOnSuccessListener(aVoid -> {
                        finish();
                    });
        });

        String dateOfBirth = getIntent().getStringExtra("dateOfBirth");
        this.calendar.setTimeInMillis(Long.parseLong(dateOfBirth));

        int year = this.calendar.get(Calendar.YEAR);
        int month = (this.calendar.get(Calendar.MONTH)+1);
        int dayOfMonth = this.calendar.get(Calendar.DAY_OF_MONTH);

        dateOfBirth = String.format("%s.%s.%s", year, (month < 10 ? "0" + month : month), (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth));

        this.dateOfBirth = (EditText) findViewById(R.id.dateOfBirth);
        this.dateOfBirth.setInputType(0);
        this.dateOfBirth.setText(dateOfBirth);
        this.dateOfBirth.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(this, listener, year, month - 1, dayOfMonth);
            dialog.show();
        });
    }

    private DatePickerDialog.OnDateSetListener listener = (view, year, monthOfYear, dayOfMonth) -> {

        this.calendar.set(year, monthOfYear, dayOfMonth);

        String month = ((monthOfYear + 1) < 10 ? "0" + (monthOfYear+1) : ""+(monthOfYear+1));
        String date = ((dayOfMonth + 1) < 10 ? "0" + dayOfMonth : ""+dayOfMonth);

        this.dateOfBirth.setText(String.format("%s.%s.%s", year, month, date));
    };
}
