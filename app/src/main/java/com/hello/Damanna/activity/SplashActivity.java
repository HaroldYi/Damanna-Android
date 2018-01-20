package com.hello.Damanna.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hello.Damanna.R;
import com.sendbird.android.SendBird;

/**
 * Created by lji5317 on 09/01/2018.
 */

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser fUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAuth = FirebaseAuth.getInstance();
        this.fUser = this.mAuth.getCurrentUser();

        if (this.fUser != null) {

            SendBird.connect(this.fUser.getUid(), (user, e) -> {
                if (e != null) {
                    // Error.
                    Log.e("sendBirdErr", e.getMessage());
                    return;
                } else {
                    String token = FirebaseInstanceId.getInstance().getToken();
                    SendBird.registerPushTokenForCurrentUser(token, (ptrs, e1) -> {
                        if (e1 != null) {
                            return;
                        }

                        if (ptrs == SendBird.PushTokenRegistrationStatus.PENDING) {
                            // Try registering the token after a connection has been successfully established.
                        }
                    });
                }
            });

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("member").document(this.fUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {

                        Object nation = document.getData().get("nation");
                        Object identity = document.getData().get("identity");

                        if(nation == null) {
                            startActivity(new Intent(this, SelectCountryActivity.class));
                        } else if(identity == null) {
                            startActivity(new Intent(this, SelectRoleActivity.class));
                        } else if(nation != null && identity != null) {
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);

                            finish();
                        }
                    } else {

                    }
                } else {
                    /*Crashlytics.logException(task.getException());*/
                }
            });
        } else {
            startActivity(new Intent(SplashActivity.this, SignActivity.class));

            /*Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();

            Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                            ))
                    .setLogo(R.color.fui_transparent)
                    .setTheme(R.style.firebase_ui)
                    .build();

            startActivityForResult(intent, RC_SIGN_IN);*/
        }
    }
}
