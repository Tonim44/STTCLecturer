package id.ac.sttccirebon.lecture.ui.home

import android.os.Parcel
import android.os.Parcelable

data class Mengajar(
    val mata_kuliah: String?,
    val kelas: String?,
    val ruangan: String?,
    val tanggal: String?,
    val jam: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mata_kuliah)
        parcel.writeString(kelas)
        parcel.writeString(ruangan)
        parcel.writeString(tanggal)
        parcel.writeString(jam)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Mengajar> {
        override fun createFromParcel(parcel: Parcel): Mengajar {
            return Mengajar(parcel)
        }

        override fun newArray(size: Int): Array<Mengajar?> {
            return arrayOfNulls(size)
        }
    }
}
