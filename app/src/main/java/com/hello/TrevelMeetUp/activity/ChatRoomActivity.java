package com.hello.TrevelMeetUp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.common.Constant;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.List;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

/**
 * Created by lji5317 on 11/12/2017.
 */

public class ChatRoomActivity extends BaseActivity {

    private GroupChannel groupChannel;
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
                Log.e("groupChannel1", e.getMessage());
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
                        Log.e("groupChannel", e1.getMessage());
                        return;
                    }

                    loadChatMessages();
                });
            } else {
                GroupChannel.getChannel(channelUrl, (groupChannel, e1) -> {
                    // public void onResult(GroupChannel groupChannel, SendBirdException e) {
                    if (e1 != null) {
                        // Error!
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
    }
}
