package com.keiferstone.dndinitiativetracker

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Character(
        var name: String,
        var modifier: Int) : Parcelable {
    var id: String = UUID.randomUUID().toString()
    var d20: Int = 1
    var isMarked: Boolean = false
    val initiative: Int get() = modifier + d20

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Character) return false
        val character = other as Character?
        return id == character?.id
    }

    override fun hashCode(): Int { return id.hashCode() }

    override fun describeContents(): Int { return 0 }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.name)
        dest.writeInt(this.modifier)
        dest.writeString(this.id)
        dest.writeInt(this.d20)
        dest.writeInt(if (this.isMarked) 1 else 0)
    }

    private constructor(parcelIn: Parcel) : this(
            parcelIn.readString(), parcelIn.readInt()) {
        id = parcelIn.readString()
        d20 = parcelIn.readInt()
        isMarked = parcelIn.readInt() != 0
    }

    companion object {
        @Suppress("unused")
        val CREATOR = createParcel { Character(it) }
    }
}
