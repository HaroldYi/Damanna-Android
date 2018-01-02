package com.hello.TrevelMeetUp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.adapter.GridViewAdapter;
import com.hello.TrevelMeetUp.vo.Photo;
import com.hello.TrevelMeetUp.fragment.UserInfo;
import com.hello.TrevelMeetUp.view.ExpandableHeightGridView;

import java.util.ArrayList;
import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class UserInfoActivity extends AppCompatActivity {

    private ExpandableHeightGridView gridView;

    private List<Photo> photoList;
    private GridViewAdapter adapter;

    private FirebaseFirestore db;

    private String uid = "";
    private String userName = "";

    private String profileUrl = "";

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_layout);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        this.uid = intent.getStringExtra("uid");
        this.userName = intent.getStringExtra("userName");

        this.profileUrl = intent.getStringExtra("profileUrl");

        /*this.bitmap = intent.getParcelableExtra("bitmapImage");*/

        initUI();
    }

    private void initUI() {

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.user_info_navi);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();

        /*models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
                        .title("Fav")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fourth),
                        Color.parseColor(colors[3]))
                        .title("Like")
                        .build()
        );*/
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fifth),
                        Color.parseColor(colors[4]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("Chat")
                        .build()
        );

        navigationTabBar.setModels(models);

        Bundle bundle = new Bundle();
        bundle.putString("uid", this.uid);
        bundle.putString("userName", this.userName);
        bundle.putString("profileUrl", this.profileUrl);

        Fragment fragment = new UserInfo();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, fragment).commit();

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(NavigationTabBar.Model model, int index) {
                switch (index) {
                    /*case 0 :
                        break;

                    case 1 :
                        break;*/

                    case 0 :
                        Intent intent1 = new Intent(getApplicationContext(), ChatRoomActivity.class);
                        intent1.putExtra("uid", uid);
                        intent1.putExtra("userName", userName);
                        intent1.putExtra("profileUrl", profileUrl);
                        startActivity(intent1);
                        break;

                    default:
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
}
