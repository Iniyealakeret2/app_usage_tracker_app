package com.hour.hour.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.hour.hour.R
import com.hour.hour.helper.CalendarHelper
import com.hour.hour.helper.LP
import com.hour.hour.helper.PackageHelper.getAppIcon
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.model.UsageSummary

class UsageSummaryView(context: Context, private var onClick: OnClickListener) : LinearLayout(context) {
    val margin = dp(13)
    val icon = ImageView(context)
    val appName = TextView(context)
    val usageSummary = TextView(context)
    val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
    var packageName: String? = null

    init {
        setup()
    }

    private fun setup() {
        val contentContainer = LinearLayout(context)
        val img = ImageView(context).apply { setImageDrawable(context.getDrawable(R.drawable.ic_chevron_right_black_24dp)) }

        appName.apply {
            textSize = 18f
            setTextColor(Color.parseColor("#404040"))
        }
        progressBar.apply {
            progressDrawable = context.getDrawable(R.drawable.custom_progress_bar_horizontal)
            progressTintList = ColorStateList.valueOf(Color.RED)
        }

        usageSummary.apply {
            textSize = 13f
        }

        contentContainer.apply {
            orientation = LinearLayout.VERTICAL
            addView(appName)
            addView(progressBar, LP.MATCH_PARENT, dp(10))
            addView(usageSummary)
        }

        addView(icon, LP.linear(dp(40), dp(40), Gravity.CENTER_VERTICAL)
                .setMargins(0, 0, margin * 2, 0)
                .build())
        addView(contentContainer, LP.linear(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                .build())
        addView(img, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER_VERTICAL).build())

        background = resources.getDrawable(R.drawable.btn_rounded, null)
        setBackgroundColor(Color.WHITE)
        setPadding(dp(25), dp(12), dp(25), dp(12))
        elevation = 4f

        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.elevation = 8f
            } else if (event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP) {
                v.elevation = 4f
            }

            if (event.action == MotionEvent.ACTION_UP) {
                onClick.onClick(v)
                return@setOnTouchListener false
            }
            true
        }
        val lp = LinearLayout.LayoutParams(LP.MATCH_PARENT, LP.WRAP_CONTENT)
        lp.setMargins(dp(25), dp(8), dp(25), dp(8))
        layoutParams = lp
    }

    fun bind(u: UsageSummary, progress: Int) {
        icon.setImageDrawable(getAppIcon(context, u.packageName))
        appName.text = u.appName
        packageName = u.packageName
        progressBar.progress = progress
        usageSummary.text = "You have used for " + CalendarHelper.toReadableDuration(u.useTimeTotal)
    }
}
