package com.app.rewardsbattle.utils;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class URLDrawable extends BitmapDrawable {
    protected Drawable drawable;

    @Override
    public void draw(Canvas canvas) {
        // override the draw to facilitate refresh function later
        try {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
