package com.hour.hour

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hour.hour.helper.Logger
import com.hour.hour.helper.ScreenUnlockHelper

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent) {
        if (p1.action == Intent.ACTION_USER_PRESENT) {
            Logger.d("ScreenUnlockReceiver", "onReceive")
            ScreenUnlockHelper.countScreenUnlock(p0)
        }
    }
}
