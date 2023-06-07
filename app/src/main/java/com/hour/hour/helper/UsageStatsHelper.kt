package com.hour.hour.helper

import android.app.usage.UsageEvents
import android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND
import android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND
import android.app.usage.UsageStatsManager
import android.content.Context
import com.hour.hour.MainApplication.Companion.store
import com.hour.hour.model.UsageDigest
import com.hour.hour.model.UsageRecord
import org.json.JSONObject
import java.io.File
import java.lang.ref.WeakReference

object UsageStatsHelper {
    data class Result(val records: List<UsageRecord> = listOf(), val lastEndTime: Long = 0, val foregroundPackageName: String? = null)

    private var context: WeakReference<Context>? = null
    fun setup(c: Context) {
        context = WeakReference(c)
    }

    const val HOUR_24 = 1000 * 60 * 60 * 24L
    private var mLastForegroundEvent: String? = null
    private var mLastTimeStamp: Long = 0

    fun getLatestEvent(usageStatsManager: UsageStatsManager, startTime: Long, endTime: Long): Result {
        val records = arrayListOf<UsageRecord>()
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastEndTime: Long = 0

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val packageName = event.packageName
            val timeStamp = event.timeStamp
            val eventType = event.eventType

            if (eventType == MOVE_TO_BACKGROUND && mLastForegroundEvent == packageName && timeStamp > mLastTimeStamp) {
                mLastForegroundEvent?.let {
                    recordUsage(it, mLastTimeStamp, timeStamp - mLastTimeStamp)
                    records.add(UsageRecord(packageName, mLastTimeStamp, timeStamp - mLastTimeStamp))
                }
                mLastForegroundEvent = null
                lastEndTime = timeStamp
            } else if (eventType == MOVE_TO_FOREGROUND) {
                mLastForegroundEvent = packageName
                mLastTimeStamp = timeStamp
            }
        }
        return Result(records, lastEndTime, mLastForegroundEvent)
    }

    private fun recordUsage(packageName: String, startTime: Long, duration: Long) {
        val context = context?.get() ?: return
        Logger.d("recordUsage", "$packageName   from ${CalendarHelper.getDate(startTime)}  -  ${duration / 1000}s")
        val filename = CalendarHelper.getDateCondensed(startTime)
        val path = File(context.filesDir.path + "/" + filename)
        CsvHelper.write(path, listOf(UsageRecord(packageName, startTime, duration)))
    }

    fun queryIntervalUsage(duration: Long): ArrayList<UsageRecord> {
        val context = context?.get() ?: return arrayListOf()
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - duration
        val records = arrayListOf<UsageRecord>()

        for (i in startTime..currentTime step HOUR_24) {
            val filename = CalendarHelper.getDateCondensed(i)
            val file = File(context.filesDir.path + "/" + filename)
            records.addAll(CsvHelper.read(file))
        }
        return records
    }

    private fun filter(records: List<UsageRecord>): List<UsageRecord> {
        val list = store().view.state.notTrackingList
        return records.filter { !list.contains(it.packageName) }
    }

    fun getTodayUsageTime(filter: Boolean = false): Long {
        val records = if (filter) filter(queryTodayUsage()) else queryTodayUsage()
        return records.map { it.duration }.sum()
    }

    fun queryUsage(day: String): List<UsageRecord> {
        val context = context?.get() ?: return listOf()
        val file = File(context.filesDir.path + "/" + day)
        return CsvHelper.read(file)
    }

    fun queryTodayUsage(): List<UsageRecord> {
        return queryUsage(CalendarHelper.getDateCondensed(System.currentTimeMillis()))
    }

    fun query24hUsage(): List<UsageRecord> {
        return queryIntervalUsage(HOUR_24).filter { it.starTime >= System.currentTimeMillis() - HOUR_24 }
    }

    fun getAverageUsageTime(filter: Boolean = false): Long {
        val context = context?.get() ?: return 0
        val ntList = store().view.state.notTrackingList
        val pref = context.getSharedPreferences(UsageDigest.TAG, Context.MODE_PRIVATE)
        val all = if (filter) pref.all.filter { !ntList.contains(it.key) } else pref.all
        var sum = 0L
        var count = 0
        for ((_, value) in all) {
            val t = UsageDigest.fromJson(JSONObject(value as? String)).totalTime
            if (t != 0L) {
                sum += t
                count += 1
            }
        }
        if (count == 0 || all.isEmpty()) {
            return 0L
        } else {
            return (sum / count)
        }
    }
}
