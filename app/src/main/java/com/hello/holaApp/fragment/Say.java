package com.hello.holaApp.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.hello.holaApp.R;
import com.hello.holaApp.activity.MainActivity;
import com.hello.holaApp.activity.PopupActivity;
import com.hello.holaApp.activity.UserInfoActivity;
import com.hello.holaApp.adapter.NewSayListViewAdapter;
import com.hello.holaApp.vo.SayVo;
import com.marshalchen.ultimaterecyclerview.RecyclerItemClickListener;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class Say extends BaseFragment implements View.OnClickListener {

    private UltimateRecyclerView listView;
    private List<SayVo> sayVoList;
    private FirebaseFirestore db;

    private MainActivity activity;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private NewSayListViewAdapter sayListViewAdapter;

    private TextView noSayList;
    private RelativeLayout noDataArea;
    private RelativeLayout sayListArea;

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

        this.noSayList = (TextView) view.findViewById(R.id.no_say_list);
        this.noDataArea = (RelativeLayout) view.findViewById(R.id.no_data_area);
        this.sayListArea = (RelativeLayout) view.findViewById(R.id.say_list_area);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Medium.ttf");
        title.setTypeface(typeface);

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
        /*this.sayListViewAdapter.setCustomLoadMoreView(
                LayoutInflater.from(getActivity()).inflate(R.layout.custom_bottom_progressbar, null));*/

        this.listView = (UltimateRecyclerView) view.findViewById(R.id.say_list);
        /*this.listView.setEmptyView(R.layout.empty_view, UltimateRecyclerView.EMPTY_CLEAR_ALL);*/
        this.listView.setVisibility(View.INVISIBLE);
        /*this.listView.setLoadMoreView(LayoutInflater.from(getActivity())
                .inflate(R.layout.custom_bottom_progressbar, null));*/
        /*this.listView.setDefaultOnRefreshListener(() -> new Handler().postDelayed(() -> {

            lastYn = true;

            *//*this.listView.reenableLoadmore();*//*
            sayListViewAdapter.clear();
            sayVoList.clear();

            query = db.collection("say/")
                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                    .limit(limit);

            loadingData(query, false);
        }, 1000));*/

        /*this.listView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {

                Log.d("itemsCount", itemsCount+"");

                linearLayoutManager.scrollToPositionWithOffset(maxLastVisiblePosition,-1);
                linearLayoutManager.scrollToPosition(maxLastVisiblePosition);
                loadingData(query);
                progressON(getResources().getString(R.string.loading));
            }
        });

        this.listView.reenableLoadmore();*/

        this.listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    /*Toast.makeText(getContext(), "Last Position", Toast.LENGTH_SHORT).show();*/
                    progressON(getResources().getString(R.string.loading));
                    loadingData(query, false);
                }
            }
        });

        this.linearLayoutManager = new LinearLayoutManager(getActivity());
        this.listView.setLayoutManager(this.linearLayoutManager);
        this.listView.setAdapter(this.sayListViewAdapter);
        this.listView.setHasFixedSize(false);

        this.listView.addOnItemTouchListener(
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
        );

        this.activity = (MainActivity) getActivity();

        this.query = this.db.collection("say/")
                .orderBy("reg_dt", Query.Direction.DESCENDING)
                .limit(this.limit);

        this.progressON(getResources().getString(R.string.loading));

        this.loadingData(this.query, true);

        /*this.listView.setOnItemClickListener((adapterView, view1, index, l) -> {

            if(FirebaseAuth.getInstance().getCurrentUser() != null) {

                String uid = this.sayVoList.get(index).getUid();

                if (uid.equals(this.mAuth.getUid())) {
                    *//*this.activity.onFragmentChange(2);*//*
                } else {

                    ((MainActivity)getActivity()).tabIndex = 0;

                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("userName", this.sayVoList.get(index).getUserName());
                    intent.putExtra("profileUrl", this.sayVoList.get(index).getPhotoUrl());
                *//*intent.putExtra("bitmapImage", this.sayVoList.get(index).getBitmap());*//*

                    startActivity(intent);
                }
            } else {
                startActivity(new Intent(getActivity(), SignActivity.class));
            }
        });*/

        /*this.listView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
    }

    private void loadingData(Query queryParam, boolean initYn) {

        List<SayVo> tempSayVoList = new ArrayList<>();

        queryParam
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

            /*sayListViewAdapter = new NewSayListViewAdapter(getActivity(), sayVoList);
            listView.setAdapter(sayListViewAdapter);*/

                    List<DocumentSnapshot> documentSnapshotList = value.getDocuments();
                    int size = documentSnapshotList.size();
                    DocumentSnapshot last = null;
                    Log.d("sizeeeeeee", size+"");
                    if(size > 0) {
                        last = documentSnapshotList.get(size - 1);

                        query = db.collection("say/")
                                .orderBy("reg_dt", Query.Direction.DESCENDING)
                                .startAfter(last)
                                .limit(limit);

                /*listView.setSelection(sayListViewAdapter.getCount() - 1);*/
                        if(size < limit) {
                            lastYn = true;
                            /*this.listView.disableLoadmore();*/
                        }

                        for (DocumentSnapshot document : documentSnapshotList) {
                            SayVo sayVo = new SayVo();

                            String memberId = document.getString("member_id");

                            sayVo.setUid(memberId);
                            sayVo.setMsg(document.getData().get("content").toString());

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

                            sayVo.setRegMin(regMin);

                            tempSayVoList.add(sayVo);
                            Log.d("sizeee", tempSayVoList.size() + "");
                        }

                        listView.setVisibility(View.VISIBLE);
                        noDataArea.setVisibility(View.GONE);
                        sayListArea.setVisibility(View.VISIBLE);
                        this.noSayList.setVisibility(View.GONE);
                    } else if(size == 0 && initYn) {
                        lastYn = true;
                        listView.setVisibility(View.GONE);
                        noDataArea.setVisibility(View.VISIBLE);
                        sayListArea.setVisibility(View.GONE);
                        this.noSayList.setVisibility(View.VISIBLE);
                    } else {
                        lastYn = true;
                        listView.setVisibility(View.GONE);
                    }
                });

        this.db.collection("member")
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    GeoPoint myGeoPoint = null;

                    for (DocumentSnapshot doc : value) {
                        String uid = doc.getString("id");
                        if(uid.equals(this.user.getUid())) {
                            myGeoPoint = doc.getGeoPoint("location");
                            break;
                        }
                    }

                    for (DocumentSnapshot doc : value) {
                        String uid = doc.getString("id");

                        if(uid != null) {
                            for (SayVo sayVo : tempSayVoList) {
                                if (uid.equals(sayVo.getUid())) {
                                    sayVo.setUserName(doc.getString("name"));
                                    sayVo.setNation(doc.getString("nation"));
                                    sayVo.setIdentity(doc.getString("identity"));
                                    sayVo.setPhotoUrl(doc.getString("profileUrl"));

                                    GeoPoint geoPoint = doc.getGeoPoint("location");
                                    Location loc = new Location("pointA");
                                    Location loc1 = new Location("pointB");

                                    loc.setLatitude(geoPoint.getLatitude());
                                    loc.setLongitude(geoPoint.getLongitude());

                                    loc1.setLatitude(myGeoPoint.getLatitude());
                                    loc1.setLongitude(myGeoPoint.getLongitude());

                                    String distance = String.format("%.2fkm", (loc.distanceTo(loc1) / 1000));
                                    sayVo.setDistance(String.format("%s / %s", sayVo.getRegMin(), distance));
                                }
                            }
                        }
                    }

                    for (SayVo sayVo : tempSayVoList) {
                        this.sayListViewAdapter.insert(sayVo, this.sayListViewAdapter.getAdapterItemCount());
                    }

                    progressOFF();
                });

        /*query
        *//*.whereEqualTo("member_id", this.user.getUid())*//*
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                this.sayListViewAdapter = new SayListViewAdapter(getActivity(), this.sayVoList);
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

                        String memberId = document.getData().get("member_id").toString();

                        this.db.collection("member/")
                                .whereEqualTo("id", memberId)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        for (DocumentSnapshot document1 : task1.getResult()) {
                                            SayVo sayVo = new SayVo();

                                            String nation = (document1.getData().get("nation") != null ? document1.getData().get("nation").toString() : "");
                                            String identity = (document1.getData().get("identity") != null ? document1.getData().get("identity").toString() : "");

                                            long regDt = ((Date) document.getData().get("reg_dt")).getTime();
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

                                            sayVo.setUid(document1.getData().get("id").toString());
                                            sayVo.setUserName(document1.getData().get("name").toString());
                                            sayVo.setNation(nation);
                                            sayVo.setIdentity(identity);
                                            sayVo.setPhotoUrl(document1.getData().get("profileUrl").toString());
                                            sayVo.setMsg(document.getData().get("content").toString());

                                            GeoPoint geoPoint = (GeoPoint) document1.getData().get("location");

                                            Location loc = new Location("pointA");
                                            Location loc1 = new Location("pointB");

                                            loc.setLatitude(geoPoint.getLatitude());
                                            loc.setLongitude(geoPoint.getLongitude());

                                            loc1.setLatitude(this.latitude);
                                            loc1.setLongitude(this.longitude);

                                            String distance = String.format("%.2fkm", (loc.distanceTo(loc1) / 1000));
                                            sayVo.setDistance(String.format("%s / %s", regMin, distance));

                                            this.sayVoList.add(sayVo);
                                            this.sayListViewAdapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FIREERROR", e.getMessage());
                                });

                    }

                    if(size < this.limit) {
                        this.lastYn = true;
                    }

                    this.progressOFF();
                    this.listView.setVisibility(View.VISIBLE);
                } else {
                    this.lastYn = true;
                    this.progressOFF();
                    this.listView.setVisibility(View.VISIBLE);
                }

                *//*new Handler().postDelayed(() -> {
                    progressOFF();
                    this.listView.setVisibility(View.VISIBLE);
                }, 100);*//*

            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });*/
    }
}
