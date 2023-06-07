package com.hour.hour

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hour.hour.helper.Logger

class BootDeviceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.d("BootDeviceReceiver", "ACTION_BOOT_COMPLETED")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val serviceComponent = ComponentName(context, MyService::class.java)
                val builder = JobInfo.Builder(0, serviceComponent)
                builder.setOverrideDeadline(5000)
                context.getSystemService(JobScheduler::class.java).schedule(builder.build())
            } else {
                context.startService(Intent(context, MyService::class.java))
            }
        }
    }
}
