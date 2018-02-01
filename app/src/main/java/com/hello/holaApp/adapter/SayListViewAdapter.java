package com.hello.holaApp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.hello.holaApp.R;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.VolleySingleton;
import com.hello.holaApp.vo.SayVo;

import java.util.List;

/**
 * Created by lji5317 on 13/12/2017.
 */

@Deprecated
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

            /*this.distance.setText("");*/

            view.setTag(this);
        }
    }
}
