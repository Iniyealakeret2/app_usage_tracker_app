package com.hour.hour.model

import android.content.Context
import com.hour.hour.helper.NotTrackingListHelper
import com.hour.hour.helper.PackageHelper
import com.hour.hour.iface.SerializableToJson
import org.json.JSONObject

data class UsageSummary(
        val appName: String,
        val packageName: String,
        val useTimeTotal: Long
) : SerializableToJson {
    override fun toJson(): JSONObject {
        return JSONObject()
                .put("appName", appName)
                .put("packageName", packageName)
                .put("useTimeTotal", useTimeTotal)
    }

    companion object {
        fun makeSummary(context: Context, records: List<UsageRecord>): List<UsageSummary> {
            val dict = hashMapOf<String, Long>()
            for (r in records) {
                dict[r.packageName] = (dict[r.packageName] ?: 0) + r.duration
            }
            return dict.map {
                UsageSummary(PackageHelper.getAppName(context, it.key), it.key, it.value)
            }.sortedByDescending { it.useTimeTotal }
        }

        fun getSummary(context: Context, days: List<String>): List<UsageSummary> {
            val digests = UsageDigest.load(context, days)
            val totalTime = hashMapOf<String, Long>()
            val appName = hashMapOf<String, String>()
            for (d in digests) {
                for (r in d.summaries) {
                    totalTime[r.packageName] = (totalTime[r.packageName] ?: 0) + r.useTimeTotal
                    appName[r.packageName] = r.appName
                }
            }
            return totalTime.map {
                UsageSummary(appName[it.key] ?: it.key, it.key, it.value)
            }.sortedByDescending { it.useTimeTotal }
        }

        fun getFilteredSummary(context: Context, days: List<String>): List<UsageSummary> {
            return filter(context, getSummary(context, days))
        }

        fun filter(context: Context, l: List<UsageSummary>): List<UsageSummary> {
            val list = NotTrackingListHelper.loadNotTrackingList(context)
            return l.filter { !list.contains(it.packageName) }
        }

        fun fromJson(json: JSONObject): UsageSummary {
            return UsageSummary(json.getString("appName"), json.getString("packageName"), json.getLong("useTimeTotal"))
        }
    }
}
