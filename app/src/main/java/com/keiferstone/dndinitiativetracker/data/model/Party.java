package com.keiferstone.dndinitiativetracker.data.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party implements Parcelable {
    private final String id;
    private String name;
    private List<Character> characters = new ArrayList<>();

    public Party() {
        this.id = UUID.randomUUID().toString();
    }

    public Party(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(@NonNull List<Character> characters) {
        this.characters = characters;
    }

    public boolean hasCharacters() {
        return !characters.isEmpty();
    }

    public void addOrUpdateCharacter(Character character) {
        if (characters.contains(character)) {
            characters.remove(character);
        }
        characters.add(character);
    }

    public void deleteCharacter(Character character) {
        characters.remove(character);
    }

    private Party(Parcel in) {
        id = in.readString();
        name = in.readString();
        characters = in.createTypedArrayList(Character.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeTypedList(characters);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Party> CREATOR = new Creator<Party>() {
        @Override
        public Party createFromParcel(Parcel in) {
            return new Party(in);
        }

        @Override
        public Party[] newArray(int size) {
            return new Party[size];
        }
    };
}
