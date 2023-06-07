package com.hour.hour.helper

import android.content.Context

object ScreenUnlockHelper {
    private const val TAG = "ScreenUnlockHelper"

    fun countScreenUnlock(context: Context) {
        val pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val day = CalendarHelper.getDateCondensed(System.currentTimeMillis())
        val n = pref.getInt(day, 0) + 1
        val prefEdit = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit()
        prefEdit.putInt(day, n)
        prefEdit.apply()
    }

    fun getUnlockCount(context: Context, day: String): Int {
        val pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        return pref.getInt(day, 0)
    }

    fun getTodayUnlockCount(context: Context): Int {
        val day = CalendarHelper.getDateCondensed(System.currentTimeMillis())
        return getUnlockCount(context, day)
    }

    fun getYesterdayUnlockCount(context: Context): Int {
        val day = CalendarHelper.getDateCondensed(System.currentTimeMillis() - UsageStatsHelper.HOUR_24)
        return getUnlockCount(context, day)
    }

    fun getNDayUnlockCount(context: Context, n: Int): Int {
        val currentTime = System.currentTimeMillis()
        val startTime = System.currentTimeMillis() - (n - 1) * UsageStatsHelper.HOUR_24
        val pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        var sum = 0
        for (i in startTime..currentTime step UsageStatsHelper.HOUR_24) {
            val day = CalendarHelper.getDateCondensed(i)
            sum += pref.getInt(day, 0)
        }
        return sum
    }
}
