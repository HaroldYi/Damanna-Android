package com.hello.holaApp.activity;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lji5317 on 08/01/2018.
 */

public class ChangeCountry extends AppCompatActivity {

    private String nation = "";

    private boolean selectYn = false;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_country);

        CommonFunction.sendMsg(getApplicationContext());

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
        title.setText(getResources().getString(R.string.change_nation));

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);

        backBtn.setOnClickListener(view1 -> {
            finish();
        });

        Button saveBtn = (Button) actionView.findViewById(R.id.saveBtn);

        Spinner spinner = (Spinner) findViewById(R.id.country_list);
        List<String> nationList = new ArrayList<>();

        this.db.collection("nation/")
                .orderBy("nation_kr", Query.Direction.ASCENDING)
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Crashlytics.logException(e);
                        return;
                    }

                    for (DocumentSnapshot document : value) {
                        String nation = document.getString("nation_kr");
                        nationList.add(nation);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, nationList);
                    spinner.setAdapter(adapter);

                    int index = 0;

                    for(String country : nationList) {
                        Intent intent = getIntent();
                        String nation = intent.getStringExtra("nation");

                        if(nation.equals(country)) {
                            break;
                        }

                        index++;
                    }
                    spinner.setSelection(index);
                });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectYn = true;
                nation = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectYn = false;
            }
        });

        saveBtn.setOnClickListener(view -> {

            if(this.selectYn) {
                this.db.collection("member").document(this.user.getUid())
                        .update("nation", this.nation)
                        .addOnSuccessListener(aVoid -> {
                            finish();
                        });
            }
        });
    }
}
