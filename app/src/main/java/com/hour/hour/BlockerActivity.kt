package com.hour.hour

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.hour.hour.helper.LP
import com.hour.hour.helper.ResourceHelper.dp

class BlockerActivity : AppCompatActivity() {
    private var mRouter: Router? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = FrameLayout(this)
        layout.setBackgroundColor(Color.WHITE)
        setContentView(layout)
        mRouter = Conductor.attachRouter(this, layout, savedInstanceState)
        setup(layout)
    }

    private fun setup(layout: FrameLayout) {

        val img = ImageView(this).apply {
            setImageResource(R.drawable.logo)
        }
        val textView = TextView(this).apply {
            text = "Your usage exceeds limit.\n Why not take a break?"
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val closeStrictModeTtext = TextView(this).apply {
            val stringBuilder = SpannableStringBuilder("Click to Turn off Strict Mode")
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(this@BlockerActivity, MainActivity::class.java))
//                    finish()
                }
            }
            stringBuilder.setSpan(clickableSpan, 0, 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            movementMethod = LinkMovementMethod.getInstance()
            text = stringBuilder
        }

        layout.apply {
            setBackgroundColor(Color.parseColor("#AAAAAAAA"))

            addView(closeStrictModeTtext, LP.frame(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL)
                    .setMargins(dp(20), dp(20), dp(20), 0)
                    .build())
            addView(img, LP.frame(dp(200), dp(200), Gravity.CENTER).build())
            addView(textView, LP.frame(LP.MATCH_PARENT, LP.WRAP_CONTENT, Gravity.BOTTOM)
                    .setMargins(dp(20), 0, dp(20), dp(60))
                    .build())
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
}
