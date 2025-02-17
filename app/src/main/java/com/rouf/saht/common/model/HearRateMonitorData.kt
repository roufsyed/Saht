package com.rouf.saht.common.model

import android.os.Parcel
import android.os.Parcelable
import com.github.mikephil.charting.data.Entry

data class HeartRateMonitorData(
    var duration: Int = 0,
    var unit: String = "",
    var sensitivityLevel: HeartRateMonitorSensitivity = HeartRateMonitorSensitivity.LOW,
    var bpm: Int = 0,
    var bpmGraphEntries: MutableList<Entry> = mutableListOf<Entry>(),
    var timeStamp: Long = System.currentTimeMillis(),
    var activityPerformed: String = "",
    var isResting: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        HeartRateMonitorSensitivity.valueOf(parcel.readString() ?: "LOW"),
        parcel.readInt(),
        parcel.createTypedArrayList(Entry.CREATOR) ?: mutableListOf<Entry>(),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(duration)
        parcel.writeString(unit)
        parcel.writeString(sensitivityLevel.name)
        parcel.writeInt(bpm)
        parcel.writeTypedList(bpmGraphEntries)
        parcel.writeLong(timeStamp)
        parcel.writeString(activityPerformed)
        parcel.writeByte(if (isResting) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HeartRateMonitorData> {
        override fun createFromParcel(parcel: Parcel): HeartRateMonitorData {
            return HeartRateMonitorData(parcel)
        }

        override fun newArray(size: Int): Array<HeartRateMonitorData?> {
            return arrayOfNulls(size)
        }
    }
}