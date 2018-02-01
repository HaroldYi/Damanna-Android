package com.hello.holaApp.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.hello.holaApp.R;
import com.hello.holaApp.adapter.PeopleListViewAdapter;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class People extends Fragment implements View.OnClickListener {

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

    public People() {
        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_layout, container, false);
        UltimateRecyclerView listView = (UltimateRecyclerView) view.findViewById(R.id.people_list);

        List<UserVo> userVoList = new ArrayList<>();
        Double latitude = CommonFunction.getLatitude();
        Double longitude = CommonFunction.getLongitude();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("path/to/geofire");
        this.geoFire = new GeoFire(ref);
        GeoQuery geoQuery = this.geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 1);

        this.adapter = new PeopleListViewAdapter(getContext(), userVoList);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(this.adapter);
        listView.setHasFixedSize(true);

        HashMap<String, UserVo> userMap = new HashMap();

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

                                userMap.put(uid, userVo);

                                geoFire.setLocation(uid, new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude()), (key, error) -> {
                                    if (error != null) {
                                        System.err.println("There was an error saving the location to GeoFire: " + error);
                                    } else {
                                        System.out.println("Location saved on server successfully!");
                                    }
                                });
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
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
        this.geoFire.removeLocation("7XIFHLj0frO0F5fICDStNWA7BJD3");
    }

    @Override
    public void onClick(View view) {

    }
}
