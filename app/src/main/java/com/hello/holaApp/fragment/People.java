package com.hello.holaApp.fragment;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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
import com.hello.holaApp.adapter.GridViewAdapter;
import com.hello.holaApp.R;
import com.hello.holaApp.vo.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class People extends Fragment implements View.OnClickListener {

    private GridViewAdapter adapter;

    private static String TAG = "fireStoreTag";

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    private GeoFire geoFire;

    public People() {
        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_layout, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.home_grid);

        List<Photo> photoList = new ArrayList<>();
        Double latitude = 0.0;
        Double longitude = 0.0;

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
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
            }
        } catch(SecurityException ex) {
            Log.e("gpsERR", ex.toString());
            ex.printStackTrace();
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("path/to/geofire");
        this.geoFire = new GeoFire(ref);
        GeoQuery geoQuery = this.geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 1);
        Double finalLatitude = latitude;
        Double finalLongitude = longitude;

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("enterrrrr", String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                if(!key.equals(user.getUid())) {
                    Photo photo = new Photo();
                    /*photo.setPhotoUrl("https://yt3.ggpht.com/-v0soe-ievYE/AAAAAAAAAAI/AAAAAAAAAAA/OixOH_h84Po/s288-c-k-no-mo-rj-c0xffffff/photo.jpg");*/
                    /*photo.setDistance(distance(location.latitude, location.longitude, finalLatitude, finalLongitude, "kilometer"));*/

                    Location loc = new Location("pointA");
                    Location loc1 = new Location("pointB");

                    loc.setLatitude(location.latitude);
                    loc.setLongitude(location.longitude);

                    loc1.setLatitude(finalLatitude);
                    loc1.setLongitude(finalLongitude);

                    Log.d("distance", loc.distanceTo(loc1)+"");

                    photo.setDistance(loc.distanceTo(loc1));
                    photo.setUpdateTime(1);
                    photoList.add(photo);
                }

                adapter.notifyDataSetChanged();
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
                /*.whereEqualTo("id", user.getUid())*/
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String uid = document.get("id").toString();
                            if(user == null || !uid.equals(user.getUid())) {
                                GeoPoint geoPoint = (GeoPoint) document.get("location");
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

        adapter = new GridViewAdapter(getContext(), photoList);
        gridView.setAdapter(adapter);

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

    // LocationListener 정의
    private LocationListener gpsListener = new LocationListener() {

        // LocationManager 에서 위치정보가 변경되면 호출
        @Override
        public void onLocationChanged(Location location) {

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

    /**
     * 두 지점간의 거리 계산
     *
     * @param lat1 지점 1 위도
     * @param lon1 지점 1 경도
     * @param lat2 지점 2 위도
     * @param lon2 지점 2 경도
     * @param unit 거리 표출단위
     * @return
     */
    /*private double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit.equals("kilometer")) {
            dist = dist * 1.609344;
        } else if(unit.equals("meter")) {
            dist = dist * 1609.344;
        }

        return (dist);
    }

    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }*/
}
