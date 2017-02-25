package com.keiferstone.dndinitiativetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class CharacterStorage {
    private static final String CHARACTER_STORAGE = "characterStorage";

    private SharedPreferences sharedPreferences;

    CharacterStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(CHARACTER_STORAGE, Context.MODE_PRIVATE);
    }

    @NonNull
    List<Character> loadAllCharacters() {
        List<Character> characters = new ArrayList<>();
        Map<String, ?> characterPrefs = sharedPreferences.getAll();
        for (Object characterJson : characterPrefs.values()) {
            if (characterJson instanceof String) {
                characters.add(new Gson().fromJson((String) characterJson, Character.class));
            }
        }
        return characters;
    }

    void saveCharacter(@NonNull Character character) {
        sharedPreferences.edit().putString(character.getId(), new Gson().toJson(character)).apply();
    }

    void deleteCharacter(@NonNull Character character) {
        sharedPreferences.edit().remove(character.getId()).apply();
    }
}
