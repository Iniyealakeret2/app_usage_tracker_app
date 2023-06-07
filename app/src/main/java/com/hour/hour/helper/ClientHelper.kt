package com.hour.hour.helper

import android.content.Context
import android.provider.Settings
import com.hour.hour.AppConfig
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException

object ClientHelper {
    enum class FailCode {
        CONNECTION_FAIL,
        IO_FAIL,
        INVALID_REQUEST
    }

    interface RequestCallback {
        fun onSuccess()
        fun onFail(code: FailCode)
    }
    fun checkConnectivity(): Boolean {
        val client = OkHttpClient()
        try {
            val request = Request.Builder().url(AppConfig.SERVER_ADDRESS).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            return body == "Hello!"
        } catch (e: Exception) {
            Logger.e("ClientHelper", "${e.message}")
            return false
        }
    }

    fun send(context: Context, method: String, data: JSONObject, cb: RequestCallback?) {
        Thread {
            try {
                Logger.d("ClientHelper", "start request")
                val json = "application/json; charset=utf-8".toMediaTypeOrNull() ?: return@Thread
                val message = JSONObject()
                        .put("method", method)
                        .put("deviceId", Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
                        .put("data", data)
                        .toString()
                val client = OkHttpClient()
                val body = RequestBody.create(json, message)
                val request = Request.Builder().url(AppConfig.SERVER_ADDRESS).post(body).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Logger.d("ClientHelper", "response: $responseBody")
                if (responseBody == "OK") {
                    cb?.onSuccess()
                } else {
                    throw ConnectException()
                }
            } catch (e: ConnectException) {
                Logger.e("ClientHelper", "${e.message}")
                cb?.onFail(FailCode.CONNECTION_FAIL)
            } catch (e: IOException) {
                Logger.e("ClientHelper", "${e.message}")
                cb?.onFail(FailCode.IO_FAIL)
            } catch (e: Exception) {
                Logger.e("ClientHelper", "${e.message}")
                cb?.onFail(FailCode.INVALID_REQUEST)
            }
        }.start()
    }
}
