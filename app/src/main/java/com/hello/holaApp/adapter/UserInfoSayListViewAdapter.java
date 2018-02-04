package com.hello.holaApp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.holaApp.R;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.vo.SayVo;

import java.util.List;

/**
 * Created by lji5317 on 04/02/2018.
 */

public class UserInfoSayListViewAdapter extends NewSayListViewAdapter {
    private Context context;
    private List<SayVo> sayVoList;
    private ImageLoader imageLoader;

    private boolean profileYn = false;

    private static String TAG = "cloudFireStore";

    public UserInfoSayListViewAdapter(Context context, List<SayVo> sayVoList, boolean profileYn) {
        super(context, sayVoList, profileYn);
        this.context = context;
        this.sayVoList = sayVoList;
        this.profileYn = profileYn;
    }

    public UserInfoSayListViewAdapter(Context context, List<SayVo> sayVoList) {
        super(context, sayVoList);
        this.context = context;
        this.sayVoList = sayVoList;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int index) {

        if(this.profileYn) {
            ((NewSayListViewAdapter.ViewHolder) holder).delSayBtn.setVisibility(View.VISIBLE);
            ((NewSayListViewAdapter.ViewHolder) holder).delSayBtn.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
                alertDialogBuilder.setTitle(context.getResources().getString(R.string.delete_msg));
                alertDialogBuilder.setMessage(context.getResources().getString(R.string.delete))
                        .setCancelable(false)
                        .setPositiveButton(context.getResources().getString(R.string.delete), (dialog, id) -> {

                            String sayId = sayVoList.get(((NewSayListViewAdapter.ViewHolder) holder).getAdapterPosition()).getSayId();
                            FirebaseFirestore.getInstance().collection("say").document(sayId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                        remove(((NewSayListViewAdapter.ViewHolder) holder).getAdapterPosition());
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

        if(this.profileYn) {
            ((NewSayListViewAdapter.ViewHolder) holder).delSayBtn.setVisibility(View.VISIBLE);
            ((NewSayListViewAdapter.ViewHolder) holder).delSayBtn.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
                alertDialogBuilder.setTitle(String.format(context.getResources().getString(R.string.delete_title), "Say"));
                alertDialogBuilder.setMessage(context.getResources().getString(R.string.delete))
                        .setCancelable(false)
                        .setPositiveButton(context.getResources().getString(R.string.delete), (dialog, id) -> {

                            String sayId = sayVoList.get(((NewSayListViewAdapter.ViewHolder) holder).getAdapterPosition()).getSayId();
                            FirebaseFirestore.getInstance().collection("say").document(sayId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                        remove(((NewSayListViewAdapter.ViewHolder) holder).getAdapterPosition());
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

            ((ViewHolder) holder).sayLayout.setVisibility(View.VISIBLE);
            ((NewSayListViewAdapter.ViewHolder) holder).userName.setText(userInfo);
            ((NewSayListViewAdapter.ViewHolder) holder).distance.setText(this.sayVoList.get(index).getDistance());
            ((NewSayListViewAdapter.ViewHolder) holder).content.setText(this.sayVoList.get(index).getMsg());
        } else {
            ((ViewHolder) holder).sayLayout.setVisibility(View.VISIBLE);
            ((NewSayListViewAdapter.ViewHolder) holder).noSayList.setVisibility(View.VISIBLE);
            ((NewSayListViewAdapter.ViewHolder) holder).noSayMsg.setText(this.sayVoList.get(index).getMsg());
            ((NewSayListViewAdapter.ViewHolder) holder).sayCard.setVisibility(View.GONE);
        }

        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Medium.ttf");
        ((NewSayListViewAdapter.ViewHolder) holder).userName.setTypeface(typeface);

        ((NewSayListViewAdapter.ViewHolder) holder).img.setImageUrl(this.sayVoList.get(index).getPhotoUrl(), this.imageLoader);
    }
}
