package com.keiferstone.dndinitiativetracker.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.keiferstone.dndinitiativetracker.data.model.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PartyStorage {
    private static final String PARTY_STORAGE = "partyStorage";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    PartyStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PARTY_STORAGE, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    List<Party> loadAllParties() {
        List<Party> parties = new ArrayList<>();
        for (String id : sharedPreferences.getAll().keySet()) {
            parties.add(loadParty(id));
        }
        return parties;
    }

    Party loadParty(String id) {
        return gson.fromJson(sharedPreferences.getString(id, null), Party.class);
    }

    void saveParty(@NonNull Party party) {
        sharedPreferences.edit().putString(party.getId(), gson.toJson(party)).apply();
    }

    void deleteParty(String id) {
        sharedPreferences.edit().remove(id).apply();
    }
}
