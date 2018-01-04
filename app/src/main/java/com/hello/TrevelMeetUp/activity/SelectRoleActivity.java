package com.hello.TrevelMeetUp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.common.CommonFunction;
import com.meg7.widget.CircleImageView;

/**
 * Created by lji5317 on 04/01/2018.
 */

public class SelectRoleActivity extends BaseActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_role);

        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();

        this.db = FirebaseFirestore.getInstance();

        TextView tv = (TextView) findViewById(R.id.user_name);
        tv.setText(this.user.getDisplayName());

        Bitmap profileBitmap = CommonFunction.getBitmapFromURL(this.user.getPhotoUrl().toString());
        CircleImageView imageView = (CircleImageView) findViewById(R.id.user_profile_photo);
        imageView.setImageBitmap(profileBitmap);

        Button student = (Button) findViewById(R.id.student);
        student.setOnClickListener(this);

        Button wh = (Button) findViewById(R.id.wh);
        wh.setOnClickListener(this);

        Button traveler = (Button) findViewById(R.id.traveler);
        traveler.setOnClickListener(this);

        Button resident = (Button) findViewById(R.id.resident);
        resident.setOnClickListener(this);

        Button etc = (Button) findViewById(R.id.etc);
        etc.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String identity = "";

        switch (view.getId()) {
            case R.id.student:
                identity = "유학생";
                break;

            case R.id.wh:
                identity = "워킹홀리데이";
                break;

            case R.id.traveler:
                identity = "여행자";
                break;

            case R.id.resident:
                identity = "교민";
                break;

            default:
                identity = "기타";
                break;
        }

        this.db.collection("member").document(this.user.getUid())
                .update("identity", identity)
                .addOnSuccessListener(aVoid -> {
                    for(Activity activity : super.actList) {
                        activity.finish();
                    }

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
