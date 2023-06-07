package com.hour.hour.helper

import android.util.Log
//import com.hour.hour.BuildConfig
import io.reactivex.android.BuildConfig

@Suppress("unused")
object Logger {
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.d(tag, msg)
    }
    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }
}
