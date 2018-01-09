package com.hello.TrevelMeetUp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.common.DownloadImageTask;
import com.hello.TrevelMeetUp.vo.Photo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class GridViewAdapter extends BaseAdapter {

    private static Context context;
    private List<Photo> list;
    private boolean flag = false;

    public GridViewAdapter(Context context, List<Photo> list) {
        this.context = context;
        this.list = list;
    }

    public GridViewAdapter(Context context, List<Photo> list, boolean flag) {
        this.context = context;
        this.list = list;
        this.flag = flag;
    }

    @Override
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

        ViewHolder holder = (ViewHolder) view.getTag();
        Photo photo = list.get(index);

        if(photo.getBitmap() == null) {
            if (photo.getKind().equals("profile")) {

                /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
                downloadImageTask.execute(photo.getFileName());*/


            } else if (photo.getKind().equals("photo")) {
                StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("images/thumbnail/" + photo.getFileName() + "_thumbnail");

                final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    // Data for "images/island.jpg" is returns, use this as needed
                    holder.img.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                    Log.e("bytess", exception.getMessage());
                });
            } else if (photo.getKind().equals("add_btn")) {
                holder.img.setImageResource(R.drawable.add_btn);
            } else if (photo.getKind().equals("logo_t")) {
                holder.img.setImageResource(R.drawable.logo_t);
            }
        } else
            holder.img.setImageBitmap(photo.getBitmap());

        return view;
    }

    class ViewHolder {
        private TextView distance;
        private TextView updateTime;
        private ImageView img;

        private boolean flag;

        public ViewHolder(View view) {

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

            img = (ImageView) view.findViewById(R.id.img1);
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) img.getLayoutParams();
            params.width = metrics.widthPixels / 4;
            params.height = metrics.widthPixels / 4;

            img.setLayoutParams(params);

            view.setTag(this);
        }
    }


    /*public class GridItem extends LinearLayout {
        private TextView distance;
        private TextView updateTime;
        private ImageView img;

        private boolean flag;

        public GridItem(Context context, boolean flag) {
            super(context);

            this.flag = flag;
            init(context);
        }

        public void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.griditem,this);
            if(!this.flag) {

                LinearLayout linearLayout = new LinearLayout(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                linearLayout.setLayoutParams(params);

                LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvParams.setMargins(0, 10, 0, 0);
                tvParams.weight = 1;

                TextView distance = new TextView(getContext());
                distance.setTextSize(20);
                distance.setLayoutParams(params);

                TextView updateTime = new TextView(getContext());
                updateTime.setTextSize(20);
                updateTime.setLayoutParams(params);

                linearLayout.addView(distance);
                linearLayout.addView(updateTime);
            }

            img = (ImageView)findViewById(R.id.img1);
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);

            LayoutParams params = (LayoutParams) img.getLayoutParams();
            params.width = metrics.widthPixels / 4;
            params.height = metrics.widthPixels / 4;

            img.setLayoutParams(params);
        }

        public void setData(Photo photo) {
            if(!this.flag) {
                distance.setText(String.format("%.2f", photo.getDistance()));
                updateTime.setText("0000");
            }

            if(photo.getBitmap() == null) {
                if (photo.getKind().equals("profile")) {

                    DownloadImageTask downloadImageTask = new DownloadImageTask(img, "grid");
                    downloadImageTask.execute(photo.getFileName());

                } else if (photo.getKind().equals("photo")) {
                    StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("images/thumbnail/" + photo.getFileName() + "_thumbnail");

                    final long ONE_MEGABYTE = 1024 * 1024;
                    islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        // Data for "images/island.jpg" is returns, use this as needed
                        img.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                        Log.e("bytess", exception.getMessage());
                    });
                } else if (photo.getKind().equals("add_btn")) {
                    img.setImageResource(R.drawable.add_btn);
                } else if (photo.getKind().equals("logo_t")) {
                    img.setImageResource(R.drawable.logo_t);
                }
            } else
                img.setImageBitmap(photo.getBitmap());
        }
    }*/
}
