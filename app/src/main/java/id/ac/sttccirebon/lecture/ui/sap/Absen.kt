package id.ac.sttccirebon.lecture.ui.sap

import android.os.Parcel
import android.os.Parcelable

data class Absen(
    val id: String?,
    val nama: String?,
    val status: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nama)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Absen> {
        override fun createFromParcel(parcel: Parcel): Absen {
            return Absen(parcel)
        }

        override fun newArray(size: Int): Array<Absen?> {
            return arrayOfNulls(size)
        }
    }
}