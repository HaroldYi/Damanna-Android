package com.hello.TrevelMeetUp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.common.RadiusImageView;
import com.hello.TrevelMeetUp.common.VolleySingleton;
import com.hello.TrevelMeetUp.vo.SayVo;

import java.util.List;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class SayListViewAdapter extends BaseAdapter {

    private Context context;
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
            /*view = new SayListViewAdapter.ListItem(context, R.drawable.conversation);*/
            resource = R.drawable.conversation;

            new ViewHolder(view, resource);
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        String userInfo = String.format("%s (%s, %s)", this.sayVoList.get(index).getUserName(), this.sayVoList.get(index).getNation(), this.sayVoList.get(index).getIdentity());

        holder.userName.setText(userInfo);
        holder.content.setText(this.sayVoList.get(index).getMsg());
        holder.distance.setText(this.sayVoList.get(index).getDistance());

        /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img);
        downloadImageTask.execute(this.sayVoList.get(index).getPhotoUrl());*/

        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Medium.ttf");
        holder.userName.setTypeface(typeface);

        holder.img.setImageUrl(this.sayVoList.get(index).getPhotoUrl(), this.imageLoader);

        return view;
    }

    class ViewHolder {
        LinearLayout sayLayout;
        RadiusImageView img;
        TextView userName;
        TextView content;
        TextView distance;

        public ViewHolder(View view, Integer resource) {
            this.sayLayout = (LinearLayout) view.findViewById(R.id.say_layout);
            this.userName = (TextView) view.findViewById(R.id.user_name);
            this.content = (TextView) view.findViewById(R.id.content);
            this.img = (RadiusImageView) view.findViewById(R.id.user_profile_photo);
            this.img.setRadius(100f);
            this.distance = (TextView) view.findViewById(R.id.distance);

            /*this.distance.setText("");*/

            view.setTag(this);
        }
    }
}
