package com.hello.holaApp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hello.holaApp.R;
import com.hello.holaApp.common.DateUtils;
import com.hello.holaApp.common.FileUtils;
import com.hello.holaApp.common.RadiusNetworkImageView;
import com.hello.holaApp.common.TextUtils;
import com.hello.holaApp.common.TypingIndicator;
import com.hello.holaApp.common.VolleySingleton;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Displays a list of Group Channels within a SendBird application.
 */
public class GroupChannelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<GroupChannel> mChannelList;
    private Context mContext;
    private ConcurrentHashMap<SimpleTarget<Bitmap>, Integer> mSimpleTargetIndexMap;
    private ConcurrentHashMap<SimpleTarget<Bitmap>, GroupChannel> mSimpleTargetGroupChannelMap;
    private ConcurrentHashMap<String, Integer> mChannelImageNumMap;
    private ConcurrentHashMap<String, ImageView> mChannelImageViewMap;
    private ConcurrentHashMap<String, SparseArray<Bitmap>> mChannelBitmapMap;

    private boolean mIsCacheLoading = false;

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    private FirebaseAuth mAuth;
    private FirebaseUser fUser;

    public interface OnItemClickListener {
        void onItemClick(GroupChannel channel);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(GroupChannel channel);
    }

    public GroupChannelListAdapter(Context context) {
        mContext = context;

        mSimpleTargetIndexMap = new ConcurrentHashMap<>();
        mSimpleTargetGroupChannelMap = new ConcurrentHashMap<>();
        mChannelImageNumMap = new ConcurrentHashMap<>();
        mChannelImageViewMap = new ConcurrentHashMap<>();
        mChannelBitmapMap = new ConcurrentHashMap<>();

        mChannelList = new ArrayList<>();

        this.mAuth = FirebaseAuth.getInstance();
        this.fUser = mAuth.getCurrentUser();
    }

    public void clearMap() {
        mSimpleTargetIndexMap.clear();
        mSimpleTargetGroupChannelMap.clear();
        mChannelImageNumMap.clear();
        mChannelImageViewMap.clear();
        mChannelBitmapMap.clear();
    }

    public void load() {
        try {
            File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
            appDir.mkdirs();

            File dataFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + "channel_list") + ".data");

            String content = FileUtils.loadFromFile(dataFile);
            String[] dataArray = content.split("\n");

            // Reset channel list, then add cached data.
            mChannelList.clear();
            for(int i = 0; i < dataArray.length; i++) {
                mChannelList.add((GroupChannel) BaseChannel.buildFromSerializedData(Base64.decode(dataArray[i], Base64.DEFAULT | Base64.NO_WRAP)));
            }

            mIsCacheLoading = true;

            notifyDataSetChanged();
        } catch(Exception e) {
            // Nothing to load.
        }
    }

    public void save() {
        try {
            StringBuilder sb = new StringBuilder();
            if (mChannelList != null && mChannelList.size() > 0) {
                // Convert current data into string.
                GroupChannel channel = null;
                for (int i = 0; i < Math.min(mChannelList.size(), 100); i++) {
                    channel = mChannelList.get(i);
                    sb.append("\n");
                    sb.append(Base64.encodeToString(channel.serialize(), Base64.DEFAULT | Base64.NO_WRAP));
                }
                // Remove first newline.
                sb.delete(0, 1);

                String data = sb.toString();
                String md5 = TextUtils.generateMD5(data);

                // Save the data into file.
                File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
                appDir.mkdirs();

                File hashFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + "channel_list") + ".hash");
                File dataFile = new File(appDir, TextUtils.generateMD5(SendBird.getCurrentUser().getUserId() + "channel_list") + ".data");

                try {
                    String content = FileUtils.loadFromFile(hashFile);
                    // If data has not been changed, do not save.
                    if(md5.equals(content)) {
                        return;
                    }
                } catch(IOException e) {
                    // File not found. Save the data.
                }

                FileUtils.saveToFile(dataFile, data);
                FileUtils.saveToFile(hashFile, md5);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_group_channel, parent, false);

        return new ChannelHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ChannelHolder) holder).bind(mContext, mChannelList.get(position), mItemClickListener, mItemLongClickListener);

        /*List<Member> memberList = mChannelList.get(position).getMembers();
        for(Member member : memberList) {
            GradientDrawable drawable = (GradientDrawable) ((ChannelHolder) holder).connectionStatus.getBackground();

            if(!member.getUserId().equals(this.fUser.getUid())) {
                ColorDrawable colorDrawable = null;

                if (member.getConnectionStatus().equals(User.ConnectionStatus.ONLINE)) {
                    colorDrawable = new ColorDrawable(Color.rgb(26, 197, 118));
                } else if (member.getConnectionStatus().equals(User.ConnectionStatus.OFFLINE)) {
                    colorDrawable = new ColorDrawable(Color.rgb(204, 204, 204));
                }

                drawable.setColor(colorDrawable.getColor());
            }
        }*/
    }

    @Override
    public int getItemCount() {
        return mChannelList.size();
    }

    public void setGroupChannelList(List<GroupChannel> channelList) {
        mChannelList = channelList;
        mIsCacheLoading = false;
        notifyDataSetChanged();
    }

    public void addLast(GroupChannel channel) {
        mChannelList.add(channel);
        notifyDataSetChanged();
    }

    // If the channel is not in the list yet, adds it.
    // If it is, finds the channel in current list, and replaces it.
    // Moves the updated channel to the front of the list.
    public void updateOrInsert(BaseChannel channel) {
        if (!(channel instanceof GroupChannel)) {
            return;
        }

        GroupChannel groupChannel = (GroupChannel) channel;

        for (int i = 0; i < mChannelList.size(); i++) {
            if (mChannelList.get(i).getUrl().equals(groupChannel.getUrl())) {
                mChannelList.remove(mChannelList.get(i));
                mChannelList.add(0, groupChannel);
                notifyDataSetChanged();
                Log.v(GroupChannelListAdapter.class.getSimpleName(), "Channel replaced.");
                return;
            }
        }

        mChannelList.add(0, groupChannel);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    /**
     * A ViewHolder that contains UI to display information about a GroupChannel.
     */
    private class ChannelHolder extends RecyclerView.ViewHolder {

        TextView topicText, lastMessageText, unreadCountText, dateText, memberCountText;
        RadiusNetworkImageView coverImage;
        LinearLayout typingIndicatorContainer;

        ImageView connectionStatus;

        FirebaseAuth mAuth;
        FirebaseUser fUser;
        ImageLoader imageLoader;

        ChannelHolder(View itemView) {
            super(itemView);

            topicText = (TextView) itemView.findViewById(R.id.text_group_channel_list_topic);
            lastMessageText = (TextView) itemView.findViewById(R.id.text_group_channel_list_message);
            unreadCountText = (TextView) itemView.findViewById(R.id.text_group_channel_list_unread_count);
            dateText = (TextView) itemView.findViewById(R.id.text_group_channel_list_date);
            memberCountText = (TextView) itemView.findViewById(R.id.text_group_channel_list_member_count);
            coverImage = (RadiusNetworkImageView) itemView.findViewById(R.id.image_group_channel_list_cover);
            coverImage.setRadius(100f);

            this.connectionStatus = (ImageView) itemView.findViewById(R.id.connectionStatus);
            this.connectionStatus.setVisibility(View.GONE);

            typingIndicatorContainer = (LinearLayout) itemView.findViewById(R.id.container_group_channel_list_typing_indicator);

            this.mAuth = FirebaseAuth.getInstance();
            this.fUser = mAuth.getCurrentUser();
        }

        /**
         * Binds views in the ViewHolder to information contained within the Group Channel.
         * @param context
         * @param channel
         * @param clickListener A listener that handles simple clicks.
         * @param longClickListener A listener that handles long clicks.
         */
        void bind(final Context context, final GroupChannel channel,
                  @Nullable final OnItemClickListener clickListener,
                  @Nullable final OnItemLongClickListener longClickListener) {
            topicText.setText(TextUtils.getGroupChannelTitle(channel));
            memberCountText.setText(String.valueOf(channel.getMemberCount()));

            this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();

            if (!mIsCacheLoading) {
                List<Member> memberList = channel.getMembers();
                for (Member member : memberList) {
                    if(!member.getUserId().equals(this.fUser.getUid())) {
                        this.coverImage.setImageUrl(member.getProfileUrl(), imageLoader);
                    }
                }
                /*setChannelImage(context, channel, coverImage);*/
            }

            int unreadCount = channel.getUnreadMessageCount();
            // If there are no unread messages, hide the unread count badge.
            if (unreadCount == 0) {
                unreadCountText.setVisibility(View.INVISIBLE);
            } else {
                unreadCountText.setVisibility(View.VISIBLE);
                unreadCountText.setText(String.valueOf(channel.getUnreadMessageCount()));
            }

            BaseMessage lastMessage = channel.getLastMessage();
            if (lastMessage != null) {
                // Display information about the most recently sent message in the channel.
                dateText.setText(String.valueOf(DateUtils.formatDateTime(lastMessage.getCreatedAt())));

                // Bind last message text according to the type of message. Specifically, if
                // the last message is a File Message, there must be special formatting.
                if (lastMessage instanceof UserMessage) {
                    lastMessageText.setText(((UserMessage) lastMessage).getMessage());
                } else if (lastMessage instanceof AdminMessage) {
                    lastMessageText.setText(((AdminMessage) lastMessage).getMessage());
                } else {
                    String lastMessageString = String.format(
                            context.getString(R.string.group_channel_list_file_message_text),
                            ((FileMessage) lastMessage).getSender().getNickname());
                    lastMessageText.setText(lastMessageString);
                }
            }

            /*
             * Set up the typing indicator.
             * A typing indicator is basically just three dots contained within the layout
             * that animates. The animation is implemented in the {@link TypingIndicator#animate() class}
             */
            ArrayList<ImageView> indicatorImages = new ArrayList<>();
            indicatorImages.add((ImageView) typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_1));
            indicatorImages.add((ImageView) typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_2));
            indicatorImages.add((ImageView) typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_3));

            TypingIndicator indicator = new TypingIndicator(indicatorImages, 600);
            indicator.animate();

            // debug
//            typingIndicatorContainer.setVisibility(View.VISIBLE);
//            lastMessageText.setText(("Someone is typing"));

            // If someone in the channel is typing, display the typing indicator.
            if (channel.isTyping()) {
                typingIndicatorContainer.setVisibility(View.VISIBLE);
                lastMessageText.setText(("Someone is typing"));
            } else {
                // Display typing indicator only when someone is typing
                typingIndicatorContainer.setVisibility(View.GONE);
            }

            // Set an OnClickListener to this item.
            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onItemClick(channel);
                    }
                });
            }

            // Set an OnLongClickListener to this item.
            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onItemLongClick(channel);

                        // return true if the callback consumed the long click
                        return true;
                    }
                });
            }
        }

        /*private void setChannelImage(Context context, GroupChannel channel, MultiImageView multiImageView) {
            List<Member> members = channel.getMembers();
            if (members != null) {
                int size = members.size();
                if (size >= 1) {
                    int imageNum = size;
                    if (size >= 4) {
                        imageNum = 4;
                    }

                    if (!mChannelImageNumMap.containsKey(channel.getUrl())) {
                        mChannelImageNumMap.put(channel.getUrl(), imageNum);
                        mChannelImageViewMap.put(channel.getUrl(), multiImageView);

                        multiImageView.clear();

                        for (int index = 0; index < imageNum; index++) {
                            SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> glideAnimation) {
                                    Integer index = mSimpleTargetIndexMap.get(this);
                                    if (index != null) {
                                        GroupChannel channel = mSimpleTargetGroupChannelMap.get(this);

                                        SparseArray<Bitmap> array = mChannelBitmapMap.get(channel.getUrl());
                                        if (array == null) {
                                            array = new SparseArray<>();
                                            mChannelBitmapMap.put(channel.getUrl(), array);
                                        }
                                        array.put(index, resource);

                                        Integer num = mChannelImageNumMap.get(channel.getUrl());
                                        if (num != null) {
                                            if (array.size() == num) {
                                                MultiImageView multiImageView = (MultiImageView) mChannelImageViewMap.get(channel.getUrl());

                                                for (int i = 0; i < array.size(); i++) {
                                                    multiImageView.addImage(array.get(i));
                                                }
                                            }
                                        }
                                    }
                                }
                            };

                            mSimpleTargetIndexMap.put(simpleTarget, index);
                            mSimpleTargetGroupChannelMap.put(simpleTarget, channel);

                            RequestOptions myOptions = new RequestOptions()
                                    .dontAnimate()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .placeholder(null);

                            Glide.with(context)
                                    .asBitmap()
                                    .load(members.get(index).getProfileUrl())
                                    .apply(myOptions)
                                    .into(simpleTarget);
                        }
                    }
                }
            }
        }*/
    }
}