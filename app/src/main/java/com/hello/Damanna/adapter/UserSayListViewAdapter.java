package com.hello.Damanna.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hello.Damanna.R;
import com.hello.Damanna.vo.SayVo;

import java.util.List;

/**
 * Created by lji5317 on 14/12/2017.
 */

public class UserSayListViewAdapter extends BaseAdapter {
    private static Context context;
    private int resource;
    private List<SayVo> sayVoList;

    public UserSayListViewAdapter(Context context, int resource, List<SayVo> sayVoList) {
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

        if (view == null) {
            view = View.inflate(this.context.getApplicationContext(), R.layout.item_list_app, null);

            new ViewHolder(view);
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.userName.setText("");
        holder.content.setText(this.sayVoList.get(index).getMsg());

        if(sayVoList.get(index).isNoMsg()) {
            holder.content.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            holder.content.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
        holder.distance.setText("");

        return view;
    }

    class ViewHolder {
        ImageView img;
        TextView userName;
        TextView content;
        TextView distance;

        public ViewHolder(View view) {
            userName = (TextView) view.findViewById(R.id.user_name);
            content = (TextView) view.findViewById(R.id.content);
            distance = (TextView) view.findViewById(R.id.distance);

            img = (ImageView) view.findViewById(R.id.user_profile_photo);
            img.setVisibility(View.GONE);

            view.setTag(this);
        }
    }
}
