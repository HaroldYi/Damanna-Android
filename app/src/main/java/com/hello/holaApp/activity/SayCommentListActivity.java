package com.hello.holaApp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.holaApp.R;
import com.hello.holaApp.adapter.CommentListViewAdapter;
import com.hello.holaApp.adapter.NewSayListViewAdapter;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.fragment.Say;
import com.hello.holaApp.vo.SayVo;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mabbas007.tagsedittext.TagsEditText;

/**
 * Created by lji5317 on 08/02/2018.
 */

public class SayCommentListActivity extends AppCompatActivity implements TagsEditText.TagsEditListener {

    private FirebaseUser user;
    private FirebaseFirestore db;

    public static String uid;
    public static String sayId;
    public static String userName;

    public static TagsEditText writeCommentText;

    private int i = 0;
    private int j = 0;

    private String userId;
    private boolean isLiked;
    private ArrayList<String> likeMemberList;
    private ArrayList<String> likeSayList;
    private SayVo sayVo;

    public static String commentId;
    public static boolean reCommentYn = false;
    private List<UserVo> userVoList = new ArrayList<>();

    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.say_comment_list);

        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.db = FirebaseFirestore.getInstance();

        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.new_say_action_bar, null);
        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/NotoSans-Medium.ttf");
        title.setTypeface(typeface);

        actionBar.setCustomView(actionView);

        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setVisibility(View.GONE);

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view1 -> finish());

        RadiusNetworkImageView imageView = findViewById(R.id.user_photo);
        imageView.setRadius(100f);

        Intent intent = getIntent();

        this.sayVo = (SayVo) intent.getSerializableExtra("sayVo");

        /*String uid = intent.getStringExtra("uid");
        String sayId = intent.getStringExtra("sayId");
        String userName = intent.getStringExtra("userName");
        String identity = intent.getStringExtra("identity");
        String profileUrl = intent.getStringExtra("profileUrl");
        String nation = intent.getStringExtra("nation");
        String distance = intent.getStringExtra("distance");
        String content = intent.getStringExtra("content");*/

        uid = sayVo.getUid();
        sayId = sayVo.getSayId();
        userName = intent.getStringExtra("userName");
        String identity = intent.getStringExtra("identity");
        String profileUrl = intent.getStringExtra("profileUrl");
        String nation = intent.getStringExtra("nation");
        String distance = sayVo.getDistance();
        String content = sayVo.getMsg();

        ArrayList<HashMap<String, Object>> commentList = sayVo.getCommentList();
        ArrayList<HashMap<String, Object>> commentReplyList = sayVo.getCommentReplyList();
        this.likeSayList = intent.getStringArrayListExtra("likeSayList");

        if(commentList == null) {
            commentList = new ArrayList<HashMap<String, Object>>();
        }

        if(commentReplyList == null) {
            commentReplyList = new ArrayList<HashMap<String, Object>>();
        }

        int index = intent.getIntExtra("index", 0);

        this.isLiked = intent.getBooleanExtra("isLiked", false);

        this.likeMemberList = intent.getStringArrayListExtra("likeMemberList");

        if(identity.indexOf("워킹") != -1) {
            identity = "워홀";
        }

        TextView userNameView = findViewById(R.id.user_name);
        userNameView.setText(String.format("%s (%s, %s)", userName, nation, identity));

        TextView distanceView = findViewById(R.id.distance);
        distanceView.setText(distance);
        distanceView.setTypeface(typeface);

        imageView.setImageUrl(profileUrl, VolleySingleton.getInstance(this).getImageLoader());

        TextView contentVIew = findViewById(R.id.content);
        contentVIew.setText(content);

        title.setText(userName);

        ImageView imageView1 = findViewById(R.id.like_ic);
        TextView likeCnt = findViewById(R.id.like_cnt);

        TextView commentCnt = findViewById(R.id.comment_cnt);

        final String[] likeCntStr = {"0"};

        if(likeMemberList != null && likeMemberList.size() > 0) {
            likeCntStr[0] = String.valueOf(likeMemberList.size());
        }

        String commentCntStr = "0";

        if(commentList != null && commentList.size() > 0) {
            commentCntStr = String.valueOf(commentList.size() + commentReplyList.size());
        }

        likeCnt.setText(likeCntStr[0]);
        commentCnt.setText(commentCntStr);

        if(this.isLiked) {
            imageView1.setImageResource(R.drawable.ic_heart_red);
        } else {
            imageView1.setImageResource(R.drawable.ic_heart);
        }

        LinearLayout commentProfile = findViewById(R.id.profile_area);
        String finalIdentity = identity;
        commentProfile.setOnClickListener(v -> {

            if(!this.userId.equals(uid)) {
                Intent userIntent = new Intent(getApplicationContext(), UserInfoActivity.class);
                userIntent.putExtra("uid", uid);
                userIntent.putExtra("userName", userName);
                userIntent.putExtra("identity", finalIdentity);
                userIntent.putExtra("profileUrl", profileUrl);

                Say.resumeYn = true;

                startActivity(userIntent);
            }
        });

        LinearLayout layout = findViewById(R.id.like_people_list);

        int value = CommonFunction.convertTodp(this, 45);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(CommonFunction.convertTodp(this, 5), CommonFunction.convertTodp(this, 2), CommonFunction.convertTodp(this, 5), CommonFunction.convertTodp(this, 2));
        params.width = value;
        params.height = value;

        if(likeMemberList != null && likeMemberList.size() > 0) {
            for(int i = 0 ; i < likeMemberList.size() ; i++) {
                DocumentReference docRef = this.db.collection("member").document(likeMemberList.get(i));
                int finalI = i;
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserVo userVo = new UserVo();
                        userVo.setUid(documentSnapshot.getString("id"));
                        userVo.setUserName(documentSnapshot.getString("name"));
                        userVo.setIdentity(documentSnapshot.getString("identity"));
                        userVo.setPhotoUrl(documentSnapshot.getString("profileUrl"));

                        userVoList.add(userVo);

                        RadiusNetworkImageView imageView1 = new RadiusNetworkImageView(getApplicationContext());

                        imageView1.setRadius(100f);
                        imageView1.setImageUrl(documentSnapshot.getString("profileUrl"), VolleySingleton.getInstance(getApplicationContext()).getImageLoader());
                        imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView1.setLayoutParams(params);

                        imageView1.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                            intent.putExtra("uid", documentSnapshot.getString("id"));
                            intent.putExtra("userName", documentSnapshot.getString("name"));
                            intent.putExtra("identity", documentSnapshot.getString("identity"));
                            intent.putExtra("profileUrl", documentSnapshot.getString("profileUrl"));

                            /*intent.putExtra("bitmapImage", sayVoList.get(index).getBitmap());*/

                            Say.resumeYn = true;

                            startActivity(intent);
                        });

                        layout.addView(imageView1);

                        /*if(likeMemberList.size() == 5 && finalI == 4) {*/
                        if(finalI == 4) {

                            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                            int textMargin = CommonFunction.convertTodp(getApplicationContext(), 15);
                            int imageMargin = CommonFunction.convertTodp(getApplicationContext(), 20);

                            textParams.setMargins(50, textMargin, 0, textMargin);
                            textParams.width = CommonFunction.convertTodp(getApplicationContext(), 50);
                            textParams.height = CommonFunction.convertTodp(getApplicationContext(), 50);

                            imageParams.setMargins(0, imageMargin, 30, imageMargin);
                            imageParams.width = CommonFunction.convertTodp(getApplicationContext(), 24);
                            imageParams.height = CommonFunction.convertTodp(getApplicationContext(), 12);

                            TextView seeAllTx = new TextView(getApplicationContext());
                            seeAllTx.setText("See All");
                            seeAllTx.setTextColor(Color.rgb(4, 196, 201));
                            seeAllTx.setLayoutParams(textParams);
                            layout.addView(seeAllTx);

                            ImageView iv = new ImageView(getApplicationContext());
                            iv.setImageResource(R.drawable.ic_see_all);

                            iv.setLayoutParams(imageParams);
                            layout.addView(iv);
                        }
                        /*}*/
                    }
                });
            }
        }

        UltimateRecyclerView commentListView = (UltimateRecyclerView) findViewById(R.id.comment_list);
        CommentListViewAdapter commentListViewAdapter = new CommentListViewAdapter(this, commentList, commentReplyList);

        commentListView.setLayoutManager(new LinearLayoutManager(this));
        commentListView.setAdapter(commentListViewAdapter);
        commentListView.setHasFixedSize(false);

        /*commentListView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int index) {
                        *//*writeCommentText.addChip(userName, "");
                        RadiusNetworkImageView img = view.findViewById(R.id.user_profile_photo);
                        img.setOnClickListener(v -> {
                            Log.d("testtttt", "tetststststs");
                        });*//*
                    }
                })
        );*/

        /*commentListView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                int position = rv.getChildAdapterPosition(child);

                RadiusNetworkImageView img = child.findViewById(R.id.user_profile_photo);

                UserVo userVo = userVoList.get(position);

                img.setOnClickListener(v -> {
                    Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                    intent.putExtra("uid", userVo.getUid());
                    intent.putExtra("userName", userVo.getUserName());
                    intent.putExtra("identity", userVo.getIdentity());
                    intent.putExtra("profileUrl", userVo.getPhotoUrl());

                    Say.resumeYn = true;

                    startActivity(intent);
                });

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });*/

        FloatingActionButton writeCommentBtn = (FloatingActionButton) findViewById(R.id.write_comment_btn);
        this.writeCommentText = (TagsEditText) findViewById(R.id.write_comment_text);
        this.writeCommentText.setHint(String.format(getResources().getString(R.string.empty_arg), "댓글"));

        /*this.writeCommentText.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chip, int newSize) {
                // chip added
                // newSize is the size of the updated selected chip list
                writeCommentText.setFocusable(true);
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {
                // chip removed
                // newSize is the size of the updated selected chip list
                reCommentYn = false;
            }

            @Override
            public void onTextChanged(CharSequence text) {
                // text changed
                comment = text.toString();
            }
        });*/

        checkIndex();

        LinearLayout likeBtn = findViewById(R.id.like_btn);
        String finalIdentity1 = identity;
        likeBtn.setOnClickListener(v -> {

            checkIndex();

            int length = likeMemberList.size();

            // say -> like_members 업데이트
            // say_member_like 추가하기

            if(this.isLiked && length > 0 && userVoList.size() > 0) {
                likeMemberList.remove(this.i);
                likeSayList.remove(this.j);
                length--;
                this.isLiked = false;
                imageView1.setImageResource(R.drawable.ic_heart);

                int idx = 0;
                for(; idx < userVoList.size() ; idx++) {
                    if(this.user.getUid().equals(userVoList.get(idx).getUid())) {
                        break;
                    }
                }

                layout.removeAllViews();

                userVoList.remove(idx);

                idx = 0;
                for(; idx < userVoList.size() ; idx++) {
                    RadiusNetworkImageView myProfilePhoto = new RadiusNetworkImageView(getApplicationContext());

                    myProfilePhoto.setRadius(100f);
                    myProfilePhoto.setImageUrl(userVoList.get(idx).getPhotoUrl(), VolleySingleton.getInstance(getApplicationContext()).getImageLoader());
                    myProfilePhoto.setScaleType(ImageView.ScaleType.FIT_XY);
                    myProfilePhoto.setLayoutParams(params);

                    layout.addView(myProfilePhoto);
                }
            } else {
                UserVo userVo = new UserVo();
                userVo.setUid(userId);
                userVo.setUserName(user.getDisplayName());
                userVo.setPhotoUrl(user.getPhotoUrl().toString());
                userVo.setIdentity(finalIdentity1);

                userVoList.add(userVo);

                likeMemberList.add(userId);
                likeSayList.add(sayVo.getSayId());
                length++;
                this.isLiked = true;
                imageView1.setImageResource(R.drawable.ic_heart_red);

                RadiusNetworkImageView myProfilePhoto = new RadiusNetworkImageView(getApplicationContext());

                myProfilePhoto.setRadius(100f);
                myProfilePhoto.setImageUrl(user.getPhotoUrl().toString(), VolleySingleton.getInstance(getApplicationContext()).getImageLoader());
                myProfilePhoto.setScaleType(ImageView.ScaleType.FIT_XY);
                myProfilePhoto.setLayoutParams(params);

                layout.addView(myProfilePhoto);
            }

            sayVo.setLikeMembers(likeMemberList);
            Say.sayVoList.set(index, sayVo);

            this.db.collection("say").document(sayVo.getSayId())
                    .update("like_members", likeMemberList)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot successfully written!");

                        this.db.collection("member").document(this.userId)
                                .update("like_say", likeSayList)
                                .addOnSuccessListener(aVoid1 -> {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");

                                })
                                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

            likeCnt.setText(String.valueOf(length));
        });

        ArrayList<HashMap<String, Object>> finalCommentList = commentList;
        ArrayList<HashMap<String, Object>> finalCommentReplyList = commentReplyList;
        writeCommentBtn.setOnClickListener(v -> {

            List<String> tagList = writeCommentText.getTags();
            String tag = "";

            if(tagList.size() > 0) {
                tag = writeCommentText.getTags().get(0);
                reCommentYn = true;
            } else {
                reCommentYn = false;
            }

            String comment = writeCommentText.getEditableText().toString();
            if(!tag.isEmpty()) {
                comment = comment.replace(tag, "");
                comment = comment.substring(1, comment.length());
            }

            if(comment != null && !comment.isEmpty()) {

                HashMap<String, Object> commentMap = new HashMap<>();

                commentMap.put("comment", comment);
                commentMap.put("member_id", this.user.getUid());
                commentMap.put("reg_dt", new Date());

                HashMap<String, Object> sayMap = new HashMap<>();

                if(reCommentYn) {
                    commentMap.put("comment_id", commentId);
                    finalCommentReplyList.add(commentMap);
                    sayMap.put("comment_reply_list", finalCommentReplyList);
                    sayVo.setCommentReplyList(finalCommentReplyList);
                    finalCommentReplyList.clear();
                    finalCommentReplyList.add(commentMap);
                } else {
                    commentMap.put("id", String.valueOf(System.currentTimeMillis()));
                    finalCommentList.add(commentMap);
                    sayMap.put("comment_list", finalCommentList);
                }

                if(!this.isLiked) {
                    likeMemberList.add(this.user.getUid());
                    sayVo.setLikeMembers(likeMemberList);
                    sayMap.put("like_members", likeMemberList);
                }

                DocumentReference sayRef = this.db.collection("say").document(sayId);
                sayRef
                        .update(sayMap)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                            sayVo.setCommentList(finalCommentList);

                            commentCnt.setText(String.valueOf(finalCommentList.size() + finalCommentReplyList.size()));

                            if(!this.isLiked) {
                                imageView1.setImageResource(R.drawable.ic_heart_red);
                                this.isLiked = true;
                                likeCntStr[0] = String.valueOf(likeMemberList.size());
                                likeCnt.setText(likeCntStr[0]);

                                likeSayList.add(sayId);
                                NewSayListViewAdapter.likeSayList.add(sayId);

                                params.setMargins(10, 0, 10, 0);
                                params.width = value;
                                params.height = value;

                                RadiusNetworkImageView myProfilePhoto = new RadiusNetworkImageView(getApplicationContext());

                                myProfilePhoto.setRadius(100f);
                                myProfilePhoto.setImageUrl(user.getPhotoUrl().toString(), VolleySingleton.getInstance(getApplicationContext()).getImageLoader());
                                myProfilePhoto.setScaleType(ImageView.ScaleType.CENTER);
                                myProfilePhoto.setLayoutParams(params);

                                layout.addView(myProfilePhoto);

                                DocumentReference memberRef = this.db.collection("member").document(this.user.getUid());
                                memberRef
                                        .update("like_say", likeSayList)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                        })
                                        .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
                            }

                            if(!reCommentYn) {
                                finalCommentList.remove(finalCommentList.size() - 1);
                                commentListViewAdapter.insertLastInternal(finalCommentList, commentMap);
                            } else {
                                /*finalCommentReplyList.remove(finalCommentReplyList.size() - 1);
                                commentListViewAdapter.insertLastInternal(finalCommentReplyList, commentMap);*/
                                CommentListViewAdapter.commentReplyList = finalCommentReplyList;
                            }

                            commentListViewAdapter.notifyDataSetChanged();

                            Say.sayVoList.set(index, sayVo);
                            writeCommentText.getEditableText().clear();
                            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
            } else {
                Toast.makeText(this, String.format(this.getResources().getString(R.string.empty_arg), "댓글"), Toast.LENGTH_SHORT).show();
            }
        });

        /*commentListView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int index) {
                        Log.d("vidd", String.valueOf(view.getId()));
                    }
                })
        );*/
    }

    @Override
    public void onTagsChanged(Collection<String> tags) {
        Log.d(TAG, "Tags changed: ");
        Log.d(TAG, Arrays.toString(tags.toArray()));

        if(tags.size() == 0) {
            this.reCommentYn = false;
        }
    }

    @Override
    public void onEditingFinished() {

    }

    private void checkIndex() {
        int likeMemberListSize = (this.likeMemberList != null ? this.likeMemberList.size() : 0);

        if(likeMemberListSize > 0 && likeSayList.size() > 0) {
            for (; this.i < likeMemberListSize; this.i++) {
                if (likeMemberList.get(this.i).equals(this.userId)) {
                    this.isLiked = true;
                    break;
                }
            }

            for (; this.j < likeSayList.size(); this.j++) {
                if (likeSayList.get(this.j).equals(sayVo.getSayId())) {
                    break;
                }
            }
        }
    }
}
