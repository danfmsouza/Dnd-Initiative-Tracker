package com.keiferstone.dndinitiativetracker.util;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtils {
    public static float dpToPx(float dp) {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        return dp * ((float) dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float pxToDp(float px) {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        return px / ((float) dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
