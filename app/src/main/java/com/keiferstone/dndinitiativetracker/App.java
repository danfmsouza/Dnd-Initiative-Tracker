package com.keiferstone.dndinitiativetracker;


import android.app.Application;

import com.keiferstone.dndinitiativetracker.data.PartyManager;
import com.keiferstone.dndinitiativetracker.data.Preferences;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Droidiga.otf")
                .build()
        );

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        PartyManager.init(this);
    }
}
