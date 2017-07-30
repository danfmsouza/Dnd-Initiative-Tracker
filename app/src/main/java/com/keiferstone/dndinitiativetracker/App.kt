package com.keiferstone.dndinitiativetracker

import android.app.Application
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Droidiga.otf")
                .build()
        )
    }
}