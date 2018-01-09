package com.hello.TrevelMeetUp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.common.BaseSwipListAdapter;
import com.hello.TrevelMeetUp.common.DownloadImageTask;
import com.meg7.widget.CircleImageView;
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

    private static Context context;
    private int resource;
    private List<GroupChannel> channelList;

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

        BaseMessage message = channelList.get(i).getLastMessage();
        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage)message;
            holder.lastMessage.setText(userMessage.getMessage());

            int unreadMessageCount = channelList.get(i).getUnreadMessageCount();

            if(unreadMessageCount < 1) {
                holder.unreadMessageCount.setText("0");
                holder.unreadMessageCount.setVisibility(View.VISIBLE);
            } else {
                holder.unreadMessageCount.setText(String.valueOf(unreadMessageCount));
                holder.unreadMessageCount.setVisibility(View.VISIBLE);
            }

            for(User user : channelList.get(i).getMembers()) {
                if(!user.getUserId().equals(this.uid)) {

                    String userName = user.getNickname();
                    holder.userName.setText(userName);

                    long dateOfSent = message.getCreatedAt();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    holder.dateOfSent.setText(sdf.format(new Date(dateOfSent)));

                    DownloadImageTask downloadImageTask = new DownloadImageTask(holder.userProfilePhoto, "list");
                    downloadImageTask.execute(user.getProfileUrl());
                }
            }
        }

        return view;
    }

    class ViewHolder {
        CircleImageView userProfilePhoto;
        TextView lastMessage;
        TextView unreadMessageCount;
        TextView userName;
        TextView dateOfSent;

        public ViewHolder(View view) {
            userProfilePhoto = (CircleImageView) view.findViewById(R.id.user_profile_photo);
            userName = (TextView) view.findViewById(R.id.user_name);
            lastMessage = (TextView) view.findViewById(R.id.content);
            unreadMessageCount = (TextView) view.findViewById(R.id.unreadMessageCount);
            dateOfSent = (TextView) view.findViewById(R.id.date_of_sent);

            view.setTag(this);
        }
    }
}
