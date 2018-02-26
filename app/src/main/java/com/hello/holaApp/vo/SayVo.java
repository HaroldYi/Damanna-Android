package com.hello.holaApp.vo;

import android.graphics.Bitmap;

import com.facebook.common.Common;
import com.google.firebase.firestore.GeoPoint;
import com.hello.holaApp.common.CommonFunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class SayVo implements Serializable {

    private String sayId = "";

    private String uid = "";
    private String userName = "";
    private String nation = "";
    private String identity = "";

    private String photoUrl = "";
    private String msg = "";
    /*private GeoPoint location;*/

    private String regMin = "";

    private boolean noMsg = false;

    private Bitmap bitmap;

    private String distance;

    private ArrayList<String> likeMembers;

    private ArrayList<HashMap<String, Object>> commentList;

    private ArrayList<HashMap<String, Object>> commentReplyList;

    public String getSayId() {
        return sayId;
    }

    public void setSayId(String sayId) {
        this.sayId = sayId;
    }

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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
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

    public String getRegMin() {
        return regMin;
    }

    public void setRegMin(String regMin) {
        this.regMin = regMin;
    }

    public ArrayList<String> getLikeMembers() {
        return likeMembers;
    }

    public void setLikeMembers(ArrayList<String> likeMembers) {
        this.likeMembers = likeMembers;
    }

    public ArrayList<HashMap<String, Object>> getCommentList() {
        return commentList;
    }

    public void setCommentList(ArrayList<HashMap<String, Object>> commentList) {
        this.commentList = commentList;
    }

    public ArrayList<HashMap<String, Object>> getCommentReplyList() {
        return commentReplyList;
    }

    public void setCommentReplyList(ArrayList<HashMap<String, Object>> commentReplyList) {
        this.commentReplyList = commentReplyList;
    }

    /*public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        if(location == null) {
            location = new GeoPoint(0, 0);
        }
        this.location = location;
    }*/
}
