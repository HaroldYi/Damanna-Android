package com.hello.Damanna.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.hello.Damanna.R;
import com.hello.Damanna.common.RadiusNetworkImageView;
import com.hello.Damanna.common.VolleySingleton;
import com.hello.Damanna.vo.SayVo;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.List;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class NewSayListViewAdapter extends UltimateViewAdapter {

    /*private Context context;
    private List<SayVo> sayVoList;
    private ImageLoader imageLoader;

    private static String TAG = "cloudFireStore";

    public SayListViewAdapter(Context context, List<SayVo> sayVoList) {
        this.context = context;
        this.sayVoList = sayVoList;
    }

    @Override
    public int getCount() {
        return sayVoList.size();
    }

    @Override
    public Object getItem(int index) {
        return sayVoList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {

        Integer resource = null;

        if (view == null) {
            view = View.inflate(this.context.getApplicationContext(), R.layout.item_list_app, null);
            *//*view = new SayListViewAdapter.ListItem(context, R.drawable.conversation);*//*
            resource = R.drawable.conversation;

            new ViewHolder(view, resource);
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        if(!this.sayVoList.get(index).isNoMsg()) {

            String nation = this.sayVoList.get(index).getNation();
            String identity = this.sayVoList.get(index).getIdentity();

            String userInfo = "";

            if(nation != null && !nation.isEmpty()
              && identity != null && !identity.isEmpty() ) {
                userInfo = String.format("%s (%s, %s)", this.sayVoList.get(index).getUserName(), nation, identity);
            } else if((nation == null || nation.isEmpty())
                    && (identity != null && !identity.isEmpty()) ) {
                userInfo = String.format("%s (%s)", this.sayVoList.get(index).getUserName(), identity);
            } else if((nation != null && !nation.isEmpty())
                    && (identity == null || identity.isEmpty()) ) {
                userInfo = String.format("%s (%s)", this.sayVoList.get(index).getUserName(), nation);
            } else {
                userInfo = this.sayVoList.get(index).getUserName();
            }

            holder.userName.setText(userInfo);
            holder.distance.setText(this.sayVoList.get(index).getDistance());
        } else {
            holder.content.setGravity(Gravity.CENTER);
            holder.userName.setVisibility(View.GONE);
            holder.distance.setVisibility(View.GONE);
        }

        holder.content.setText(this.sayVoList.get(index).getMsg());

        *//*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
        downloadImageTask.execute(this.sayVoList.get(index).getPhotoUrl());*//*

        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Medium.ttf");
        holder.userName.setTypeface(typeface);

        holder.img.setImageUrl(this.sayVoList.get(index).getPhotoUrl(), this.imageLoader);

        return view;
    }

    class ViewHolder {
        LinearLayout sayLayout;
        RadiusNetworkImageView img;
        TextView userName;
        TextView content;
        TextView distance;

        public ViewHolder(View view, Integer resource) {
            this.sayLayout = (LinearLayout) view.findViewById(R.id.say_layout);
            this.userName = (TextView) view.findViewById(R.id.user_name);
            this.content = (TextView) view.findViewById(R.id.content);
            this.img = (RadiusNetworkImageView) view.findViewById(R.id.user_profile_photo);
            this.img.setRadius(100f);
            this.distance = (TextView) view.findViewById(R.id.distance);

            *//*this.distance.setText("");*//*

            view.setTag(this);
        }
    }*/

    private Context context;
    private List<SayVo> sayVoList;
    private ImageLoader imageLoader;

    private static String TAG = "cloudFireStore";

    public NewSayListViewAdapter(Context context, List<SayVo> sayVoList) {
        this.context = context;
        this.sayVoList = sayVoList;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int index) {
        if(!this.sayVoList.get(index).isNoMsg()) {

            String nation = this.sayVoList.get(index).getNation();
            String identity = this.sayVoList.get(index).getIdentity();

            String userInfo = "";

            if(nation != null && !nation.isEmpty()
                    && identity != null && !identity.isEmpty() ) {
                userInfo = String.format("%s (%s, %s)", this.sayVoList.get(index).getUserName(), nation, identity);
            } else if((nation == null || nation.isEmpty())
                    && (identity != null && !identity.isEmpty()) ) {
                userInfo = String.format("%s (%s)", this.sayVoList.get(index).getUserName(), identity);
            } else if((nation != null && !nation.isEmpty())
                    && (identity == null || identity.isEmpty()) ) {
                userInfo = String.format("%s (%s)", this.sayVoList.get(index).getUserName(), nation);
            } else {
                userInfo = this.sayVoList.get(index).getUserName();
            }

            ((ViewHolder) holder).userName.setText(userInfo);
            ((ViewHolder) holder).distance.setText(this.sayVoList.get(index).getDistance());
        } else {
            ((ViewHolder) holder).content.setGravity(Gravity.CENTER);
            ((ViewHolder) holder).userName.setVisibility(View.GONE);
            ((ViewHolder) holder).distance.setVisibility(View.GONE);
        }

        ((ViewHolder) holder).content.setText(this.sayVoList.get(index).getMsg());

        /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
        downloadImageTask.execute(this.sayVoList.get(index).getPhotoUrl());*/

        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Medium.ttf");
        ((ViewHolder) holder).userName.setTypeface(typeface);

        ((ViewHolder) holder).img.setImageUrl(this.sayVoList.get(index).getPhotoUrl(), this.imageLoader);
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
        RadiusNetworkImageView img;
        TextView userName;
        TextView content;
        TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
//            itemView.setOnTouchListener(new SwipeDismissTouchListener(itemView, null, new SwipeDismissTouchListener.DismissCallbacks() {
//                @Override
//                public boolean canDismiss(Object token) {
//                    Logs.d("can dismiss");
//                    return true;
//                }
//
//                @Override
//                public void onDismiss(View view, Object token) {
//                   // Logs.d("dismiss");
//                    remove(getPosition());
//
//                }
//            }));
            this.sayLayout = (LinearLayout) itemView.findViewById(R.id.say_layout);
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
