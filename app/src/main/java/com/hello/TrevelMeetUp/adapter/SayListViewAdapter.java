package com.hello.TrevelMeetUp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.common.DownloadImageTask;
import com.hello.TrevelMeetUp.vo.SayVo;
import com.meg7.widget.CircleImageView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class SayListViewAdapter extends BaseAdapter {

    private static Context context;
    private List<SayVo> sayVoList;

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
        holder.distance.setText(String.format("%.2fm", this.sayVoList.get(index).getDistance()));

        DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img, "list");
        downloadImageTask.execute(this.sayVoList.get(index).getPhotoUrl());

        return view;
    }

    class ViewHolder {
        LinearLayout sayLayout;
        LinearLayout layout;
        CircleImageView img;
        TextView userName;
        TextView content;
        TextView distance;

        public ViewHolder(View view, Integer resource) {
            this.sayLayout = (LinearLayout) view.findViewById(R.id.say_layout);
            this.userName = (TextView) view.findViewById(R.id.user_name);
            this.content = (TextView) view.findViewById(R.id.content);
            this.img = (CircleImageView) view.findViewById(R.id.user_profile_photo);
            this.distance = (TextView) view.findViewById(R.id.distance);

            /*this.distance.setText("");*/

            view.setTag(this);
        }
    }
}
