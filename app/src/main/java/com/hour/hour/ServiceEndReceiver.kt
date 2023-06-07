package com.hour.hour

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hour.hour.helper.Logger

class ServiceEndReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Logger.d("ServiceEndReceiver", "onReceive")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startService(Intent(context, MyService::class.java))
            // TODO: Change this
        } else {
            context.startService(Intent(context, MyService::class.java))
        }
    }
}
