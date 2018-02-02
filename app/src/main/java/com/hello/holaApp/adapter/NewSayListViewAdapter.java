package com.hello.holaApp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.holaApp.R;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.vo.SayVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.List;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class NewSayListViewAdapter extends UltimateViewAdapter {

    private Context context;
    private List<SayVo> sayVoList;
    private ImageLoader imageLoader;

    private boolean profileYn = false;

    private static String TAG = "cloudFireStore";

    public NewSayListViewAdapter(Context context, List<SayVo> sayVoList, boolean profileYn) {
        this.context = context;
        this.sayVoList = sayVoList;
        this.profileYn = profileYn;
    }

    public NewSayListViewAdapter(Context context, List<SayVo> sayVoList) {
        this.context = context;
        this.sayVoList = sayVoList;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int index) {

        if(this.profileYn) {
            ((ViewHolder) holder).delSayBtn.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).delSayBtn.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
                alertDialogBuilder.setTitle(String.format(context.getResources().getString(R.string.delete_title), "Say"));
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

        if (!this.sayVoList.get(index).isNoMsg()) {

            String nation = this.sayVoList.get(index).getNation();
            String identity = this.sayVoList.get(index).getIdentity();

            String userInfo = "";

            if (nation != null && !nation.isEmpty()
                    && identity != null && !identity.isEmpty()) {
                userInfo = String.format("%s (%s, %s)", this.sayVoList.get(index).getUserName(), nation, identity);
            } else if ((nation == null || nation.isEmpty())
                    && (identity != null && !identity.isEmpty())) {
                userInfo = String.format("%s (%s)", this.sayVoList.get(index).getUserName(), identity);
            } else if ((nation != null && !nation.isEmpty())
                    && (identity == null || identity.isEmpty())) {
                userInfo = String.format("%s (%s)", this.sayVoList.get(index).getUserName(), nation);
            } else {
                userInfo = this.sayVoList.get(index).getUserName();
            }

            ((ViewHolder) holder).userName.setText(userInfo);
            ((ViewHolder) holder).distance.setText(this.sayVoList.get(index).getDistance());
            ((ViewHolder) holder).content.setText(this.sayVoList.get(index).getMsg());
        } else {
            ((ViewHolder) holder).noSayList.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).noSayMsg.setText(this.sayVoList.get(index).getMsg());
            ((ViewHolder) holder).sayCard.setVisibility(View.GONE);
                /*((ViewHolder) holder).content.setVisibility(View.GONE);
                ((ViewHolder) holder).userName.setVisibility(View.GONE);
                ((ViewHolder) holder).distance.setVisibility(View.GONE);
                ((ViewHolder) holder).delSayBtn.setVisibility(View.GONE);*/
        }

        /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
        downloadImageTask.execute(this.sayVoList.get(index).getPhotoUrl());*/

        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Medium.ttf");
        ((ViewHolder) holder).userName.setTypeface(typeface);

        ((ViewHolder) holder).img.setImageUrl(this.sayVoList.get(index).getPhotoUrl(), this.imageLoader);

        /*((ViewHolder) holder).sayLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this.context, UserInfoActivity.class);
            intent.putExtra("uid", this.sayVoList.get(index).getUid());
            intent.putExtra("userName", this.sayVoList.get(index).getUserName());
            intent.putExtra("profileUrl", this.sayVoList.get(index).getPhotoUrl());
            *//*intent.putExtra("bitmapImage", this.sayVoList.get(index).getBitmap());*//*
        });*/
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
