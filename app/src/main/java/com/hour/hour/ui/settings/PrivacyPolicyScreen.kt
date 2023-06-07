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

class PrivacyPolicyScreen : BaseController() {
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
            text = "\nCollection of Information\n\n" +
                    "We collect information about you and your use of our service,  your interactions with us and your apps usage time.\n\n\n" +
                    "Use of Information\n\n" +
                    "We use information to analyze and for research purpose. " +
                    "Only statistics and result of analyze will be used. Individuals' information would not be disclosed to any party.\n\n"
        }

        contentLayout.apply {
            orientation = LinearLayout.VERTICAL
            addView(textView, LP.linear(LP.MATCH_PARENT, LP.MATCH_PARENT).build())
        }

        baseLayout.apply {
            addView(actionBar, LP.frame(LP.MATCH_PARENT, ResourceHelper.dp(50)).build())
            addView(contentLayout, LP.frame(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .setMargins(0, ResourceHelper.dp(50), 0, 0)
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
                text = "Privacy Policy"
                Touchable.make(this@apply)
            }

            elevation = 2f
            setBackgroundColor(Color.WHITE)

            addLeftButton(R.drawable.ic_arrow_back_24dp, View.OnClickListener { _ -> popController() })
            addLeftView(title)
        }
    }
}
