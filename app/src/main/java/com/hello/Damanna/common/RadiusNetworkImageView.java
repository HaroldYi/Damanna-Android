package com.hello.Damanna.common;

/**
 * Created by lji5317 on 10/01/2018.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public class RadiusNetworkImageView extends NetworkImageView {

    // 라운드처리 강도 값을 크게하면 라운드 범위가 커짐
    private float radius;

    public RadiusNetworkImageView(Context context) {
        super(context);
    }

    public RadiusNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadiusNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}