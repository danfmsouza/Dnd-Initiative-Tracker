package com.keiferstone.caster;

import android.content.Intent;
import android.support.v4.BuildConfig;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Display;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;

public class PresentationService extends CastRemoteDisplayLocalService {

    public static final String STATUS_READY = BuildConfig.APPLICATION_ID + ".READY";

    private CustomCastPresentation mPresentation;

    @Override
    public void onCreatePresentation(Display display) {
        createPresentation(display);
    }

    @Override
    public void onDismissPresentation() {
        dismissPresentation();
    }

    private void createPresentation(Display display) {
        dismissPresentation();

        mPresentation = new CustomCastPresentation(this, display);
        mPresentation.show();

        notifyPresentationReady();
    }

    private void dismissPresentation() {
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    private void notifyPresentationReady() {
        Intent intent = new Intent(STATUS_READY);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}