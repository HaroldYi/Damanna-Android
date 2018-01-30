package com.hello.holaApp.common;

/**
 * Created by lji5317 on 10/01/2018.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

public class RadiusImageButton extends android.support.v7.widget.AppCompatImageButton {

    // 라운드처리 강도 값을 크게하면 라운드 범위가 커짐
    private float radius;

    public RadiusImageButton(Context context) {
        super(context);
    }

    public RadiusImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadiusImageButton(Context context, AttributeSet attrs, int defStyle) {
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