package com.hello.holaApp.vo;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

/**
 * Created by lji5317 on 31/01/2018.
 */

public class UserVo {
    private String uid;
    private String userName;
    private GeoPoint geoPoint;
    private int age;
    private String gender;
    private String nation;
    private String identity;
    private String photoUrl;
    private float  distance;
    private List<PhotoVo> photoVoList;
    private List<String> sayLikeList;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public List<PhotoVo> getPhotoVoList() {
        return photoVoList;
    }

    public void setPhotoVoList(List<PhotoVo> photoVoList) {
        this.photoVoList = photoVoList;
    }

    public List<String> getSayLikeList() {
        return sayLikeList;
    }

    public void setSayLikeList(List<String> sayLikeList) {
        this.sayLikeList = sayLikeList;
    }
}
