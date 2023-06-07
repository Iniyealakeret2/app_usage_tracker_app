package com.hour.hour.helper

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

object PackageHelper {
    fun getAppName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e("getAppName", "fail ($packageName) ${e.message} - ${e.localizedMessage}")
            packageName
        }
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            val icon = context.packageManager.getApplicationIcon(packageName)
            icon
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e("getAppIcon", "fail ($packageName)${e.message} - ${e.localizedMessage}")
            null
        }
    }
}
