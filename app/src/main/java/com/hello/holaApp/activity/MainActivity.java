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

import com.crashlytics.android.Crashlytics;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.fragment.GroupChannelListFragment;
import com.hello.holaApp.fragment.People;
import com.hello.holaApp.fragment.Profile;
import com.hello.holaApp.fragment.Say;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBirdException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity {

    private boolean fromFragmentYn = false;

    private long backKeyPressedTime = 0;
    private Toast toast;

    public static Activity activity;
    public static NavigationTabBar navigationTabBar;
    private JSONObject userInfo;

    public static int tabIndex = 0;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "FacebookLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.activity = this;

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
        models.add(
                new NavigationTabBar.Model.Builder(
                        AppCompatResources.getDrawable(this.activity, R.drawable.ic_feed),
                        Color.parseColor(colors[0]))
                        .badgeTitle("")
                        .title("")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        AppCompatResources.getDrawable(this.activity, R.drawable.ic_fa_users),
                        Color.parseColor(colors[0]))
                        .badgeTitle("")
                        .title("")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        AppCompatResources.getDrawable(this.activity, R.drawable.ic_chat),
                        Color.parseColor(colors[0]))
                        .badgeTitle("")
                        .title("")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        AppCompatResources.getDrawable(this.activity, R.drawable.ic_account),
                        Color.parseColor(colors[0]))
                        .badgeTitle("")
                        .title("")
                        .build()
        );

        this.navigationTabBar.setIsBadged(true);
        this.navigationTabBar.setBadgeBgColor(Color.RED);
        this.navigationTabBar.setIconSizeFraction(0.55f);
        this.navigationTabBar.setBadgeGravity(NavigationTabBar.BadgeGravity.TOP);
        this.navigationTabBar.setBadgePosition(NavigationTabBar.BadgePosition.RIGHT);
        this.navigationTabBar.setModels(models);
        this.navigationTabBar.setBadgeSize(20);
        this.navigationTabBar.setBadgeTitleColor(Color.WHITE);
        this.navigationTabBar.setModelIndex(this.tabIndex, true);

        GroupChannelListQuery mChannelListQuery = GroupChannel.createMyGroupChannelListQuery();

        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    return;
                }

                /*final NavigationTabBar.Model model = MainActivity.navigationTabBar.getModels().get(2);
                if(list.size() > 0) {

                    int unReadCnt = 0;
                    for(GroupChannel channel : list) {
                        unReadCnt += channel.getUnreadMessageCount();
                    }

                    if(unReadCnt > 0) {
                        model.updateBadgeTitle(String.valueOf(unReadCnt));
                        model.showBadge();
                    } else {
                        model.hideBadge();
                    }

                } else {
                    model.hideBadge();
                }*/
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (tabIndex) {

            case 0:
                tabIndex = 0;
                transaction.replace(R.id.contentContainer, new Say(), "say").commitAllowingStateLoss();
                break;

            case 1:
                tabIndex = 1;
                transaction.replace(R.id.contentContainer, new People(), "people").commitAllowingStateLoss();
                break;

            case 2:
                tabIndex = 2;
                transaction.replace(R.id.contentContainer, new GroupChannelListFragment(), "chat").commitAllowingStateLoss();
                break;

            case 3:
                tabIndex = 3;
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
                        tabIndex = 0;
                        fragmentTransaction.replace(R.id.contentContainer, new Say(), "say").commit();
                        break;

                    /*case 1 :
                            *//*transaction.replace(R.id.contentContainer, new Main()).commit();*//*
                        break;*/
                }

                switch (index) {
                    case 1 :
                        tabIndex = 1;
                        /*fragmentTransaction.replace(R.id.contentContainer, new Chat()).commit();*/
                        fragmentTransaction.replace(R.id.contentContainer, new People(), "people").commitAllowingStateLoss();
                        break;

                    case 2 :
                        tabIndex = 2;
                        /*fragmentTransaction.replace(R.id.contentContainer, new Chat()).commit();*/
                        fragmentTransaction.replace(R.id.contentContainer, new GroupChannelListFragment(), "chat").commitAllowingStateLoss();
                        break;

                    case 3 :
                        tabIndex = 3;
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
        this.toast = Toast.makeText(activity, getResources().getString(R.string.press_backbtn_again), Toast.LENGTH_SHORT);
        this.toast.show();
    }
}
