package com.hour.hour.model

import android.content.Context
import com.hour.hour.helper.CalendarHelper
import com.hour.hour.helper.CsvHelper
import com.hour.hour.helper.Logger
import com.hour.hour.helper.NotTrackingListHelper
import com.hour.hour.helper.ScreenUnlockHelper
import com.hour.hour.iface.SerializableToJson
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@Suppress("LiftReturnOrAssignment")
data class UsageDigest(
        val date: String,
        val summaries: List<UsageSummary>,
        val totalTime: Long,
        val unlockCount: Int
) : SerializableToJson {
    override fun toJson(): JSONObject {
        val summaryList = JSONArray()
        for (i in summaries) {
            summaryList.put(i.toJson())
        }
        return JSONObject()
                .put("date", date)
                .put("totalTime", totalTime)
                .put("unlockCount", unlockCount)
                .put("summaryList", summaryList)
    }

    companion object {
        const val TAG = "UsageDigest"

        fun fromJson(json: JSONObject): UsageDigest {
            val summary = arrayListOf<UsageSummary>()
            val summaryJson = json.getJSONArray("summaryList")
            for (i in 0 until summaryJson.length()) {
                val item = summaryJson.getJSONObject(i)
                try {
                    summary.add(UsageSummary.fromJson(item))
                } catch (e: Exception) {
                    Logger.d(TAG, "fromJson failed: i = $i")
                }
            }
            return UsageDigest(json.getString("date"), summary, json.getLong("totalTime"), json.getInt("unlockCount"))
        }

        fun make(context: Context, day: String): UsageDigest {
            val file = File(context.filesDir.path + "/" + day)
            val records = CsvHelper.read(file)
            Logger.d(TAG, "make $day - ${records.size}")
            if (records.isEmpty()) {
                return UsageDigest(day, listOf(), 0, 0)
            }
            val summary = UsageSummary.makeSummary(context, records)
            return UsageDigest(day, summary, summary.map { it.useTimeTotal }.sum(), ScreenUnlockHelper.getUnlockCount(context, day))
        }

        fun loadFiltered(context: Context, days: List<String>): List<UsageDigest> {
            val l = days.map { load(context, it) }
            val list = NotTrackingListHelper.loadNotTrackingList(context)
            return l.map {
                it.copy(summaries = it.summaries.filter { !list.contains(it.packageName) })
            }
        }

        fun loadFiltered(context: Context, day: String): UsageDigest {
            val list = NotTrackingListHelper.loadNotTrackingList(context)
            val digest = load(context, day)
            val s = digest.summaries.filter { !list.contains(it.packageName) }
            return digest.copy(totalTime = s.map { it.useTimeTotal }.sum(), summaries = s)
        }

        fun load(context: Context, days: List<String>): List<UsageDigest> {
            return days.map { load(context, it) }
        }

        fun load(context: Context, day: String): UsageDigest {
            if (day == CalendarHelper.getDateCondensed(System.currentTimeMillis())) {
                return make(context, day)
            }

            try {
                val pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                val str = pref.getString(day, null) ?: throw Exception("record $day not found")
                return UsageDigest.fromJson(JSONObject(str))
            } catch (e: Exception) {
                val digest = make(context, day)
                save(context, digest)
                return digest
            }
        }

        private fun save(context: Context, digest: UsageDigest) {
            val prefEdit = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit()
            prefEdit.putString(digest.date, digest.toJson().toString())
            prefEdit.apply()
        }
    }
}
