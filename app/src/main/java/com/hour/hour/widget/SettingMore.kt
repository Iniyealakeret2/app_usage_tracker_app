package com.hour.hour.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.hour.hour.R
import com.hour.hour.helper.LP

@SuppressLint("ResourceType")
class SettingMore(context: Context) : RelativeLayout(context) {
    val title = TextView(context)
    val content = TextView(context)
    val more = ImageView(context)

    init {
        title.apply {
            textSize = 16f
            setTextColor(Color.parseColor("#404040"))
            id = 1
        }
        content.apply {
            id = 2
        }
        more.apply {
            id = 3
            setImageDrawable(resources.getDrawable(R.drawable.ic_chevron_right_black_24dp, null))
        }
        addView(title)
        addView(content, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                .toLeftOf(more.id)
                .belowOf(title.id)
                .build())
        addView(more, LP.relative(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                .alignParentEnd()
                .alignCenterVertical()
                .build())
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled) {
            title.alpha = 1f
            content.alpha = 1f
            more.alpha = 1f
        } else {
            title.alpha = 0.5f
            content.alpha = 0.5f
            more.alpha = 0.5f
        }
        super.setEnabled(enabled)
    }

    fun setPadding(margin: Int) {
        setPadding(margin * 2, margin, margin * 2, margin)
        more.setPadding(margin * 2, 0, 0, 0)
    }
}
