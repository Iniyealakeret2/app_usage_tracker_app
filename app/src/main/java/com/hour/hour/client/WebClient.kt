package com.hour.hour.client

import android.os.Handler
import java.io.IOException
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.MediaType
import org.json.JSONException
import com.hour.hour.helper.Logger
import com.hour.hour.iface.SerializableToJson
import okhttp3.MediaType.Companion.toMediaTypeOrNull

@Suppress("unused", "PrivatePropertyName", "UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")
class WebClient {
    private val TAG = "WebClient"

    //region Singleton
    companion object {
        private val sClient = WebClient()
        fun client(): WebClient {
            return sClient
        }
    }
    //endregion

    //region Client State
    private val mHttpClient = OkHttpClient()
    private var mAuthToken = ""
    //endregion

    //region Callback Status
    enum class Result {
        Success, ServerUnreachable, PayloadError
    }
    //endregion

    //region Mock Up Facility
    private val MOCK_CALLBACK_DELAY = 3000L
    private val mMockHandler = Handler()
    //endregion

    //region ApiBuilder
    class ApiBuilder internal constructor(url: String) {
        private enum class RequestType { GET, POST, PUT, DELETE }
        private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        private val mUrl = url
        private var mType = RequestType.GET
        private var mBody: RequestBody? = null
        private var mToken: String? = null
        fun post(body: RequestBody): ApiBuilder {
            mType = RequestType.POST
            mBody = body
            return this
        }
        fun put(body: RequestBody): ApiBuilder {
            mType = RequestType.PUT
            mBody = body
            return this
        }
        fun delete(body: RequestBody?): ApiBuilder {
            mType = RequestType.DELETE
            mBody = body
            return this
        }
        fun post(data: SerializableToJson): ApiBuilder {
            try {
                post(RequestBody.create(JSON, data.toJson().toString(0)))
            } catch (e: JSONException) {
                Logger.e("WebClient", "Json Exception on input: ${e.message}")
            }
            return this
        }
        fun put(data: SerializableToJson): ApiBuilder {
            try {
                post(RequestBody.create(JSON, data.toJson().toString(0)))
            } catch (e: JSONException) {
                Logger.e("WebClient", "Json Exception on input: ${e.message}")
            }
            return this
        }
        fun delete(data: SerializableToJson): ApiBuilder {
            try {
                delete(RequestBody.create(JSON, data.toJson().toString(0)))
            } catch (e: JSONException) {
                Logger.e("WebClient", "Json Exception on input: ${e.message}")
            }
            return this
        }
        fun token(token: String): ApiBuilder {
            mToken = token
            return this
        }
        fun execute(http: OkHttpClient, cb: ((Result, Response?, String?) -> Unit)?) {
            val req = Request.Builder().url(mUrl)
            when (mType) {
                RequestType.GET -> { }
                RequestType.POST -> { mBody?.let { req.post(it) } }
                RequestType.PUT -> { mBody?.let { req.put(it) } }
                RequestType.DELETE -> { req.delete(mBody) }
            }
            // Insert token if needed
            http.newCall(req.build()).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cb?.invoke(Result.ServerUnreachable, null, null)
                }
                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    cb?.invoke(Result.Success, response, response.body?.string())
                }
            })
        }
    }
    //endregion
}
