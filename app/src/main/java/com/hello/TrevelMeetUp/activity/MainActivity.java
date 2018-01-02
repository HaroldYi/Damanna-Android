package com.hello.TrevelMeetUp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.fragment.Chat;
import com.hello.TrevelMeetUp.fragment.Profile;
import com.hello.TrevelMeetUp.fragment.Say;

import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends FragmentActivity {

    private FirebaseAuth mAuth;
    private boolean fromFragmentYn = false;

    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;
    private NavigationTabBar navigationTabBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_ntb);

        this.mAuth = FirebaseAuth.getInstance();
        this.activity = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initUI();
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
                transaction.replace(R.id.contentContainer, new Chat()).addToBackStack("chat").commit();
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
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
                        .title("Say")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fourth),
                        Color.parseColor(colors[3]))
                        .title("Chat")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fifth),
                        Color.parseColor(colors[4]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("My")
                        .build()
        );

        this.navigationTabBar.setModels(models);
        this.navigationTabBar.setModelIndex(0, true);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.contentContainer, new Say(), "say").commit();

        this.navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(NavigationTabBar.Model model, int index) {

                fromFragmentYn = false;

                FirebaseUser fUser = mAuth.getCurrentUser();
                FragmentManager fragmentManager = getSupportFragmentManager();

                switch (index) {
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
                }
            }

            @Override
            public void onEndTabSelected(NavigationTabBar.Model model, int index) {

            }
        });

        navigationTabBar.postDelayed(() -> {
            for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                navigationTabBar.postDelayed(() -> model.showBadge(), i * 100);
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
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                this.backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }

            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finish();
                this.toast.cancel();
            }
        }
    }

    public void showGuide() {
        this.toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        this.toast.show();
    }
}
