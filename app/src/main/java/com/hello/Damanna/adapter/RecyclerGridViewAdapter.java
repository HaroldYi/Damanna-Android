package com.hello.Damanna.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hello.Damanna.R;
import com.hello.Damanna.common.VolleySingleton;
import com.hello.Damanna.vo.Photo;

import java.util.List;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class RecyclerGridViewAdapter extends RecyclerView.Adapter<RecyclerGridViewAdapter.ViewHolder> {

    private Context context;
    private List<Photo> list;
    private boolean flag = false;
    private ImageLoader imageLoader;

    public RecyclerGridViewAdapter(Context context, List<Photo> list) {
        this.context = context;
        this.list = list;
    }

    public RecyclerGridViewAdapter(Context context, List<Photo> list, boolean flag) {
        this.context = context;
        this.list = list;
        this.flag = flag;
    }

/*    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int index) {
        return list.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {

        if(view == null) {
            view = View.inflate(this.context.getApplicationContext(), R.layout.griditem, null);
            new ViewHolder(view);
        }

        this.imageLoader = VolleySingleton.getInstance(this.context).getImageLoader();

        ViewHolder holder = (ViewHolder) view.getTag();
        Photo photo = list.get(index);

        if(photo.getBitmap() == null) {
            if (photo.getKind().equals("profile")) {

                *//*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
                downloadImageTask.execute(photo.getFileName());*//*

            } else if (photo.getKind().equals("photo")) {
                StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("thumbnail/" + photo.getFileName() + "_thumbnail.jpg");

                islandRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    //do something with downloadurl
                    holder.img.setImageUrl(downloadUrl.toString(), this.imageLoader);
                });

                *//*final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    // Data for "images/island.jpg" is returns, use this as needed

                    Bitmap src = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap resized = Bitmap.createScaledBitmap(src, src.getWidth(), src.getHeight() / 2, true);

                    holder.img.setBackground(new BitmapDrawable(resized));
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                    Log.e("bytess", exception.getMessage());
                });*//*
            } else if (photo.getKind().equals("add_btn")) {
                holder.img.setDefaultImageResId(R.drawable.add_btn);
            } else if (photo.getKind().equals("logo_t")) {
                holder.img.setDefaultImageResId(R.drawable.logo_t);
            }
        } else
            holder.img.setImageBitmap(photo.getBitmap());

        return view;
    }*/

    // 필수로 Generate 되어야 하는 메소드 1 : 새로운 뷰 생성
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 새로운 뷰를 만든다
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.griditem, parent,false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    // 필수로 Generate 되어야 하는 메소드 2 : ListView의 getView 부분을 담당하는 메소드
    @Override
    public void onBindViewHolder(ViewHolder holder, int index) {
        this.imageLoader = VolleySingleton.getInstance(this.context).getImageLoader();

        Photo photo = list.get(index);

        if(photo.getBitmap() == null) {
            if (photo.getKind().equals("profile")) {

                /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
                downloadImageTask.execute(photo.getFileName());*/

            } else if (photo.getKind().equals("photo")) {
                StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("thumbnail/" + photo.getFileName() + "_thumbnail.jpg");

                islandRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    //do something with downloadurl
                    holder.img.setImageUrl(downloadUrl.toString(), this.imageLoader);
                });

                /*final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    // Data for "images/island.jpg" is returns, use this as needed

                    Bitmap src = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap resized = Bitmap.createScaledBitmap(src, src.getWidth(), src.getHeight() / 2, true);

                    holder.img.setBackground(new BitmapDrawable(resized));
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                    Log.e("bytess", exception.getMessage());
                });*/
            } else if (photo.getKind().equals("add_btn")) {
                holder.img.setDefaultImageResId(R.drawable.add_btn);
            } else if (photo.getKind().equals("logo_t")) {
                holder.img.setDefaultImageResId(R.drawable.logo_t);
            }
        } else
            holder.img.setImageBitmap(photo.getBitmap());
    }

    // 필수로 Generate 되어야 하는 메소드 3
    @Override
    public int getItemCount() {
        return this.list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private NetworkImageView img;

        private boolean flag;

        public ViewHolder(View view) {
            super(view);

            if(!this.flag) {

                LinearLayout linearLayout = new LinearLayout(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                linearLayout.setLayoutParams(params);

                LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvParams.setMargins(0, 10, 0, 0);
                tvParams.weight = 1;

                TextView distance = new TextView(context);
                distance.setTextSize(20);
                distance.setLayoutParams(params);

                TextView updateTime = new TextView(context);
                updateTime.setTextSize(20);
                updateTime.setLayoutParams(params);

                linearLayout.addView(distance);
                linearLayout.addView(updateTime);
            }

            this.img = (NetworkImageView) view.findViewById(R.id.img1);
            /*DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) img.getLayoutParams();
            params.width = metrics.widthPixels / 4;
            params.height = metrics.heightPixels / 4;

            img.setLayoutParams(params);*/

            view.setTag(this);
        }
    }
}
