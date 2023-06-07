package com.hour.hour.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bluelinelabs.conductor.RouterTransaction
import com.hour.hour.R
import com.hour.hour.helper.LP
import com.hour.hour.helper.PermissionHelper
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.ui.base.BaseController
import com.hour.hour.widget.ActionBar

class EnableScreen : BaseController() {
    var mDoneBtn: Button? = null
    var mAllowView: RelativeLayout? = null
    var mDoneView: LinearLayout? = null

    //region Lifecycle
    override fun onCreateView(context: Context): View {
        return setup(context)
    }

    private fun setup(context: Context): View {
        val ACTIONBAR_ID = 1
        val ARROW_ID = 2
        val layout = RelativeLayout(context)
        val actionBar = ActionBar(context)
        val doneBtn = Button(context)
        val allowView = RelativeLayout(context)
        val doneView = LinearLayout(context)
        mDoneBtn = doneBtn
        mAllowView = allowView
        mDoneView = doneView

        allowView.apply {
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(10), dp(20), dp(10))
            val title = TextView(context).apply {
                setText(R.string.enable_allowbutton_title)
                textSize = 20f
                setTextColor(Color.parseColor("#202020"))
            }
            val hint = TextView(context).apply {
                setText(R.string.enable_allowbutton_hint)
            }
            val textContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(title)
                addView(hint)
            }
            val arrow = ImageView(context).apply {
                setImageDrawable(resources.getDrawable(R.drawable.ic_keyboard_arrow_right_green_24dp, context.theme))
                id = ARROW_ID
            }
            addView(textContainer, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .toLeftOf(ARROW_ID)
                    .build())
            addView(arrow, LP.relative(dp(24), dp(24))
                    .alignParentEnd()
                    .alignCenterVertical()
                    .build())
            setOnTouchListener(onAllow)
            elevation = 6f
        }

        doneView.apply {
            orientation = LinearLayout.VERTICAL
            val tick = ImageView(context).apply {
                setImageDrawable(resources.getDrawable(R.drawable.ic_done_green_24dp, context.theme))
            }
            val title = TextView(context).apply {
                setText(R.string.enable_done_title)
                textSize = 28f
                setTextColor(Color.parseColor("#202020"))
            }
            val hint = TextView(context).apply {
                setText(R.string.enable_done_hint)
            }

            addView(tick, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL).build())
            addView(title, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL)
                    .setMargins(0, dp(20), 0, dp(8))
                    .build())
            addView(hint, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL).build())
        }

        actionBar.apply {
            setTitle(R.string.enable_title)
            id = ACTIONBAR_ID
        }
        doneBtn.apply {
            setBackgroundResource(R.drawable.btn_rounded)
            setText(R.string.enable_doneBtn)
            setTextColor(Color.WHITE)
            setOnClickListener(onDone)
            setEnable(PermissionHelper.hasAppUsagePermission(context))
        }

        activity?.actionBar?.show()
        layout.apply {
            addView(actionBar, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .build())
            addView(allowView, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .setMargins(dp(20), dp(30), dp(20), 0)
                    .belowOf(actionBar.id)
                    .build())
            addView(doneBtn, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .alignParentBottom()
                    .setMargins(dp(20), 0, dp(20), dp(20))
                    .build())
            addView(doneView, LP.relative(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .alignParentCenter()
                    .build())
        }
        return layout
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)

        setEnable(PermissionHelper.hasAppUsagePermission(activity))
    }
    //endregion

    //region UI Events
    private fun setEnable(enable: Boolean) {
        mDoneBtn?.apply {
            if (enable) {
                setBackgroundColor(Color.parseColor("#69f0ae"))
                isClickable = true
                mAllowView?.animate()?.cancel()
                mAllowView?.alpha = 0f
                mDoneView?.animate()?.cancel()
                mDoneView?.animate()?.alpha(1f)?.duration = 2000
            } else {
                setBackgroundColor(Color.parseColor("#d0d0d0"))
                isClickable = false
                mDoneView?.animate()?.cancel()
                mDoneView?.alpha = 0f
                mAllowView?.animate()?.cancel()
                mAllowView?.alpha = 1f
            }
        }
    }

    private val onAllow = View.OnTouchListener { _, event ->
        if (event?.action == MotionEvent.ACTION_UP) {
            PermissionHelper.getAppUsagePermission(activity ?: return@OnTouchListener true)
            return@OnTouchListener false
        }
        true
    }

    private val onDone = View.OnClickListener {
        router.setRoot(RouterTransaction.with(MainScreen()))
    }
    //endregion
}
