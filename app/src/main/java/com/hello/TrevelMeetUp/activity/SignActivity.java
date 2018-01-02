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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.vo.Photo;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lji5317 on 05/12/2017.
 */

public class SignActivity extends Activity {

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    private LoginManager loginManager;
    private FirebaseFirestore db;

    private Double latitude = 0.0;
    private Double longitude = 0.0;

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

        Button button = (Button) findViewById(R.id.signInBtn);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        this.mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void facebookLoginOnClick(View view) {
        this.loginManager.logInWithReadPermissions(SignActivity.this, Arrays.asList("public_profile", "email"));
        this.loginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
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
                    gpsListener);

            // 네트워크를 이용한 위치 요청
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    gpsListener);

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

        Map<String, Object> user = new HashMap<>();
        user.put("id", currentUser.getUid());
        user.put("email", currentUser.getEmail());
        user.put("gender", "male");
        user.put("name", currentUser.getDisplayName());
        user.put("profileUrl", currentUser.getPhotoUrl().toString());
        user.put("location", new GeoPoint(this.latitude, this.longitude));

        this.db.collection("member").document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        this.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        List<String> userList = new ArrayList<>();
                        userList.add(this.mAuth.getCurrentUser().getUid());
                        addUserDb(this.mAuth.getCurrentUser());

                        SendBird.createUserListQuery(userList);
                        try {
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String pushToken = FirebaseInstanceId.getInstance().getToken();

                        SendBird.connect(this.mAuth.getCurrentUser().getUid(), new SendBird.ConnectHandler() {
                                    @Override
                                    public void onConnected(User user, SendBirdException e) {
                                        if (e != null) {
                                            // Error.
                                            return;
                                        }

                                        SendBird.updateCurrentUserInfo(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getPhotoUrl().toString(), new SendBird.UserInfoUpdateHandler() {
                                            @Override
                                            public void onUpdated(SendBirdException e) {
                                                if (e != null) {
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
                                                        finish();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
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
