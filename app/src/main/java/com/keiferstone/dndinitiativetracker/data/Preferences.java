package com.keiferstone.dndinitiativetracker.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;
import static com.keiferstone.dndinitiativetracker.ui.activity.MainActivity.MODE_SIMPLE;

public class Preferences {
    private static final String PREFERENCES_FILE = "preferences";
    private static final String PREF_MODE = "modePref";
    private static final String PREF_ACTIVE_PARTY_ID = "activePartyIdPref";

    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
    }

    public void setMode(int mode) {
        sharedPreferences.edit().putInt(PREF_MODE, mode).apply();
    }

    public int getMode() {
        return sharedPreferences.getInt(PREF_MODE, MODE_SIMPLE);
    }

    public void setActivePartyId(String activePartyId) {
        sharedPreferences.edit().putString(PREF_ACTIVE_PARTY_ID, activePartyId).apply();
    }

    public String getActivePartyId() {
        return sharedPreferences.getString(PREF_ACTIVE_PARTY_ID, null);
    }
}
