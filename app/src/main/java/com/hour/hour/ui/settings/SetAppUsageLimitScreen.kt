package com.hour.hour.ui.settings

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.hour.hour.MainApplication
import com.hour.hour.R
import com.hour.hour.helper.LP
import com.hour.hour.helper.ResourceHelper
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.helper.Touchable
import com.hour.hour.redux.ViewStore
import com.hour.hour.ui.base.BaseController

class SetAppUsageLimitScreen : BaseController() {
    override fun onCreateView(context: Context): View {
        return setup(context)
    }
    private fun setup(context: Context): View {
        val baseLayout = LinearLayout(context)
        val optionContainer = LinearLayout(context)
        val cancel = TextView(context)
        val done = TextView(context)
        val seekBar = SeekBar(context)
        val limit = TextView(context)
        val limitContainer = LinearLayout(context)
        val time = MainApplication.store().view.state.usageLimit

        cancel.apply {
            textSize = 20f
            text = "CANCEL"
            setTextColor(Color.WHITE)
            setPadding(0, dp(20), 0, dp(20))
            Touchable.make(this)
            gravity = Gravity.CENTER
            setOnClickListener { popController() }
        }

        done.apply {
            textSize = 20f
            text = "DONE"
            setTextColor(Color.WHITE)
            setPadding(0, dp(20), 0, dp(20))
            Touchable.make(this)
            gravity = Gravity.CENTER
            setOnClickListener {
                MainApplication.store().dispatch(ViewStore.Action.SetUsageLimit(seekBar.progress))
                popController()
            }
        }

        optionContainer.apply {
            setBackgroundColor(ResourceHelper.color(R.color.light))
            addView(cancel, LP.linear(0, LP.WRAP_CONTENT).setWeight(1).build())
            addView(done, LP.linear(0, LP.WRAP_CONTENT).setWeight(1).build())
        }

        seekBar.apply {
            max = 120
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
                    limit.text = "%02d:%02d HOURS".format(p1 / 60, p1 % 60)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) { }
            })
            progress = time
        }

        limit.apply {
            setTextColor(Color.WHITE)
            textSize = 30f
            minWidth = dp(20)
            this.backgroundTintList = null
        }

        limitContainer.apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ResourceHelper.color(R.color.primary))

            addView(seekBar, LP.linear(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .setMargins(dp(40), dp(100), dp(40), 0)
                    .build())
            addView(limit, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER)
                    .setMargins(0, dp(80), 0, dp(30))
                    .build())
        }

        baseLayout.apply {
            orientation = LinearLayout.VERTICAL

            addView(optionContainer, LP.linear(LP.MATCH_PARENT, LP.WRAP_CONTENT).build())
            addView(limitContainer, LP.linear(LP.MATCH_PARENT, LP.WRAP_CONTENT).build())
        }

        return baseLayout
    }
}
