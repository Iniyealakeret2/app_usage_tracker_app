package com.hour.hour.helper

import android.content.Context
import com.hour.hour.model.NotTrackingRecord
import com.hour.hour.model.UsageSummary

object NotTrackingListHelper {
    private fun getNotTrackingRecords(context: Context): List<NotTrackingRecord> {
        val pref = context.getSharedPreferences("NotTrackingListHelper", Context.MODE_PRIVATE)
        val list = pref.all
        // key = package name
        // value = app name
        return list.map { NotTrackingRecord(it.value.toString(), it.key, true) }
    }

    fun loadNotTrackingList(context: Context): List<String> {
        val pref = context.getSharedPreferences("NotTrackingListHelper", Context.MODE_PRIVATE)
        val list = pref.all
        return list.keys.toList()
    }

    fun addRecords(context: Context, l: List<NotTrackingRecord>) {
        Logger.d("NotTrackingListHelper", "add ${l.map { it.packageName }}")
        val prefEdit = context.getSharedPreferences("NotTrackingListHelper", Context.MODE_PRIVATE).edit()
        for (r in l) {
            prefEdit.putString(r.packageName, r.appName)
        }
        prefEdit.apply()
    }

    fun removeRecords(context: Context, l: List<NotTrackingRecord>) {
        Logger.d("NotTrackingListHelper", "remove ${l.map { it.packageName }}")
        val prefEdit = context.getSharedPreferences("NotTrackingListHelper", Context.MODE_PRIVATE).edit()
        for (r in l) {
            prefEdit.remove(r.packageName)
        }
        prefEdit.apply()
    }

    fun getList(context: Context): List<NotTrackingRecord> {
        val time = System.currentTimeMillis()
        val summary = UsageSummary.getSummary(context,
                ((time - 2 * UsageStatsHelper.HOUR_24)..time step UsageStatsHelper.HOUR_24).map { CalendarHelper.getDateCondensed(it) }
        )
        val trackingList = getNotTrackingRecords(context)
        val hashMap = hashMapOf<String, NotTrackingRecord>()
        hashMap.putAll(summary.map { Pair(it.packageName, NotTrackingRecord(it.appName, it.packageName, false)) })
        hashMap.putAll(trackingList.map { Pair(it.packageName, it) })
        return hashMap.values.sortedBy { it.appName }
    }
}
