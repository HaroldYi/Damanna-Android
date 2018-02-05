package com.hello.holaApp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.hello.holaApp.R;
import com.hello.holaApp.activity.MainActivity;
import com.hello.holaApp.activity.UserInfoActivity;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.vo.SayVo;
import com.hello.holaApp.vo.UserVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class NewSayListViewAdapter extends UltimateViewAdapter {

    private Context context;
    private List<SayVo> sayVoList;
    private ImageLoader imageLoader;
    private static Map<String, UserVo> userMap;

    private boolean profileYn = false;

    private static String TAG = "cloudFireStore";

    public NewSayListViewAdapter(Context context, List<SayVo> sayVoList, boolean profileYn) {
        this.context = context;
        this.sayVoList = sayVoList;
        this.profileYn = profileYn;
        this.userMap = new HashMap();
        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();
    }

    public NewSayListViewAdapter(Context context, List<SayVo> sayVoList) {
        this.context = context;
        this.sayVoList = sayVoList;
        this.userMap = new HashMap();
        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int index) {

        if(this.profileYn) {
            ((ViewHolder) holder).delSayBtn.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).delSayBtn.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
                alertDialogBuilder.setTitle(context.getResources().getString(R.string.delete_msg));
                alertDialogBuilder.setMessage(context.getResources().getString(R.string.delete))
                        .setCancelable(false)
                        .setPositiveButton(context.getResources().getString(R.string.delete), (dialog, id) -> {

                            String sayId = sayVoList.get(((ViewHolder) holder).getAdapterPosition()).getSayId();
                            FirebaseFirestore.getInstance().collection("say").document(sayId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                        remove(((ViewHolder) holder).getAdapterPosition());
                                            /*if(this.sayVoList.size() == 0) {
                                                ((ViewHolder) holder).noSayList.setVisibility(View.VISIBLE);
                                                ((ViewHolder) holder).noSayMsg.setText(context.getResources().getString(R.string.no_data));
                                                ((ViewHolder) holder).sayCard.setVisibility(View.GONE);
                                            } else {
                                                ((ViewHolder) holder).noSayList.setVisibility(View.GONE);
                                                ((ViewHolder) holder).noSayMsg.setText(this.sayVoList.get(index).getMsg());
                                                ((ViewHolder) holder).sayCard.setVisibility(View.VISIBLE);
                                            }*/

                                        if(this.sayVoList.size() == 0) {
                                            SayVo sayVo = new SayVo();
                                            sayVo.setMsg(context.getResources().getString(R.string.no_data));
                                            sayVo.setNoMsg(true);

                                            this.sayVoList.add(sayVo);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error deleting document", e);
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

        if(this.getAdapterItemCount() > index) {
            if (!this.sayVoList.get(index).isNoMsg()) {

                String uid = sayVoList.get(index).getUid();
                UserVo userVo = userMap.get(uid);

                if(userVo == null) {
                    DocumentReference docRef = FirebaseFirestore.getInstance().collection("member").document(uid);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());

                                    String userName = document.get("name").toString();
                                    String nation = document.get("nation").toString();
                                    String identity = document.get("identity").toString();
                                    String profileUrl = document.get("profileUrl").toString();
                                    GeoPoint geoPoint = document.getGeoPoint("location");

                                    UserVo userVo = new UserVo();

                                    userVo.setUid(uid);
                                    userVo.setUserName(userName);
                                    userVo.setIdentity(identity);
                                    userVo.setNation(nation);
                                    userVo.setPhotoUrl(profileUrl);
                                    userVo.setGeoPoint(geoPoint);

                                    userMap.put(uid, userVo);
                                    setData(userVo, sayVoList.get(index), holder);
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                } else {
                    this.setData(userVo, this.sayVoList.get(index), holder);
                }
            } else {
                /*((ViewHolder) holder).noSayList.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).noSayMsg.setText(this.sayVoList.get(index).getMsg());*/
                ((ViewHolder) holder).sayCard.setVisibility(View.VISIBLE);
                /*((ViewHolder) holder).content.setVisibility(View.GONE);
                ((ViewHolder) holder).userName.setVisibility(View.GONE);
                ((ViewHolder) holder).distance.setVisibility(View.GONE);
                ((ViewHolder) holder).delSayBtn.setVisibility(View.GONE);*/
            }

            /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
            downloadImageTask.execute(this.sayVoList.get(index).getPhotoUrl());*/

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Medium.ttf");
            ((ViewHolder) holder).userName.setTypeface(typeface);

            /*((ViewHolder) holder).img.setImageUrl(this.sayVoList.get(index).getPhotoUrl(), this.imageLoader);*/

            /*((ViewHolder) holder).sayLayout.setOnClickListener(v -> {
                Intent intent = new Intent(this.context, UserInfoActivity.class);
                intent.putExtra("uid", this.sayVoList.get(index).getUid());
                intent.putExtra("userName", this.sayVoList.get(index).getUserName());
                intent.putExtra("profileUrl", this.sayVoList.get(index).getPhotoUrl());
                *//*intent.putExtra("bitmapImage", this.sayVoList.get(index).getBitmap());*//*
            });*/
        }
    }

    @Override
    public int getAdapterItemCount() {
        return sayVoList.size();
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
                .inflate(R.layout.item_list_app, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    private void setData(UserVo userVo, SayVo sayVo, RecyclerView.ViewHolder holder) {
        String userInfo = "";

        String userName = userVo.getUserName();
        String nation = userVo.getNation();
        String identity = userVo.getIdentity();
        GeoPoint geoPoint = userVo.getGeoPoint();

        Location loc = new Location("pointA");
        Location loc1 = new Location("pointB");

        loc.setLatitude(geoPoint.getLatitude());
        loc.setLongitude(geoPoint.getLongitude());

        loc1.setLatitude(CommonFunction.getLatitude());
        loc1.setLongitude(CommonFunction.getLongitude());

        String distance = String.format("%.2fkm", (loc.distanceTo(loc1) / 1000));

        distance = String.format("%s / %s", sayVo.getRegMin(), distance);

        if(identity.indexOf("워킹") != -1) {
            identity = "워홀";
        }

        if (nation != null && !nation.isEmpty()
                && identity != null && !identity.isEmpty()) {
            userInfo = String.format("%s (%s, %s)", userName, nation, identity);
        } else if ((nation == null || nation.isEmpty())
                && (identity != null && !identity.isEmpty())) {
            userInfo = String.format("%s (%s)", userName, identity);
        } else if ((nation != null && !nation.isEmpty())
                && (identity == null || identity.isEmpty())) {
            userInfo = String.format("%s (%s)", userName, nation);
        } else {
            userInfo = userName;
        }

        ((ViewHolder) holder).userName.setText(userInfo);
        ((ViewHolder) holder).distance.setText(distance);
        ((ViewHolder) holder).content.setText(sayVo.getMsg());

        ((ViewHolder) holder).img.setImageUrl(userVo.getPhotoUrl(), this.imageLoader);
        ((ViewHolder) holder).sayLayout.setVisibility(View.VISIBLE);

        ((ViewHolder) holder).sayLayout.setOnClickListener(v -> {
            MainActivity.tabIndex = 0;

            if(!sayVo.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                Intent intent = new Intent(context, UserInfoActivity.class);
                intent.putExtra("uid", sayVo.getUid());
                intent.putExtra("userName", userName);
                intent.putExtra("identity", sayVo.getIdentity());
                intent.putExtra("profileUrl", userVo.getPhotoUrl());
                /*intent.putExtra("bitmapImage", sayVoList.get(index).getBitmap());*/

                context.startActivity(intent);
            } else {
                Toast.makeText(context, "본인의 정보는 Profile메뉴를 이용하여 주십시오", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void insert(SayVo sayVo, int position) {
        insertInternal(this.sayVoList, sayVo, position);
    }

    public void insert(List<SayVo> sayVoList) {
        insertInternal(sayVoList, this.sayVoList);
    }

    public void remove(int position) {
        removeInternal(this.sayVoList, position);
    }

    public void clear() {
        clearInternal(this.sayVoList);
    }


    public void swapPositions(int from, int to) {
        swapPositions(this.sayVoList, from, to);
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

        LinearLayout sayLayout;

        CardView sayCard;
        RadiusNetworkImageView img;
        TextView userName;
        TextView content;
        TextView distance;
        ImageButton delSayBtn;

        CardView noSayList;
        TextView noSayMsg;

        public ViewHolder(View itemView) {
            super(itemView);
            this.sayLayout = (LinearLayout) itemView.findViewById(R.id.say_layout);
            this.sayLayout.setVisibility(View.INVISIBLE);
            this.sayCard = (CardView) itemView.findViewById(R.id.say_card);
            this.userName = (TextView) itemView.findViewById(R.id.user_name);
            this.content = (TextView) itemView.findViewById(R.id.content);
            this.img = (RadiusNetworkImageView) itemView.findViewById(R.id.user_profile_photo);
            this.img.setRadius(100f);
            this.distance = (TextView) itemView.findViewById(R.id.distance);
            this.delSayBtn = (ImageButton) itemView.findViewById(R.id.del_say_btn);
            this.noSayList = (CardView) itemView.findViewById(R.id.no_say_list);
            this.noSayMsg = (TextView) itemView.findViewById(R.id.no_say_msg);
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
