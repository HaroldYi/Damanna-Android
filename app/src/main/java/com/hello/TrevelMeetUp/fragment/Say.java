package com.hello.TrevelMeetUp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.activity.MainActivity;
import com.hello.TrevelMeetUp.activity.PopupActivity;
import com.hello.TrevelMeetUp.activity.SignActivity;
import com.hello.TrevelMeetUp.activity.UserInfoActivity;
import com.hello.TrevelMeetUp.adapter.SayListViewAdapter;
import com.hello.TrevelMeetUp.vo.SayVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class Say extends BaseFragment implements View.OnClickListener {

    private ListView listView;
    private List<SayVo> sayVoList;
    private FirebaseFirestore db;

    private MainActivity activity;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private SayListViewAdapter sayListViewAdapter;

    private Double latitude = 0.0;
    private Double longitude = 0.0;

    private boolean lastitemVisibleFlag = false;
    private boolean lastYn = false;

    private int limit = 25;

    private Query query;

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    private static String TAG = "cloudFireStore";

    @Override
    public void onClick(View view) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 이용한 위치 요청
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    gpsListener);

            // 네트워크를 이용한 위치 요청
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    gpsListener);

            // 위치요청을 한 상태에서 위치추적되는 동안 먼저 최근 위치를 조회해서 set
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                this.latitude = lastLocation.getLatitude();
                this.longitude = lastLocation.getLongitude();
            }
        } catch(SecurityException ex) {
            Log.e("gpsERR", ex.toString());
            ex.printStackTrace();
        }

        this.mAuth = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.say_layout, container, false);

        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        floatingActionButton.bringToFront();

        floatingActionButton.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), PopupActivity.class);
            intent.putExtra("data", "Test Popup");
            startActivityForResult(intent, 1);
        });

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.say_swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            this.sayVoList.clear();

            this.query = this.db.collection("say/")
                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                    .limit(this.limit);

            loadingData(this.query);

            swipeRefreshLayout.setRefreshing(false);
        });

        this.listView = (ListView) view.findViewById(R.id.say_list);
        this.listView.setVisibility(View.INVISIBLE);

        this.db = FirebaseFirestore.getInstance();
        this.sayVoList = new ArrayList<>();

        this.activity = (MainActivity) getActivity();

        this.query = this.db.collection("say/")
                .orderBy("reg_dt", Query.Direction.DESCENDING)
                .limit(this.limit);

        this.progressON(getResources().getString(R.string.loading));
        this.loadingData(this.query);

        this.listView.setOnItemClickListener((adapterView, view1, index, l) -> {

            if(FirebaseAuth.getInstance().getCurrentUser() != null) {

                String uid = this.sayVoList.get(index).getUid();

                if (uid.equals(this.mAuth.getUid())) {
                    /*this.activity.onFragmentChange(2);*/
                } else {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("userName", this.sayVoList.get(index).getUserName());
                    intent.putExtra("profileUrl", this.sayVoList.get(index).getPhotoUrl());
                /*intent.putExtra("bitmapImage", this.sayVoList.get(index).getBitmap());*/

                    startActivity(intent);
                }
            } else {
                startActivity(new Intent(getActivity(), SignActivity.class));
            }
        });

        this.listView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            //데이터 받기
            String sayContent = data.getStringExtra("sayContent");

            Map<String, Object> stringMap = new HashMap<>();

            stringMap.put("member_id", this.user.getUid());
            stringMap.put("name", this.user.getDisplayName());
            stringMap.put("profileUrl", this.user.getPhotoUrl().toString());
            stringMap.put("content", sayContent);
            stringMap.put("reg_dt", new Date());

            DocumentReference memberReference = this.db.collection("member").document(this.user.getUid());
            stringMap.put("member", memberReference);

            this.db.collection("say")
                    .add(stringMap)
                    .addOnSuccessListener(documentReference -> {
                        SayVo say = new SayVo();
                        say.setMsg(sayContent);

                        say.setUid(this.user.getUid());
                        say.setUserName(this.user.getDisplayName());
                        say.setPhotoUrl(this.user.getPhotoUrl().toString());

                        for(int i = 0 ; i < this.sayVoList.size() ; i++) {
                            if(i != this.sayVoList.size() -1) {
                                SayVo temp = this.sayVoList.get(i + 1);
                                this.sayVoList.set(i, temp);
                            } else {
                                this.sayVoList.set(0, say);
                            }
                        }

                        this.sayListViewAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        //
                        Log.w(TAG, "Error adding document", e);
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    // LocationListener 정의
    private LocationListener gpsListener = new LocationListener() {

        // LocationManager 에서 위치정보가 변경되면 호출
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void loadingData(Query query) {

        query
        /*.whereEqualTo("member_id", this.user.getUid())*/
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                this.sayListViewAdapter = new SayListViewAdapter(getActivity(), R.id.conversation, this.sayVoList);
                this.listView.setAdapter(this.sayListViewAdapter);

                List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                int size = documentSnapshotList.size();
                DocumentSnapshot last = null;

                if(size > 0) {
                    last = documentSnapshotList.get(size - 1);

                    this.query = this.db.collection("say/")
                            .orderBy("reg_dt", Query.Direction.DESCENDING)
                            .startAfter(last)
                            .limit(this.limit);

                    this.listView.setSelection(this.sayListViewAdapter.getCount() - 1);

                    for (DocumentSnapshot document : documentSnapshotList) {

                        DocumentReference documentReference = (DocumentReference) document.getData().get("member");

                        Task<DocumentSnapshot> documentSnapshotTask = documentReference.get();

                        documentSnapshotTask.addOnCompleteListener(task1 -> {
                            SayVo sayVo = new SayVo();

                            sayVo.setUid(task1.getResult().getData().get("id").toString());
                            sayVo.setUserName(task1.getResult().getData().get("name").toString());
                            sayVo.setPhotoUrl(task1.getResult().getData().get("profileUrl").toString());
                            sayVo.setMsg(document.getData().get("content").toString());

                            GeoPoint geoPoint = (GeoPoint) task1.getResult().getData().get("location");

                            Location loc = new Location("pointA");
                            Location loc1 = new Location("pointB");

                            loc.setLatitude(geoPoint.getLatitude());
                            loc.setLongitude(geoPoint.getLongitude());

                            loc1.setLatitude(this.latitude);
                            loc1.setLongitude(this.longitude);

                            float distance = loc.distanceTo(loc1);
                            sayVo.setDistance(distance);

                            this.sayVoList.add(sayVo);
                            this.sayListViewAdapter.notifyDataSetChanged();
                        });
                    }

                    if(size < this.limit) {
                        this.lastYn = true;
                    }

                    this.progressOFF();
                    this.listView.setVisibility(View.VISIBLE);
                } else {
                    this.lastYn = true;
                }

                /*new Handler().postDelayed(() -> {
                    progressOFF();
                    this.listView.setVisibility(View.VISIBLE);
                }, 100);*/

            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });
    }
}
