package com.hello.holaApp.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hello.holaApp.R;
import com.hello.holaApp.activity.ChatRoomActivity;
import com.hello.holaApp.activity.CreateGroupChannelActivity;
import com.hello.holaApp.activity.MainActivity;
import com.hello.holaApp.adapter.GroupChannelListAdapter;
import com.hello.holaApp.common.CommonFunction;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;

import static android.app.Activity.RESULT_OK;

public class GroupChannelListFragment extends BaseFragment {

    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";
    public static final String EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL";
    public static final int INTENT_REQUEST_NEW_GROUP_CHANNEL = 302;

    private FirebaseAuth mAuth;
    private FirebaseUser fUser;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GroupChannelListAdapter mChannelListAdapter;
    /*private FloatingActionButton mCreateChannelFab;*/
    private GroupChannelListQuery mChannelListQuery;
    private SwipeRefreshLayout mSwipeRefresh;
    private TextView noChat;

    private boolean firstYn = true;

    public static GroupChannelListFragment newInstance() {
        GroupChannelListFragment fragment = new GroupChannelListFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAuth = FirebaseAuth.getInstance();
        this.fUser = mAuth.getCurrentUser();

        SendBird.connect(this.fUser.getUid(), new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    return;
                }
            }
        });

        mChannelListAdapter = new GroupChannelListAdapter(getActivity());
        mChannelListAdapter.load();

        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.activity_action_bar, null);
        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText("Chat");

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Medium.ttf");
        title.setTypeface(typeface);

        actionBar.setCustomView(actionView);
        actionBar.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChannelListAdapter.save();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("LIFECYCLE", "GroupChannelListFragment onCreateView()");

        progressON(getResources().getString(R.string.loading));

        View rootView = inflater.inflate(R.layout.fragment_group_channel_list, container, false);

        setRetainInstance(true);

        // Change action bar title
        /*((ChatRoomActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.all_group_channels));*/

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_group_channel_list);
        /*mCreateChannelFab = (FloatingActionButton) rootView.findViewById(R.id.fab_group_channel_list);*/
        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout_group_channel_list);

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                refreshChannelList(15);
            }
        });

        noChat = (TextView) rootView.findViewById(R.id.no_chat_list);
        /*mCreateChannelFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateGroupChannelActivity.class);
                startActivityForResult(intent, INTENT_REQUEST_NEW_GROUP_CHANNEL);
            }
        });*/

        setUpRecyclerView();
        setUpChannelListAdapter();
        return rootView;
    }

    @Override
    public void onResume() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onResume()");

        refreshChannelList(15);

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {

                mSwipeRefresh.setVisibility(View.VISIBLE);
                noChat.setVisibility(View.GONE);

                mChannelListAdapter.clearMap();
                mChannelListAdapter.updateOrInsert(baseChannel);
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                mChannelListAdapter.notifyDataSetChanged();
            }
        });

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onPause()");

        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }

    @Override
    public void onDetach() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onDetach()");
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_NEW_GROUP_CHANNEL) {
            if (resultCode == RESULT_OK) {
                // Channel successfully created
                // Enter the newly created channel.
                String newChannelUrl = data.getStringExtra(CreateGroupChannelActivity.EXTRA_NEW_CHANNEL_URL);
                if (newChannelUrl != null) {
                    enterGroupChannel(newChannelUrl);
                }
            } else {
                Log.d("GrChLIST", "resultCode not STATUS_OK");
            }
        }
    }

    // Sets up recycler view
    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChannelListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // If user scrolls to bottom of the list, loads more channels.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                    loadNextChannelList();
                }

                firstYn = false;
            }
        });
    }

    // Sets up channel list adapter
    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(new GroupChannelListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupChannel channel) {
                enterGroupChannel(channel);
            }
        });

        mChannelListAdapter.setOnItemLongClickListener(new GroupChannelListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final GroupChannel channel) {
                showChannelOptionsDialog(channel);
            }
        });
    }

    /**
     * Displays a dialog listing channel-specific options.
     */
    private void showChannelOptionsDialog(final GroupChannel channel) {
        String[] options;
        final boolean pushCurrentlyEnabled = channel.isPushEnabled();

        options = pushCurrentlyEnabled
                ? new String[]{getResources().getString(R.string.quit), String.format(getResources().getString(R.string.change_status_notification), getResources().getString(R.string.off))}
                : new String[]{getResources().getString(R.string.quit), String.format(getResources().getString(R.string.change_status_notification), getResources().getString(R.string.on))};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Show a dialog to confirm that the user wants to leave the channel.
                            new AlertDialog.Builder(getActivity())
                                    /*.setTitle("나가기 " + channel.getName() + "?")*/
                                    .setTitle(getResources().getString(R.string.quit_msg))
                                    .setPositiveButton(getResources().getString(R.string.quit), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            leaveChannel(channel);
                                        }
                                    })
                                    .setNegativeButton(getResources().getString(R.string.cancel_btn), null)
                                    .create().show();
                        } else if (which == 1) {
                            setChannelPushPreferences(channel, !pushCurrentlyEnabled);
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * Turns push notifications on or off for a selected channel.
     * @param channel   The channel for which push preferences should be changed.
     * @param on    Whether to set push notifications on or off.
     */
    private void setChannelPushPreferences(final GroupChannel channel, final boolean on) {
        // Change push preferences.
        channel.setPushPreference(on, new GroupChannel.GroupChannelSetPushPreferenceHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    Crashlytics.logException(e);

                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                String toast = on
                        ? String.format(getResources().getString(R.string.change_status_notification_msg), "꺼졌습니다")
                        : String.format(getResources().getString(R.string.change_status_notification_msg), "켜졌습니다");

                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT)
                        .show();

                refreshChannelList(15);
            }
        });
    }

    /**
     * Enters a Group Channel. Upon entering, a GroupChatFragment will be inflated
     * to display messages within the channel.
     *
     * @param channel The Group Channel to enter.
     */
    void enterGroupChannel(GroupChannel channel) {
        final String channelUrl = channel.getUrl();

        enterGroupChannel(channelUrl);
    }

    /**
     * Enters a Group Channel with a URL.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    void enterGroupChannel(String channelUrl) {
        /*GroupChatFragment fragment = GroupChatFragment.newInstance(channelUrl);
        getFragmentManager().beginTransaction()
                .replace(R.id.container_group_channel, fragment)
                .addToBackStack(null)
                .commit();*/

        ((MainActivity)getActivity()).tabIndex = 2;

        CommonFunction.updateNotificationBadge();

        GroupChannel.getChannel(channelUrl, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Crashlytics.logException(e);

                    e.printStackTrace();
                    return;
                }

                Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                intent.putExtra("channelUrl", channelUrl);
                intent.putExtra("uid", fUser.getUid());
                intent.putExtra("profileUrl", fUser.getPhotoUrl().toString());

                List<Member> memberList = groupChannel.getMembers();
                for (Member member : memberList) {
                    if(!member.getUserId().equals(fUser.getUid())) {
                        intent.putExtra("senderName", member.getNickname());
                        break;
                    }
                }

                startActivity(intent);

                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    /**
     * Creates a new query to get the list of the user's Group Channels,
     * then replaces the existing dataset.
     *
     * @param numChannels The number of channels to load.
     */
    private void refreshChannelList(int numChannels) {
        mChannelListQuery = GroupChannel.createMyGroupChannelListQuery();
        mChannelListQuery.setLimit(numChannels);

        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    return;
                }

                if(list.size() > 0) {
                    mSwipeRefresh.setVisibility(View.VISIBLE);
                    noChat.setVisibility(View.GONE);

                    /*int unReadCnt = 0;
                    for(GroupChannel channel : list) {
                        unReadCnt += channel.getUnreadMessageCount();
                    }

                    final NavigationTabBar.Model model = MainActivity.navigationTabBar.getModels().get(2);
                    model.updateBadgeTitle(String.valueOf(unReadCnt));
                    model.showBadge();*/

                } else {
                    mSwipeRefresh.setVisibility(View.GONE);
                    noChat.setVisibility(View.VISIBLE);
                }

                mChannelListAdapter.clearMap();
                mChannelListAdapter.setGroupChannelList(list);

                progressOFF();
            }
        });

        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    /**
     * Loads the next channels from the current query instance.
     */
    private void loadNextChannelList() {
        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    return;
                }

                for (GroupChannel channel : list) {
                    mChannelListAdapter.addLast(channel);
                }
            }
        });
    }

    /**
     * Leaves a Group Channel.
     *
     * @param channel The channel to leave.
     */
    private void leaveChannel(final GroupChannel channel) {
        channel.leave(new GroupChannel.GroupChannelLeaveHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Crashlytics.logException(e);
                    return;
                }

                // Re-query message list
                refreshChannelList(15);
            }
        });
    }
}
