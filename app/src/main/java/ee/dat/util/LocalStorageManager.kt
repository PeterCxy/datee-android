package ee.dat.util

import android.content.Context
import android.content.SharedPreferences
import ee.dat.DateeApplication

object LocalStorageManager {
    private val prefs: SharedPreferences by lazy {
        DateeApplication.context.getSharedPreferences("local_storage", Context.MODE_PRIVATE)
    }

    fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getLong(key: String): Long {
        return prefs.getLong(key, Long.MIN_VALUE);
    }

    fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }
}