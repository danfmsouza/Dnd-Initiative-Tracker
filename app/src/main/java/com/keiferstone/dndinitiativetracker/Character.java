package com.keiferstone.dndinitiativetracker;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

class Character implements Parcelable {
    private String id;
    private String name;
    private int modifier;
    private int d20;
    private boolean marked;

    Character(String name, int modifier, int d20) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.modifier = modifier;
        this.d20 = d20;
        this.marked = false;
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

    int getModifier() {
        return modifier;
    }

    void setModifier(int modifier) {
        this.modifier = modifier;
    }

    int getD20() {
        return d20;
    }

    void setD20(int d20) {
        this.d20 = d20;
    }

    boolean isMarked() {
        return marked;
    }

    void setMarked(boolean marked) {
        this.marked = marked;
    }

    int getInitiative() {
        return getModifier() + getD20();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Character)) return false;

        Character character = (Character) o;

        return getId().equals(character.getId());

    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.modifier);
        dest.writeInt(this.marked ? 1 : 0);
    }

    private Character(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.modifier = in.readInt();
        this.marked = in.readInt() == 1;
    }

    public static final Parcelable.Creator<Character> CREATOR = new Parcelable.Creator<Character>() {
        @Override
        public Character createFromParcel(Parcel source) {
            return new Character(source);
        }

        @Override
        public Character[] newArray(int size) {
            return new Character[size];
        }
    };
}
