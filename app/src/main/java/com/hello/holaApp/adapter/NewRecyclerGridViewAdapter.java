package com.hello.holaApp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hello.holaApp.R;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.fragment.Profile;
import com.hello.holaApp.vo.PhotoVo;
import com.marshalchen.ultimaterecyclerview.UltimateGridLayoutAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import java.util.List;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class NewRecyclerGridViewAdapter extends UltimateGridLayoutAdapter<PhotoVo, NewRecyclerGridViewAdapter.ViewHolder> {

    private Context context;
    private List<PhotoVo> list;
    private boolean profileYn = true;
    private static ImageLoader imageLoader;

    private static String TAG = "cloudFireStore";

    public NewRecyclerGridViewAdapter(Context context, List<PhotoVo> list) {
        super(list);
        this.context = context;
        this.list = list;
        this.imageLoader = VolleySingleton.getInstance(this.context).getImageLoader();
    }

    public NewRecyclerGridViewAdapter(Context context, List<PhotoVo> list, boolean profileYn) {
        super(list);
        this.context = context;
        this.list = list;
        this.profileYn = profileYn;
        this.imageLoader = VolleySingleton.getInstance(this.context).getImageLoader();
    }

    /**
     * the layout id for the normal data
     *
     * @return the ID
     */
    @Override
    protected int getNormalLayoutResId() {
        return ViewHolder.layout;
    }

    /**
     * this is the Normal View Holder initiation
     *
     * @param view view
     * @return holder
     */
    @Override
    protected ViewHolder newViewHolder(View view) {
        return new ViewHolder(view);
    }


    @Override
    public long generateHeaderId(int position) {
        return 0;
    }

    /**
     * binding normal view holder
     *
     * @param holder   holder class
     * @param data     data
     * @param position position
     */
    @Override
    protected void withBindHolder(ViewHolder holder, PhotoVo data, int position) {
    }

    @Override
    protected void bindNormal(ViewHolder holder, PhotoVo photoVo, int position) {
        /*b.img.setImageResource(jRitem.photo_id);*/

        holder.img.setOnClickListener(v -> {

            String kind = photoVo.getKind();

            if(kind.equals("photo") || kind.equals("profile")) {
                Profile.viewPhoto(photoVo.getOriginalUrl(), "jpg");
            } else {
                Profile.chageProfileYn(false);
                Profile.showCameraDialog();
            }
        });

        if(photoVo.getBitmap() == null) {

            if (photoVo.getKind().equals("profile")) {

                /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
                downloadImageTask.execute(photoVo.getFileName());*/

            } else if (photoVo.getKind().equals("photo")) {

                holder.img.setImageUrl(photoVo.getThumbnailUrl(), this.imageLoader);

                if(this.profileYn) {
                    holder.delPhotoBtn.setVisibility(View.VISIBLE);
                }

                /*StorageReference islandRef = FirebaseStorage.getInstance().getReference().child("thumbnail/" + photoVo.getFileName() + "_thumbnail.jpg");

                islandRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    //do something with downloadurl
                    Log.d("downloadUrl", downloadUrl.toString());

                    holder.img.setImageUrl(downloadUrl.toString(), this.imageLoader);
                    holder.delPhotoBtn.setVisibility(View.VISIBLE);
                });

                final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    // Data for "images/island.jpg" is returns, use this as needed

                    Bitmap src = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap resized = Bitmap.createScaledBitmap(src, src.getWidth(), src.getHeight() / 2, true);

                    holder.img.setBackground(new BitmapDrawable(resized));
                    if(profileYn) {
                        holder.delPhotoBtn.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                    Log.e("bytess", exception.getMessage());
                });*/
            } else if (photoVo.getKind().equals("add_btn")) {
                /*holder.img.setImageUrl("https://pbs.twimg.com/profile_images/839721704163155970/LI_TRk1z_400x400.jpg", this.imageLoader);*/
                holder.img.setDefaultImageResId(R.drawable.add_btn);
            } else if (photoVo.getKind().equals("logo_t")) {
                holder.img.setDefaultImageResId(R.drawable.logo_t);
            }
        } else {
            holder.img.setBackground(new BitmapDrawable(photoVo.getBitmap()));
            if(profileYn) {
                holder.delPhotoBtn.setVisibility(View.VISIBLE);
            }
        }

        holder.delPhotoBtn.setOnClickListener(v -> {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
            alertDialogBuilder.setTitle("사진삭제");
            alertDialogBuilder.setMessage("삭제하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("삭제", (dialog, id) -> {

                        // Create a storage reference from our app
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                        // Create a reference to the file to delete
                        StorageReference desertRef = storageRef.child(String.format("original/%s.jpg", photoVo.getFileName()));

                        // Delete the file
                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                // Create a storage reference from our app
                                StorageReference storageThumRef = FirebaseStorage.getInstance().getReference();

                                // Create a reference to the file to delete
                                StorageReference desertThumRef = storageThumRef.child(String.format("thumbnail/%s_thumbnail.jpg", photoVo.getFileName()));
                                desertThumRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                        String photoId = photoVo.getPhotoId();
                                        FirebaseFirestore.getInstance().collection("photo").document(photoId)
                                                .delete()
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                    removeAt(holder.getAdapterPosition());
                                                })
                                                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                            }
                        });
                    })
                    .setNegativeButton("취소", (dialog, id) -> {
                        // 다이얼로그를 취소한다
                        dialog.cancel();
                    });

            // 다이얼로그 생성
            AlertDialog alertDialog = alertDialogBuilder.create();

            // 다이얼로그 보여주기
            alertDialog.show();
        });
    }

    @Override
    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.griditem, parent, false);

        NewRecyclerGridViewAdapter.ViewHolder vh = new NewRecyclerGridViewAdapter.ViewHolder(v);
        return vh;
    }

    /*@Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }*/

    @Override
    public UltimateRecyclerviewViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new UltimateRecyclerviewViewHolder(parent);
    }

    @Override
    public ViewHolder newFooterHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public ViewHolder newHeaderHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getAdapterItemCount() {
        return list.size();
    }

    public void clear() {
        clearInternal(this.list);
    }

    class ViewHolder extends UltimateRecyclerviewViewHolder {
        private NetworkImageView img;
        private ImageButton delPhotoBtn;
        public static final int layout = R.layout.griditem;
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
            this.delPhotoBtn = (ImageButton) view.findViewById(R.id.del_photo_btn);
            this.delPhotoBtn.bringToFront();
            /*DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) img.getLayoutParams();
            params.width = metrics.widthPixels / 4;
            params.height = metrics.heightPixels / 4;

            img.setLayoutParams(params);*/



            /*view.setTag(this);*/
        }
    }
}
