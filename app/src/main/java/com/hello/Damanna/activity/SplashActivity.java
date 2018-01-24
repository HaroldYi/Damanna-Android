package com.hello.Damanna.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hello.Damanna.BuildConfig;
import com.hello.Damanna.R;
import com.hello.Damanna.vo.Photo;
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

    private FirebaseAuth mAuth;
    private FirebaseUser fUser;
    private FirebaseFirestore db;

    private JSONObject userInfo;

    private Intent intent;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "FacebookLogin";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAuth = FirebaseAuth.getInstance();
        this.fUser = this.mAuth.getCurrentUser();
        this.db = FirebaseFirestore.getInstance();

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

            this.checkMember();

        } else {
            startActivity(new Intent(SplashActivity.this, SignActivity.class));

            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();

            /*AuthUI.IdpConfig.Builder facebookBuilder = new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER);
            facebookBuilder.setPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

            AuthUI.IdpConfig.Builder googleBuilder = new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER);
            googleBuilder.setPermissions(Arrays.asList(Scopes.EMAIL, Scopes.PROFILE, Scopes.PLUS_ME));

            Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                    .setAvailableProviders(Arrays.asList(
                            facebookBuilder.build(),
                            googleBuilder.build()
                    ))
                    .setLogo(R.color.fui_transparent)
                    .setTheme(R.style.firebase_ui)
                    .build();

            startActivityForResult(intent, RC_SIGN_IN);*/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.intent = data;

        List<String> providers = this.mAuth.getCurrentUser().getProviders();
        String provider = providers.get(0);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign in succeeded
                if(provider.indexOf("facebook") != -1) {
                    addUser(this.mAuth.getCurrentUser());
                } else if(provider.indexOf("google") != -1) {

                    IdpResponse idpResponse = IdpResponse.fromResultIntent(data);

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
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                /*updateUI(null);*/
            }
        }
    }

    private void checkMember() {
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
                        startActivity(new Intent(this, MainActivity.class));
                    }

                    finish();
                } else {

                }
            } else {
                    /*Crashlytics.logException(task.getException());*/
            }
        }).addOnFailureListener(command -> {
            Log.d("ERRRR", command.getMessage());
        });
    }

    private void addUser(FirebaseUser currentUser) {

        List<Photo> photoList = new ArrayList<>();

        /*LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
        }*/

        DocumentReference docRef = this.db.collection("member").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    setContentView(R.layout.activity_main);
                    this.checkMember();
                } else {

                    List<String> providers = this.mAuth.getCurrentUser().getProviders();
                    String provider = providers.get(0);

                    if(provider.indexOf("facebook") != -1) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                AccessToken.getCurrentAccessToken(),
                                (object, response) -> {
                                    // Application code
                                    userInfo = response.getJSONObject();

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

                                    addUserDB(facebookId, dateOfBirth, gender);
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, name, gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    } else if(provider.indexOf("google") != -1) {
                        /*GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);*/
                    }
                }
            } else {
                /*Crashlytics.logException(task.getException());*/
            }
        });
    }


    private void addUserDB(String facebookId, String dateOfBirth, String gender) {

        Map<String, Object> userMap = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(dateOfBirth.split("/")[2]), Integer.parseInt(dateOfBirth.split("/")[0]) - 1, Integer.parseInt(dateOfBirth.split("/")[1]));
        userMap.put("facebookId", facebookId);
        userMap.put("id", this.fUser.getUid());
        userMap.put("email", this.fUser.getEmail());
        userMap.put("dateOfBirth", new Date(calendar.getTimeInMillis()));
        userMap.put("gender", gender);
        userMap.put("name", this.fUser.getDisplayName());
        userMap.put("profileUrl", this.fUser.getPhotoUrl().toString());
                                    /*userMap.put("location", new GeoPoint(latitude, longitude));*/
        this.db.collection("member").document(this.fUser.getUid())
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
                                                    /*Crashlytics.logException(e);*/
                            return;
                        }

                        SendBird.updateCurrentUserInfo(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getPhotoUrl().toString(), e12 -> {
                            if (e12 != null) {
                                // Error.
                                                        /*Crashlytics.logException(e12);*/
                                return;
                            }

                            /*SendBird.unregisterPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), handler);*/

                            SendBird.registerPushTokenForCurrentUser(pushToken, (ptrs, e1) -> {
                                if (e1 != null) {
                                                            /*Crashlytics.logException(e1);*/
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
                .addOnFailureListener(e -> {
                                            /*Crashlytics.logException(e);*/
                });
    }
}

class GoogleAdditionalDetailsTask extends AsyncTask<GoogleSignInAccount, Void, Person> {

    private static final String TAG = "googleResult";

    private static final String GOOGLE_CLIENT_ID = "629394807535-bb46mu7u97ck8fiue3k8ini2am7n37gd.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "DwupZLq9H2oNGZaAZWWBcQPQ";

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
            profile = peopleService.people().get("people/me").setRequestMaskIncludeField("person.names, person.emailAddresses, person.genders, person.birthdays").execute();
        } catch (IOException e) {
            Log.d(TAG, "doInBackground: " + e.getMessage());
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

        if (person != null) {
            if (person.getGenders() != null && person.getGenders().size() > 0) {
                profileGender = person.getGenders().get(0).getValue();
            }
            if (person.getBirthdays() != null && person.getBirthdays().get(0).size() > 0) {
//                    yyyy-MM-dd
                com.google.api.services.people.v1.model.Date dobDate = person.getBirthdays().get(0).getDate();
                if (dobDate.getYear() != null) {
                    profileBirthday = dobDate.getYear() + "-" + dobDate.getMonth() + "-" + dobDate.getDay();
                }
            }
            if (person.getBiographies() != null && person.getBiographies().size() > 0) {
                profileAbout = person.getBiographies().get(0).getValue();
            }
            if (person.getCoverPhotos() != null && person.getCoverPhotos().size() > 0) {
                profileCover = person.getCoverPhotos().get(0).getUrl();
            }
            Log.d(TAG, String.format("googleOnComplete: gender: %s, birthday: %s, about: %s, cover: %s", profileGender, profileBirthday, profileAbout, profileCover));
        }
    }
}