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

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by lji5317 on 13/12/2017.
 */

public class SayListViewAdapter extends BaseAdapter {

    private static Context context;
    private int resource;
    private List<SayVo> sayVoList;

    private static String TAG = "cloudFireStore";

    public SayListViewAdapter(Context context, int resource, List<SayVo> sayVoList) {
        this.context = context;
        this.resource = resource;
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
            if(this.resource == R.id.conversation) {
                /*view = new SayListViewAdapter.ListItem(context, R.drawable.conversation);*/
                resource = R.drawable.conversation;
            }

            new ViewHolder(view, resource);
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.userName.setText(this.sayVoList.get(index).getUserName());
        holder.content.setText(this.sayVoList.get(index).getMsg());
        holder.dateOfSent.setText(String.format("%.2fm", this.sayVoList.get(index).getDistance()));

        DownloadImageTask downloadImageTask = new DownloadImageTask(holder.img, "list");
        downloadImageTask.execute(sayVoList.get(index).getPhotoUrl());

        return view;
    }

    class ViewHolder {
        LinearLayout sayLayout;
        LinearLayout layout;
        ImageView img;
        TextView userName;
        TextView content;
        TextView dateOfSent;

        public ViewHolder(View view, Integer resource) {
            sayLayout = (LinearLayout) view.findViewById(R.id.say_layout);
            userName = (TextView) view.findViewById(R.id.user_name);
            content = (TextView) view.findViewById(R.id.content);
            img = (ImageView) view.findViewById(R.id.user_profile_photo);
            dateOfSent = (TextView) view.findViewById(R.id.date_of_sent);

            if(resource != null) {
                layout = (LinearLayout) view.findViewById(R.id.conversation);
                layout.setBackground(content.getResources().getDrawable(R.drawable.conversation));
            }

            /*dateOfSent.setText("");*/

            view.setTag(this);
        }
    }
}
