package com.hello.holaApp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.hello.holaApp.R;
import com.hello.holaApp.activity.SayCommentListActivity;
import com.hello.holaApp.activity.UserInfoActivity;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.fragment.Say;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class CommentListViewAdapter extends UltimateViewAdapter {

    private Context context;
    private ArrayList<HashMap<String, Object>> commentList;
    public static ArrayList<HashMap<String, Object>> commentReplyList;
    private ImageLoader imageLoader;
    private FirebaseFirestore db;

    private boolean profileYn = false;

    private static String TAG = "cloudFireStore";

    public CommentListViewAdapter(Context context, ArrayList<HashMap<String, Object>> commentList, ArrayList<HashMap<String, Object>> commentReplyList) {
        this.context = context;
        this.commentList = commentList;
        this.commentReplyList = commentReplyList;
        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int index) {
        ((ViewHolder) holder).commentLayout.setVisibility(View.VISIBLE);

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Medium.ttf");
        String comment = (this.commentList.get(index).get("comment") != null ? this.commentList.get(index).get("comment").toString() : "");
        ((ViewHolder) holder).content.setText(comment);
        ((ViewHolder) holder).content.setTypeface(typeface);

        ((ViewHolder) holder).userName.setTypeface(typeface);
        ((ViewHolder) holder).distance.setTypeface(typeface);

        for(int i = 0 ; i < commentReplyList.size() ; i++) {
            if(this.commentReplyList.get(i).get("comment_id") != null && this.commentList.get(index).get("id") != null && commentReplyList.get(i).get("comment_id").toString().equals(commentList.get(index).get("id").toString())) {
                RelativeLayout replyLayout = new RelativeLayout(this.context);
                RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                replyLayout.setLayoutParams(layoutParam);

                String uid = (this.commentReplyList.get(i).get("member_id") != null ? this.commentReplyList.get(i).get("member_id").toString() : "");
                long regDt = ((Date) this.commentReplyList.get(i).get("reg_dt")).getTime();

                DocumentReference docRef = this.db.collection("member").document(uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());

                                String uid = document.get("id").toString();
                                String userName = document.get("name").toString();
                                String nation = document.get("nation").toString();
                                String identity = document.get("identity").toString();
                                String profileUrl = document.get("profileUrl").toString();
                                GeoPoint geoPoint = document.getGeoPoint("location");

                                long now = System.currentTimeMillis();

                                long regTime = (now - regDt) / 60000;

                                String regMin = "";
                                if (regTime < 60) {
                                    regMin = String.format("%dmin", regTime);
                                } else if (regTime >= 60 && regTime < 1440) {
                                    regMin = String.format("%dh", (int) (regTime / 60));
                                } else if (regTime > 1440) {
                                    regMin = String.format("%dd", (int) (regTime / 1440));
                                }

                                Location loc = new Location("pointA");
                                Location loc1 = new Location("pointB");

                                loc.setLatitude(geoPoint.getLatitude());
                                loc.setLongitude(geoPoint.getLongitude());

                                loc1.setLatitude(CommonFunction.getLatitude());
                                loc1.setLongitude(CommonFunction.getLongitude());

                                String distance = String.format("%.2fkm", (loc.distanceTo(loc1) / 1000));

                                TextView userNameView = new TextView(context);
                                userNameView.setTextColor(Color.BLACK);
                                userNameView.setTypeface(typeface);

                                TextView distanceView = new TextView(context);
                                distanceView.setTextColor(Color.rgb( 155, 155, 155));
                                distanceView.setTypeface(typeface);

                                RelativeLayout.LayoutParams userNameParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                RelativeLayout.LayoutParams distanceParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                userNameParam.setMargins(CommonFunction.convertTodp(context, 125), CommonFunction.convertTodp(context, 20), 0, CommonFunction.convertTodp(context, 30));
                                distanceParam.setMargins(CommonFunction.convertTodp(context, 125), CommonFunction.convertTodp(context, 50), 0, CommonFunction.convertTodp(context, 50));

                                userNameView.setLayoutParams(userNameParam);
                                distanceView.setLayoutParams(distanceParam);

                                replyLayout.addView(userNameView);
                                replyLayout.addView(distanceView);

                                userNameView.setText(String.format("%s (%s, %s)", userName, nation, identity));
                                distanceView.setText(String.format("%s / %s", regMin, distance));

                                RadiusNetworkImageView pImageView = new RadiusNetworkImageView(context);
                                pImageView.setRadius(100f);
                                pImageView.setImageUrl(profileUrl, imageLoader);

                                RelativeLayout.LayoutParams imageParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                imageParam.width = CommonFunction.convertTodp(context, 50);
                                imageParam.height = CommonFunction.convertTodp(context, 50);
                                imageParam.setMargins(CommonFunction.convertTodp(context, 65), CommonFunction.convertTodp(context, 20), 0, CommonFunction.convertTodp(context, 60));

                                pImageView.setLayoutParams(imageParam);

                                pImageView.setOnClickListener(v -> {
                                    Intent intent = new Intent(context, UserInfoActivity.class);
                                    intent.putExtra("uid", uid);
                                    intent.putExtra("userName", userName);
                                    intent.putExtra("identity", identity);
                                    intent.putExtra("profileUrl", profileUrl);

                                    /*intent.putExtra("bitmapImage", sayVoList.get(index).getBitmap());*/

                                    Say.resumeYn = true;

                                    context.startActivity(intent);
                                });

                                replyLayout.addView(pImageView);

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

                TextView commentView = new TextView(this.context);
                commentView.setText(this.commentReplyList.get(i).get("comment").toString());
                commentView.setTextColor(Color.rgb( 155, 155, 155));

                RelativeLayout.LayoutParams commentParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                commentParam.setMargins(CommonFunction.convertTodp(context, 70), CommonFunction.convertTodp(context, 90), 0, CommonFunction.convertTodp(context, 10));
                commentView.setLayoutParams(commentParam);

                replyLayout.addView(commentView);

                AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

                ImageView iv = new ImageView(this.context);
                iv.setImageResource(R.drawable.ic_fa_reply);

                RelativeLayout.LayoutParams ivParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ivParam.setMargins(CommonFunction.convertTodp(context, 30), CommonFunction.convertTodp(context, 30), 0, CommonFunction.convertTodp(context, 30));
                ivParam.width = CommonFunction.convertTodp(context, 20);
                ivParam.height = CommonFunction.convertTodp(context, 20);
                iv.setLayoutParams(ivParam);

                replyLayout.addView(iv);

                ((ViewHolder) holder).commentLayout.addView(replyLayout);
            }
        }

        String uid = (this.commentList.get(index).get("member_id") != null ? this.commentList.get(index).get("member_id").toString() : "");

        long regDt = ((Date) this.commentList.get(index).get("reg_dt")).getTime();

        DocumentReference docRef = this.db.collection("member").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());

                        String uid = document.get("id").toString();
                        String userName = document.get("name").toString();
                        String nation = document.get("nation").toString();
                        String identity = document.get("identity").toString();
                        String profileUrl = document.get("profileUrl").toString();
                        GeoPoint geoPoint = document.getGeoPoint("location");

                        long now = System.currentTimeMillis();

                        long regTime = (now - regDt) / 60000;

                        String regMin = "";
                        if (regTime < 60) {
                            regMin = String.format("%dmin", regTime);
                        } else if (regTime >= 60 && regTime < 1440) {
                            regMin = String.format("%dh", (int) (regTime / 60));
                        } else if (regTime > 1440) {
                            regMin = String.format("%dd", (int) (regTime / 1440));
                        }

                        Location loc = new Location("pointA");
                        Location loc1 = new Location("pointB");

                        loc.setLatitude(geoPoint.getLatitude());
                        loc.setLongitude(geoPoint.getLongitude());

                        loc1.setLatitude(CommonFunction.getLatitude());
                        loc1.setLongitude(CommonFunction.getLongitude());

                        String distance = String.format("%.2fkm", (loc.distanceTo(loc1) / 1000));

                        ((ViewHolder) holder).userName.setText(String.format("%s (%s, %s)", userName, nation, identity));
                        ((ViewHolder) holder).distance.setText(String.format("%s / %s", regMin, distance));

                        ((ViewHolder) holder).img.setImageUrl(profileUrl, imageLoader);

                        ((ViewHolder) holder).img.setOnClickListener(v -> {
                            Intent intent = new Intent(context, UserInfoActivity.class);
                            intent.putExtra("uid", uid);
                            intent.putExtra("userName", userName);
                            intent.putExtra("identity", identity);
                            intent.putExtra("profileUrl", profileUrl);

                            /*intent.putExtra("bitmapImage", sayVoList.get(index).getBitmap());*/

                            Say.resumeYn = true;

                            context.startActivity(intent);
                        });

                        ((ViewHolder) holder).commentLayout.setOnClickListener(v -> {

                            SayCommentListActivity.reCommentYn = true;
                            SayCommentListActivity.commentId = commentList.get(index).get("id").toString();

                            SayCommentListActivity.writeCommentText.requestFocus();

                            SayCommentListActivity.writeCommentText.setTags(document.getString("name"), "");

                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(SayCommentListActivity.writeCommentText, InputMethodManager.SHOW_IMPLICIT);
                        });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public int getAdapterItemCount() {
        return this.commentList.size();
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
                .inflate(R.layout.comment_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
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

        LinearLayout commentLayout;
        LinearLayout commentProfile;

        RadiusNetworkImageView img;
        TextView userName;
        TextView content;
        TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            this.commentLayout = (LinearLayout) itemView.findViewById(R.id.comment_item);
            this.commentLayout.setVisibility(View.INVISIBLE);
            this.commentProfile = (LinearLayout) itemView.findViewById(R.id.comment_profile);
            this.userName = (TextView) itemView.findViewById(R.id.user_name);
            this.content = (TextView) itemView.findViewById(R.id.content);
            this.img = (RadiusNetworkImageView) itemView.findViewById(R.id.user_profile_photo);
            this.img.setRadius(100f);
            this.distance = (TextView) itemView.findViewById(R.id.distance);
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

    /*public String getItem(int position) {
        if (customHeaderView != null)
            position--;
        // URLogs.d("position----"+position);
        if (position >= 0 && position < stringList.size())
            return stringList.get(position);
        else return "";
    }*/
}
