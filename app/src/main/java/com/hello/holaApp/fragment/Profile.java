package com.hello.holaApp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hello.holaApp.R;
import com.hello.holaApp.activity.MainActivity;
import com.hello.holaApp.activity.PhotoViewerActivity;
import com.hello.holaApp.activity.PopupActivity;
import com.hello.holaApp.activity.SettingActivity;
import com.hello.holaApp.activity.ViewPhotoActivity;
import com.hello.holaApp.adapter.NewRecyclerGridViewAdapter;
import com.hello.holaApp.adapter.UserInfoSayListViewAdapter;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.common.Constant;
import com.hello.holaApp.common.EqualSpacingItemDecoration;
import com.hello.holaApp.common.RadiusImageButton;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.vo.PhotoVo;
import com.hello.holaApp.vo.SayVo;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.grid.BasicGridLayoutManager;
import com.sendbird.android.SendBird;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

/**
 * Created by lji5317 on 05/12/2017.
 */

public class Profile extends BaseFragment implements View.OnClickListener, MaterialTabListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private Bitmap profileBitmap;

    private UltimateRecyclerView gridView;
    private String kind;
    private int position;

    public List<PhotoVo> photoVoList;
    private NewRecyclerGridViewAdapter adapter;
    private RadiusImageButton profileCameraBtn;
    private RadiusNetworkImageView profileImageView;

    private FirebaseFirestore db;

    private UltimateRecyclerView listView;
    private List<SayVo> sayVoList;
    private UserInfoSayListViewAdapter userSayListViewAdapter;

    private Button button;
    private int lastIndex;

    private ImageLoader imageLoader;

    private MaterialTabHost tabHost;

    private CardView photoListView;

    private String regMin = "";

    private final int limit = 5;
    private Query sayQuery;
    private Query photoQuery;

    private View view;
    private static Activity activity;
    private LinearLayoutManager linearLayoutManager;
    private BasicGridLayoutManager basicGridLayoutManager;

    private static boolean profileYn = false;
    private boolean lastYn = false;

    private int selectedColour = Color.rgb(3, 196, 201);
    private int unSelectedColour = Color.rgb(176, 176, 176);

    private int moreNum = 2, columns = 2;

    private static Profile.ProfileCamera cameraProfile;

    private static String TAG = "cloudFireStore";

    public Profile() {
        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();
        // Access a Cloud Firestore instance from your Activity
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view) {

        /*switch (view.getId()) {
            case R.id.take:
                takePhoto();
                break;

            case R.id.choose:
                selectPhoto();
                break;
        }*/
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = getActivity();
        this.cameraProfile = new ProfileCamera();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).tabIndex = 3;

        progressON(getResources().getString(R.string.loading));

        this.view = inflater.inflate(R.layout.profile_layout, container, false);
        this.view.setVisibility(View.INVISIBLE);

        this.tabHost = (MaterialTabHost) view.findViewById(R.id.tabHost);

        this.photoListView = (CardView) view.findViewById(R.id.photo_list_view);

        // init view pager
        String[] tabList = getResources().getStringArray(R.array.tab_list);

        // insert all tabs from pagerAdapter data
        for (int i = 0; i < tabList.length ; i++) {

            MaterialTab tab = this.tabHost.newTab()
                    .setText(tabList[i])
                    .setTabListener(this);

            this.tabHost.addTab(tab);
        }

        this.tabHost.setSelectedNavigationItem(0);
        this.tabHost.getCurrentTab().setTextColor(this.selectedColour);

        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.activity_action_bar, null);
        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText("Profile");

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Medium.ttf");
        title.setTypeface(typeface);

        actionBar.setCustomView(actionView);

        ImageButton settingBtn = (ImageButton) actionView.findViewById(R.id.setting_btn);
        settingBtn.setVisibility(View.VISIBLE);
        settingBtn.setOnClickListener(view1 -> {
            ((MainActivity)getActivity()).tabIndex = 3;
            startActivity(new Intent(getActivity(), SettingActivity.class));
        });

        /*this.profileCameraBtn = (ImageView) this.view.findViewById(R.id.profile_camera_btn);
        *//*this.profileCameraBtn.setDefaultImageResId(R.drawable.ic_fa_camera);*//*
        this.profileCameraBtn.bringToFront();*/

        this.profileImageView = (RadiusNetworkImageView) this.view.findViewById(R.id.user_profile_photo);
        this.profileImageView.setRadius(25f);
        this.profileImageView.setOnClickListener(view1 -> {

            String profileUrl = this.user.getPhotoUrl().toString();

            if(profileUrl.indexOf("scontent.xx.fbcdn.net") == -1)
                viewPhoto(profileUrl, "jpg");
        });

        this.profileCameraBtn = (RadiusImageButton) this.view.findViewById(R.id.profile_camera_btn);
        this.profileCameraBtn.setRadius(30f);
        this.profileCameraBtn.bringToFront();
        this.profileCameraBtn.setOnClickListener(view1 -> {
            profileYn = true;
            showCameraDialog();
        });

        TextView textView = (TextView) this.view.findViewById(R.id.user_profile_name);
        textView.setTypeface(typeface);

        this.profileBitmap = CommonFunction.getBitmapFromURL(this.user.getPhotoUrl().toString());

        this.imageLoader = VolleySingleton.getInstance(getActivity()).getImageLoader();

        /*this.profileImageView.bringToFront();*/
        this.profileImageView.setImageUrl(this.user.getPhotoUrl().toString(), this.imageLoader);
        textView.setText(this.user.getDisplayName());

        /*view.setVisibility(View.INVISIBLE);*/

        this.gridView = (UltimateRecyclerView) view.findViewById(R.id.photo_list);
        this.gridView.addItemDecoration(new EqualSpacingItemDecoration(6, EqualSpacingItemDecoration.GRID));
        /*this.gridView.setVisibility(View.INVISIBLE);*/
        /*this.gridView.setExpanded(true);*/

        this.listView = (UltimateRecyclerView) view.findViewById(R.id.say_list);
        /*this.listView.setVisibility(View.INVISIBLE);*/
        this.linearLayoutManager = new LinearLayoutManager(getActivity());
        this.listView.setLayoutManager(this.linearLayoutManager);
        this.listView.setEmptyView(R.layout.empty_view, UltimateRecyclerView.EMPTY_SHOW_LOADMORE_ONLY);

        /*this.listView.setLoadMoreView(LayoutInflater.from(getActivity())
                .inflate(R.layout.custom_bottom_progressbar, null));*/

        this.photoVoList = new ArrayList<>();
        this.sayVoList = new ArrayList<>();

        this.userSayListViewAdapter = new UserInfoSayListViewAdapter(getActivity(), this.sayVoList, true);
        this.listView.setAdapter(this.userSayListViewAdapter);
        this.listView.setHasFixedSize(false);

        /*this.listView.reenableLoadmore();

        this.listView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                loadingSayData(sayQuery, true);
                linearLayoutManager.scrollToPositionWithOffset(maxLastVisiblePosition,-1);
                linearLayoutManager.scrollToPosition(maxLastVisiblePosition);
            }
        });*/

        this.listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;

                if (lastVisibleItemPosition == itemTotalCount && !lastYn) {
                    /*Toast.makeText(getContext(), "Last Position", Toast.LENGTH_SHORT).show();*/
                    progressON(getResources().getString(R.string.loading));
                    loadingSayData(sayQuery, true);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        /* this.listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //현재 화면에 보이는 첫번째 리스트 아이템의 번호(firstVisibleItem) + 현재 화면에 보이는 리스트 아이템의 갯수(visibleItemCount)가 리스트 전체의 갯수(totalItemCount) -1 보다 크거나 같을때
                lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //OnScrollListener.SCROLL_STATE_IDLE은 스크롤이 이동하다가 멈추었을때 발생되는 스크롤 상태입니다.
                //즉 스크롤이 바닦에 닿아 멈춘 상태에 처리를 하겠다는 뜻
                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastitemVisibleFlag && !lastYn) {
                    loadingData(query);
                }
            }
        });*/

        this.button = (Button) view.findViewById(R.id.newSayBtn);
        this.button.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), PopupActivity.class);
            intent.putExtra("data", "Test Popup");
            startActivityForResult(intent, 1);
        });

        this.adapter = new NewRecyclerGridViewAdapter(getActivity(), this.photoVoList);
        this.adapter.setSpanColumns(2);

        this.basicGridLayoutManager = new BasicGridLayoutManager(getActivity(), this.columns, this.adapter);

        this.gridView.setLayoutManager(this.basicGridLayoutManager);
        this.gridView.setAdapter(this.adapter);

        this.gridView.setHasFixedSize(true);
        this.gridView.setSaveEnabled(true);
        this.gridView.setClipToPadding(false);

        /*this.gridView.reenableLoadmore();

        this.gridView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                loadingPhotoData(photoQuery, true);
            }
        });*/

        this.db.collection("member/")
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        for (DocumentSnapshot document1 : task1.getResult()) {
                            UserVo userVo = new UserVo();
                            String uid = document1.getData().get("id").toString();
                            GeoPoint geoPoint = (GeoPoint) document1.getData().get("location");

                            userVo.setUid(uid);
                            userVo.setUserName(document1.getData().get("name").toString());
                            userVo.setIdentity(document1.getData().get("identity").toString());
                            userVo.setNation(document1.getData().get("nation").toString());
                            userVo.setPhotoUrl(document1.getData().get("profileUrl").toString());
                            userVo.setGeoPoint(geoPoint);

                            String identity = document1.getData().get("identity").toString();
                            String nation = document1.getData().get("nation").toString();

                            String gender = document1.getData().get("gender").toString();
                            gender = (gender.equals("male") ? "남자" : "여자");

                            long dateOfBirth = document1.getDate("dateOfBirth").getTime();
                            long now = System.currentTimeMillis();

                            Calendar birthCalendar = Calendar.getInstance();
                            birthCalendar.setTimeInMillis(dateOfBirth);

                            int yearOfBirth = birthCalendar.get(Calendar.YEAR);

                            Calendar nowCalender = Calendar.getInstance();
                            nowCalender.setTimeInMillis(now);

                            int nowYear = nowCalender.get(Calendar.YEAR);

                            int koreanAge = nowYear - yearOfBirth + 1;

                            String age = String.format("%d세, %s", koreanAge, gender);
                            TextView ageView = (TextView) view.findViewById(R.id.age);
                            ageView.setText(age);

                            nation = String.format("%s, %s", nation, identity);
                            TextView identityView = (TextView) view.findViewById(R.id.identity);
                            identityView.setText(nation);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREERROR", e.getMessage());
                });

        /*this.db.collection("photo/")
                .whereEqualTo("member_id", this.user.getUid())
                *//*.orderBy("reg_dt", Query.Direction.ASCENDING)*//*
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (this.photoVoList != null && !this.photoVoList.isEmpty()) {
                        this.photoVoList.clear();
                    }

                    PhotoVo addBtn = new PhotoVo();
                    addBtn.setKind("add_btn");
                    *//*this.photoVoList.add(addBtn);*//*
                    this.adapter.insertLast(addBtn);

                    for (DocumentSnapshot document : value) {
                        PhotoVo photo = new PhotoVo();
                        photo.setPhotoId(document.getString("id"));
                        photo.setFileName(document.getString("fileName").toString());
                        photo.setKind("photo");
                        *//*this.photoVoList.add(photo);*//*
                        this.adapter.insertLast(photo);

                        StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("original/" + document.getString("fileName") + ".jpg");

                        islandRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            //do something with downloadurl
                            photo.setFileUrl(downloadUrl.toString());
                        }).addOnFailureListener(e1 -> {
                            Log.d("에러~", e1.getMessage());
                        });
                    }

                    afterAdd();

                    *//*this.adapter.clearAdapter();
                    this.adapter.addNewValues(this.photoVoList);*//*
                    *//*this.adapter.notifyDataSetChanged();*//*

                    *//*this.adapter.notifyDataSetChanged();
                    this.gridView.invalidateViews();
                    this.gridView.setAdapter(this.adapter);*//*
                });*/

        this.photoQuery = this.db.collection("photo/")
                .whereEqualTo("member_id", this.user.getUid())
                .orderBy("reg_dt", Query.Direction.DESCENDING)
                .limit(this.limit);

        loadingPhotoData(this.photoQuery, false);

        this.sayQuery = this.db.collection("say/")
                .whereEqualTo("member_id", this.user.getUid())
                .orderBy("reg_dt", Query.Direction.DESCENDING)
                .limit(this.limit);

        loadingSayData(this.sayQuery, false);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if(requestCode != 1) {*/
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {

                case Constant.GALLERY_CODE:
                    cameraProfile.sendPicture(data, Constant.GALLERY_CODE); //갤러리에서 가져오기
                    break;
                case Constant.CAMERA_CODE:
                    cameraProfile.sendPicture(data, Constant.CAMERA_CODE); //카메라에서 가져오기
                    break;

                default:
                    break;
            }
        }
        /*}*/
        /*else if(requestCode == 1) {
            if(resultCode == Activity.RESULT_OK) {
                //데이터 받기
                String sayContent = data.getStringExtra("sayContent");

                Map<String, Object> stringMap = new HashMap<>();

                stringMap.put("member_id", this.user.getUid());
                stringMap.put("content", sayContent);
                stringMap.put("reg_dt", new Date());

                DocumentReference memberReference = this.db.collection("member").document(this.user.getUid());
                stringMap.put("member", memberReference);

                this.db.collection("say")
                        .add(stringMap)
                        .addOnSuccessListener(documentReference -> {
                            //
                            if(this.sayVoList.size() == 1 && this.sayVoList.get(0).isNoMsg()) {
                                this.sayVoList.clear();
                            }

                            SayVo say = new SayVo();
                            say.setMsg(sayContent);

                            this.sayVoList.add(say);
                            this.userSayListViewAdapter.notifyDataSetChanged();

                        })
                        .addOnFailureListener(e -> {
                            //
                            Log.w(TAG, "Error adding document", e);
                        });
            }
        }*/
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

    public static void chageProfileYn(boolean param) {
        profileYn = param;
    }

    protected void afterAdd() {

    }

    private void viewPhoto(Bitmap bitmap) {
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(getActivity(), ViewPhotoActivity.class);
        if(bitmap != null) {
            intent.putExtra("bitmap", bitmap);
        }
        intent.putExtra("photoUrl", photoVoList.get(this.position).getFileName());
        startActivityForResult(intent, 1);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public static void viewPhoto(String url, String type) {
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(activity, PhotoViewerActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("type", type);
        activity.startActivityForResult(intent, 1);
        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private View.OnTouchListener menuListener = (v, event) -> {
        getActivity().openOptionsMenu();
        return false;
    };

    private void loadingPhotoData(Query queryParam, boolean loadMoreYn) {
        queryParam
                .whereEqualTo("member_id", this.user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if(!loadMoreYn) {
                            PhotoVo addBtn = new PhotoVo();
                            addBtn.setKind("add_btn");
                            this.adapter.insertLast(addBtn);
                        }

                        int size = task.getResult().size();
                        DocumentSnapshot last = null;

                        if (size > 0) {

                            last = task.getResult().getDocuments().get(size - 1);

                            this.photoQuery = db.collection("photo/")
                                    .whereEqualTo("member_id", this.user.getUid())
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
                                if(document.getData().get("file_name") != null) {
                                    photoVo.setFileName(document.getData().get("file_name").toString());
                                }
                                photoVo.setKind("photoVo");
                                this.adapter.insertLast(photoVo);

                                /*StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("original/" + document.getData().get("fileName").toString() + ".jpg");

                                islandRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                                    //do something with downloadurl
                                    photoVo.setFileUrl(downloadUrl.toString());
                                }).addOnFailureListener(e -> {
                                    Log.d("에러~", e.getMessage());
                                });*/
                            }
                        } else {
                            this.gridView.disableLoadmore();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void loadingSayData(Query queryParam, boolean loadMoreYn) {
        queryParam
                .whereEqualTo("member_id", this.user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        int size = task.getResult().size();
                        DocumentSnapshot last = null;

                        if(size > 0) {

                            last = task.getResult().getDocuments().get(size - 1);

                            this.sayQuery = this.db.collection("say/")
                                    .whereEqualTo("member_id", this.user.getUid())
                                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                                    .startAfter(last)
                                    .limit(this.limit);

                            if(size < this.limit) {
                                /*this.listView.disableLoadmore();*/
                                lastYn = true;
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
                                sayVo.setSayId(document.get("id").toString());
                                sayVo.setUserName(this.user.getDisplayName());
                                sayVo.setMsg(document.getData().get("content").toString());
                                sayVo.setPhotoUrl(this.user.getPhotoUrl().toString());
                                sayVo.setDistance(this.regMin);
                                sayVo.setNoMsg(false);

                                this.userSayListViewAdapter.insert(sayVo, this.userSayListViewAdapter.getAdapterItemCount());
                            }

                        } else {

                            if(!loadMoreYn) {
                                SayVo sayVo = new SayVo();
                                sayVo.setMsg(getResources().getString(R.string.no_data));
                                sayVo.setNoMsg(true);

                                this.userSayListViewAdapter.insert(sayVo, this.userSayListViewAdapter.getAdapterItemCount());
                            }

                            lastYn = true;
                        }

                        progressOFF();
                        this.view.setVisibility(View.VISIBLE);
                        this.listView.setVisibility(View.VISIBLE);

                        /*new Handler().postDelayed(() -> {
                            progressOFF();
                            this.view.setVisibility(View.VISIBLE);
                            this.listView.setVisibility(View.VISIBLE);
                        }, 150);*/
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    public static void showCameraDialog() {
        final List<String> listItems = new ArrayList<>();
        listItems.add("사진 촬영하여 등록");
        listItems.add("사진 앨범에서 등록");

        if(profileYn) {
            listItems.add("삭제");
        }

        listItems.add("취소");
        final CharSequence[] items =  listItems.toArray(new String[ listItems.size()]);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        /*alertDialogBuilder.setTitle("Say삭제");*/
        alertDialogBuilder.setItems(items, (dialog, index) -> {

            if(profileYn) {
                switch (index) {
                    case 0:
                        cameraProfile.takePhoto();
                        break;

                    case 1:
                        cameraProfile.selectPhoto();
                        break;

                    case 2:
                        String profileUrl = "https://scontent.xx.fbcdn.net/v/t1.0-1/c29.0.100.100/p100x100/10354686_10150004552801856_220367501106153455_n.jpg?oh=abb02c803534c00048bc66ee3119bfbf&oe=5AF01677";
                        cameraProfile.changeProfilePhoto(profileUrl);
                        break;

                    case 3:
                        dialog.cancel();
                        break;
                }
            } else {
                switch (index) {
                    case 0:
                        cameraProfile.takePhoto();
                        break;

                    case 1:
                        cameraProfile.selectPhoto();
                        break;

                    case 2:
                        dialog.cancel();
                        break;
                }
            }
        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    class ProfileCamera {

        private Uri mImageCaptureUri;
        private String imgPath = "";

        private void changeProfilePhoto(String profileUrl) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(profileUrl))
                    .build();

            /*new Handler().postDelayed(() -> {
                BaseApplication.getInstance().progressON(this, "Saving...");
            }, 500);
*/
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("profileUrl", profileUrl);

                            FirebaseFirestore.getInstance().collection("member").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .update(userInfo)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");

                                        SendBird.updateCurrentUserInfo(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), profileUrl, e12 -> {
                                            if (e12 != null) {
                                                // Error.
                                                Crashlytics.logException(e12);
                                                return;
                                            }

                                            profileImageView.setImageUrl(profileUrl, imageLoader);
                                        });
                                    })
                                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                        }
                    });
        }

        private void sendPicture(Intent data, int cameraCode) {

            String fileName = "";

            switch (cameraCode) {

                case Constant.GALLERY_CODE:
                    this.imgPath = getRealPathFromURI(data.getData());
                    fileName = data.getData().getLastPathSegment(); //갤러리에서 가져오기
                    break;
                case Constant.CAMERA_CODE:
                    fileName = this.mImageCaptureUri.getLastPathSegment(); //카메라에서 가져오기
                    break;

                default:
                    break;
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("original/" + fileName + ".jpg");

            StorageReference storageThumRef = null;
            if(!profileYn) {
                storageThumRef = storage.getReference().child("thumbnail/" + fileName + "_thumbnail.jpg");
            }

            ExifInterface exif = null;

            try {
                exif = new ExifInterface(this.imgPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);

            // 1. 촬영이미지 스토리지에 업로드
            // 2. 업로드된 url photo에 추가.
            // 3. DB에 추가

            // Get the data from an ImageView as bytes
            BitmapFactory.Options options = new BitmapFactory.Options();
            /*options.outHeight = 1000;
            options.outWidth = 1000;*/

            Bitmap bitmap = BitmapFactory.decodeFile(this.imgPath, options);//경로를 통해 비트맵으로 전환
            bitmap = rotate(bitmap, exifDegree);

            Bitmap thumbnail = null;
            ByteArrayOutputStream bitmapThumOps = null;
            UploadTask uploadThumTask = null;
            if(!profileYn) {
                thumbnail = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                bitmapThumOps = new ByteArrayOutputStream();

                thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bitmapThumOps);
                byte[] bitmapThumByte = bitmapThumOps.toByteArray();
                uploadThumTask = storageThumRef.putBytes(bitmapThumByte);
            }

            final int viewHeight = 1000;

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if(height > viewHeight) {
                float percente = (float)(height / 100);
                float scale = (float)(viewHeight / percente);
                width *= (scale / 100);
                height *= (scale / 100);
            }

            bitmap = Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, true);

            ByteArrayOutputStream bitmapOps = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bitmapOps);

            byte[] bitmapByte = bitmapOps.toByteArray();
            UploadTask uploadTask = storageRef.putBytes(bitmapByte);

            Bitmap finalBitmap = thumbnail;

            UploadTask finalUploadThumTask = uploadThumTask;
            String finalFileName1 = fileName;
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
            }).addOnSuccessListener(taskSnapshot -> {
                if(!profileYn) {
                    String finalFileName = finalFileName1;
                    finalUploadThumTask.addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                    }).addOnSuccessListener(taskThumSnapshot -> {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                        Map<String, Object> photoMap = new HashMap<>();

                        photoMap.put("member_id", user.getUid());
                        photoMap.put("original_img", taskSnapshot.getDownloadUrl().toString());
                        photoMap.put("thumbnail_img", taskThumSnapshot.getDownloadUrl().toString());
                        photoMap.put("file_name", finalFileName);
                        photoMap.put("reg_dt", new Date());

                        DocumentReference photoReference = db.collection("photo").document();
                        photoMap.put("id", photoReference.getId());

                        PhotoVo newPhotoVo = new PhotoVo();

                        newPhotoVo.setPhotoId(photoReference.getId());
                        newPhotoVo.setThumbnailUrl(taskThumSnapshot.getDownloadUrl().toString());
                        newPhotoVo.setOriginalUrl(taskSnapshot.getDownloadUrl().toString());
                        newPhotoVo.setBitmap(finalBitmap);
                        newPhotoVo.setFileName(finalFileName);
                        newPhotoVo.setKind("photo");

                        adapter.insertInternal(photoVoList, newPhotoVo, 1);

                        photoReference
                                .set(photoMap)
                                .addOnSuccessListener(documentReference -> {
                                    //
                                    Log.d(TAG, "DocumentSnapshot written with ID: " + photoReference.getId());
                                })
                                .addOnFailureListener(e -> {
                                    //
                                    Log.w(TAG, "Error adding document", e);
                                });

                        adapter = new NewRecyclerGridViewAdapter(getActivity(), photoVoList);
                        gridView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                /*new Handler().postDelayed(() -> {
                    progressOFF();
                    this.listView.setVisibility(View.VISIBLE);
                }, 1000);*/
                    });
                } else {
                    // 프사 업데이트
                    String profileUrl = taskSnapshot.getDownloadUrl().toString();
                    changeProfilePhoto(profileUrl);
                }
            });
        }

        public Bitmap rotate(Bitmap src, float degree) {
            // Matrix 객체 생성
            Matrix matrix = new Matrix();
            // 회전 각도 셋팅
            matrix.postRotate(degree);

            // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
            try {
                return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                        src.getHeight(), matrix, true);
            } catch (Exception e) {
                return src;
            }
        }

        /*public static String getRealPathFromURI(Context context, Uri contentUri) {
            //copy file and send new file path
            String fileName = getFileName(contentUri);
            if (!TextUtils.isEmpty(fileName)) {
                File copyFile = new File("img/" + File.separator + fileName);
                copy(context, contentUri, copyFile);
                return copyFile.getAbsolutePath();
            }
            return null;
        }

        public static String getFileName(Uri uri) {
            if (uri == null) return null;
            String fileName = null;
            String path = uri.getPath();
            int cut = path.lastIndexOf('/');
            if (cut != -1) {
                fileName = path.substring(cut + 1);
            }
            return fileName;
        }

        public static void copy(Context context, Uri srcUri, File dstFile) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
                if (inputStream == null) return;
                OutputStream outputStream = new FileOutputStream(dstFile);
                IOUtils.copy(inputStream, outputStream);
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        public String getRealPathFromURI(Uri contentUri) {
            int column_index=0;
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
            if(cursor.moveToFirst()) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            }

            return cursor.getString(column_index);
        }

        public int exifOrientationToDegrees(int exifOrientation) {
            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            }
            return 0;
        }

        private Uri getFileUri() {
            File dir = new File( getActivity().getFilesDir(), "img" );
            if ( !dir.exists() ) {
                dir.mkdirs();
            }
            File file = new File( dir, System.currentTimeMillis() + "");
            this.imgPath = file.getAbsolutePath();
            return FileProvider.getUriForFile( getActivity(), "com.hello.holaApp.provider", file );
        }

        private void takePhoto() {

            this.mImageCaptureUri = getFileUri();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            intent.putExtra( MediaStore.EXTRA_OUTPUT, this.mImageCaptureUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            List<ResolveInfo> resolvedIntentActivities = getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, this.mImageCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            startActivityForResult(intent, Constant.CAMERA_CODE);
        }

        private void selectPhoto() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Constant.GALLERY_CODE);
        }
    }
}
