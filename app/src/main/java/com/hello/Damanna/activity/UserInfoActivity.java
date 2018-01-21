package com.hello.Damanna.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hello.Damanna.R;
import com.hello.Damanna.adapter.GridViewAdapter;
import com.hello.Damanna.adapter.NewSayListViewAdapter;
import com.hello.Damanna.adapter.SayListViewAdapter;
import com.hello.Damanna.common.BaseApplication;
import com.hello.Damanna.common.RadiusNetworkImageView;
import com.hello.Damanna.common.VolleySingleton;
import com.hello.Damanna.view.ExpandableHeightListView;
import com.hello.Damanna.vo.Photo;
import com.hello.Damanna.view.ExpandableHeightGridView;
import com.hello.Damanna.vo.SayVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class UserInfoActivity extends AppCompatActivity implements MaterialTabListener {

    private static String TAG = "cloudFireStore";

    private UltimateRecyclerView listView;
    private LinearLayoutManager linearLayoutManager;
    private List<SayVo> sayVoList;
    private List<Photo> photoList;

    private ExpandableHeightGridView gridView;
    private NewSayListViewAdapter userSayListViewAdapter;

    private ImageLoader imageLoader;

    private FirebaseAuth mAuth;

    private View view;

    private MaterialTabHost tabHost;

    private CardView photoListView;

    private int selectedColour = Color.rgb(3, 196, 201);
    private int unSelectedColour = Color.rgb(176, 176, 176);

    private int position;
    private String userName;
    private String uid;

    private final int limit = 5;
    private Query query;

    private FirebaseFirestore db;

    private Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.activity = this;

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

        this.db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();

        this.uid = intent.getStringExtra("uid");
        this.userName = intent.getStringExtra("userName");
        String profileUrl = intent.getStringExtra("profileUrl");

        this.photoListView = (CardView) view.findViewById(R.id.photo_list_view);

        this.gridView = (ExpandableHeightGridView) findViewById(R.id.photo_list);
        this.gridView.setExpanded(true);
        this.gridView.setVisibility(View.INVISIBLE);

        /*Bitmap bitmap = CommonFunction.getBitmapFromURL(profileUrl);*/

        RadiusNetworkImageView imageView = (RadiusNetworkImageView) findViewById(R.id.user_profile_photo);
        imageView = (RadiusNetworkImageView) findViewById(R.id.user_profile_photo);
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

        this.photoList = new ArrayList<>();
        this.sayVoList = new ArrayList<>();

        this.listView = (UltimateRecyclerView) findViewById(R.id.say_list);
        this.linearLayoutManager = new LinearLayoutManager(this);
        this.listView.setLayoutManager(this.linearLayoutManager);
        this.userSayListViewAdapter = new NewSayListViewAdapter(this, this.sayVoList);
        this.listView.setAdapter(this.userSayListViewAdapter);
        this.listView.setHasFixedSize(false);

        this.listView.reenableLoadmore();
        this.listView.setLoadMoreView(LayoutInflater.from(this)
                .inflate(R.layout.custom_bottom_progressbar, null));

        this.listView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                        BaseApplication.getInstance().progressON(activity, getResources().getString(R.string.loading));

                        loadingData(query);
                        // linearLayoutManager.scrollToPositionWithOffset(maxLastVisiblePosition,-1);
                        //   linearLayoutManager.scrollToPosition(maxLastVisiblePosition);

                    }
                }, 1000);
            }
        });
        this.listView.setDefaultOnRefreshListener(() -> new Handler().postDelayed(() -> {

            userSayListViewAdapter.clear();
            sayVoList.clear();
            this.query = db.collection("say/")
                    .whereEqualTo("member_id", this.uid)
                /*.orderBy("reg_dt", Query.Direction.DESCENDING)*/
                    .limit(this.limit);

            loadingData(query);
        }, 1000));

        this.db.collection("photo/")
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

        this.query = db.collection("say/")
                .whereEqualTo("member_id", this.uid)
                /*.orderBy("reg_dt", Query.Direction.DESCENDING)*/
                .limit(this.limit);

        loadingData(this.query);

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
                this.listView.setVisibility(View.VISIBLE);
                this.photoListView.setVisibility(View.GONE);
                break;

            case 1 :
                this.listView.setVisibility(View.GONE);
                this.photoListView.setVisibility(View.VISIBLE);
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

    private void loadingData(Query queryParam) {

        queryParam
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        int size = task.getResult().size();
                        DocumentSnapshot last = null;

                        last = task.getResult().getDocuments().get(size - 1);

                        query = db.collection("say/")
                                .orderBy("reg_dt", Query.Direction.DESCENDING)
                                .startAfter(last)
                                .limit(limit);

                        if(size < limit) {
                            this.listView.disableLoadmore();
                        }

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

                                                    this.userSayListViewAdapter.insert(sayVo, this.userSayListViewAdapter.getAdapterItemCount());

                                                    new Handler().postDelayed(() -> {
                                                        BaseApplication.getInstance().progressOFF();
                                                        view.setVisibility(View.VISIBLE);
                                                        this.gridView.setVisibility(View.VISIBLE);
                                                        this.listView.setVisibility(View.VISIBLE);
                                                    }, 100);
                                                }
                                            } else {
                                                SayVo sayVo = new SayVo();
                                                sayVo.setMsg("등록된 내용이 없습니다.");
                                                sayVo.setNoMsg(true);
                                                this.userSayListViewAdapter.insert(sayVo, this.userSayListViewAdapter.getAdapterItemCount());
                                            }

                                            BaseApplication.getInstance().progressOFF();
                                            this.view.setVisibility(View.VISIBLE);
                                            this.gridView.setVisibility(View.VISIBLE);
                                            this.listView.setVisibility(View.VISIBLE);
                                        } else {
                                            Log.w(TAG, "Error getting documents.", task1.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        /*queryParam
                .whereEqualTo("member_id", this.uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        int size = task.getResult().size();
                        DocumentSnapshot last = null;

                        if(size > 0) {

                            last = task.getResult().getDocuments().get(size - 1);

                            query = db.collection("say/")
                                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                                    .startAfter(last)
                                    .limit(limit);

                            if(size < limit) {
                                this.listView.disableLoadmore();
                            }

                            for (DocumentSnapshot document : task.getResult()) {

                                long now = System.currentTimeMillis();
                                long regDt = ((Date)document.getData().get("reg_dt")).getTime();
                                long regTime = (now - regDt) / 60000;

                                if(regTime < 60) {
                                    this.regMin = String.format("%dmin", regTime);
                                } else if(regTime >= 60 && regTime < 1440) {
                                    this.regMin = String.format("%dh", (int)(regTime / 60));
                                } else if(regTime > 1440) {
                                    this.regMin = String.format("%dd", (int)(regTime / 1440));
                                }

                                SayVo sayVo = new SayVo();
                                sayVo.setUserName(this.user.getDisplayName());
                                sayVo.setMsg(document.getData().get("content").toString());
                                sayVo.setPhotoUrl(this.user.getPhotoUrl().toString());
                                sayVo.setDistance(this.regMin);
                                sayVo.setNoMsg(false);

                                this.userSayListViewAdapter.insert(sayVo, this.userSayListViewAdapter.getAdapterItemCount());
                            }

                        } else {
                            SayVo sayVo = new SayVo();
                            sayVo.setMsg("등록된 내용이 없습니다.");
                            sayVo.setNoMsg(true);

                            this.userSayListViewAdapter.insert(sayVo, this.userSayListViewAdapter.getAdapterItemCount());
                        }

                        new Handler().postDelayed(() -> {
                            progressOFF();
                            this.view.setVisibility(View.VISIBLE);
                            this.listView.setVisibility(View.VISIBLE);
                            this.gridView.setVisibility(View.VISIBLE);
                        }, 150);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });*/
    }
}
