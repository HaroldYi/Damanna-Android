package com.hello.holaApp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.holaApp.R;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.sendbird.android.SendBird;

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

        /*Bitmap profileBitmap = CommonFunction.getBitmapFromURL(this.user.getPhotoUrl().toString());*/
        RadiusNetworkImageView imageView = (RadiusNetworkImageView) findViewById(R.id.user_profile_photo);
        imageView.setRadius(175f);
        imageView.setImageUrl(this.user.getPhotoUrl().toString(), VolleySingleton.getInstance(this).getImageLoader());

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
                identity = getResources().getString(R.string.identity_student);
                break;

            case R.id.wh:
                identity = getResources().getString(R.string.identity_wh);
                break;

            case R.id.traveler:
                identity = getResources().getString(R.string.identity_traveler);
                break;

            case R.id.resident:
                identity = getResources().getString(R.string.identity_resident);
                break;

            default:
                identity = "기타";
                break;
        }

        String finalIdentity1 = identity;
        this.db.collection("member").document(this.user.getUid())
                .update("identity", identity)
                .addOnSuccessListener(aVoid -> {
                    /*for(Activity activity : super.actList) {
                        activity.finish();
                    }*/

                    String finalIdentity = finalIdentity1;
                    SendBird.connect(mAuth.getCurrentUser().getUid(), (user, e) -> {
                        if (e != null) {
                            // Error.
                            Crashlytics.logException(e);
                            return;
                        }

                        SendBird.updateCurrentUserInfo(String.format("%s(%s)", mAuth.getCurrentUser().getDisplayName(), finalIdentity), mAuth.getCurrentUser().getPhotoUrl().toString(), e12 -> {
                            if (e12 != null) {
                                // Error.
                                Crashlytics.logException(e12);
                                return;
                            }
                        });
                    });

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
