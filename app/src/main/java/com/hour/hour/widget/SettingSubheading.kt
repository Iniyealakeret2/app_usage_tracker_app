package com.hour.hour.widget

import android.content.Context
import android.widget.TextView
import com.hour.hour.R
import com.hour.hour.helper.ResourceHelper

class SettingSubheading(context: Context) : TextView(context) {
    init {
        textSize = 12f
        setTextColor(ResourceHelper.color(R.color.primary))
    }
    fun setPadding(margin: Int) {
        setPadding(2 * margin, margin, 2 * margin, 0)
    }
}
