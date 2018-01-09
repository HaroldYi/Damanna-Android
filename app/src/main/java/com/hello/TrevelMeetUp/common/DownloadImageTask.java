package com.hello.TrevelMeetUp.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hello.TrevelMeetUp.R;
import com.sendbird.android.SendBird;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by lji5317 on 19/12/2017.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView bmImage;
    private static Context context;
    private String flag;

    public DownloadImageTask(ImageView bmImage, String flag) {
        this.bmImage = bmImage;
        this.flag = flag;
    }

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    public DownloadImageTask(String flag) {
        this.flag = flag;
    }

    @Override
    protected Bitmap doInBackground(String... args) {
        String imageURL = args[0];
        return loadBackgroundBitmap(imageURL);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        /*if (this.flag != null && this.flag.equals("list")) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bmImage.getLayoutParams();
            params.setMargins(20, 20, 0, 20);
        }
*/
        this.bmImage.setImageBitmap(result);
    }

    private static byte[] convertInputStreamToByteArray(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while(result !=-1) {
            byte b = (byte)result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toByteArray();
    }

    private static Bitmap loadBackgroundBitmap(String imageURL) {

        InputStream input = null;
        try {
            URL url = new URL(imageURL);
            HttpsURLConnection connection = (HttpsURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();

            input = connection.getInputStream();
        } catch (Exception e) {

        }

        /*// 폰의 화면 사이즈를 구한다.
        Display display = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        // 읽어들일 이미지의 사이즈를 구한다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;*/

        byte [] content = null;
        try {
            content = convertInputStreamToByteArray(input);
        } catch (Exception e) {

        }
        /*BitmapFactory.decodeByteArray(content, 0, content.length, options);

        BitmapFactory.decodeStream(input, null, options);

        // 화면 사이즈에 가장 근접하는 이미지의 리스케일 사이즈를 구한다.
        // 리스케일의 사이즈는 짝수로 지정한다. (이미지 손실을 최소화하기 위함.)
        float widthScale = (float)options.outWidth / (float)displayWidth;
        float heightScale = (float)options.outHeight / (float)displayHeight;
        float scale = widthScale > heightScale ? widthScale : heightScale;

        if(scale >= 8) {
            options.inSampleSize = 8;
        } else if(scale >= 6) {
            options.inSampleSize = 6;
        } else if(scale >= 4) {
            options.inSampleSize = 4;
        } else if(scale >= 2) {
            options.inSampleSize = 2;
        } else {
            options.inSampleSize = 1;
        }
        options.inJustDecodeBounds = false;*/

        return BitmapFactory.decodeByteArray(content, 0, content.length);
    }
}
