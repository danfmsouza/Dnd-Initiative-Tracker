package com.keiferstone.dndinitiativetracker;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

class Character implements Parcelable {
    private String id;
    private String name;
    private int initiative;
    private boolean marked;

    Character(String name, int initiative) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.initiative = initiative;
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

    int getInitiative() {
        return initiative;
    }

    public void setInitiative(int initiative) {
        this.initiative = initiative;
    }

    public boolean isMarked() {
        return marked;
    }

    void setMarked(boolean marked) {
        this.marked = marked;
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
        dest.writeString(this.id.toString());
        dest.writeString(this.name);
        dest.writeInt(this.initiative);
        dest.writeInt(this.marked ? 1 : 0);
    }

    protected Character(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.initiative = in.readInt();
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
