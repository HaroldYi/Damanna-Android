package com.hello.holaApp.activity;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.fragment.GroupChannelListFragment;
import com.hello.holaApp.fragment.Profile;
import com.hello.holaApp.fragment.Say;

import org.json.JSONObject;

import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser fUser;
    private FirebaseFirestore db;
    private boolean fromFragmentYn = false;

    private long backKeyPressedTime = 0;
    private Toast toast;

    public static Activity activity;
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
        this.fUser = FirebaseAuth.getInstance().getCurrentUser();

        Log.d("Created", "Called onCreate");

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        CommonFunction.sendMsg(getApplicationContext());

        setContentView(R.layout.activity_main);
        this.initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign in succeeded
                *//*updateUI(mAuth.getCurrentUser());*//*
                addUser(this.mAuth.getCurrentUser());
            } else {
                // Sign in failed
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                *//*updateUI(null);*//*
            }
        }*/
    }

    /*@Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }*/

    private void initUI() {

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        this.navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();

        /*models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_first),
                        Color.parseColor(colors[0]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                        .title("People")
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
                        .badgeTitle("1")
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