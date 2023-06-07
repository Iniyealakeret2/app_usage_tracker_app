package com.hour.hour.client

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.hour.hour.helper.ClientHelper
import com.hour.hour.helper.Logger
import com.hour.hour.helper.UsageStatsHelper
import com.hour.hour.model.UsageDigest
import org.json.JSONArray
import org.json.JSONObject
import java.util.TimerTask
import java.util.concurrent.locks.ReentrantLock

class UpdateServerTask(val context: Context) : TimerTask() {
    private var mPref: SharedPreferences? = null

    companion object {
        val updateLock = ReentrantLock()
    }

    override fun run() {
        val pref = context.getSharedPreferences("MyService", Context.MODE_PRIVATE)
        val prefEdit = context.getSharedPreferences("MyService", Context.MODE_PRIVATE).edit()
        val allDays = context.getSharedPreferences("UsageDigest", Context.MODE_PRIVATE).all.keys
        val updatedDays = pref.getStringSet("updated", setOf<String>())
        val oldToBeUpdate = pref.getStringSet("toBeUpdate", setOf<String>())
        val toBeUpdate = allDays.filter { !updatedDays!!.contains(it) }

        mPref = pref

        if (oldToBeUpdate != toBeUpdate) {
            updateLock.lock()
            prefEdit.putStringSet("toBeUpdate", toBeUpdate.toSet())
            prefEdit.apply()
            updateLock.unlock()
        }

        if (toBeUpdate.isEmpty() || !ClientHelper.checkConnectivity()) return

        for (day in toBeUpdate) {
            val digest = UsageDigest.load(context, day)
            val summary = UsageStatsHelper.queryUsage(day)
            val jsonArray = JSONArray()
            for (s in summary) {
                jsonArray.put(s.toJson())
            }
            val json = JSONObject()
                    .put("digest", digest.toJson())
                    .put("summary", jsonArray)

            if (digest.totalTime == 0L) {
                recordUpdate(day, pref, prefEdit)
            } else {
                Logger.d("updateServerTask", "update $day start")

                ClientHelper.send(context, "summary", json, object : ClientHelper.RequestCallback {
                    override fun onSuccess() {
                        Logger.d("updateServerTask", "update $day success")
                        recordUpdate(day, pref, prefEdit)
                    }

                    override fun onFail(code: ClientHelper.FailCode) {
                        Logger.d("updateServerTask", "update $day fail")
                    }
                })
            }
        }
    }

    @SuppressLint("MutatingSharedPrefs")
    private fun recordUpdate(day: String, pref: SharedPreferences, prefEdit: SharedPreferences.Editor) {
        updateLock.lock()

        val updatedDays = pref.getStringSet("updated", setOf<String>())?.toMutableSet()
        val toBeUpdate = pref.getStringSet("toBeUpdate", setOf<String>())

        updatedDays?.add(day)
        toBeUpdate?.remove(day)

        prefEdit.putStringSet("updated", updatedDays)
        prefEdit.putStringSet("toBeUpdate", toBeUpdate)
        prefEdit.commit()

        updateLock.unlock()
    }
}
