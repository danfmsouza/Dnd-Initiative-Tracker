package com.keiferstone.dndinitiativetracker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

internal class CharacterStorage(context: Context) {
    private val sharedPreferences: SharedPreferences
            = context.getSharedPreferences("characterStorage", Context.MODE_PRIVATE)

    fun loadAllCharacters(): List<Character> {
        return sharedPreferences.all.values
                .filterIsInstance<String>()
                .map { Gson().fromJson(it, Character::class.java) }
    }

    fun saveCharacter(character: Character) {
        sharedPreferences.edit().putString(character.id, Gson().toJson(character)).apply()
    }

    fun deleteCharacter(character: Character) {
        sharedPreferences.edit().remove(character.id).apply()
    }
}