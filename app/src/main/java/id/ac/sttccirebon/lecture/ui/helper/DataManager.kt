package id.ac.sttccirebon.lecture.ui.helper

import android.content.Context
import android.content.SharedPreferences

class DataManager(private val baseContext: Context) {

    private val SHARED_DATA = "loyalti_shared_preferences"
    var sharePref : SharedPreferences = baseContext.getSharedPreferences(SHARED_DATA, Context.MODE_PRIVATE)

    private fun editor(): SharedPreferences.Editor {
        return sharePref.edit()
    }

    fun putString(name: String, value: String) {
        editor().putString(name, value).apply()
    }


    fun getString(name: String): String? {
        return sharePref.getString(name, null)
    }

    fun getStringSet(name: String): Set<String>? {
        return sharePref.getStringSet(name, null)
    }

    fun getInt(name: String): Int? {
        return sharePref.getInt(name, 0)
    }

    fun remove(name: String) {
        editor().remove(name).apply()
    }

    fun clear() {
        editor().clear().apply()
    }

    fun instance(): DataManager {
        return DataManager(baseContext)
    }

    fun putUserData(token: String) {
        instance().putString("token", token )
    }

    fun putProfile(fotoProfil: String) {
        instance().putString("photo_profile", fotoProfil)
    }

    fun putUsername(User : String){
        instance().putString("user", User)
    }

    fun putStringPdf(Pdf : String){
        instance().putString("pdf", Pdf)
    }

    fun putId(Id : String){
        instance().putString("id", Id)
    }

}