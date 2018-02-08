package com.hello.holaApp.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import com.crashlytics.android.Crashlytics;
import com.hello.holaApp.R;
import com.hello.holaApp.activity.ChatRoomActivity;
import com.hello.holaApp.activity.MainActivity;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by lji5317 on 20/12/2017.
 */

public class CommonFunction {

    private static double latitude = 0f;
    private static double longitude = 0f;

    public static double getLatitude() {
        return latitude;
    }

    public static void setLatitude(double latitude) {
        CommonFunction.latitude = latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    public static void setLongitude(double longitude) {
        CommonFunction.longitude = longitude;
    }

    @Deprecated
    public static Bitmap getBitmapFromURL(String imageURL) {
        try {
            URL url = new URL(imageURL);
            HttpsURLConnection connection = (HttpsURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input).copy(Bitmap.Config.ARGB_8888, true);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Deprecated
    public static Bitmap getRoundedCornerBitmapFromUrl(String imageURL) {

        Bitmap bitmap;

        try {
            URL url = new URL(imageURL);
            HttpsURLConnection connection = (HttpsURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input).copy(Bitmap.Config.ARGB_8888, true);
        } catch (IOException e) {
            // Log exception
            Crashlytics.logException(e);
            return null;
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 50;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static void sendMsg(Context context) {

        updateNotificationBadge();

        SendBird.addChannelHandler(Constant.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {

                if (baseMessage instanceof UserMessage) {

                    UserMessage userMessage = (UserMessage)baseMessage;

                    /*// message is a UserMessage
                    Intent intent = new Intent(context, ChatRoomActivity.class);
                    intent.putExtra("channelUrl", userMessage.getChannelUrl());
                    intent.putExtra("senderName", userMessage.getSender().getNickname());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1 *//* Request code *//*, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    String msg = userMessage.getMessage();

                    Notification.Builder notificationBuilder = new Notification.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(context.getResources().getString(R.string.app_name))
                            .setContentText(msg)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());
                    */

                    sendNotification(context, userMessage.getMessage(), userMessage.getChannelUrl(), userMessage.getSender().getNickname(), false);
                } else if (baseMessage instanceof FileMessage) {
                    // message is a FileMessage
                } else if (baseMessage instanceof AdminMessage) {
                    // message is an AdminMessage
                }
            }
        });
    }

    public static void sendNotification(Context context, String messageBody, String channelUrl, String senderName, boolean offYn) {

        updateNotificationBadge();

        Intent intent = new Intent(context, ChatRoomActivity.class);
        intent.putExtra("channelUrl", channelUrl);
        intent.putExtra("senderName", senderName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);

        if(pref.getBoolean("notification", true)) {

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification.Builder notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setContentText(messageBody)
                    .setAutoCancel(true);

            if(pref.getBoolean("bell", true) && pref.getBoolean("vibration", true)) {
                notificationBuilder.setSound(defaultSoundUri);
                notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            } else if(pref.getBoolean("bell", true) && !pref.getBoolean("vibration", true)) {
                notificationBuilder.setSound(defaultSoundUri);
            } else if(!pref.getBoolean("bell", true) && pref.getBoolean("vibration", true)) {
                notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }

            notificationBuilder.setPriority(Notification.PRIORITY_MAX)
                    /*.setDefaults(Notification.DEFAULT_ALL)*/
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    public static void updateNotificationBadge() {
        GroupChannelListQuery mChannelListQuery = GroupChannel.createMyGroupChannelListQuery();
        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    return;
                }

                final NavigationTabBar.Model model = MainActivity.navigationTabBar.getModels().get(2);
                if(list.size() > 0) {

                    int unReadCnt = 0;
                    for(GroupChannel channel : list) {
                        unReadCnt += channel.getUnreadMessageCount();
                    }

                    if(unReadCnt > 0) {
                        model.hideBadge();
                        model.setBadgeTitle(String.valueOf(unReadCnt));
                        model.showBadge();
                    } else {
                        model.hideBadge();
                    }

                } else {
                    model.hideBadge();
                }
            }
        });
    }

    public static int convertTodp(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) px, context.getResources().getDisplayMetrics());
    }
}
