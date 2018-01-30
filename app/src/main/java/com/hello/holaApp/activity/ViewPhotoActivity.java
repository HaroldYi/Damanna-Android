package com.hello.holaApp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.common.DownloadImageTask;
import com.hello.holaApp.common.VolleySingleton;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by lji5317 on 13/12/2017.
 */

@Deprecated
public class ViewPhotoActivity extends BaseActivity {

    private NetworkImageView imageView;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonFunction.sendMsg(getApplicationContext());

        //타이틀바 없애기
        /*requestWindowFeature(Window.FEATURE_NO_TITLE);*/
        setContentView(R.layout.view_photo_activity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.

        View view = getLayoutInflater().inflate(R.layout.custom_action, null);
        actionBar.setCustomView(view);

        Button backBtn = (Button) view.findViewById(R.id.backBtn);
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);

        backBtn.setOnClickListener(view1 -> {
            finish();
        });

        //데이터 가져오기
        Intent intent = getIntent();
        String photoUrl = intent.getStringExtra("photoUrl");
        Bitmap bitmap = intent.getParcelableExtra("bitmap");
        String userName = intent.getStringExtra("userName");
        textView.setText(userName);

        this.imageView = (NetworkImageView) findViewById(R.id.imageView);

        PhotoViewAttacher attacher = new PhotoViewAttacher(this.imageView);
        attacher.setZoomable(true);
        attacher.setScaleType(ImageView.ScaleType.FIT_XY);

        this.imageLoader = VolleySingleton.getInstance(this).getImageLoader();

        if(bitmap == null) {
            /*this.imageView.setImageUrl(photoUrl, this.imageLoader);*/
            StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("original/" + photoUrl + ".jpg");

            islandRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                //do something with downloadurl
                this.imageView.setImageUrl(downloadUrl.toString(), this.imageLoader);
            });

        }
            /*getBitmapFromURL(photoUrl);*/

        else
            /*this.imageView.setImageBitmap(bitmap);*/
            this.imageView.setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    private void getBitmapFromURL(String fileName) {
        if(fileName.indexOf("http") != -1) {
            DownloadImageTask downloadImageTask = new DownloadImageTask(this.imageView);
            downloadImageTask.execute(fileName);
        } else {
            StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("images/" + fileName);

            final long ONE_MEGABYTE = 10240 * 10240;
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                // Data for "images/island.jpg" is returns, use this as needed
                this.imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Log.e("bytess", exception.getMessage());
            });
        }
    }
}
