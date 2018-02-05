package com.hello.holaApp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hello.holaApp.R;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.vo.PhotoVo;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class PeopleListViewAdapter extends UltimateViewAdapter {

    private Context context;
    private List<UserVo> userVoList;
    private ImageLoader imageLoader;

    private static Map<String, List<PhotoVo>> photoVoMap;

    private static String TAG = "cloudFireStore";

    public PeopleListViewAdapter(Context context, List<UserVo> userVoList) {
        this.context = context;
        this.userVoList = userVoList;
        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();
        this.photoVoMap = new HashMap<>();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int index) {

        String uid = this.userVoList.get(index).getUid();
        String userName = String.format("%s (%.2fkm)", userVoList.get(index).getUserName(), userVoList.get(index).getDistance());

        ((ViewHolder) holder).userProfileName.setText(userName);
        ((ViewHolder) holder).img.setImageUrl(userVoList.get(index).getPhotoUrl(), this.imageLoader);

        String age = String.format("%s세, %s", userVoList.get(index).getAge(), userVoList.get(index).getGender());
        String identity = String.format("%s, %s", userVoList.get(index).getNation(), userVoList.get(index).getIdentity());

        ((ViewHolder) holder).age.setText(age);
        ((ViewHolder) holder).identity.setText(identity);

        //peopleImageScrollList = (ScrollView)

        List<PhotoVo> photoVoList = photoVoMap.get(uid);
        if(photoVoList == null) {
            FirebaseFirestore.getInstance().collection("photo/")
                    .whereEqualTo("member_id", uid)
                    .orderBy("reg_dt", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            int size = task.getResult().size();

                            List<PhotoVo> photoList = new ArrayList<>();
                            if (size > 0) {

                                for (DocumentSnapshot document1 : task.getResult()) {

                                    PhotoVo photoVo = new PhotoVo();
                                    photoVo.setPhotoId(document1.getString("id"));
                                    photoVo.setThumbnailUrl(document1.getData().get("thumbnail_img").toString());
                                    photoVo.setOriginalUrl(document1.getData().get("original_img").toString());
                                    if(document1.getData().get("file_name") != null) {
                                        photoVo.setFileName(document1.getData().get("file_name").toString());
                                    }

                                    photoList.add(photoVo);
                                }

                                photoVoMap.put(uid, photoList);
                                setPhotoList(photoList, holder);
                            } else {

                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }

                    /*Log.d(TAG, document.getId() + " => " + document.getData());*/
                    });
        } else {
            this.setPhotoList(photoVoList, holder);
        }

        /*List<PhotoVo> photoVoList = userVoList.get(index).getPhotoVoList();*/
    }

    @Override
    public int getAdapterItemCount() {
        return userVoList.size();
    }

    @Override
    public RecyclerView.ViewHolder newFooterHolder(View view) {
        // return new itemCommonBinder(view, false);
        return new UltimateRecyclerviewViewHolder<>(view);
    }

    @Override
    public RecyclerView.ViewHolder newHeaderHolder(View view) {
        return new UltimateRecyclerviewViewHolder<>(view);
    }

    @Override
    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.people_list_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    private void setPhotoList(List<PhotoVo> photoVoList, RecyclerView.ViewHolder holder) {

        HorizontalScrollView peopleImageScrollList = ((ViewHolder) holder).peopleImageScrollList;
        LinearLayout layout = ((ViewHolder) holder).peopleImageList;

        if(photoVoList != null && photoVoList.size() > 0) {

            peopleImageScrollList.setVisibility(View.VISIBLE);
            layout.removeAllViews();
            layout.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 65, this.context.getResources().getDisplayMetrics());

            int listSize = photoVoList.size();

            for(int i = 0 ; i < listSize ; i++) {

                params.setMargins(10, 0, 0, 0);
                params.width = value;
                params.height = value;

                RadiusNetworkImageView imageView = new RadiusNetworkImageView(this.context);
                imageView.setRadius(10f);

                imageView.setImageUrl(photoVoList.get(i).getThumbnailUrl(), this.imageLoader);
                imageView.setLayoutParams(params);
                layout.addView(imageView);
            }

            if(listSize == 5) {
                RadiusNetworkImageView imageView = new RadiusNetworkImageView(this.context);
                params.setMargins(5, 0, 0, 0);

                imageView.setRadius(10f);
                imageView.setDefaultImageResId(R.drawable.show_more);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setLayoutParams(params);
                layout.addView(imageView);
            }
        } else {
            peopleImageScrollList.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
        }
    }

    public void insert(UserVo userVo, int position) {
        insertInternal(this.userVoList, userVo, position);
    }

    public void insert(List<UserVo> userVoList) {
        insertInternal(userVoList, this.userVoList);
    }

    public void remove(int position) {
        removeInternal(this.userVoList, position);
    }

    public void clear() {
        clearInternal(this.userVoList);
    }


    public void swapPositions(int from, int to) {
        swapPositions(this.userVoList, from, to);
    }

    @Override
    public long generateHeaderId(int position) {
        // URLogs.d("position--" + position + "   " + getItem(position));
        /*if (getItem(position).length() > 0)
            return getItem(position).charAt(0);
        else return -1;*/

        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.stick_header_item, viewGroup, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        /*if (fromPosition > 0 && toPosition > 0) {
            swapPositions(fromPosition, toPosition);
//        notifyItemMoved(fromPosition, toPosition);
            super.onItemMove(fromPosition, toPosition);
        }*/

    }

    @Override
    public void onItemDismiss(int position) {
        /*if (position > 0) {
            remove(position);
            // notifyItemRemoved(position);
//        notifyDataSetChanged();
            super.onItemDismiss(position);
        }
*/
    }
//
//    private int getRandomColor() {
//        SecureRandom rgen = new SecureRandom();
//        return Color.HSVToColor(150, new float[]{
//                rgen.nextInt(359), 1, 1
//        });
//    }

    public void setOnDragStartListener(OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;

    }

    class ViewHolder extends UltimateRecyclerviewViewHolder {

        TextView userProfileName;
        RadiusNetworkImageView img;
        TextView age;
        TextView identity;

        HorizontalScrollView peopleImageScrollList;
        LinearLayout peopleImageList;

        public ViewHolder(View itemView) {
            super(itemView);
            userProfileName = (TextView) itemView.findViewById(R.id.user_profile_name);
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Medium.ttf");
            userProfileName.setTypeface(typeface);
            img = (RadiusNetworkImageView) itemView.findViewById(R.id.user_profile_photo);
            img.setRadius(100f);

            age = (TextView) itemView.findViewById(R.id.age);
            identity = (TextView) itemView.findViewById(R.id.identity);

            peopleImageScrollList = (HorizontalScrollView) itemView.findViewById(R.id.people_image_scroll_list);
            peopleImageList = (LinearLayout) itemView.findViewById(R.id.people_image_list);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
