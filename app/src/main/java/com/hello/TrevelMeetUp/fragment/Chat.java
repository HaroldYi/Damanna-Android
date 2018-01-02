package com.hello.TrevelMeetUp.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.activity.ChatRoomActivity;
import com.hello.TrevelMeetUp.adapter.ChatListViewAdapter;
import com.hello.TrevelMeetUp.common.Constant;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.intentservice.chatui.models.ChatMessage;

/**
 * Created by lji5317 on 06/12/2017.
 */

public class Chat extends BaseFragment implements View.OnClickListener {

    private SwipeMenuListView listView;
    private FirebaseAuth mAuth;
    private FirebaseUser fUser;

    private List<GroupChannel> channels;

    private ChatListViewAdapter chatListViewAdapter;

    private int position;

    @Override
    public void onClick(View view) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.progressON(getResources().getString(R.string.loading));

        View view = inflater.inflate(R.layout.chat_layout, container, false);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.say_swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            this.loadingChatRoomList();
            swipeRefreshLayout.setRefreshing(false);
        });

        this.listView = (SwipeMenuListView) view.findViewById(R.id.listView);
        this.listView.setVisibility(View.INVISIBLE);

        this.mAuth = FirebaseAuth.getInstance();
        this.fUser = this.mAuth.getCurrentUser();

        SendBird.connect(this.fUser.getUid(), (user, e) -> {
            if (e != null) {
                // Error.
                Log.e("sendBirdErr", e.getMessage());
                return;
            } else {
                this.loadingChatRoomList();
            }
        });

        SendBird.addChannelHandler(Constant.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                int i = 0;
                for(GroupChannel groupChannel : channels) {
                    if(groupChannel.getUrl().equals(baseChannel.getUrl())) {
                        if(channels.isEmpty()) {
                            channels.add(groupChannel);
                        } else {
                            channels.set(i, groupChannel);
                        }
                    }
                    i++;
                }

                chatListViewAdapter.notifyDataSetChanged();
            }
        });

        // set creator
        this.listView.setMenuCreator(menu -> {
            // create "open" item
            SwipeMenuItem openItem = new SwipeMenuItem(
                    getActivity().getApplicationContext());
            // set item background
            openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                    0xCE)));
            // set item width
            openItem.setWidth(this.dp2px(90));
            // set item title
            openItem.setTitle("Open");
            // set item title fontsize
            openItem.setTitleSize(18);
            // set item title font color
            openItem.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(openItem);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(
                    getActivity().getApplicationContext());
            // set item background
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                    0x3F, 0x25)));
            // set item width
            deleteItem.setWidth(this.dp2px(90));
            // set a icon
            /*deleteItem.setIcon(R.drawable.ic_delete);*/

            // set item title fontsize
            deleteItem.setTitleSize(18);

            deleteItem.setTitleColor(Color.WHITE);
            deleteItem.setTitle("Leave");
            // add to menu
            menu.addMenuItem(deleteItem);
        });

        this.listView.setOnItemClickListener((adapterView, view1, position, l_position) -> {
            // AdapterView<?> adapterView, View view, int i, long ladapterView.

            TextView userName = (TextView) view1.findViewById(R.id.user_name);

            Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
            intent.putExtra("channelUrl", this.channels.get(position).getUrl());
            intent.putExtra("userName", userName.getText().toString());

            this.position = position;

            this.channels.get(position).markAsRead();
            this.chatListViewAdapter.notifyDataSetChanged();

            startActivity(intent);
        });

        this.listView.setOnMenuItemClickListener((position, menu, index) -> {
            switch (index) {
                case 0:
                    // open
                    TextView userName = (TextView) listView.getChildAt(index).findViewById(R.id.user_name);
                    Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                    intent.putExtra("channelUrl", channels.get(position).getUrl());
                    intent.putExtra("userName", userName.getText().toString());

                    this.position = position;

                    this.channels.get(position).markAsRead();
                    this.chatListViewAdapter.notifyDataSetChanged();

                    startActivity(intent);
                    break;
                case 1:
                    // Leave Room
                    /*GroupChannel.getChannel(channels.get(position).getUrl(), (groupChannel, e) -> {
                        groupChannel.leave(e1 -> {
                            if (e1 != null) {
                                // Error.
                                return;
                            }
                        });
                    });*/
                    break;
            }
            // false : close the menu; true : not close the menu
            return false;
        });

        this.listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.chatListViewAdapter != null) {
            int i = 0;
            for(GroupChannel groupChannel : this.channels) {
                if(this.channels.isEmpty()) {
                    this.channels.add(groupChannel);
                } else {
                    this.channels.set(i, groupChannel);
                }
                i++;
            }

            this.chatListViewAdapter.notifyDataSetChanged();
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void loadingChatRoomList() {
        GroupChannelListQuery channelListQuery = GroupChannel.createMyGroupChannelListQuery();

        List<String> userList = new ArrayList<>();
        userList.add(this.mAuth.getCurrentUser().getUid());
        channelListQuery.setUserIdsExactFilter(userList);

        channelListQuery.next((List<GroupChannel> channels, SendBirdException er) -> {
            if (er != null) {
                // Error.
                return;
            } else {
                this.channels = channels;
                this.chatListViewAdapter = new ChatListViewAdapter(getActivity(), R.layout.chat_layout, channels, fUser.getUid());
                this.listView.setAdapter(this.chatListViewAdapter);

                this.progressOFF();
                this.listView.setVisibility(View.VISIBLE);
                        /*new Handler().postDelayed(() -> {
                            progressOFF();
                            this.listView.setVisibility(View.VISIBLE);
                        }, 100);*/
            }
        });
    }
}
