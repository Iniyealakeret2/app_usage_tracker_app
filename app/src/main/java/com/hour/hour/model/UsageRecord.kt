package com.hour.hour.model

import com.hour.hour.helper.CalendarHelper
import com.hour.hour.iface.SerializableToJson
import org.json.JSONObject

data class UsageRecord(
        val packageName: String = "",
        val starTime: Long = 0,
        val duration: Long = 0
) : SerializableToJson {
    override fun toJson(): JSONObject {
        return JSONObject().put("packageName", packageName)
                .put("starTime", starTime)
                .put("duration", duration)
    }

    fun toArray(): Array<String> {
        return arrayOf(packageName, starTime.toString(), duration.toString())
    }

    override fun toString(): String {
        return "packageName: $packageName  startTime: ${CalendarHelper.getDate(starTime)}  duration: ${duration / 1000}s"
    }
}
