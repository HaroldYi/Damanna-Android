package com.hello.holaApp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hello.holaApp.BuildConfig;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.vo.PhotoVo;
import com.sendbird.android.SendBird;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lji5317 on 09/01/2018.
 */

public class SplashActivity extends AppCompatActivity {

    private static Context context;

    private FirebaseAuth mAuth;

    private FirebaseUser fUser;
    private FirebaseFirestore db;

    private JSONObject userInfo;

    private GoogleApiClient mGoogleApiClient;

    private String secret;

    private static LocationManager locationManager;

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_SIGN_GOOGLE = 9002;
    private static final String TAG = "FacebookLogin";

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = this;

        MainActivity.tabIndex = 0;

        this.mAuth = FirebaseAuth.getInstance();
        this.fUser = this.mAuth.getCurrentUser();
        this.db = FirebaseFirestore.getInstance();

        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        /*Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();*/
                        settingLocation();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Toast.makeText(SplashActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();

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

            this.checkMember(this.fUser.getUid());

        } else {
            /*startActivity(new Intent(SplashActivity.this, SignActivity.class));*/

            Toast.makeText(this, getResources().getString(R.string.need_signin), Toast.LENGTH_SHORT).show();

            AuthUI.IdpConfig.Builder facebookBuilder = new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER);
            facebookBuilder.setPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

            AuthUI.IdpConfig.Builder googleBuilder = new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER);
            googleBuilder.setPermissions(Arrays.asList(Scopes.EMAIL, Scopes.PROFILE, Scopes.PLUS_ME, "https://www.googleapis.com/auth/user.birthday.read"));

            Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                    .setAvailableProviders(Arrays.asList(
                            facebookBuilder.build(),
                            googleBuilder.build()
                    ))
                    .setLogo(R.color.fui_transparent)
                    .setTheme(R.style.firebase_ui)
                    .build();

            startActivityForResult(intent, RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(this.mAuth.getCurrentUser() != null) {
            List<String> providers = this.mAuth.getCurrentUser().getProviders();
            String provider = providers.get(0);

            if (requestCode == RC_SIGN_IN) {
                if (resultCode == RESULT_OK) {
                    // Sign in succeeded
                    if(provider.indexOf("facebook") != -1) {
                        addUser(this.mAuth.getCurrentUser(), data);
                    } else if(provider.indexOf("google") != -1) {

                        setupGoogleAdditionalDetailsLogin(getResources().getString(R.string.googleClientId));
                        startGoogleAdditionalRequest();
                        /*GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                        if(result.isSuccess()) {
                            GoogleSignInAccount acct = result.getSignInAccount();
                            // execute AsyncTask to get data from Google People API
                            new GoogleAdditionalDetailsTask().execute(acct);
                        } else {
                            Log.d(TAG, "googleAdditionalDetailsResult: fail");
                        }*/
                    }
                } else {
                    // Sign in failed
                    Toast.makeText(this, getResources().getString(R.string.failed_signin), Toast.LENGTH_SHORT).show();
                    /*updateUI(null);*/
                }
            } else if(requestCode == RC_SIGN_GOOGLE) {
                addUser(this.mAuth.getCurrentUser(), data);
                return;
            }
        } else {
            /*finish();*/
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    // LocationListener 정의
    private static LocationListener gpsListener = new LocationListener() {

        // LocationManager 에서 위치정보가 변경되면 호출
        @Override
        public void onLocationChanged(Location location) {
            CommonFunction.setLatitude(location.getLatitude());
            CommonFunction.setLongitude(location.getLongitude());
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

    private void settingLocation() {

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                CommonFunction.setLatitude(lastLocation.getLatitude());
                CommonFunction.setLongitude(lastLocation.getLongitude());
            }
        } catch(SecurityException ex) {
            Log.e("gpsERR", ex.toString());
            Crashlytics.logException(ex);
            ex.printStackTrace();
        }
    }

    private void setupGoogleAdditionalDetailsLogin(String googleClientId) {
        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
        // basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(googleClientId)
                .requestServerAuthCode(googleClientId)
                .requestScopes(new Scope("profile"))
                .build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed: ");
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void googleAdditionalDetailsResult(Intent data) {

        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        Log.d(TAG, "googleAdditionalDetailsResult: ");
        if (result.isSuccess()) {
            // Signed in successfully
            GoogleSignInAccount acct = result.getSignInAccount();
            // execute AsyncTask to get data from Google People API
            new GoogleAdditionalDetailsTask().execute(acct);
        } else {
            Log.d(TAG, "googleAdditionalDetailsResult: fail");
        }
    }

    private void startGoogleAdditionalRequest() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_GOOGLE);
    }

    private void checkMember(String uid) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("member").document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {

                    docRef
                        .update("location", new GeoPoint(CommonFunction.getLatitude(), CommonFunction.getLongitude()))
                        .addOnSuccessListener(aVoid -> {
                            locationManager.removeUpdates(gpsListener);
                        })
                        .addOnFailureListener(command -> {
                            Log.d("ERRRR", command.getMessage());
                        });

                    Object nation = document.getData().get("nation");
                    Object identity = document.getData().get("identity");

                    if(nation == null) {
                        startActivity(new Intent(this, SelectCountryActivity.class));
                    } else if(identity == null) {
                        startActivity(new Intent(this, SelectRoleActivity.class));
                    } else if(nation != null && identity != null) {
                        startActivity(new Intent(this, MainActivity.class));
                    }

                    finish();
                } else {

                }
            } else {
                Crashlytics.logException(task.getException());
            }
        }).addOnFailureListener(command -> {
            Log.d("ERRRR", command.getMessage());
        });
    }

    private void addUser(FirebaseUser currentUser, Intent data) {

        this.fUser = currentUser;

        List<PhotoVo> photoVoList = new ArrayList<>();

        DocumentReference docRef = this.db.collection("member").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    this.checkMember(currentUser.getUid());
                } else {

                    Map<String, Object> stringMap = new HashMap<>();

                    stringMap.put("member_id", currentUser.getUid());
                    stringMap.put("content", String.format(getResources().getString(R.string.signupped), currentUser.getDisplayName()));
                    stringMap.put("reg_dt", new Date());

                    DocumentReference sayReference = this.db.collection("say").document();
                    stringMap.put("id", sayReference.getId());

                    sayReference.set(stringMap)
                            .addOnSuccessListener(documentReference -> {
                                //액티비티(팝업) 닫기

                            })
                            .addOnFailureListener(e -> {
                                //
                                Crashlytics.logException(e);
                                Log.w(TAG, "Error adding document", e);
                            });

                    List<String> providers = currentUser.getProviders();
                    String provider = providers.get(0);

                    if(provider.indexOf("facebook") != -1) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                AccessToken.getCurrentAccessToken(),
                                (object, response) -> {
                                    // Application code
                                    this.userInfo = response.getJSONObject();

                                    String facebookId = "";
                                    String dateOfBirth = "";
                                    String gender = "";

                                    try {
                                        facebookId = this.userInfo.get("id").toString();
                                    } catch (Exception e) {
                                        facebookId = "";
                                    }

                                    try {
                                        dateOfBirth = this.userInfo.get("birthday").toString();
                                    } catch (Exception e) {
                                        dateOfBirth = "";
                                    }

                                    try {
                                        gender = this.userInfo.get("gender").toString();
                                    } catch (Exception e) {
                                        gender = "";
                                    }

                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(Integer.parseInt(dateOfBirth.split("/")[2]), Integer.parseInt(dateOfBirth.split("/")[0]) - 1, Integer.parseInt(dateOfBirth.split("/")[1]));

                                    addUserDB(facebookId, calendar, gender, dateOfBirth);
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, name, gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    } else if(provider.indexOf("google") != -1) {
                        googleAdditionalDetailsResult(data);
                    }
                }
            } else {
                Crashlytics.logException(task.getException());
            }
        });
    }


    protected static void addUserDB(String facebookId, Calendar dateOfBirth, String gender, String dateOfBirthStr) {

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, Object> userMap = new HashMap<>();

        userMap.put("facebookId", facebookId);
        userMap.put("id", fUser.getUid());
        userMap.put("email", fUser.getEmail());
        userMap.put("dateOfBirth", new Date(dateOfBirth.getTimeInMillis()));
        userMap.put("gender", gender);
        userMap.put("name", fUser.getDisplayName());
        userMap.put("profileUrl", fUser.getPhotoUrl().toString());
        userMap.put("location", new GeoPoint(CommonFunction.getLatitude(), CommonFunction.getLongitude()));
        FirebaseFirestore.getInstance().collection("member").document(fUser.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully written!");

                    locationManager.removeUpdates(gpsListener);

                    List<String> userList = new ArrayList<>();
                    userList.add(fUser.getUid());

                    SendBird.createUserListQuery(userList);
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String pushToken = FirebaseInstanceId.getInstance().getToken();

                    SendBird.connect(fUser.getUid(), (user, e) -> {
                        if (e != null) {
                            // Error.
                            Crashlytics.logException(e);
                            return;
                        }

                        SendBird.updateCurrentUserInfo(fUser.getDisplayName(), fUser.getPhotoUrl().toString(), e12 -> {
                            if (e12 != null) {
                                // Error.
                                Crashlytics.logException(e12);
                                return;
                            }

                            /*SendBird.unregisterPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), handler);*/

                            SendBird.registerPushTokenForCurrentUser(pushToken, (ptrs, e1) -> {
                                if (e1 != null) {
                                    Crashlytics.logException(e1);
                                    return;
                                }

                                if (ptrs == SendBird.PushTokenRegistrationStatus.PENDING) {
                                    // Try registering the token after a connection has been successfully established.
                                } else {
                                    /*finish();*/
                                    if((gender != null && !gender.isEmpty()) && (dateOfBirthStr != null && !dateOfBirthStr.isEmpty())) {
                                        context.startActivity(new Intent(context, SelectCountryActivity.class));
                                    } else {
                                        context.startActivity(new Intent(context, ChangeGenderActivity.class));
                                    }
                                }
                            });
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Crashlytics.logException(e);
                });
    }
}

class GoogleAdditionalDetailsTask extends AsyncTask<GoogleSignInAccount, Void, Person> {

    private static final String TAG = "googleResult";

    private static final String GOOGLE_CLIENT_ID = "629394807535-53bv3fvsg0kd53oa0qoc8k59kd9j9qt1.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "wJm0I2pfh7lU8VInxn4S19b1";

    @Override
    protected Person doInBackground(GoogleSignInAccount... googleSignInAccounts) {
        Person profile = null;
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            //Redirect URL for web based applications.
            // Can be empty too.
            String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";

            // Exchange auth code for access token
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    httpTransport,
                    jsonFactory,
                    GOOGLE_CLIENT_ID,
                    GOOGLE_CLIENT_SECRET,
                    googleSignInAccounts[0].getServerAuthCode(),
                    redirectUrl
            ).execute();

            GoogleCredential credential = new GoogleCredential.Builder()
                    .setClientSecrets(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET)
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .build();

            credential.setFromTokenResponse(tokenResponse);

            PeopleService peopleService = new PeopleService.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName("damanna")
                    .build();

            // Get the user's profile
            profile = peopleService.people().get("people/me").setPersonFields("birthdays,genders").execute();
        } catch (IOException e) {
            Log.d(TAG, "doInBackground: " + e.getMessage());
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return profile;
    }

    @Override
    protected void onPostExecute(Person person) {
        String profileGender = "";
        String profileBirthday = "";
        String profileAbout = "";
        String profileCover = "";

        Calendar dateOfBirth = Calendar.getInstance();

        if (person != null) {

            if (person.getGenders() != null && person.getGenders().size() > 0) {
                profileGender = person.getGenders().get(0).getValue();
            }
            if (person.getBirthdays() != null && person.getBirthdays().get(0).size() > 0) {
//                    yyyy-MM-dd
                com.google.api.services.people.v1.model.Date dobDate = person.getBirthdays().get(0).getDate();
                if (dobDate.getYear() != null) {

                    String month = (dobDate.getMonth() < 10 ? "0" + dobDate.getMonth() : dobDate.getMonth().toString());
                    String dayOfMonth = (dobDate.getDay() < 10 ? "0" + dobDate.getDay() : dobDate.getDay().toString());

                    profileBirthday = dobDate.getYear() + "-" + month + "-" + dayOfMonth;

                    dateOfBirth.set(dobDate.getYear(), dobDate.getMonth() - 1, dobDate.getDay());
                }
            }
            if (person.getBiographies() != null && person.getBiographies().size() > 0) {
                profileAbout = person.getBiographies().get(0).getValue();
            }
            if (person.getCoverPhotos() != null && person.getCoverPhotos().size() > 0) {
                profileCover = person.getCoverPhotos().get(0).getUrl();
            }

            Log.d(TAG, String.format("googleOnComplete: gender: %s, birthday: %s, about: %s, cover: %s", profileGender, profileBirthday, profileAbout, profileCover));

            SplashActivity.addUserDB("", dateOfBirth, profileGender, profileBirthday);
        } else {
            Log.d(TAG, "NULLLL!!!!");
        }
    }
}