package com.hour.hour.ui.settings

import android.content.Context
import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.hour.hour.R
import com.hour.hour.helper.LP
import com.hour.hour.helper.ResourceHelper
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.helper.Touchable
import com.hour.hour.ui.base.BaseController
import com.hour.hour.widget.ActionBar

class FaqScreen : BaseController() {
    override fun onCreateView(context: Context): View {
        return setup(context)
    }

    private fun setup(context: Context): View {
        val baseLayout = FrameLayout(context)
        val actionBar = setupActionBar(context)
        val contentLayout = LinearLayout(context)
        val textView = TextView(context)

        textView.apply {
            movementMethod = ScrollingMovementMethod()
            setPadding(dp(26), 0, dp(26), 0)
            text = "\nQ1: Why I can't see last 7 days / last 30 days records?\n" +
                    "A: The app starts tracking your usage after it is being installed. Past data might not be shown.\n" +
                    "\n" +
                    "Q2: Why I can't see my recent records when I haven't opened my app for a few days?\n" +
                    "A: Please turn off 'App Optimisation' for this app. It sometimes prevent this app from working.\n" +
                    "\n" +
                    "Q3: I turned off 'App Optimisation', but it is still not working?\n" +
                    "A: Enable Notification Bar for stability.\n" +
                    "\n" +
                    "Q4: What if I don't want to track my productivity apps?\n" +
                    "A: Go to 'Setting' --> 'Not Tracking List' and choose apps you don't want to track.\n" +
                    "\n" +
                    "Q5: I found a bug. What can I do?\n" +
                    "A: Please feel free to email me. You can find my contact in 'About This App'.\n" +
                    "\n"
        }

        contentLayout.apply {
            orientation = LinearLayout.VERTICAL
            addView(textView, LP.linear(LP.MATCH_PARENT, LP.MATCH_PARENT).build())
        }

        baseLayout.apply {
            addView(actionBar, LP.frame(LP.MATCH_PARENT, dp(50)).build())
            addView(contentLayout, LP.frame(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .setMargins(0, dp(50), 0, 0)
                    .build())
        }

        return baseLayout
    }

    private fun setupActionBar(context: Context): ActionBar {
        return ActionBar(context).apply {
            val title = TextView(context).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                typeface = ResourceHelper.font(R.font.barlow_condensed_medium)
                textSize = 22f
                setTextColor(ResourceHelper.color(R.color.primary))
                text = "Frequently Asked Questions"
                Touchable.make(this@apply)
            }

            elevation = 2f
            setBackgroundColor(Color.WHITE)

            addLeftButton(R.drawable.ic_arrow_back_24dp, View.OnClickListener { _ -> popController() })
            addLeftView(title)
        }
    }
}
