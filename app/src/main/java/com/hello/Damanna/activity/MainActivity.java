package com.hello.Damanna.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hello.Damanna.R;
import com.hello.Damanna.fragment.GroupChannelListFragment;
import com.hello.Damanna.fragment.Profile;
import com.hello.Damanna.fragment.Say;
import com.hello.Damanna.vo.Photo;
import com.sendbird.android.SendBird;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser fUser;
    private FirebaseFirestore db;
    private boolean fromFragmentYn = false;

    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;
    private NavigationTabBar navigationTabBar;
    private JSONObject userInfo;

    public static int tabIndex = 0;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "FacebookLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.activity = this;
        this.db = FirebaseFirestore.getInstance();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /*for(int i = 0 ; i < 250 ; i++) {
            Map<String, Object> stringMap = new HashMap<>();

            stringMap.put("member_id", "F4E6UNusiwUKGdyeT874gLlkKt13");
            stringMap.put("content", "TestData_" + new Random().nextInt(1000));
            stringMap.put("reg_dt", new Date());

            FirebaseFirestore.getInstance().collection("say")
                    .add(stringMap)
                    .addOnSuccessListener(documentReference -> {

                        Log.d("FireStore", "Error adding document");

                    })
                    .addOnFailureListener(e -> {
                        //
                        Log.w("FireStore", "Error adding document", e);
                    });
        }

        for(int i = 0 ; i < 250 ; i++) {
            Map<String, Object> stringMap1 = new HashMap<>();

            stringMap1.put("member_id", "soTMIOa9QKM36iqewGoY4R7VWDx2");
            stringMap1.put("content", "TestData_" + new Random().nextInt(1000));
            stringMap1.put("reg_dt", new Date());

            FirebaseFirestore.getInstance().collection("say")
                    .add(stringMap1)
                    .addOnSuccessListener(documentReference -> {

                        Log.d("FireStore", "Error adding document");

                    })
                    .addOnFailureListener(e -> {
                        //
                        Log.w("FireStore", "Error adding document", e);
                    });
        }

        for(int i = 0 ; i < 250 ; i++) {
            Map<String, Object> stringMap2 = new HashMap<>();

            stringMap2.put("member_id", "u1aL13IqvvgtJfYwa7UCoqcsxjT2");
            stringMap2.put("content", "TestData_" + new Random().nextInt(1000));
            stringMap2.put("reg_dt", new Date());

            FirebaseFirestore.getInstance().collection("say")
                    .add(stringMap2)
                    .addOnSuccessListener(documentReference -> {

                        Log.d("FireStore", "Error adding document");

                    })
                    .addOnFailureListener(e -> {
                        //
                        Log.w("FireStore", "Error adding document", e);
                    });
        }

        for(int i = 0 ; i < 250 ; i++) {
            Map<String, Object> stringMap3 = new HashMap<>();

            stringMap3.put("member_id", "F4E6UNusiwUKGdyeT874gLlkKt13");
            stringMap3.put("content", "TestData_" + new Random().nextInt(1000));
            stringMap3.put("reg_dt", new Date());

            FirebaseFirestore.getInstance().collection("say")
                    .add(stringMap3)
                    .addOnSuccessListener(documentReference -> {

                        Log.d("FireStore", "Error adding document");

                    })
                    .addOnFailureListener(e -> {
                        //
                        Log.w("FireStore", "Error adding document", e);
                    });
        }*/

        setContentView(R.layout.activity_main);
        this.initUI();

        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        /*Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();*/
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign in succeeded
                /*updateUI(mAuth.getCurrentUser());*/
                addUser(this.mAuth.getCurrentUser());
            } else {
                // Sign in failed
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                /*updateUI(null);*/
            }
        }
    }

    /*@Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }*/

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
                    this.initUI();
                    /*this.navigationTabBar.setModelIndex(0, true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.contentContainer, new Say()).addToBackStack("say").commit();*/
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

    // 메인프레그먼트에서 호출되는 메소드
    public void onFragmentChange (int index) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (index) {
            case 0 :
                this.navigationTabBar.setModelIndex(0, true);
                transaction.replace(R.id.contentContainer, new Say()).addToBackStack("say").commit();
                break;

            case 1 :
                this.navigationTabBar.setModelIndex(1, true);
                transaction.replace(R.id.contentContainer, new GroupChannelListFragment()).addToBackStack("chat").commit();
                break;

            case 2:
                this.navigationTabBar.setModelIndex(2, true);
                transaction.replace(R.id.contentContainer, new Profile()).addToBackStack("profile").commit();
                break;
        }
        this.fromFragmentYn = true;
    }

    private void initUI() {

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        this.navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();

        /*models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_first),
                        Color.parseColor(colors[0]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                        .title("Home")
                        .build()
        );*/
        /*models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
                        .title("Club")
                        .build()
        );*/
        models.add(
                new NavigationTabBar.Model.Builder(
                        AppCompatResources.getDrawable(this.activity, R.drawable.ic_feed),
                        Color.parseColor(colors[0]))
                        .title("")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        AppCompatResources.getDrawable(this.activity, R.drawable.ic_chat),
                        Color.parseColor(colors[0]))
                        .title("")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        AppCompatResources.getDrawable(this.activity, R.drawable.ic_account),
                        Color.parseColor(colors[0]))
                        .title("")
                        .build()
        );

        this.navigationTabBar.setIconSizeFraction(0.40f);
        this.navigationTabBar.setModels(models);
        this.navigationTabBar.setModelIndex(this.tabIndex, true);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (tabIndex) {
            case 0:
                transaction.replace(R.id.contentContainer, new Say(), "say").commitAllowingStateLoss();
                break;

            case 1:
                transaction.replace(R.id.contentContainer, new GroupChannelListFragment(), "chat").commitAllowingStateLoss();
                break;

            case 2:
                transaction.replace(R.id.contentContainer, new Profile(), "profile").commitAllowingStateLoss();
                break;
        }

        this.navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(NavigationTabBar.Model model, int index) {

                fromFragmentYn = false;

                /*FragmentManager fragmentManager = getSupportFragmentManager();*/
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                /*switch (index) {
                    case 0 :

                        if(fragmentManager.findFragmentByTag("say") == null) {
                            fragmentManager.beginTransaction().add(R.id.contentContainer, new Say(), "say").commit();
                        } else {
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("say")).commit();
                        }

                        if(fragmentManager.findFragmentByTag("chat") != null) {
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("chat")).commit();
                        }

                        if(fragmentManager.findFragmentByTag("profile") != null) {
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("profile")).commit();
                        }

                        break;

                    default:
                        if (fUser != null) {
                            switch (index) {
                                case 1 :
                                    if(fragmentManager.findFragmentByTag("chat") == null) {
                                        fragmentManager.beginTransaction().add(R.id.contentContainer, new Chat(), "chat").commit();
                                    } else {
                                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("chat")).commit();
                                    }

                                    if(fragmentManager.findFragmentByTag("say") != null) {
                                        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("say")).commit();
                                    }

                                    if(fragmentManager.findFragmentByTag("profile") != null) {
                                        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("profile")).commit();
                                    }

                                    break;

                                case 2 :
                                    if(fragmentManager.findFragmentByTag("profile") == null) {
                                        fragmentManager.beginTransaction().add(R.id.contentContainer, new Profile(), "profile").commit();
                                    } else {
                                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("profile")).commit();
                                    }

                                    if(fragmentManager.findFragmentByTag("say") != null) {
                                        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("say")).commit();
                                    }

                                    if(fragmentManager.findFragmentByTag("chat") != null) {
                                        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("chat")).commit();
                                    }

                                    break;

                                default:
                                    break;
                            }
                        } else {
                            startActivity(new Intent(MainActivity.this, SignActivity.class));
                        }
                        break;
                }*/

                switch (index) {
                    case 0 :
                        fragmentTransaction.replace(R.id.contentContainer, new Say(), "say").commit();
                        break;

                    /*case 1 :
                            *//*transaction.replace(R.id.contentContainer, new Main()).commit();*//*
                        break;*/
                }

                switch (index) {
                    case 1 :
                        /*fragmentTransaction.replace(R.id.contentContainer, new Chat()).commit();*/
                        fragmentTransaction.replace(R.id.contentContainer, new GroupChannelListFragment(), "chat").commitAllowingStateLoss();
                        break;

                    case 2 :
                        fragmentTransaction.replace(R.id.contentContainer, new Profile(), "profile").commitAllowingStateLoss();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onEndTabSelected(NavigationTabBar.Model model, int index) {

            }
        });

        this.navigationTabBar.postDelayed(() -> {
            for (int i = 0; i < this.navigationTabBar.getModels().size(); i++) {
                final NavigationTabBar.Model model = this.navigationTabBar.getModels().get(i);
                this.navigationTabBar.postDelayed(() -> model.showBadge(), i * 100);
            }
        }, 500);
    }

    @Override
    public void onBackPressed() {
        if (this.fromFragmentYn) {
            // 이전화면
            this.fromFragmentYn = false;
            this.navigationTabBar.setModelIndex(0, true);
            getSupportFragmentManager().popBackStackImmediate("profile", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            // 종료전 확인
            if (System.currentTimeMillis() > this.backKeyPressedTime + 2000) {
                this.backKeyPressedTime = System.currentTimeMillis();
                this.showGuide();
                return;
            }

            if (System.currentTimeMillis() <= this.backKeyPressedTime + 2000) {
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                this.toast.cancel();
            }
        }
    }

    private void showGuide() {
        this.toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        this.toast.show();
    }
}
