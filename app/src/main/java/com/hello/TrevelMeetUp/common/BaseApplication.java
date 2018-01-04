package com.hello.TrevelMeetUp.common;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.hello.TrevelMeetUp.R;
import com.sendbird.android.SendBird;
import com.tsengvn.typekit.Typekit;

import io.fabric.sdk.android.Fabric;

/**
 * Created by TedPark on 2017. 3. 18..
 */

public class BaseApplication extends Application {

    private static BaseApplication baseApplication;
    AppCompatDialog progressDialog;

    public static BaseApplication getInstance() {
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this, "fonts/NotoSans-Regular.ttf"))
                .addBold(Typekit.createFromAsset(this, "fonts/NotoSans-Bold.ttf"));

        SendBird.init(getResources().getString(R.string.app_id), getApplicationContext());

        Fabric.with(this, new Crashlytics());

        this.baseApplication = this;
    }

    public void progressON(Activity activity, String message) {

        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            progressSET(message);
        } else {

            this.progressDialog = new AppCompatDialog(activity);
            this.progressDialog.setCancelable(false);
            this.progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            this.progressDialog.setContentView(R.layout.progress_loading);
            this.progressDialog.show();

        }

        final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
        img_loading_frame.post(() -> frameAnimation.start());

        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }

    public void progressSET(String message) {

        if (this.progressDialog == null || !this.progressDialog.isShowing()) {
            return;
        }

        TextView tv_progress_message = (TextView) this.progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }

    public void progressOFF() {
        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
    }
}
