package com.hello.holaApp.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.hello.holaApp.R;
import com.hello.holaApp.activity.MainActivity;
import com.hello.holaApp.activity.PopupActivity;
import com.hello.holaApp.adapter.NewSayListViewAdapter;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.vo.SayVo;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class Say extends BaseFragment implements View.OnClickListener {

    public static boolean resumeYn = false;

    private int radius = 10;

    private UltimateRecyclerView listView;
    public static List<SayVo> sayVoList;
    private FirebaseFirestore db;

    private MainActivity activity;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private NewSayListViewAdapter sayListViewAdapter;

    /*private TextView noSayList;*/
    private RelativeLayout noDataArea;
    private RelativeLayout sayListArea;

    private Map<String, UserVo> userMap;

    private LinearLayoutManager linearLayoutManager;

    private boolean lastitemVisibleFlag = false;
    private boolean lastYn = false;

    private final int limit = 10;

    private Query query;

    private View view;

    private static String TAG = "cloudFireStore";

    private boolean loadmoreYn = false;

    @Override
    public void onClick(View view) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();

        if(this.view == null) {
                    this.view = inflater.inflate(R.layout.say_layout, container, false);
        } else {
            return this.view;
        }

        ((MainActivity) getActivity()).getSupportActionBar().show();

        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.activity_action_bar, null);
        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText("Say");

        /*this.noSayList = (TextView) view.findViewById(R.id.no_say_list);*/
        /*this.noDataArea = (RelativeLayout) view.findViewById(R.id.no_data_area);*/
        this.sayListArea = (RelativeLayout) view.findViewById(R.id.say_list_area);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Medium.ttf");
        title.setTypeface(typeface);

        /*Spinner spinner = (Spinner) actionView.findViewById(R.id.distance_filter);
        spinner.setVisibility(View.VISIBLE);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int index, long id) {
                switch (index) {
                    case 1:
                        radius = 10;
                        break;

                    case 2:
                        radius = 50;
                        break;

                    case 3:
                        radius = 100;
                        break;

                    default:
                        radius = 0;
                        break;
                }

                listView.reenableLoadmore();
                sayListViewAdapter.clear();
                sayVoList.clear();

                // 33.4935048
                // 126.4981709

                double lat = LatitudeInDifference(radius) / 1.4;
                double lon = LongitudeInDifference(CommonFunction.getLatitude(), radius) / 1.4;

                GeoPoint greaterGeopoint = new GeoPoint(CommonFunction.getLatitude() + lat, CommonFunction.getLongitude() + lon);
                GeoPoint lesserGeopoint = new GeoPoint(CommonFunction.getLatitude() - lat, CommonFunction.getLongitude() - lon);
                *//*double lat = 0.0144927536231884;
                double lon = 0.0181818181818182;

                GeoPoint greaterGeopoint = new GeoPoint(CommonFunction.getLatitude() + (lat * radius), CommonFunction.getLongitude() + (lon * radius));
                GeoPoint lesserGeopoint = new GeoPoint(CommonFunction.getLatitude() - (lat * radius), CommonFunction.getLongitude() - (lon * radius));*//*

                Location loc = new Location("pointA");
                Location loc1 = new Location("pointB");

                loc.setLatitude(greaterGeopoint.getLatitude());
                loc.setLongitude(greaterGeopoint.getLongitude());

                loc1.setLatitude(CommonFunction.getLatitude());
                loc1.setLongitude(CommonFunction.getLongitude());

                Log.d("distance", loc1.distanceTo(loc)/1000+"");

                query = db.collection("say/");

                if(radius != 0) {
                    query = query
                                .whereGreaterThanOrEqualTo("location", lesserGeopoint)
                                .whereLessThanOrEqualTo("location", greaterGeopoint);
                } else {
                    query = query
                    .whereLessThanOrEqualTo("reg_dt", new Date())
                    .orderBy("reg_dt", Query.Direction.DESCENDING);
                }

                query = query.limit(limit);

                loadingData(query, true);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });*/

        actionBar.setCustomView(actionView);

        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        floatingActionButton.bringToFront();

        floatingActionButton.setOnClickListener(view1 -> {

            ((MainActivity)getActivity()).tabIndex = 0;

            Intent intent = new Intent(getActivity(), PopupActivity.class);
            intent.putExtra("data", "Test Popup");
            startActivityForResult(intent, 1);
        });

        /*SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.say_swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            this.sayVoList.clear();

            this.query = this.db.collection("say/")
                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                    .limit(this.limit);

            loadingData(this.query);

            swipeRefreshLayout.setRefreshing(false);
        });*/

        this.sayVoList = new ArrayList<>();
        this.sayListViewAdapter = new NewSayListViewAdapter(getActivity(), this.sayVoList);
        this.sayListViewAdapter.setCustomLoadMoreView(
                LayoutInflater.from(getActivity()).inflate(R.layout.custom_bottom_progressbar, null));

        this.listView = (UltimateRecyclerView) view.findViewById(R.id.say_list);
        this.listView.setEmptyView(R.layout.empty_view, UltimateRecyclerView.EMPTY_SHOW_LOADMORE_ONLY);
        this.listView.setVisibility(View.INVISIBLE);
        /* this.listView.setLoadMoreView(LayoutInflater.from(getActivity())
                .inflate(R.layout.custom_bottom_progressbar, null));*/

        this.listView.setDefaultOnRefreshListener(() -> {

            lastYn = true;

            listView.reenableLoadmore();
            sayListViewAdapter.clear();
            sayVoList.clear();

            query = db.collection("say/")
                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                    .limit(limit);

            linearLayoutManager.scrollToPosition(0);
            progressON(getResources().getString(R.string.loading));
            loadingData(query, false);
            /*spinner.setSelection(0);*/
        });

        this.listView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {

                Log.d("itemsCount", itemsCount+"");

                linearLayoutManager.scrollToPositionWithOffset(maxLastVisiblePosition,-1);
                linearLayoutManager.scrollToPosition(maxLastVisiblePosition);
                loadingData(query, false);
                progressON(getResources().getString(R.string.loading));
            }
        });

        this.listView.reenableLoadmore();

        /*this.listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;

                if (lastVisibleItemPosition == itemTotalCount && !lastYn) {
                    *//*Toast.makeText(getContext(), "Last Position", Toast.LENGTH_SHORT).show();*//*
                    progressON(getResources().getString(R.string.loading));
                    loadingData(query, false);
                }
            }
        });*/

        this.linearLayoutManager = new LinearLayoutManager(getActivity());
        this.listView.setLayoutManager(this.linearLayoutManager);
        this.listView.setAdapter(this.sayListViewAdapter);
        this.listView.setHasFixedSize(false);

        /*this.listView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int index) {

                        ((MainActivity)getActivity()).tabIndex = 0;

                        if(!sayVoList.get(index).getUid().equals(user.getUid())) {
                            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                            intent.putExtra("uid", sayVoList.get(index).getUid());
                            intent.putExtra("userName", sayVoList.get(index).getUserName());
                            intent.putExtra("identity", sayVoList.get(index).getIdentity());
                            intent.putExtra("profileUrl", sayVoList.get(index).getPhotoUrl());
                            intent.putExtra("bitmapImage", sayVoList.get(index).getBitmap());

                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "본인의 정보는 Profile메뉴를 이용하여 주십시오", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        );*/

        /*this.sayListViewAdapter = new NewSayListViewAdapter(getActivity(), this.sayVoList);
        this.listView.setAdapter(sayListViewAdapter);*/

        this.activity = (MainActivity) getActivity();

        this.query = this.db.collection("say/")
                .orderBy("reg_dt", Query.Direction.DESCENDING)
                .limit(this.limit);

        this.progressON(getResources().getString(R.string.loading));

        this.userMap = new HashMap();
        DocumentReference docRef = this.db.collection("member").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                        NewSayListViewAdapter.likeSayList = (ArrayList<String>) document.get("like_say");
                        NewSayListViewAdapter.likeSayList = (NewSayListViewAdapter.likeSayList == null ? new ArrayList<>() : NewSayListViewAdapter.likeSayList);
                        /*spinner.setSelection(0);*/
                        loadingData(query, true);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if(resultCode == Activity.RESULT_OK) {
            //데이터 받기
            String sayContent = data.getStringExtra("sayContent");

            Map<String, Object> stringMap = new HashMap<>();

            stringMap.put("member_id", this.user.getUid());
            stringMap.put("content", sayContent);
            stringMap.put("reg_dt", new Date());

            *//*DocumentReference memberReference = this.db.collection("member").document(this.user.getUid());
            stringMap.put("member", memberReference);*//*

            DocumentReference sayReference = this.db.collection("say").document();
            stringMap.put("id", sayReference.getId());

            sayReference.set(stringMap)
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
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        this.sayListViewAdapter = new NewSayListViewAdapter(getActivity(), this.sayVoList);
        this.listView.setAdapter(this.sayListViewAdapter);
    }

    //반경 m이내의 위도차(degree)
    public double LatitudeInDifference(int diff){
        //지구반지름
        final int earth = 6371;    //단위m

        return (diff*360.0) / (2*Math.PI*earth);
    }

    //반경 m이내의 경도차(degree)
    public double LongitudeInDifference(double _latitude, int diff){
        //지구반지름
        final int earth = 6371;    //단위m

        double ddd = Math.cos(0);
        double ddf = Math.cos(Math.toRadians(_latitude));

        return (diff*360.0) / (2*Math.PI*earth*Math.cos(Math.toRadians(_latitude)));
    }

    private void loadingData(Query queryParam, boolean initYn) {

        queryParam
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        List<DocumentSnapshot> documentSnapshotList = task.getResult().getDocuments();

                        int size = documentSnapshotList.size();
                        DocumentSnapshot last = null;

                        if(size < this.limit) {
                            this.lastYn = true;
                            this.listView.disableLoadmore();
                        }

                        if(size > 0) {
                            last = documentSnapshotList.get(size - 1);

                            this.query = this.db.collection("say/")
                                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                                    .startAfter(last)
                                    .limit(this.limit);

                            for (DocumentSnapshot document : documentSnapshotList) {

                                String memberId = document.getString("member_id");
                                /*UserVo user = userMap.get(memberId);

                                if (user != null) {*/

                                Map<String, Object> map = document.getData();

                                SayVo sayVo = new SayVo();

                                sayVo.setSayId(document.getString("id"));
                                sayVo.setUid(memberId);
                                sayVo.setMsg(document.getString("content"));

                                long regDt = document.getDate("reg_dt").getTime();
                                long now = System.currentTimeMillis();

                                long regTime = (now - regDt) / 60000;

                                String regMin = "";
                                if (regTime < 60) {
                                    regMin = String.format("%dmin", regTime);
                                } else if (regTime >= 60 && regTime < 1440) {
                                    regMin = String.format("%dh", (int) (regTime / 60));
                                } else if (regTime > 1440) {
                                    regMin = String.format("%dd", (int) (regTime / 1440));
                                }

                                sayVo.setLocation(document.getGeoPoint("location"));
                                sayVo.setLikeMembers((ArrayList<String>) document.get("like_members"));
                                sayVo.setCommentList((ArrayList<HashMap<String, Object>>) document.get("comment_list"));
                                sayVo.setCommentReplyList((ArrayList<HashMap<String, Object>>) document.get("comment_reply_list"));

                                sayVo.setRegMin(regMin);

                                /*sayVo.setUserName(user.getUserName());
                                sayVo.setNation(user.getNation());
                                sayVo.setIdentity(user.getIdentity());
                                sayVo.setPhotoUrl(user.getPhotoUrl());*/

                                /*GeoPoint geoPoint = user.getGeoPoint();
                                Location loc = new Location("pointA");
                                Location loc1 = new Location("pointB");

                                loc.setLatitude(geoPoint.getLatitude());
                                loc.setLongitude(geoPoint.getLongitude());

                                loc1.setLatitude(CommonFunction.getLatitude());
                                loc1.setLongitude(CommonFunction.getLongitude());

                                String distance = String.format("%.2fkm", (loc.distanceTo(loc1) / 1000));
                                sayVo.setDistance(String.format("%s / %s", sayVo.getRegMin(), distance));*/

                                this.sayListViewAdapter.insert(sayVo, this.sayListViewAdapter.getAdapterItemCount());
                        /*}*/
                            }

                            this.progressOFF();
                            listView.setVisibility(View.VISIBLE);
                    /*noDataArea.setVisibility(View.GONE);*/
                            sayListArea.setVisibility(View.VISIBLE);
                    /*this.noSayList.setVisibility(View.GONE);*/
                        } else if(size == 0 && initYn) {
                            lastYn = true;
                    /*listView.setVisibility(View.GONE);
                    noDataArea.setVisibility(View.VISIBLE);
                    sayListArea.setVisibility(View.GONE);
                    this.noSayList.setVisibility(View.VISIBLE);*/
                        } else {
                            lastYn = true;
                    /*listView.setVisibility(View.GONE);*/
                        }

                        progressOFF();

                    } else {
                        Crashlytics.logException(task.getException());
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
}
