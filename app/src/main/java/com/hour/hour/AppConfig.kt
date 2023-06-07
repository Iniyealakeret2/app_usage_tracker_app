package com.hour.hour

import io.reactivex.android.BuildConfig

@Suppress("unused")
object AppConfig {
    const val SPLASH_DURATION = 300L // splash screen duration in ms, 0 to disable
    const val TIMER_CHECK_PERIOD = 2000L
    const val TIMER_UPDATE_SERVER_PERIOD = 120000L // 2 minutes
    const val MAIN_SCREEN_UPDATEVIEW_TIMER = 2500L
    val SERVER_ADDRESS: String = when (BuildConfig.FLAVOR) {
        "staging" -> {
            "http://192.168.40.1:8080/"
        }
        "prod" -> {
            "http://35.241.72.8:8080/"
        }
        else -> {
            "http://35.241.72.8:8080/"
        }
    }
    const val SERVER_PORT = 8080

}
