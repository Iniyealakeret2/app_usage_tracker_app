package com.hour.hour.helper

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

object PermissionHelper {
    fun getAppUsagePermission(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun hasAppUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, context.applicationInfo.uid, context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
