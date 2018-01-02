package com.hello.TrevelMeetUp.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by lji5317 on 20/12/2017.
 */

public class CommonFunction {
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
}
