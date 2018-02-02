package com.hello.holaApp.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.hello.holaApp.R;
import com.hello.holaApp.activity.MainActivity;
import com.hello.holaApp.activity.UserInfoActivity;
import com.hello.holaApp.adapter.PeopleListViewAdapter;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.vo.PhotoVo;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.RecyclerItemClickListener;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class People extends BaseFragment implements View.OnClickListener {

    private PeopleListViewAdapter adapter;

    private static String TAG = "fireStoreTag";

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private static HashMap<String, UserVo> userMap;
    private static List<UserVo> userVoList;

    public People() {
        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(this.userMap == null) {
            this.userMap = new HashMap();
        }

        if(this.userVoList == null) {
            this.userVoList = new ArrayList<>();
        }

        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.activity_action_bar, null);
        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText("People");

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Medium.ttf");
        title.setTypeface(typeface);

        actionBar.setCustomView(actionView);

        progressON(getResources().getString(R.string.loading));

        View view = inflater.inflate(R.layout.home_layout, container, false);
        view.setVisibility(View.INVISIBLE);
        UltimateRecyclerView listView = (UltimateRecyclerView) view.findViewById(R.id.people_list);

        List<UserVo> userVoList = new ArrayList<>();
        Double latitude = CommonFunction.getLatitude();
        Double longitude = CommonFunction.getLongitude();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("path/to/geofire");
        this.geoFire = new GeoFire(ref);
        this.geoQuery = this.geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 1);

        this.adapter = new PeopleListViewAdapter(getContext(), userVoList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(this.adapter);
        listView.setHasFixedSize(true);

        listView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int index) {

                        ((MainActivity)getActivity()).tabIndex = 1;

                        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                        intent.putExtra("uid", userVoList.get(index).getUid());
                        intent.putExtra("userName", userVoList.get(index).getUserName());
                        intent.putExtra("identity", userVoList.get(index).getIdentity());
                        intent.putExtra("profileUrl", userVoList.get(index).getPhotoUrl());
                        /*intent.putExtra("bitmapImage", userVoList.get(index).getBitmap());*/

                        startActivity(intent);
                    }
                })
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("member")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String uid = document.get("id").toString();
                            if(user == null || !uid.equals(user.getUid())) {

                                UserVo userVo = new UserVo();
                                GeoPoint geoPoint = (GeoPoint) document.getData().get("location");

                                userVo.setUid(uid);
                                userVo.setUserName(document.getData().get("name").toString());

                                String gender = document.getData().get("gender").toString();
                                gender = (gender.equals("male") ? "남자" : "여자");

                                userVo.setGender(gender);

                                long dateOfBirth = document.getDate("dateOfBirth").getTime();
                                long now = System.currentTimeMillis();

                                Calendar birthCalendar = Calendar.getInstance();
                                birthCalendar.setTimeInMillis(dateOfBirth);

                                int yearOfBirth = birthCalendar.get(Calendar.YEAR);

                                Calendar nowCalender = Calendar.getInstance();
                                nowCalender.setTimeInMillis(now);

                                int nowYear = nowCalender.get(Calendar.YEAR);

                                int koreanAge = nowYear - yearOfBirth + 1;

                                userVo.setAge(koreanAge);
                                userVo.setIdentity(document.getData().get("identity").toString());
                                userVo.setNation(document.getData().get("nation").toString());
                                userVo.setPhotoUrl(document.getData().get("profileUrl").toString());
                                userVo.setGeoPoint(geoPoint);

                                this.db.collection("photo/")
                                        .whereEqualTo("member_id", uid)
                                        .orderBy("reg_dt", Query.Direction.DESCENDING)
                                        .limit(5)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {

                                                int size = task1.getResult().size();

                                                if (size > 0) {

                                                    List<PhotoVo> photoVoList = new ArrayList<>();
                                                    for (DocumentSnapshot document1 : task1.getResult()) {

                                                        PhotoVo photoVo = new PhotoVo();
                                                        photoVo.setPhotoId(document1.getString("id"));
                                                        photoVo.setThumbnailUrl(document1.getData().get("thumbnail_img").toString());
                                                        photoVo.setOriginalUrl(document1.getData().get("original_img").toString());
                                                        if(document1.getData().get("file_name") != null) {
                                                            photoVo.setFileName(document1.getData().get("file_name").toString());
                                                        }

                                                        photoVoList.add(photoVo);
                                                    }

                                                    userVo.setPhotoVoList(photoVoList);
                                                } else {

                                                }

                                                this.userVoList.add(userVo);
                                                this.userMap.put(uid, userVo);
                                            } else {
                                                Log.w(TAG, "Error getting documents.", task.getException());
                                            }

                                            geoFire.setLocation(uid, new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude()), (key, error) -> {
                                                if (error != null) {
                                                    System.err.println("There was an error saving the location to GeoFire: " + error);
                                                } else {
                                                    System.out.println("Location saved on server successfully!");
                                                }
                                            });
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                        });
                            }
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!key.equals(user.getUid())) {
                    Log.d("enterrrrr", String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                    UserVo userVo = userMap.get(key);
                    if(userVo != null) {

                        Location loc = new Location("pointA");
                        Location loc1 = new Location("pointB");

                        loc.setLatitude(location.latitude);
                        loc.setLongitude(location.longitude);

                        loc1.setLatitude(latitude);
                        loc1.setLongitude(longitude);

                        Log.d("distance", loc.distanceTo(loc1)+"");

                        userVo.setDistance(loc.distanceTo(loc1)/1000);
                        /*photoVo.setUpdateTime(1);*/

                        adapter.insertLastInternal(userVoList, userVo);

                        progressOFF();
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d("exited", String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("moved", String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("gqr", "All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("gqerr", "There was an error with this query: " + error);
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*this.geoFire.removeLocation("7XIFHLj0frO0F5fICDStNWA7BJD3");*/
        this.geoQuery.removeAllListeners();
    }

    @Override
    public void onClick(View view) {

    }
}
