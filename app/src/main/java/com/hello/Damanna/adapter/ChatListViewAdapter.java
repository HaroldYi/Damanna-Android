package com.hello.Damanna.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.hello.Damanna.R;
import com.hello.Damanna.common.BaseSwipListAdapter;
import com.hello.Damanna.common.RadiusNetworkImageView;
import com.hello.Damanna.common.VolleySingleton;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by lji5317 on 07/12/2017.
 */

public class ChatListViewAdapter extends BaseSwipListAdapter {

    private static String TAG = "cloudFireStore";

    private Context context;
    private int resource;
    private List<GroupChannel> channelList;
    private ImageLoader imageLoader;

    private String uid;

    public ChatListViewAdapter(Context context, int resource, List<GroupChannel> channelList, String uid) {
        this.context = context;
        this.resource = resource;
        this.channelList = channelList;
        this.uid = uid;
    }

    @Override
    public boolean getSwipEnableByPosition(int position) {
        return super.getSwipEnableByPosition(position);
    }

    @Override
    public int getCount() {
        return channelList.size();
    }

    @Override
    public Object getItem(int i) {
        return channelList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = View.inflate(this.context.getApplicationContext(),
                    R.layout.chat_list_item, null);
            new ViewHolder(view);
        }
        ViewHolder holder = (ViewHolder) view.getTag();

        Typeface typeface = Typeface.createFromAsset(this.context.getAssets(), "fonts/NotoSans-Medium.ttf");
        holder.userName.setTypeface(typeface);

        BaseMessage message = channelList.get(i).getLastMessage();
        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage)message;
            holder.lastMessage.setText(userMessage.getMessage());

            int unreadMessageCount = channelList.get(i).getUnreadMessageCount();

            if(unreadMessageCount < 1) {
                holder.unreadMessageCount.setText("");
                holder.unreadMessageCount.setVisibility(View.GONE);
            } else {
                holder.unreadMessageCount.setText(String.valueOf(unreadMessageCount));
                holder.unreadMessageCount.setVisibility(View.VISIBLE);
            }

            for(User user : channelList.get(i).getMembers()) {
                if(!user.getUserId().equals(this.uid)) {

                    String userName = user.getNickname();
                    holder.userName.setText(userName);

                    GradientDrawable drawable = (GradientDrawable) holder.connectionStatus.getBackground();
                    ColorDrawable colorDrawable = null;

                    if(user.getConnectionStatus().equals(User.ConnectionStatus.ONLINE)) {
                        colorDrawable = new ColorDrawable(Color.rgb(26,197,118));
                    } else if(user.getConnectionStatus().equals(User.ConnectionStatus.OFFLINE)) {
                        colorDrawable = new ColorDrawable(Color.rgb(204,204,204));
                    }

                    drawable.setColor(colorDrawable.getColor());

                    long dateOfSent = message.getCreatedAt();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    holder.dateOfSent.setText(sdf.format(new Date(dateOfSent)));

                    /*DownloadImageTask downloadImageTask = new DownloadImageTask(holder.userProfilePhoto, context, "list");
                    downloadImageTask.execute(user.getProfileUrl());*/

                    this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();

                    holder.userProfilePhoto.setImageUrl(user.getProfileUrl(), this.imageLoader);
                }
            }
        }

        return view;
    }

    class ViewHolder {
        RadiusNetworkImageView userProfilePhoto;
        ImageView connectionStatus;
        TextView lastMessage;
        TextView unreadMessageCount;
        TextView userName;
        TextView dateOfSent;

        public ViewHolder(View view) {
            this.userProfilePhoto = (RadiusNetworkImageView) view.findViewById(R.id.user_profile_photo);
            this.userProfilePhoto.setRadius(150f);
            this.connectionStatus = (ImageView) view.findViewById(R.id.connectionStatus) ;
            this.userName = (TextView) view.findViewById(R.id.user_name);
            this.lastMessage = (TextView) view.findViewById(R.id.content);
            this.unreadMessageCount = (TextView) view.findViewById(R.id.unreadMessageCount);
            this.dateOfSent = (TextView) view.findViewById(R.id.date_of_sent);

            view.setTag(this);
        }
    }
}
