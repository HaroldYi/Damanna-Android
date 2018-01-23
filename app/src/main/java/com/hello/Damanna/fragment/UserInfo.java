package com.hello.Damanna.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.Damanna.R;
import com.hello.Damanna.activity.ViewPhotoActivity;
import com.hello.Damanna.adapter.GridViewAdapter;
import com.hello.Damanna.adapter.RecyclerGridViewAdapter;
import com.hello.Damanna.adapter.UserSayListViewAdapter;
import com.hello.Damanna.common.CommonFunction;
import com.hello.Damanna.view.ExpandableHeightGridView;
import com.hello.Damanna.view.ExpandableHeightListView;
import com.hello.Damanna.vo.Photo;
import com.hello.Damanna.vo.SayVo;
import com.meg7.widget.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lji5317 on 14/12/2017.
 */

public class UserInfo extends BaseFragment implements View.OnClickListener {

    private static String TAG = "cloudFireStore";

    private ExpandableHeightListView listView;
    private List<SayVo> sayVoList;

    private List<Photo> photoList;

    private ExpandableHeightGridView gridView;
    private UserSayListViewAdapter userSayListViewAdapter;

    private int position;
    private String userName;
    private String uid;

    @Override
    public void onClick(View view) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        progressON(getResources().getString(R.string.loading));

        View view = inflater.inflate(R.layout.user_info_fragment, container, false);
        view.setVisibility(View.INVISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Bundle bundle = getArguments();
        this.uid = bundle.getString("uid");
        this.userName = bundle.getString("userName");
        String profileUrl = bundle.getString("profileUrl");

        this.gridView = (ExpandableHeightGridView) view.findViewById(R.id.photo_list);
        this.gridView.setExpanded(true);
        this.gridView.setVisibility(View.INVISIBLE);

        Bitmap bitmap = CommonFunction.getBitmapFromURL(profileUrl);

        CircleImageView imageView = (CircleImageView) view.findViewById(R.id.user_profile_photo);
        imageView.setImageBitmap(bitmap);
        imageView.bringToFront();
        imageView.setOnClickListener(view1 -> {
            viewPhoto(bitmap);
        });

        TextView textView = (TextView) view.findViewById(R.id.user_profile_name);
        textView.setText(userName);

        this.listView = (ExpandableHeightListView) view.findViewById(R.id.say_list);
        this.listView.setExpanded(true);
        this.listView.setVisibility(View.INVISIBLE);

        this.photoList = new ArrayList<>();
        this.sayVoList = new ArrayList<>();

        db.collection("photo/")
                .whereEqualTo("member_id", this.uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Photo photo = new Photo();
                        photo.setBitmap(bitmap);
                        photo.setKind("profile");
                        photo.setFileName(profileUrl);
                        this.photoList.add(photo);

                        for (DocumentSnapshot document : task.getResult()) {
                            Photo p = new Photo();
                            p.setFileName(document.getData().get("fileName").toString());
                            p.setKind("photo");
                            this.photoList.add(p);
                        }

                        RecyclerGridViewAdapter adapter = new RecyclerGridViewAdapter(getActivity(), this.photoList, true);
                        this.gridView.setAdapter(adapter);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        db.collection("say/")
                .whereEqualTo("member_id", this.uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        this.userSayListViewAdapter = new UserSayListViewAdapter(getActivity(), R.layout.chat_layout, this.sayVoList);
                        this.listView.setAdapter(userSayListViewAdapter);

                        for (DocumentSnapshot document : task.getResult()) {

                            db.collection("member/")
                                    .whereEqualTo("id", this.uid)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            if(task1.getResult().size() > 0) {
                                                for (DocumentSnapshot document1 : task1.getResult()) {
                                                    SayVo sayVo = new SayVo();

                                                    sayVo.setUserName(document1.getData().get("name").toString());
                                                    sayVo.setPhotoUrl(document1.getData().get("profileUrl").toString());
                                                    sayVo.setMsg(document.getData().get("content").toString());
                                                    sayVo.setNoMsg(false);

                                                    this.sayVoList.add(sayVo);

                                                    /*new Handler().postDelayed(() -> {
                                                        progressOFF();
                                                        view.setVisibility(View.VISIBLE);
                                                        this.gridView.setVisibility(View.VISIBLE);
                                                        this.listView.setVisibility(View.VISIBLE);
                                                    }, 100);*/
                                                }
                                            } else {
                                                SayVo sayVo = new SayVo();
                                                sayVo.setMsg("등록된 내용이 없습니다.");
                                                sayVo.setNoMsg(true);
                                                this.sayVoList.add(sayVo);
                                            }

                                            progressOFF();
                                            view.setVisibility(View.VISIBLE);
                                            this.gridView.setVisibility(View.VISIBLE);
                                            this.listView.setVisibility(View.VISIBLE);
                                            this.userSayListViewAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.w(TAG, "Error getting documents.", task1.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        /*this.gridView.setOnItemClickListener((parent, v, position, id) -> {
            *//*Intent viewIntent = new Intent(getActivity(), ViewPhotoActivity.class);
            viewIntent.putExtra("photoUrl", photoList.get(position).getFileName());
            startActivityForResult(viewIntent, 1);*//*
            this.position = position;
            viewPhoto(null);
        });*/

        return view;
    }

    private void viewPhoto(Bitmap bitmap) {
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(getActivity(), ViewPhotoActivity.class);
        if(bitmap != null) {
            intent.putExtra("bitmap", bitmap);
        }
        intent.putExtra("photoUrl", this.photoList.get(this.position).getFileName());
        intent.putExtra("userName", this.userName);
        startActivityForResult(intent, 1);
    }
}
