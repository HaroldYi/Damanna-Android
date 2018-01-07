package com.hello.TrevelMeetUp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.activity.PopupActivity;
import com.hello.TrevelMeetUp.activity.ViewPhotoActivity;
import com.hello.TrevelMeetUp.adapter.GridViewAdapter;
import com.hello.TrevelMeetUp.adapter.UserSayListViewAdapter;
import com.hello.TrevelMeetUp.common.CommonFunction;
import com.hello.TrevelMeetUp.common.Constant;
import com.hello.TrevelMeetUp.view.ExpandableHeightGridView;
import com.hello.TrevelMeetUp.view.ExpandableHeightListView;
import com.hello.TrevelMeetUp.vo.Photo;
import com.hello.TrevelMeetUp.vo.SayVo;
import com.meg7.widget.CircleImageView;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lji5317 on 05/12/2017.
 */

public class Profile extends BaseFragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private Bitmap profileBitmap;

    private ExpandableHeightGridView gridView;
    private String kind;
    private int position;

    private List<Photo> photoList;
    private GridViewAdapter adapter;
    private CircleImageView profileImageView;

    private FirebaseFirestore db;

    private ExpandableHeightListView listView;
    private List<SayVo> sayVoList;
    private UserSayListViewAdapter userSayListViewAdapter;

    private Button button;
    private int lastIndex;

    private static String TAG = "cloudFireStore";

    public Profile() {
        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();
        // Access a Cloud Firestore instance from your Activity
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        progressON(getResources().getString(R.string.loading));

        View view = inflater.inflate(R.layout.profile_layout, container, false);
        this.profileImageView = (CircleImageView) view.findViewById(R.id.user_profile_photo);
        this.profileImageView.setOnClickListener(view1 -> {
            viewPhoto(this.profileBitmap);
        });
        TextView textView = (TextView) view.findViewById(R.id.user_profile_name);

        this.profileBitmap = CommonFunction.getBitmapFromURL(this.user.getPhotoUrl().toString());

        this.profileImageView.bringToFront();
        this.profileImageView.setImageBitmap(this.profileBitmap);
        textView.setText(this.user.getDisplayName());

        view.setVisibility(View.INVISIBLE);

        this.gridView = (ExpandableHeightGridView) view.findViewById(R.id.photo_list);
        this.gridView.setExpanded(true);
        this.gridView.setVisibility(View.INVISIBLE);

        this.listView = (ExpandableHeightListView) view.findViewById(R.id.say_list);
        this.listView.setExpanded(true);
        this.listView.setVisibility(View.INVISIBLE);

        this.photoList = new ArrayList<>();
        this.sayVoList = new ArrayList<>();

        this.button = (Button) view.findViewById(R.id.newSayBtn);
        this.button.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), PopupActivity.class);
            intent.putExtra("data", "Test Popup");
            startActivityForResult(intent, 1);
        });

        this.adapter = new GridViewAdapter(getActivity(), this.photoList, true);
        this.gridView.setAdapter(adapter);

        this.db.collection("member/")
                /*.orderBy("reg_dt", Query.Direction.ASCENDING)*/
                .document(this.user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            DateTime dateTime = new DateTime();

                            long dateOfBirth = ((Date) document.getData().get("dateOfBirth")).getTime();
                            long now = System.currentTimeMillis();

                            Calendar birthCalendar = Calendar.getInstance();
                            birthCalendar.setTimeInMillis(dateOfBirth);

                            int yearOfBirth = birthCalendar.get(Calendar.YEAR);

                            Calendar nowCalender = Calendar.getInstance();
                            nowCalender.setTimeInMillis(now);

                            int nowYear = nowCalender.get(Calendar.YEAR);

                            int koreanAge = nowYear - yearOfBirth + 1;
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        this.db.collection("photo/")
                /*.orderBy("reg_dt", Query.Direction.ASCENDING)*/
                .whereEqualTo("member_id", this.user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Photo photo = new Photo();
                        photo.setFileName(this.user.getPhotoUrl().toString());
                        photo.setKind("profile");
                        this.photoList.add(photo);

                        if(task.getResult().size() > 0) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Photo p = new Photo();
                                p.setFileName(document.getData().get("fileName").toString());
                                p.setKind("photo");
                                this.photoList.add(p);
                            }
                        }

                        Photo photo1 = new Photo();
                        photo1.setKind("add_btn");
                        this.photoList.add(photo1);

                        int size = this.photoList.size();
                        this.lastIndex = this.photoList.size() - 1;
/*
                        if(size % 4 != 0) {
                            for (int i = 0; i < (4 - size % 4); i++) {
                                Photo photo2 = new Photo();
                                photo2.setKind("logo_t");
                                this.photoList.add(photo2);
                            }
                        }*/

                        this.adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        this.db.collection("say/")
                .whereEqualTo("member_id", this.user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        this.userSayListViewAdapter = new UserSayListViewAdapter(getActivity(), R.layout.chat_layout, this.sayVoList);
                        this.listView.setAdapter(this.userSayListViewAdapter);

                        if(task.getResult().size() > 0) {
                            for (DocumentSnapshot document : task.getResult()) {

                                SayVo sayVo = new SayVo();
                                sayVo.setMsg(document.getData().get("content").toString());
                                sayVo.setNoMsg(false);

                                this.sayVoList.add(sayVo);
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

                        /*new Handler().postDelayed(() -> {
                            progressOFF();
                            view.setVisibility(View.VISIBLE);
                            this.gridView.setVisibility(View.VISIBLE);
                            this.listView.setVisibility(View.VISIBLE);
                        }, 100);*/
                        this.userSayListViewAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        this.gridView.setOnItemClickListener((parent, v, position, id) -> {
            this.kind = this.photoList.get(position).getKind();
            this.position = position;

            if(!this.kind.equals("logo_t")) {

                if(this.kind.equals("photo") || this.kind.equals("profile")) {
                    viewPhoto(null);
                } else
                    getActivity().openOptionsMenu();
            }
        });

        return view;
    }

    /*@Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(this.kind.equals("add_btn")) {
            menu.getItem(0).setVisible(false);
        }
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.popupmenu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.take:
                takePhoto();
                break;

            case R.id.choose:
                selectPhoto();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != 1) {
            if (resultCode == Activity.RESULT_OK) {

                switch (requestCode) {

                    case Constant.GALLERY_CODE:
                        sendPicture(data); //갤러리에서 가져오기
                        break;
                    case Constant.CAMERA_CODE:
                        sendPicture(data); //카메라에서 가져오기
                        break;

                    default:
                        break;
                }
            }
        } else if(requestCode == 1) {
            if(resultCode == Activity.RESULT_OK) {
                //데이터 받기
                String sayContent = data.getStringExtra("sayContent");

                Map<String, Object> stringMap = new HashMap<>();

                stringMap.put("member_id", this.user.getUid());
                stringMap.put("content", sayContent);
                stringMap.put("reg_dt", new Date());

                DocumentReference memberReference = this.db.collection("member").document(this.user.getUid());
                stringMap.put("member", memberReference);

                this.db.collection("say")
                        .add(stringMap)
                        .addOnSuccessListener(documentReference -> {
                            //
                            if(this.sayVoList.size() == 1 && this.sayVoList.get(0).isNoMsg()) {
                                this.sayVoList.clear();
                            }

                            SayVo say = new SayVo();
                            say.setMsg(sayContent);

                            this.sayVoList.add(say);
                            this.userSayListViewAdapter.notifyDataSetChanged();

                        })
                        .addOnFailureListener(e -> {
                            //
                            Log.w(TAG, "Error adding document", e);
                        });
            }
        }
    }

    private void sendPicture(Intent data) {

        Uri imgUri = data.getData();
        String imagePath = getRealPathFromURI(imgUri); // path 경로

        String fileName = imgUri.getLastPathSegment();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + fileName + ".jpg");
        StorageReference storageThumRef = storage.getReference().child("images/thumbnail/" + fileName + "_thumbnail" + ".jpg");

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        // 1. 촬영이미지 스토리지에 업로드
        // 2. 업로드된 url photo에 추가.
        // 3. DB에 추가

        // Get the data from an ImageView as bytes
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 1000;
        options.outWidth = 1000;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);//경로를 통해 비트맵으로 전환
        /*bitmap = rotate(bitmap, exifDegree);*/

        Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, bitmap.getHeight() / 10, bitmap.getHeight() / 10, false);
        ByteArrayOutputStream bitmapOps = new ByteArrayOutputStream();
        ByteArrayOutputStream bitmapThumOps = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmapOps);
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bitmapThumOps);

        byte[] bitmapByte = bitmapOps.toByteArray();
        byte[] bitmapThumByte = bitmapThumOps.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(bitmapByte);
        UploadTask uploadThumTask = storageThumRef.putBytes(bitmapThumByte);

        Bitmap finalBitmap = thumbnail;
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
        }).addOnSuccessListener(taskSnapshot -> {

            uploadThumTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
            }).addOnSuccessListener(taskThumSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                this.photoList.get(this.lastIndex).setFileName(fileName);
                this.photoList.get(this.lastIndex).setBitmap(finalBitmap);
                this.photoList.get(this.lastIndex).setKind("photo");

                Photo photo = new Photo();
                photo.setKind("add_btn");
                if((this.lastIndex + 1) < this.photoList.size()) {
                    this.photoList.set((this.lastIndex + 1), photo);
                } else {
                    this.photoList.add(photo);
                }

                /*List<Photo> tempList = new ArrayList<>();
                for(Photo temp : this.photoList) {
                    tempList.add(temp);
                }*/

                this.lastIndex++;

                /*int size = this.photoList.size() % 4;
                if(size != 0) {
                    for(int i = 0 ; i < (4 - size) ; i++) {
                        Photo photo1 = new Photo();
                        photo1.setKind("logo_t");
                        this.photoList.add(photo1);
                    }
                }*/

                /*progressON(getResources().getString(R.string.uploading));*/
                /*this.adapter.notifyDataSetChanged();*/
                this.gridView.invalidate();
                this.gridView.setAdapter(adapter);

                Map<String, Object> photoMap = new HashMap<>();

                photoMap.put("member_id", this.user.getUid());
                photoMap.put("fileName", fileName);
                photoMap.put("reg_dt", new Date());

                this.db.collection("photo")
                        .add(photoMap)
                        .addOnSuccessListener(documentReference -> {
                            //
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            //
                            Log.w(TAG, "Error adding document", e);
                        });

                /*new Handler().postDelayed(() -> {
                    progressOFF();
                    this.listView.setVisibility(View.VISIBLE);
                }, 1000);*/
            });
        });
    }

    public Bitmap rotate(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);

        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    public String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }

    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private void viewPhoto(Bitmap bitmap) {
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(getActivity(), ViewPhotoActivity.class);
        if(bitmap != null) {
            intent.putExtra("bitmap", bitmap);
        }
        intent.putExtra("photoUrl", photoList.get(this.position).getFileName());
        startActivityForResult(intent, 1);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constant.CAMERA_CODE);
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Constant.CAMERA_CODE);
    }

    private View.OnTouchListener menuListener = (v, event) -> {
        getActivity().openOptionsMenu();
        return false;
    };
}