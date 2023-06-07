package com.hour.hour.widget

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import com.hour.hour.helper.LP
import com.hour.hour.helper.ResourceHelper.dp

class SettingDivider(context: Context) : View(context) {
    private val margin = dp(13)
    init {
        setBackgroundColor(Color.parseColor("#555555"))
        val lp = LinearLayout.LayoutParams(LP.MATCH_PARENT, dp(1))
        lp.setMargins(2 * margin, 0, 2 * margin, 0)
        layoutParams = lp
    }
}
