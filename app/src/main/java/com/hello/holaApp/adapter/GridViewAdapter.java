package com.hello.holaApp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hello.holaApp.R;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.vo.PhotoVo;

import java.util.List;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private List<PhotoVo> list;
    private boolean flag = false;
    private ImageLoader imageLoader;

    public GridViewAdapter(Context context, List<PhotoVo> list) {
        this.context = context;
        this.list = list;
    }

    public GridViewAdapter(Context context, List<PhotoVo> list, boolean flag) {
        this.context = context;
        this.list = list;
        this.flag = flag;
    }

    public void clearAdapter(){
        this.list.clear();
    }

    public void addNewValues(List<PhotoVo> photoVoList){
        this.list = photoVoList;
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

        this.imageLoader = VolleySingleton.getInstance(this.context).getImageLoader();

        ViewHolder holder = (ViewHolder) view.getTag();
        PhotoVo photoVo = list.get(index);

        if(photoVo.getBitmap() == null) {
            if (photoVo.getKind().equals("profile")) {

                /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
                downloadImageTask.execute(photoVo.getFileName());*/

            } else if (photoVo.getKind().equals("photoVo")) {
                StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("thumbnail/" + photoVo.getFileName() + "_thumbnail.jpg");

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
            } else if (photoVo.getKind().equals("add_btn")) {
                holder.img.setDefaultImageResId(R.drawable.add_btn);
            } else if (photoVo.getKind().equals("logo_t")) {
                holder.img.setDefaultImageResId(R.drawable.logo_t);
            }
        } else
            holder.img.setImageBitmap(photoVo.getBitmap());

        return view;
    }

    class ViewHolder {
        private NetworkImageView img;

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
