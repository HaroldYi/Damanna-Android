package com.hello.TrevelMeetUp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.adapter.GridViewAdapter;
import com.hello.TrevelMeetUp.adapter.SayListViewAdapter;
import com.hello.TrevelMeetUp.adapter.UserSayListViewAdapter;
import com.hello.TrevelMeetUp.common.BaseApplication;
import com.hello.TrevelMeetUp.common.CommonFunction;
import com.hello.TrevelMeetUp.common.RadiusImageView;
import com.hello.TrevelMeetUp.common.VolleySingleton;
import com.hello.TrevelMeetUp.view.ExpandableHeightListView;
import com.hello.TrevelMeetUp.vo.Photo;
import com.hello.TrevelMeetUp.fragment.UserInfo;
import com.hello.TrevelMeetUp.view.ExpandableHeightGridView;
import com.hello.TrevelMeetUp.vo.SayVo;
import com.meg7.widget.CircleImageView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class UserInfoActivity extends AppCompatActivity implements MaterialTabListener {

    private static String TAG = "cloudFireStore";

    private ExpandableHeightListView listView;
    private List<SayVo> sayVoList;
    private List<Photo> photoList;

    private ExpandableHeightGridView gridView;
    private SayListViewAdapter userSayListViewAdapter;

    private ImageLoader imageLoader;

    private FirebaseAuth mAuth;

    private View view;

    private MaterialTabHost tabHost;

    private CardView photoListView;
    private ScrollView sayListView;

    private int selectedColour = Color.rgb(3, 196, 201);
    private int unSelectedColour = Color.rgb(176, 176, 176);

    private int position;
    private String userName;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAuth = FirebaseAuth.getInstance();
        BaseApplication.getInstance().progressON(this, getResources().getString(R.string.loading));

        setContentView(R.layout.user_info_layout);

        this.view = findViewById(R.id.profile_layout_sc);
        this.view.setVisibility(View.INVISIBLE);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.new_say_action_bar, null);
        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText("Profile");

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/NotoSans-Medium.ttf");
        title.setTypeface(typeface);

        actionBar.setCustomView(actionView);

        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setVisibility(View.GONE);

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view1 -> finish());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();

        this.uid = intent.getStringExtra("uid");
        this.userName = intent.getStringExtra("userName");
        String profileUrl = intent.getStringExtra("profileUrl");

        this.photoListView = (CardView) view.findViewById(R.id.photo_list_view);
        this.sayListView = (ScrollView) view.findViewById(R.id.say_list_view);

        this.gridView = (ExpandableHeightGridView) findViewById(R.id.photo_list);
        this.gridView.setExpanded(true);
        this.gridView.setVisibility(View.INVISIBLE);

        /*Bitmap bitmap = CommonFunction.getBitmapFromURL(profileUrl);*/

        RadiusImageView imageView = (RadiusImageView) findViewById(R.id.user_profile_photo);
        imageView = (RadiusImageView) findViewById(R.id.user_profile_photo);
        imageView.setRadius(25f);
        /*imageView.setOnClickListener(view1 -> {
            viewPhoto( profileBitmap);
        });*/

        this.imageLoader = VolleySingleton.getInstance(this).getImageLoader();

        imageView.bringToFront();
        imageView.setImageUrl(profileUrl, this.imageLoader);

        /*imageView.setImageBitmap(bitmap);*/
        imageView.bringToFront();

        TextView textView = (TextView) findViewById(R.id.user_profile_name);
        textView.setText(userName);

        this.tabHost = (MaterialTabHost) findViewById(R.id.tabHost);

        // init view pager
        String[] tabList = getResources().getStringArray(R.array.tab_list);

        // insert all tabs from pagerAdapter data
        for (int i = 0; i < tabList.length ; i++) {

            MaterialTab tab = this.tabHost.newTab()
                    .setText(tabList[i])
                    .setTabListener(this);

            tab.setTextColor((i == 0 ? this.selectedColour : this.unSelectedColour));

            this.tabHost.addTab(tab);
        }

        this.tabHost.setSelectedNavigationItem(0);
        this.tabHost.getCurrentTab().setTextColor(this.selectedColour);

        Button chatBtn = (Button) findViewById(R.id.chat_btn);
        chatBtn.setOnClickListener(view -> {
            Intent intent1 = new Intent(getApplicationContext(), ChatRoomActivity.class);
            intent1.putExtra("uid", this.uid);
            intent1.putExtra("userName", this.userName);
            intent1.putExtra("profileUrl", profileUrl);
            startActivity(intent1);

            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        this.listView = (ExpandableHeightListView) findViewById(R.id.say_list);
        this.listView.setExpanded(true);
        this.listView.setVisibility(View.INVISIBLE);

        this.photoList = new ArrayList<>();
        this.sayVoList = new ArrayList<>();

        db.collection("photo/")
                .whereEqualTo("member_id", this.uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (DocumentSnapshot document : task.getResult()) {
                            Photo p = new Photo();
                            p.setFileName(document.getData().get("fileName").toString());
                            p.setKind("photo");
                            this.photoList.add(p);
                        }

                        GridViewAdapter adapter = new GridViewAdapter(this, this.photoList, true);
                        this.gridView.setAdapter(adapter);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        db.collection("say/")
                .whereEqualTo("member_id", this.uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        this.userSayListViewAdapter = new SayListViewAdapter(this, this.sayVoList);
                        this.listView.setAdapter(userSayListViewAdapter);

                        for (DocumentSnapshot document : task.getResult()) {

                            db.collection("member/")
                                    .whereEqualTo("id", this.uid)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            if(task1.getResult().size() > 0) {
                                                for (DocumentSnapshot document1 : task1.getResult()) {

                                                    DateTime dateTime = new DateTime();

                                                    String gender = "";

                                                    if(document1.getData().get("gender") != null) {
                                                        gender = (gender.equals("male") ? "남자" : "여자");
                                                    } else {
                                                        gender = "성별 미입력";
                                                    }

                                                    SayVo sayVo = new SayVo();

                                                    sayVo.setUserName(document1.getData().get("name").toString());
                                                    sayVo.setPhotoUrl(document1.getData().get("profileUrl").toString());
                                                    sayVo.setMsg(document.getData().get("content").toString());
                                                    sayVo.setNoMsg(false);

                                                    TextView ageView = (TextView) findViewById(R.id.age);

                                                    if(document1.getData().get("dateOfBirth") != null) {
                                                        long dateOfBirth = ((Date) document1.getData().get("dateOfBirth")).getTime();
                                                        long now = System.currentTimeMillis();

                                                        Calendar birthCalendar = Calendar.getInstance();
                                                        birthCalendar.setTimeInMillis(dateOfBirth);

                                                        int yearOfBirth = birthCalendar.get(Calendar.YEAR);

                                                        Calendar nowCalender = Calendar.getInstance();
                                                        nowCalender.setTimeInMillis(now);

                                                        int nowYear = nowCalender.get(Calendar.YEAR);

                                                        int koreanAge = nowYear - yearOfBirth + 1;

                                                        String age = String.format("%d세, %s", koreanAge, gender);
                                                        ageView.setText(age);
                                                    } else if(document1.getData().get("dateOfBirth") == null ) {
                                                        String age = String.format("%s, %s", "나이 미입력", gender);
                                                        ageView.setText(age);
                                                    }

                                                    if (document1.getData().get("identity") != null
                                                            && document1.getData().get("nation") != null) {
                                                        String identity = document1.getData().get("identity").toString();
                                                        String nation = document1.getData().get("nation").toString();

                                                        nation = String.format("%s, %s", nation, identity);
                                                        TextView identityView = (TextView) findViewById(R.id.identity);
                                                        identityView.setText(nation);

                                                        sayVo.setIdentity(document1.getData().get("identity").toString());
                                                        sayVo.setNation(document1.getData().get("nation").toString());
                                                    }

                                                    this.sayVoList.add(sayVo);

                                                    /*new Handler().postDelayed(() -> {
                                                        progressOFF();
                                                        view.setVisibility(View.VISIBLE);
                                                        this.gridView.setVisibility(View.VISIBLE);
                                                        this.listView.setVisibility(View.VISIBLE);
                                                    }, 100);*/
                                                }
                                            } else {
                                                SayVo sayVo = new SayVo();
                                                sayVo.setMsg("등록된 내용이 없습니다.");
                                                sayVo.setNoMsg(true);
                                                this.sayVoList.add(sayVo);
                                            }

                                            BaseApplication.getInstance().progressOFF();
                                            this.view.setVisibility(View.VISIBLE);
                                            this.gridView.setVisibility(View.VISIBLE);
                                            this.listView.setVisibility(View.VISIBLE);
                                            this.userSayListViewAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.w(TAG, "Error getting documents.", task1.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        this.gridView.setOnItemClickListener((parent, v, position, id) -> {
            /*Intent viewIntent = new Intent(getActivity(), ViewPhotoActivity.class);
            viewIntent.putExtra("photoUrl", photoList.get(position).getFileName());
            startActivityForResult(viewIntent, 1);*/
            this.position = position;
            viewPhoto(null);
        });
    }

    @Override
    public void onTabSelected(MaterialTab tab) {
        // when the tab is clicked the pager swipe content to the tab position
        this.tabHost.getCurrentTab().setTextColor(this.unSelectedColour);
        this.tabHost.setSelectedNavigationItem(tab.getPosition());
        this.tabHost.getCurrentTab().setTextColor(this.selectedColour);

        switch (tab.getPosition()) {
            case 0 :
                this.sayListView.setVisibility(View.GONE);
                this.photoListView.setVisibility(View.VISIBLE);
                break;

            case 1 :
                this.sayListView.setVisibility(View.VISIBLE);
                this.photoListView.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    @Override
    public void onTabReselected(MaterialTab tab) {

    }

    @Override
    public void onTabUnselected(MaterialTab tab) {

    }

    private void viewPhoto(Bitmap bitmap) {
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, ViewPhotoActivity.class);
        if(bitmap != null) {
            intent.putExtra("bitmap", bitmap);
        }
        intent.putExtra("photoUrl", this.photoList.get(this.position).getFileName());
        intent.putExtra("userName", this.userName);
        startActivityForResult(intent, 1);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
