package com.curiousdaya.bmsanalyser.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.curiousdaya.bmsanalyser.R

class Prefs
private constructor(val context: Context) {
    val TAG = Prefs::class.java.simpleName
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: Prefs? = null
        fun getInstance(ctx: Context): Prefs {
            if (instance == null) {
                instance = Prefs(ctx)
            }
            return instance!!
        }
    }

    var bluetoothDeviceAddress: String
        get() = sharedPreferences["bluetoothDeviceAddress"]?:""
        set(value) = sharedPreferences.set("bluetoothDeviceAddress", value)

       var bluetoothDeviceName: String
        get() = sharedPreferences["bluetoothDeviceName"]?:""
        set(value) = sharedPreferences.set("bluetoothDeviceName", value)




    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is Int -> edit { it.putInt(key, value) }
            is String? -> edit { it.putString(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> Log.e(TAG, "Setting shared pref failed for key: $key and value: $value ")
        }
    }
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }
    inline operator fun <reified T : Any> SharedPreferences.get(
        key: String,
        defaultValue: T? = null
    ): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    fun deletePreferences() {
        editor.clear()
        editor.apply()
    }
}