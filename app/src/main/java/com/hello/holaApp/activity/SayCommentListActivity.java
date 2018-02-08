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
import android.widget.EditText;
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
import com.hello.holaApp.vo.SayVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by lji5317 on 08/02/2018.
 */

public class SayCommentListActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseFirestore db;

    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.say_comment_list);

        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.db = FirebaseFirestore.getInstance();

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

        SayVo sayVo = (SayVo) intent.getSerializableExtra("sayVo");

        /*String uid = intent.getStringExtra("uid");
        String sayId = intent.getStringExtra("sayId");
        String userName = intent.getStringExtra("userName");
        String identity = intent.getStringExtra("identity");
        String profileUrl = intent.getStringExtra("profileUrl");
        String nation = intent.getStringExtra("nation");
        String distance = intent.getStringExtra("distance");
        String content = intent.getStringExtra("content");*/

        String uid = sayVo.getUid();
        String sayId = sayVo.getSayId();
        String userName = intent.getStringExtra("userName");
        String identity = intent.getStringExtra("identity");
        String profileUrl = intent.getStringExtra("profileUrl");
        String nation = intent.getStringExtra("nation");
        String distance = sayVo.getDistance();
        String content = sayVo.getMsg();

        ArrayList<HashMap<String, Object>> commentList = sayVo.getCommentList();

        int index = intent.getIntExtra("index", 0);

        boolean isLiked = intent.getBooleanExtra("isLiked", false);

        ArrayList<String> likeMemberList = intent.getStringArrayListExtra("likeMemberList");

        if(identity.indexOf("워킹") != -1) {
            identity = "워홀";
        }

        TextView userNameView = findViewById(R.id.user_name);
        userNameView.setText(String.format("%s (%s, %s)", userName, nation, identity));

        TextView distanceView = findViewById(R.id.distance);
        distanceView.setText(distance);

        imageView.setImageUrl(profileUrl, VolleySingleton.getInstance(this).getImageLoader());

        TextView contentVIew = findViewById(R.id.content);
        contentVIew.setText(content);

        title.setText(userName);

        ImageView imageView1 = findViewById(R.id.like_ic);
        TextView likeCnt = findViewById(R.id.like_cnt);

        String likeCntStr = "0";

        if(likeMemberList != null && likeMemberList.size() > 0) {
            likeCntStr = String.valueOf(likeMemberList.size());
        }

        likeCnt.setText(likeCntStr);

        if(isLiked) {
            imageView1.setImageResource(R.drawable.ic_heart_red);
        } else {
            imageView1.setImageResource(R.drawable.ic_heart);
        }

        LinearLayout layout = findViewById(R.id.like_people_list);

        int value = CommonFunction.convertTodp(this, 50);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(10, 0, 10, 0);
        params.width = value;
        params.height = value;

        if(likeMemberList != null && likeMemberList.size() > 0) {
            for(int i = 0 ; i < likeMemberList.size() ; i++) {
                DocumentReference docRef = db.collection("member").document(likeMemberList.get(0));
                int finalI = i;
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        RadiusNetworkImageView imageView1 = new RadiusNetworkImageView(getApplicationContext());

                        imageView1.setRadius(100f);
                        imageView1.setImageUrl(documentSnapshot.getString("profileUrl"), VolleySingleton.getInstance(getApplicationContext()).getImageLoader());
                        imageView1.setScaleType(ImageView.ScaleType.CENTER);
                        imageView1.setLayoutParams(params);

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

        if(commentList == null) {
            commentList = new ArrayList<HashMap<String, Object>>();
        }

        UltimateRecyclerView commentListView = (UltimateRecyclerView) findViewById(R.id.comment_list);
        CommentListViewAdapter commentListViewAdapter = new CommentListViewAdapter(this, commentList);

        commentListView.setLayoutManager(new LinearLayoutManager(this));
        commentListView.setAdapter(commentListViewAdapter);
        commentListView.setHasFixedSize(false);

        FloatingActionButton writeCommentBtn = (FloatingActionButton) findViewById(R.id.write_comment_btn);
        EditText writeCommentText = (EditText) findViewById(R.id.write_comment_text);

        ArrayList<HashMap<String, Object>> finalCommentList = commentList;
        writeCommentBtn.setOnClickListener(v -> {
            String comment = writeCommentText.getText().toString();
            if(comment != null && !comment.isEmpty()) {
                HashMap<String, Object> commentMap = new HashMap<>();
                commentMap.put("comment", comment);
                commentMap.put("member_id", this.user.getUid());
                commentMap.put("reg_dt", new Date());
                /*finalCommentList.add(commentMap);*/

                DocumentReference sayRef = this.db.collection("say").document(sayId);
                sayRef
                    .update("comment_list", finalCommentList)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        sayVo.setCommentList(finalCommentList);
                        NewSayListViewAdapter.sayVoList.set(index, sayVo);
                        commentListViewAdapter.insertLastInternal(finalCommentList, commentMap);
                        writeCommentText.setText("");
                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
            } else {
                Toast.makeText(this, String.format(this.getResources().getString(R.string.empty_arg), "댓글"), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
