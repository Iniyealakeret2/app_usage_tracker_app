package com.hour.hour.helper

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CalendarHelper {
    fun getDate(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(cal.time)
    }

    fun getDateCondensed(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("yyyyMMdd").format(cal.time)
    }

    fun getTimeConsensed(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("HH:mm").format(cal.time)
    }

    fun getDateLong(time: Long): String {
        return DateFormat.getDateInstance(DateFormat.LONG).format(time)
    }

    fun getMonthDay(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("dd/MM").format(cal.time)
    }

    fun toReadableDuration(t: Long): String {
        return when {
            t / 1000 < 60 -> "${t / 1000} sec"
            t / 60000 < 60 -> "${t / 60000} min"
            else -> "${t / 3600000}h ${(t % 3600000) / 60000}m"
        }
    }
}
