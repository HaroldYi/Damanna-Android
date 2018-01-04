package com.hello.TrevelMeetUp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.common.DownloadImageTask;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class ViewPhotoActivity extends BaseActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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

        this.imageView = (ImageView) findViewById(R.id.imageView);

        PhotoViewAttacher attacher = new PhotoViewAttacher(this.imageView);
        attacher.setZoomable(true);
        /*attacher.setScaleType(ImageView.ScaleType.FIT_XY);*/

        if(bitmap == null)
            getBitmapFromURL(photoUrl);

        else
            this.imageView.setImageBitmap(bitmap);
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
