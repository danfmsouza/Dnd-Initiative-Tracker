package com.keiferstone.dndinitiativetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.keiferstone.dndinitiativetracker.MainActivity.MODE_SIMPLE;

class Preferences {
    private static final String PREF_MODE = "modePref";

    static void setMode(Context context, int mode) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(PREF_MODE, mode).apply();
    }

    static int getMode(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(PREF_MODE, MODE_SIMPLE);
    }
}
