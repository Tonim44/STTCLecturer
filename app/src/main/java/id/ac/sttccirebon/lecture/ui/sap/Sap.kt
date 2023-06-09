package id.ac.sttccirebon.lecture.ui.sap

import android.os.Parcel
import android.os.Parcelable

data class Sap(
    val id_detail_jadwal_kuliah: Int?,
    val mata_kuliah: String?,
    val tanggal: String?,
    val jam: String?,
    val program_studi: String?,
    val kelas: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id_detail_jadwal_kuliah)
        parcel.writeString(mata_kuliah)
        parcel.writeString(tanggal)
        parcel.writeString(jam)
        parcel.writeString(program_studi)
        parcel.writeString(kelas)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sap> {
        override fun createFromParcel(parcel: Parcel): Sap {
            return Sap(parcel)
        }

        override fun newArray(size: Int): Array<Sap?> {
            return arrayOfNulls(size)
        }
    }
}