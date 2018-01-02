package com.hello.TrevelMeetUp.vo;

import android.graphics.Bitmap;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class SayVo {

    private String uid = "";
    private String userName = "";

    private String photoUrl = "";
    private String msg = "";

    private boolean noMsg = false;

    private Bitmap bitmap;

    private float distance;

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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isNoMsg() {
        return noMsg;
    }

    public void setNoMsg(boolean noMsg) {
        this.noMsg = noMsg;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
