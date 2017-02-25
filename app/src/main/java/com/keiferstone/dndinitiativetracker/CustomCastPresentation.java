package com.keiferstone.caster;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.cast.CastPresentation;

/**
 * The presentation to show on the secondary display.
 * <p>
 * Note that this display may have different metrics from the display on which
 * the main activity is showing so we must be careful to use the presentation's
 * own {@link Context} whenever we load resources.
 * </p>
 */
final class CustomCastPresentation extends CastPresentation {


    CustomCastPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //noinspection ConstantConditions
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // Inflate the layout.
        setContentView(R.layout.layout_cast_presentation);
    }
}