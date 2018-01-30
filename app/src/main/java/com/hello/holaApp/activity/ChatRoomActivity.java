package com.hello.holaApp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hello.holaApp.R;
import com.hello.holaApp.common.CommonFunction;
import com.hello.holaApp.fragment.GroupChatFragment;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class ChatRoomActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser fUser;

    public static final String EXTRA_NEW_CHANNEL_URL = "EXTRA_NEW_CHANNEL_URL";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_channel);

        CommonFunction.sendMsg(getApplicationContext());

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_group_channel);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        }
*/
        this.mAuth = FirebaseAuth.getInstance();
        this.fUser = mAuth.getCurrentUser();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        View actionView = getLayoutInflater().inflate(R.layout.new_say_action_bar, null);

        TextView title = (TextView) actionView.findViewById(R.id.actionBarTitle);
        title.setText(getIntent().getStringExtra(""));

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/NotoSans-Medium.ttf");
        title.setTypeface(typeface);

        actionBar.setCustomView(actionView);

        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setVisibility(View.GONE);

        ImageButton backBtn = (ImageButton) actionView.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view1 -> {
            boolean userInfoYn = getIntent().getBooleanExtra("userInfoYn", false);
            if(!userInfoYn) {
                MainActivity.tabIndex = 1;
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        });

        /*if (savedInstanceState == null) {
            // If started from launcher, load list of Open Channels
            Fragment fragment = GroupChannelListFragment.newInstance();

            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack();

            manager.beginTransaction()
                    .replace(R.id.container_group_channel, fragment)
                    .commit();
        }*/

        String channelUrl = getIntent().getStringExtra("channelUrl");
        String uid = getIntent().getStringExtra("uid");

        SendBird.connect(fUser.getUid(), new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                title.setText(getIntent().getStringExtra("senderName"));

                if(channelUrl != null) {

                    // If started from notification
                    Fragment fragment = GroupChatFragment.newInstance(channelUrl);
                    FragmentManager manager = getSupportFragmentManager();
                    manager.beginTransaction()
                            .replace(R.id.container_group_channel, fragment)
                            .addToBackStack(null)
                            .commit();
                } else {

                    /*GroupChannel.getChannel(channelUrl, new GroupChannel.GroupChannelGetHandler() {
                        @Override
                        public void onResult(GroupChannel groupChannel, SendBirdException e) {
                            if (e != null) {
                                // Error!
                                e.printStackTrace();
                                return;
                            }

                            List<Member> memberList = groupChannel.getMembers();
                            for (Member member : memberList) {
                                if(member.getUserId().equals(uid)) {
                                    title.setText(member.getNickname());
                                    break;
                                }
                            }
                        }
                    });*/

                    List<String> userList = new ArrayList<>();
                    userList.add(uid);
                    userList.add(fUser.getUid());

                    GroupChannel.createChannelWithUserIds(userList, true, new GroupChannel.GroupChannelCreateHandler() {
                        @Override
                        public void onResult(GroupChannel groupChannel, SendBirdException e) {
                            if (e != null) {
                                // Error!
                                Log.d("errrr", e.getMessage());
                                Crashlytics.logException(e);
                                return;
                            }

                            Fragment fragment = GroupChatFragment.newInstance(groupChannel.getUrl());
                            FragmentManager manager = getSupportFragmentManager();
                            manager.beginTransaction()
                                    .replace(R.id.container_group_channel, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });
                }
            }
        });
    }

    public interface onBackPressedListener {
        boolean onBack();
    }
    private onBackPressedListener mOnBackPressedListener;

    public void setOnBackPressedListener(onBackPressedListener listener) {
        mOnBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (mOnBackPressedListener != null && mOnBackPressedListener.onBack()) {
            return;
        }
        boolean userInfoYn = getIntent().getBooleanExtra("userInfoYn", false);
        if(!userInfoYn) {
            MainActivity.tabIndex = 1;
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /*private GroupChannel groupChannel;
    private FirebaseAuth mAuth;
    private FirebaseUser fUser;

    private ChatView chatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        this.mAuth = FirebaseAuth.getInstance();
        this.fUser = mAuth.getCurrentUser();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //true설정을 해주셔야 합니다.
        actionBar.setDisplayHomeAsUpEnabled(false); //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false); //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false); //홈 아이콘을 숨김처리합니다.

        View view = getLayoutInflater().inflate(R.layout.custom_action, null);
        actionBar.setCustomView(view);

        Button backBtn = (Button) view.findViewById(R.id.backBtn);
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);

        backBtn.setOnClickListener(view1 -> {
            finish();
        });

        this.chatView = (ChatView) findViewById(R.id.chat_view);
        this.chatView.setOnSentMessageListener(chatMessage -> {
            // perform actual message sending
            if(this.groupChannel != null) {
                this.groupChannel.sendUserMessage(chatMessage.getMessage(), (userMessage, e) -> {
                    if (e != null) {
                        // Error.
                        Crashlytics.logException(e);
                        return;
                    }
                });
            }

            return true;
        });

        Intent intent = getIntent();

        String channelUrl = intent.getStringExtra("channelUrl");
        String userName = intent.getStringExtra("userName");
        String profileUrl = intent.getStringExtra("profileUrl");
        String uid = intent.getStringExtra("uid");

        textView.setText(userName);

        SendBird.connect(this.fUser.getUid(), (user, e) -> {
            if (e != null) {
                // Error.
                Crashlytics.logException(e);
                return;
            }

            if(channelUrl == null || channelUrl.isEmpty()) {

                List<String> userList = new ArrayList<>();
                userList.add(this.fUser.getUid());
                userList.add(uid);

                GroupChannel.createChannelWithUserIds(userList, true, (GroupChannel.GroupChannelCreateHandler) (gc, e1) -> {

                    this.groupChannel = gc;

                    if (e1 != null) {
                        // Error.
                        Crashlytics.logException(e1);
                        return;
                    }

                    loadChatMessages();
                });
            } else {
                GroupChannel.getChannel(channelUrl, (groupChannel, e1) -> {
                    // public void onResult(GroupChannel groupChannel, SendBirdException e) {
                    if (e1 != null) {
                        // Error!
                        Crashlytics.logException(e1);
                        return;
                    }

                    this.groupChannel = groupChannel;
                    groupChannel.markAsRead();

                    List<Member> memberList = groupChannel.getMembers();
                    for(Member member : memberList) {
                        if(member.getUserId().equals(this.fUser.getUid())) {
                            textView.setText(member.getNickname());
                        }
                    }
                    loadChatMessages();
                });
            }
        });

        SendBird.addChannelHandler(Constant.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {

                groupChannel.markAsRead();

                if (baseMessage instanceof UserMessage) {
                    // message is a UserMessage
                    UserMessage userMessage = (UserMessage)baseMessage;
                    ChatMessage chatMessage = new ChatMessage(userMessage.getMessage(), userMessage.getCreatedAt(), ChatMessage.Type.RECEIVED);
                    chatView.addMessage(chatMessage);
                } else if (baseMessage instanceof FileMessage) {
                    // message is a FileMessage
                } else if (baseMessage instanceof AdminMessage) {
                    // message is an AdminMessage
                }
            }
        });
    }

    private void loadChatMessages() {
        PreviousMessageListQuery prevMessageListQuery = this.groupChannel.createPreviousMessageListQuery();
        prevMessageListQuery.load(30, false, (messages, e1) -> {
            if (e1 != null) {
                // Error.
                Crashlytics.logException(e1);
                return;
            }

            for(BaseMessage message : messages) {
                if (message instanceof UserMessage) {
                    // message is a UserMessage
                    UserMessage userMessage = (UserMessage)message;
                    ChatMessage.Type type;

                    if(this.fUser.getUid().equals(userMessage.getSender().getUserId())) {
                        type = ChatMessage.Type.SENT;
                    } else {
                        type = ChatMessage.Type.RECEIVED;
                    }

                    ChatMessage chatMessage = new ChatMessage(userMessage.getMessage(), userMessage.getCreatedAt(), type);
                    this.chatView.addMessage(chatMessage);

                } else if (message instanceof FileMessage) {
                    // message is a FileMessage
                } else if (message instanceof AdminMessage) {
                    // message is an AdminMessage
                }
            }
        });
    }*/
}
