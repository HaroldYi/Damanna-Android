package com.hello.holaApp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
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
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.hello.holaApp.R;
import com.hello.holaApp.adapter.NewRecyclerGridViewAdapter;
import com.hello.holaApp.adapter.NewSayListViewAdapter;
import com.hello.holaApp.common.BaseApplication;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.common.EqualSpacingItemDecoration;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.vo.PhotoVo;
import com.hello.holaApp.vo.SayVo;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.RecyclerItemClickListener;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.grid.BasicGridLayoutManager;

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
    private BasicGridLayoutManager basicGridLayoutManager;
    private List<SayVo> sayVoList;
    private List<PhotoVo> photoVoList;

    private UltimateRecyclerView gridView;
    private NewSayListViewAdapter userSayListViewAdapter;
    private NewRecyclerGridViewAdapter adapter;

    private ImageLoader imageLoader;

    private FirebaseAuth mAuth;

    private UserVo userVo;

    private View view;

    private MaterialTabHost tabHost;

    private CardView photoListView;

    private int selectedColour = Color.rgb(3, 196, 201);
    private int unSelectedColour = Color.rgb(176, 176, 176);

    private int position;
    private String userName;
    private String uid;

    private int moreNum = 2, columns = 2;
    private final int limit = 10;
    private Query sayQuery;
    private Query photoQuery;

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
        String identity = intent.getStringExtra("identity");
        String profileUrl = intent.getStringExtra("profileUrl");

        title.setText(String.format("%s의 프로필", this.userName));

        /*Bitmap bitmap = CommonFunction.getBitmapFromURL(profileUrl);*/

        RadiusNetworkImageView imageView = (RadiusNetworkImageView) findViewById(R.id.user_profile_photo);
        imageView = (RadiusNetworkImageView) findViewById(R.id.user_profile_photo);
        imageView.setRadius(25f);
        imageView.setOnClickListener(view1 -> {
            viewPhoto(profileUrl, "jpg");
        });

        this.imageLoader = VolleySingleton.getInstance(this).getImageLoader();

        imageView.bringToFront();
        imageView.setImageUrl(profileUrl, this.imageLoader);

        /*imageView.setImageBitmap(bitmap);*/
        imageView.bringToFront();

        TextView textView = (TextView) findViewById(R.id.user_profile_name);
        textView.setText(userName);
        textView.setTypeface(typeface);

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
            intent1.putExtra("senderName", String.format("%s(%s)", this.userName, identity));
            intent1.putExtra("profileUrl", profileUrl);
            intent1.putExtra("userInfoYn", true);
            startActivity(intent1);

            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        this.photoVoList = new ArrayList<>();
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

                        /*BaseApplication.getInstance().progressON(activity, getResources().getString(R.string.loading));*/

                        loadingSayData(sayQuery);
                        // linearLayoutManager.scrollToPositionWithOffset(maxLastVisiblePosition,-1);
                        //   linearLayoutManager.scrollToPosition(maxLastVisiblePosition);

                    }
                }, 1000);
            }
        });

        /*this.listView.setDefaultOnRefreshListener(() -> new Handler().postDelayed(() -> {

            userSayListViewAdapter.clear();
            sayVoList.clear();
            this.sayQuery = db.collection("say/")
                    .whereEqualTo("member_id", this.uid)
                *//*.orderBy("reg_dt", Query.Direction.DESCENDING)*//*
                    .limit(this.limit);

            loadingSayData(sayQuery);
        }, 1000));*/

        this.photoListView = (CardView) view.findViewById(R.id.photo_list_view);

        this.gridView = (UltimateRecyclerView) findViewById(R.id.photo_list);
        this.gridView.addItemDecoration(new EqualSpacingItemDecoration(6, EqualSpacingItemDecoration.GRID));
        this.gridView.setVisibility(View.INVISIBLE);

        this.gridView.setHasFixedSize(true);
        this.gridView.setSaveEnabled(true);
        this.gridView.setClipToPadding(false);

        this.adapter = new NewRecyclerGridViewAdapter(this, this.photoVoList, false);
        this.adapter.setSpanColumns(2);
        this.gridView.setAdapter(this.adapter);
        this.basicGridLayoutManager = new BasicGridLayoutManager(this, this.columns, this.adapter);

        this.gridView.setLayoutManager(this.basicGridLayoutManager);

        this.gridView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int index) {
                        viewPhoto(photoVoList.get(index).getOriginalUrl(), "jpg");
                    }
                })
        );

        this.gridView.reenableLoadmore();

        this.gridView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                loadingPhotoData(photoQuery, true);
            }
        });

        /*this.gridView.setDefaultOnRefreshListener(() -> new Handler().postDelayed(() -> {

            adapter.clear();
            photoVoList.clear();
            this.photoQuery = this.db.collection("photo/")
                    .whereEqualTo("member_id", this.uid)
                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                    .limit(this.limit);

            loadingPhotoData(photoQuery, false);
        }, 1000));*/

        this.photoQuery = this.db.collection("photo/")
                .whereEqualTo("member_id", this.uid)
                .orderBy("reg_dt", Query.Direction.DESCENDING)
                .limit(this.limit);

        loadingPhotoData(this.photoQuery, false);

        userVo = new UserVo();

        db.collection("member/")
                .document(this.uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());

                            userVo.setUserName(task.getResult().getData().get("name").toString());
                            userVo.setNation(task.getResult().getData().get("nation").toString());
                            userVo.setGender(task.getResult().getData().get("gender").toString());
                            userVo.setPhotoUrl(task.getResult().getData().get("profileUrl").toString());
                            userVo.setIdentity(task.getResult().getData().get("identity").toString());

                            if (task.getResult().getData().get("dateOfBirth") != null) {
                                long dateOfBirth = ((Date) task.getResult().getData().get("dateOfBirth")).getTime();
                                long now = System.currentTimeMillis();

                                Calendar birthCalendar = Calendar.getInstance();
                                birthCalendar.setTimeInMillis(dateOfBirth);

                                int yearOfBirth = birthCalendar.get(Calendar.YEAR);

                                Calendar nowCalender = Calendar.getInstance();
                                nowCalender.setTimeInMillis(now);

                                int nowYear = nowCalender.get(Calendar.YEAR);

                                int koreanAge = nowYear - yearOfBirth + 1;

                                userVo.setAge(koreanAge);
                            } else if (task.getResult().getData().get("dateOfBirth") == null) {
                                userVo.setAge(1);
                            }

                            String age = (userVo.getAge() != 1 ? String.format("%d세", userVo.getAge()) : "나이미입력");

                            String gender = userVo.getGender();

                            if (gender != null && !gender.isEmpty()) {
                                gender = (gender.equals("male") ? getResources().getString(R.string.male) : getResources().getString(R.string.female));
                            } else {
                                gender = "성별 미입력";
                            }

                            TextView ageView = (TextView) findViewById(R.id.age);
                            ageView.setText(String.format("%s, %s", age, gender));

                            String identity1 = userVo.getIdentity();
                            String nation = userVo.getNation();
                            if (identity1 != null && nation != null) {

                                nation = String.format("%s, %s", nation, identity1);
                                TextView identityView = (TextView) findViewById(R.id.identity);
                                identityView.setText(nation);

                            }

                            GeoPoint geoPoint = (GeoPoint)task.getResult().getData().get("location");
                            userVo.setGeoPoint(geoPoint);

                            this.sayQuery = db.collection("say/")
                                    .whereEqualTo("member_id", this.uid)
                                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                                    .limit(this.limit);

                            loadingSayData(this.sayQuery);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Crashlytics.logException(task.getException());
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        /*this.gridView.setOnItemClickListener((parent, v, position, id) -> {
            *//*Intent viewIntent = new Intent(getActivity(), ViewPhotoActivity.class);
            viewIntent.putExtra("photoUrl", photoVoList.get(position).getFileName());
            startActivityForResult(viewIntent, 1);*//*
            this.position = position;
            viewPhoto(null);
        });*/
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
        intent.putExtra("photoUrl", this.photoVoList.get(this.position).getFileName());
        intent.putExtra("userName", this.userName);
        startActivityForResult(intent, 1);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void viewPhoto(String url, String type) {
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, PhotoViewerActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("type", type);
        startActivityForResult(intent, 1);
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void loadingPhotoData(Query queryParam, boolean loadMoreYn) {
        queryParam
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        int size = task.getResult().size();
                        DocumentSnapshot last = null;

                        if (size > 0) {

                            last = task.getResult().getDocuments().get(size - 1);

                            this.photoQuery = db.collection("photo/")
                                    .whereEqualTo("member_id", this.uid)
                                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                                    .startAfter(last)
                                    .limit(this.limit);

                            if(size < this.limit) {
                                this.gridView.disableLoadmore();
                            }

                            for (DocumentSnapshot document : task.getResult()) {

                                PhotoVo photoVo = new PhotoVo();
                                photoVo.setPhotoId(document.getString("id"));
                                photoVo.setThumbnailUrl(document.getData().get("thumbnail_img").toString());
                                photoVo.setOriginalUrl(document.getData().get("original_img").toString());
                                photoVo.setKind("photo");
                                this.adapter.insertLast(photoVo);

                                /*StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("original/" + document.getData().get("fileName").toString() + ".jpg");

                                islandRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                                    //do something with downloadurl
                                    photoVo.setFileUrl(downloadUrl.toString());
                                }).addOnFailureListener(e -> {
                                    Log.d("에러~", e.getMessage());
                                });*/
                            }
                        }
                    } else {
                        Crashlytics.logException(task.getException());
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void loadingSayData(Query queryParam) {

        queryParam
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        int size = task.getResult().size();
                        if (size > 0) {
                            DocumentSnapshot last = null;

                            last = task.getResult().getDocuments().get(size - 1);

                            sayQuery = db.collection("say/")
                                    .whereEqualTo("member_id", this.uid)
                                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                                    .startAfter(last)
                                    .limit(limit);

                            if (size < limit) {
                                this.listView.disableLoadmore();
                            }

                            for (DocumentSnapshot document : task.getResult()) {

                                SayVo sayVo = new SayVo();

                                sayVo.setUserName(userVo.getUserName());
                                sayVo.setPhotoUrl(userVo.getPhotoUrl());
                                sayVo.setMsg(document.getData().get("content").toString());
                                sayVo.setIdentity(userVo.getIdentity());
                                sayVo.setNation(userVo.getNation());
                                sayVo.setNoMsg(false);

                                long regDt = document.getDate("reg_dt").getTime();
                                long now = System.currentTimeMillis();

                                long regTime = (now - regDt) / 60000;

                                String regMin = "";
                                if(regTime < 60) {
                                    regMin = String.format("%dmin", regTime);
                                } else if(regTime >= 60 && regTime < 1440) {
                                    regMin = String.format("%dh", (int)(regTime / 60));
                                } else if(regTime > 1440) {
                                    regMin = String.format("%dd", (int)(regTime / 1440));
                                }

                                GeoPoint geoPoint = userVo.getGeoPoint();
                                GeoPoint myGeoPoint = new GeoPoint(CommonFunction.getLatitude(), CommonFunction.getLongitude());

                                Location loc = new Location("pointA");
                                Location loc1 = new Location("pointB");

                                loc.setLatitude(geoPoint.getLatitude());
                                loc.setLongitude(geoPoint.getLongitude());

                                loc1.setLatitude(myGeoPoint.getLatitude());
                                loc1.setLongitude(myGeoPoint.getLongitude());

                                String distance = String.format("%.2fkm", (loc.distanceTo(loc1) / 1000));
                                sayVo.setDistance(String.format("%s / %s", regMin, distance));

                                this.userSayListViewAdapter.insert(sayVo, this.userSayListViewAdapter.getAdapterItemCount());
                            }
                        } else {
                            /*Crashlytics.logException(task.getException());
                            Log.w(TAG, "Error getting documents.", task.getException());*/
                            this.listView.disableLoadmore();
                        }

                        new Handler().postDelayed(() -> {
                            BaseApplication.getInstance().progressOFF();
                            view.setVisibility(View.VISIBLE);
                            this.gridView.setVisibility(View.VISIBLE);
                            this.listView.setVisibility(View.VISIBLE);
                        }, 100);
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

                            sayQuery = db.collection("say/")
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
