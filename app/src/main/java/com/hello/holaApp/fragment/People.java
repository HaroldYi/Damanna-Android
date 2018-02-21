package com.hello.holaApp.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.hello.holaApp.adapter.PeopleListViewAdapter;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class People extends BaseFragment implements View.OnClickListener {

    private int radius = 10;

    private PeopleListViewAdapter adapter;

    private static String TAG = "fireStoreTag";

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private View view;

    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private List<UserVo> userVoList;

    private Query query;
    private UltimateRecyclerView listView;

    private static int limit = 10;

    Double latitude;
    Double longitude;

    public People() {
        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(this.userVoList == null) {
            this.userVoList = new ArrayList<>();
        }

        this.latitude = CommonFunction.getLatitude();
        this.longitude = CommonFunction.getLongitude();

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

        /*Spinner spinner = (Spinner) actionView.findViewById(R.id.distance_filter);
        spinner.setVisibility(View.VISIBLE);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int index, long id) {
                switch (index) {
                    case 1:
                        radius = 10;
                        break;

                    case 2:
                        radius = 50;

                    case 3:
                        radius = 100;
                        break;

                    default:
                        radius = 1000000000;
                        break;
                }

                adapter.clear();
                geoQuery.setRadius(radius);
                geoQuery.removeAllListeners();
                setListener();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });*/

        actionBar.setCustomView(actionView);

        progressON(getResources().getString(R.string.loading));

        this.view = inflater.inflate(R.layout.home_layout, container, false);
        view.setVisibility(View.INVISIBLE);
        this.listView = (UltimateRecyclerView) view.findViewById(R.id.people_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        this.adapter = new PeopleListViewAdapter(getContext(), userVoList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(this.adapter);
        listView.setHasFixedSize(true);
        listView.reenableLoadmore();

        listView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {

                Log.d("itemsCount", itemsCount+"");

                linearLayoutManager.scrollToPositionWithOffset(maxLastVisiblePosition,-1);
                linearLayoutManager.scrollToPosition(maxLastVisiblePosition);
                loadingPeople(query);
                progressON(getResources().getString(R.string.loading));
            }
        });

        List<UserVo> userVoList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userlocation");
        this.geoFire = new GeoFire(ref);

        // creates a new query around [latitude, longitude] with a radius of 1.0 kilometers
        this.geoQuery = this.geoFire.queryAtLocation(new GeoLocation(this.latitude, this.longitude), this.radius);
        /*this.setListener();*/
        /*listView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int index) {

                        ((MainActivity)getActivity()).tabIndex = 1;

                        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                        intent.putExtra("uid", userVoList.get(index).getUid());
                        intent.putExtra("userName", userVoList.get(index).getUserName());
                        intent.putExtra("identity", userVoList.get(index).getIdentity());
                        intent.putExtra("profileUrl", userVoList.get(index).getPhotoUrl());
                        *//*intent.putExtra("bitmapImage", userVoList.get(index).getBitmap());*//*

                        startActivity(intent);
                    }
                })
        );*/

        this.query = this.db.collection("member").orderBy("last_signIn", Query.Direction.DESCENDING).limit(this.limit);
        this.loadingPeople(this.query);

        return this.view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*this.geoFire.removeLocation("7XIFHLj0frO0F5fICDStNWA7BJD3");*/
        /*this.geoQuery.removeAllListeners();*/
    }

    @Override
    public void onClick(View view) {

    }

    private void setListener() {
        this.geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("enterrrrr", String.format("Key %s entered the search area at [%f, %f]", key, location.latitude, location.longitude));
                loadingPeople(key);

                Location loc = new Location("pointA");
                Location loc1 = new Location("pointB");

                loc.setLatitude(location.latitude);
                loc.setLongitude(location.longitude);

                loc1.setLatitude(latitude);
                loc1.setLongitude(longitude);

                Log.d("distance", loc.distanceTo(loc1)/1000+"");

                /*if(!key.equals(user.getUid())) {

                    UserVo userVo = userMap.get(key);
                    if(userVo != null) {

                        Location loc = new Location("pointA");
                        Location loc1 = new Location("pointB");

                        loc.setLatitude(location.latitude);
                        loc.setLongitude(location.longitude);

                        loc1.setLatitude(latitude);
                        loc1.setLongitude(longitude);

                        *//*Log.d("distance", loc.distanceTo(loc1)/1000+"");*//*

                        userVo.setDistance(loc.distanceTo(loc1)/1000);
                        *//*photoVo.setUpdateTime(1);*//*

                        adapter.insertLastInternal(userVoList, userVo);

                        progressOFF();
                        view.setVisibility(View.VISIBLE);
                    }
                }*/
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
    }

    private void loadingPeople(String key) {

        Query query;

        if(key != null) {
            query = this.db.collection("member").whereEqualTo("id", key)
                            .orderBy("last_signIn", Query.Direction.DESCENDING);
        } else {
            query = this.db.collection("member").orderBy("last_signIn", Query.Direction.DESCENDING);
        }

        query
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                    if(documentSnapshotList.size() > 0) {
                        for (DocumentSnapshot document : documentSnapshotList) {
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

                                Location loc = new Location("pointA");
                                Location loc1 = new Location("pointB");

                                loc.setLatitude(geoPoint.getLatitude());
                                loc.setLongitude(geoPoint.getLongitude());

                                loc1.setLatitude(latitude);
                                loc1.setLongitude(longitude);

                                float distance = loc.distanceTo(loc1) / 1000;

                                userVo.setDistance(distance);

                                String identity = (document.getData().get("identity") != null ? document.getData().get("identity").toString() : "");
                                String nation = (document.getData().get("nation") != null ? document.getData().get("nation").toString() : "");
                                String profileUrl = (document.getData().get("profileUrl") != null ? document.getData().get("profileUrl").toString() : "");

                                userVo.setAge(koreanAge);
                                userVo.setIdentity(identity);
                                userVo.setNation(nation);
                                userVo.setPhotoUrl(profileUrl);
                                userVo.setGeoPoint(geoPoint);

                                /*this.userVoList.add(userVo);*/
                                /*this.userMap.put(uid, userVo);*/

                                adapter.insertLastInternal(userVoList, userVo);
                                /*geoFire.setLocation(uid, new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude()), (key, error) -> {
                                    if (error != null) {
                                        Log.e("geoFireLog", "There was an error saving the location to GeoFire: " + error);
                                    } else {
                                        Log.d("geoFireLog", String.format("userId : %s Location saved on server successfully!", uid));
                                    }
                                });*/
                            }
                        }

                        progressOFF();
                        view.setVisibility(View.VISIBLE);

                    } else {
                        progressOFF();
                        view.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            });
    }

    private void loadingPeople(Query query) {

        query
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();
                    int size = documentSnapshotList.size();
                    DocumentSnapshot last = null;

                    if(documentSnapshotList.size() > 0) {

                        if(size < this.limit) {
                            this.listView.disableLoadmore();
                        }

                        last = documentSnapshotList.get(size - 1);
                        this.query = this.db.collection("member/")
                                .orderBy("last_signIn", Query.Direction.DESCENDING)
                                .startAfter(last)
                                .limit(this.limit);

                        for (DocumentSnapshot document : documentSnapshotList) {
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

                                Location loc = new Location("pointA");
                                Location loc1 = new Location("pointB");

                                loc.setLatitude(geoPoint.getLatitude());
                                loc.setLongitude(geoPoint.getLongitude());

                                loc1.setLatitude(latitude);
                                loc1.setLongitude(longitude);

                                float distance = loc.distanceTo(loc1) / 1000;

                                userVo.setDistance(distance);

                                String identity = (document.getData().get("identity") != null ? document.getData().get("identity").toString() : "");
                                String nation = (document.getData().get("nation") != null ? document.getData().get("nation").toString() : "");
                                String profileUrl = (document.getData().get("profileUrl") != null ? document.getData().get("profileUrl").toString() : "");

                                userVo.setAge(koreanAge);
                                userVo.setIdentity(identity);
                                userVo.setNation(nation);
                                userVo.setPhotoUrl(profileUrl);
                                userVo.setGeoPoint(geoPoint);

                            /*this.userVoList.add(userVo);*/
                            /*this.userMap.put(uid, userVo);*/

                                adapter.insertLastInternal(userVoList, userVo);
                            /*geoFire.setLocation(uid, new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude()), (key, error) -> {
                                if (error != null) {
                                    Log.e("geoFireLog", "There was an error saving the location to GeoFire: " + error);
                                } else {
                                    Log.d("geoFireLog", String.format("userId : %s Location saved on server successfully!", uid));
                                }
                            });*/
                            }
                        }

                        progressOFF();
                        view.setVisibility(View.VISIBLE);
                    } else {
                        progressOFF();
                        view.setVisibility(View.VISIBLE);
                        this.listView.disableLoadmore();
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            });
    }
}
