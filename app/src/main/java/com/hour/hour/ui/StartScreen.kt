package com.hour.hour.ui

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bluelinelabs.conductor.RouterTransaction
import com.hour.hour.MainApplication.Companion.store
import com.hour.hour.R
import com.hour.hour.helper.LP
import com.hour.hour.helper.PermissionHelper
import com.hour.hour.helper.ResourceHelper.color
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.redux.ViewStore
import com.hour.hour.ui.base.BaseController
import com.hour.hour.ui.settings.PrivacyPolicyScreen

class StartScreen : BaseController() {
    //region Lifecycle
    override fun onCreateView(context: Context): View {
        return setup(context)
    }
    private fun setup(context: Context): View {
        val TITLE_ID = 1
        val TERMSCONDITION_ID = 2
        val layout = RelativeLayout(context)
        val img = ImageView(context)
        val title = TextView(context)
        val subtitle = TextView(context)
        val startBtn = Button(context)
        val termsConditions = TextView(context)

        layout.setBackgroundColor(color(R.color.primary))

        img.apply {
            setImageResource(R.drawable.logo)
        }

        title.apply {
            setText(R.string.app_name)
            textSize = 28f
            setTextColor(Color.WHITE)
            id = TITLE_ID
        }

        subtitle.apply {
            setText(R.string.start_subtitle)
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            alpha = 0.9f
        }

        startBtn.apply {
            setBackgroundColor(Color.WHITE)
            setTextColor(R.color.primary)
            setText(R.string.start_startbutton)
            setOnClickListener(onStart)
        }

        termsConditions.apply {
//            val spannable = SpannableString("Agree to the Privacy Policy and Terms of Service")
            val stringBuilder = SpannableStringBuilder("Agree to the Privacy Policy and Terms of Service")
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    pushController(PrivacyPolicyScreen())
                }
            }
            stringBuilder.setSpan(clickableSpan, 13, 27, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            movementMethod = LinkMovementMethod.getInstance()
            text = stringBuilder
            setTextColor(Color.WHITE)
            id = TERMSCONDITION_ID
            gravity = Gravity.CENTER
            alpha = 0.8f
        }

        layout.apply {
            addView(img, LP.relative(dp(180), dp(180))
                    .aboveOf(TITLE_ID)
                    .alignCenterHorizontal()
                    .setMargins(0, 0, 0, dp(45))
                    .build())
            addView(title, LP.relative(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .alignParentCenter()
                    .alignCenterHorizontal()
                    .build())
            addView(subtitle, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .belowOf(TITLE_ID)
                    .alignCenterHorizontal()
                    .setMargins(dp(50), dp(5), dp(50), 0)
                    .build())
            addView(startBtn, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .aboveOf(TERMSCONDITION_ID)
                    .setMargins(dp(50), 0, dp(50), 0)
                    .build())
            addView(termsConditions, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .alignParentBottom()
                    .setMargins(dp(50), dp(10), dp(50), dp(15))
                    .alignCenterHorizontal()
                    .build())
        }

        return layout
    }
    //endregion

    //region UI Events
    private val onStart = View.OnClickListener {
        store().dispatch(ViewStore.Action.AgreeTermsConditions())
        if (PermissionHelper.hasAppUsagePermission(activity ?: return@OnClickListener)) {
            router.setRoot(RouterTransaction.with(MainScreen()))
        } else {
            pushController(EnableScreen())
        }
    }
    //endregion
}
