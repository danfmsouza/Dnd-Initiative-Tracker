package com.keiferstone.dndinitiativetracker.data;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.keiferstone.dndinitiativetracker.data.model.Party;

import java.util.List;

public class PartyManager {
    private static PartyManager instance;

    private Preferences preferences;
    private PartyStorage partyStorage;

    private PartyManager(Context appContext) {
        preferences = new Preferences(appContext);
        partyStorage = new PartyStorage(appContext);
    }

    /**
     * Call in {@link Application#onCreate()}.
     *
     * @param context Application context.
     */
    public static void init(@NonNull Context context) {
        if (instance == null) {
            instance = new PartyManager(context.getApplicationContext());
        }
    }

    private static void assertInitialized() {
        if (instance == null) {
            throw new IllegalStateException("PartyManager not initialized.");
        }
    }

    public static List<Party> getAllParties() {
        assertInitialized();
        return instance.partyStorage.loadAllParties();
    }

    @NonNull
    public static Party getActiveParty() {
        assertInitialized();
        // Load the active party using the saved id
        Party activeParty = getParty(instance.preferences.getActivePartyId());
        if (activeParty == null) {
            // If nothing was loaded, set a default active party
            List<Party> allParties = getAllParties();
            if (!allParties.isEmpty()) {
                // If there are other parties, use one of those
                activeParty = allParties.get(0);
            } else {
                // Otherwise created a new party
                activeParty = new Party();
                activeParty.setName("Party of n00bs");
            }
        }
        return activeParty;
    }

    @Nullable
    public static Party getParty(String id) {
        assertInitialized();
        return instance.partyStorage.loadParty(id);
    }

    public static void addOrUpdateParty(Party party) {
        assertInitialized();
        instance.partyStorage.saveParty(party);
    }

    public static void deleteParty(@NonNull Party party) {
        assertInitialized();
        instance.partyStorage.deleteParty(party.getId());
    }
}
