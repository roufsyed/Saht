package com.rouf.saht.common.model

import android.os.Parcel
import android.os.Parcelable

enum class HeartRateMonitorSensitivity(val value: Int) : Parcelable {
    LOW(2),
    MEDIUM(3),
    HIGH(5);

    constructor(parcel: Parcel) : this(parcel.readInt()) {
        // No other initialization needed for now
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HeartRateMonitorSensitivity> {
        override fun createFromParcel(parcel: Parcel): HeartRateMonitorSensitivity {
            return HeartRateMonitorSensitivity.values().find { it.value == parcel.readInt() } ?: throw IllegalArgumentException("Unknown value")
        }

        override fun newArray(size: Int): Array<HeartRateMonitorSensitivity?> {
            return arrayOfNulls(size)
        }
    }
}