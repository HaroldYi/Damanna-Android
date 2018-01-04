package com.hello.TrevelMeetUp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.vo.Photo;
import com.sendbird.android.SendBird;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lji5317 on 05/12/2017.
 */

public class SignActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    private LoginManager loginManager;
    private FirebaseFirestore db;

    private Double latitude = 0.0;
    private Double longitude = 0.0;

    private Activity activity;

    private long backKeyPressedTime = 0;
    private Toast toast;

    private static final String TAG = "FacebookLogin";

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    @Override
    protected void onStart() {
        super.onStart();
        this.mAuth = FirebaseAuth.getInstance();
        this.mCallbackManager = CallbackManager.Factory.create();
        this.loginManager = LoginManager.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        this.activity = this;

        Button button = (Button) findViewById(R.id.signInBtn);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        this.mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // 종료전 확인
        if (System.currentTimeMillis() > this.backKeyPressedTime + 2000) {
            this.backKeyPressedTime = System.currentTimeMillis();
            this.showGuide();
            return;
        }

        if (System.currentTimeMillis() <= this.backKeyPressedTime + 2000) {
            this.activity.finish();
            this.toast.cancel();
        }
    }

    private void showGuide() {
        this.toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        this.toast.show();
    }

    public void facebookLoginOnClick(View view) {
        this.loginManager.logInWithReadPermissions(SignActivity.this, Arrays.asList("public_profile", "email"));
        this.loginManager.registerCallback(this.mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
    }

    private void addUserDb(FirebaseUser currentUser) {

        List<Photo> photoList = new ArrayList<>();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 이용한 위치 요청
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this.gpsListener);

            // 네트워크를 이용한 위치 요청
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this.gpsListener);

            // 위치요청을 한 상태에서 위치추적되는 동안 먼저 최근 위치를 조회해서 set
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                this.latitude = lastLocation.getLatitude();
                this.longitude = lastLocation.getLongitude();
            }
        } catch(SecurityException ex) {
            Log.e("gpsERR", ex.toString());
            ex.printStackTrace();
        }

        DocumentReference docRef = this.db.collection("member").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    finish();
                } else {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", currentUser.getUid());
                    userMap.put("email", currentUser.getEmail());
                    userMap.put("gender", "male");
                    userMap.put("name", currentUser.getDisplayName());
                    userMap.put("profileUrl", currentUser.getPhotoUrl().toString());
                    userMap.put("location", new GeoPoint(latitude, longitude));

                    this.db.collection("member").document(currentUser.getUid())
                            .set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "DocumentSnapshot successfully written!");

                                List<String> userList = new ArrayList<>();
                                userList.add(mAuth.getCurrentUser().getUid());

                                SendBird.createUserListQuery(userList);
                                try {
                                    FirebaseInstanceId.getInstance().deleteInstanceId();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                String pushToken = FirebaseInstanceId.getInstance().getToken();

                                SendBird.connect(mAuth.getCurrentUser().getUid(), (user, e) -> {
                                    if (e != null) {
                                        // Error.
                                        return;
                                    }

                                    SendBird.updateCurrentUserInfo(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getPhotoUrl().toString(), e12 -> {
                                        if (e12 != null) {
                                            // Error.
                                            return;
                                        }

                                        SendBird.registerPushTokenForCurrentUser(pushToken, (ptrs, e1) -> {
                                            if (e1 != null) {
                                                return;
                                            }

                                            if (ptrs == SendBird.PushTokenRegistrationStatus.PENDING) {
                                                // Try registering the token after a connection has been successfully established.
                                            } else {
                                                /*finish();*/
                                                startActivity(new Intent(this, SelectCountryActivity.class));
                                            }
                                        });
                                    });
                                });
                            })
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        this.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        addUserDb(this.mAuth.getCurrentUser());
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }

                    // [START_EXCLUDE]
                    // [END_EXCLUDE]
                });
    }

    // LocationListener 정의
    private LocationListener gpsListener = new LocationListener() {

        // LocationManager 에서 위치정보가 변경되면 호출
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}
