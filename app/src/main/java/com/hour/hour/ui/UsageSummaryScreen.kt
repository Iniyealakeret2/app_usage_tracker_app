package com.hour.hour.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.AsyncTask
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.hour.hour.R
import com.hour.hour.helper.CalendarHelper
import com.hour.hour.helper.GraphHelper
import com.hour.hour.helper.LP
import com.hour.hour.helper.ResourceHelper
import com.hour.hour.helper.ResourceHelper.color
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.helper.ScreenUnlockHelper
import com.hour.hour.helper.Touchable
import com.hour.hour.helper.UsageStatsHelper
import com.hour.hour.helper.UsageStatsHelper.HOUR_24
import com.hour.hour.model.UsageSummary
import com.hour.hour.model.UsageSummary.Companion.filter
import com.hour.hour.ui.base.BaseController
import com.hour.hour.widget.ActionBar
import com.hour.hour.widget.UsageRecyclerAdapter
import com.github.mikephil.charting.charts.PieChart

class UsageSummaryScreen : BaseController() {
    enum class Mode {
        Today,
        Hour24,
        Yesterday,
        Day7,
        Day30,
    }

    var mode = Mode.Today
    private var mRecycler: RecyclerView? = null
    private var mUsageText: TextView? = null
    private var mUnlockText: TextView? = null
    private var mOverlayLayout: FrameLayout? = null
    private var mPieChart: PieChart? = null

    //region Lifecycle
    //---------------------------------------------------------------
    override fun onCreateView(context: Context): View {
        val layout = setup(context)

        AsyncTask.execute {
            onSelectListener.invoke(mode.ordinal)
        }

        return layout
    }

    private fun setup(context: Context): View {
        val baseLayout = FrameLayout(context)
        val overviewLayout = FrameLayout(context)
        val actionBar = ActionBar(context)
        val contentLayout = NestedScrollView(context)
        val pieChart = GraphHelper.plotSummaryPieChart(context)
        val recyclerView = RecyclerView(context)
        val overlayLayout = FrameLayout(context)

        mOverlayLayout = overlayLayout
        mRecycler = recyclerView
        mPieChart = pieChart

        actionBar.run {
            val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                    arrayOf("Today", "Last 24 hour", "Yesterday", "Last 7 days", "Last 30 days"))
            val title = TextView(context).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                typeface = ResourceHelper.font(R.font.barlow_condensed_medium)
                textSize = 22f
                setTextColor(color(R.color.primary))
                text = "Usage Record"
                Touchable.make(this@apply)
                setOnClickListener { popController() }
            }

            val spinner = Spinner(context).apply {
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                this@apply.adapter = adapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        if (p2 != mode.ordinal) {
                            onSelectListener.invoke(p2)
                        }
                    }
                }
            }

            elevation = 2f
            setBackgroundColor(Color.WHITE)

            addLeftButton(R.drawable.ic_arrow_back_24dp, onBackClick)
            addLeftView(title)
            addRightView(spinner)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setPadding(0, 0, 0, dp(85))
            isNestedScrollingEnabled = false
        }

        overviewLayout.apply {
            val usageText = TextView(context)
            val unlockText = TextView(context)

            mUsageText = usageText
            mUnlockText = unlockText

            usageText.apply {
                setBackgroundColor(color(R.color.light))
                setTextColor(Color.WHITE)
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(0, 0, dp(145), 0)
                elevation = 15f
            }

            unlockText.apply {
                setBackgroundColor(Color.parseColor("#1d0d3f"))
                setTextColor(Color.WHITE)
                textSize = 18f
                gravity = Gravity.CENTER
                elevation = 50f
            }

            addView(usageText, LP.frame(LP.MATCH_PARENT, dp(85))
                    .setGravity(Gravity.BOTTOM)
                    .build())
            addView(unlockText, LP.frame(dp(120), dp(85))
                    .setMargins(0, 0, dp(25), 0)
                    .setGravity(Gravity.END)
                    .build())
        }

        contentLayout.apply {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(pieChart, LP.linear(LP.MATCH_PARENT, dp(250))
                        .setMargins(dp(40), 0, dp(40), 0)
                        .build())
                addView(recyclerView)
            }
            addView(layout, LP.MATCH_PARENT, LP.MATCH_PARENT)
        }

        overlayLayout.apply {
            setBackgroundColor(Color.parseColor("#BBFFFFff"))
            visibility = View.GONE
            setOnClickListener { visibility = View.GONE }
            elevation = 4f
        }

        baseLayout.apply {
            addView(actionBar, LP.frame(LP.MATCH_PARENT, dp(50)).build())
            addView(contentLayout, LP.frame(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .setMargins(0, dp(50), 0, 0)
                    .build())
            addView(overviewLayout, LP.frame(LP.MATCH_PARENT, dp(100))
                    .setGravity(Gravity.BOTTOM)
                    .build())
            addView(overlayLayout, LP.frame(LP.MATCH_PARENT, LP.MATCH_PARENT).build())
        }

        return baseLayout
    }
    //---------------------------------------------------------------
    //endregion

    //region UI Events
    private val onBackClick = View.OnClickListener {
        popController()
    }
    private val onSelectListener = { s: Int ->
        mode = Mode.values()[s]
        activity?.let {
            Thread {
                var list = listOf<UsageSummary>()
                var unlockCount = 0
                val time = System.currentTimeMillis()

                when (mode) {
                    Mode.Today -> {
                        list = filter(it, UsageSummary.makeSummary(it, UsageStatsHelper.queryTodayUsage()))
                        unlockCount = ScreenUnlockHelper.getTodayUnlockCount(it)
                    }
                    Mode.Hour24 -> {
                        list = filter(it, UsageSummary.makeSummary(it, UsageStatsHelper.query24hUsage()))
                        unlockCount = ScreenUnlockHelper.getTodayUnlockCount(it)
                    }
                    Mode.Yesterday -> {
                        list = UsageSummary.getFilteredSummary(it, arrayListOf(CalendarHelper.getDateCondensed(time - HOUR_24)))
                        unlockCount = ScreenUnlockHelper.getTodayUnlockCount(it)
                    }
                    Mode.Day7 -> {
                        list = UsageSummary.getFilteredSummary(it,
                                ((time - 6 * HOUR_24)..time step HOUR_24).map { CalendarHelper.getDateCondensed(it) }
                        )
                        unlockCount = ScreenUnlockHelper.getNDayUnlockCount(it, 7)
                    }
                    Mode.Day30 -> {
                        list = UsageSummary.getFilteredSummary(it,
                                ((time - 29 * HOUR_24)..time step HOUR_24).map { CalendarHelper.getDateCondensed(it) }
                        )
                        unlockCount = ScreenUnlockHelper.getNDayUnlockCount(it, 30)
                    }
                }
                it.runOnUiThread {
                    setUsage(list.map { it.useTimeTotal }.sum())
                    setUnlock(unlockCount)
                    GraphHelper.setData(mPieChart ?: return@runOnUiThread, list)
                    mRecycler?.adapter = UsageRecyclerAdapter(list, onSummaryClick)
                }
            }.start()
        }
    }

    private fun setUsage(l: Long) {
        val str = CalendarHelper.toReadableDuration(l)
        val span = SpannableString("$str\nUsage Time")
        span.setSpan(RelativeSizeSpan(0.7f), str.length, span.length, 0)
        if (l / 60000 < 60) {
            span.setSpan(RelativeSizeSpan(2f), 0, str.length - 4, 0)
            span.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, str.length - 4, 0)
        } else {
            span.setSpan(RelativeSizeSpan(2f), 0, str.length, 0)
            span.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, str.length, 0)
        }
        span.setSpan(ForegroundColorSpan(Color.parseColor("#aaffffff")), str.length, span.length, 0)
        mUsageText?.text = span
    }

    private fun setUnlock(count: Int) {
        val str = count.toString()
        val span = SpannableString("$str\nScreen Unlock")
        span.setSpan(RelativeSizeSpan(2f), 0, str.length, 0)
        span.setSpan(StyleSpan(Typeface.BOLD), 0, str.length, 0)
        span.setSpan(RelativeSizeSpan(0.7f), str.length, span.length, 0)
        span.setSpan(ForegroundColorSpan(Color.parseColor("#99ffffff")), str.length, span.length, 0)
        mUnlockText?.text = span
    }

    private val onSummaryClick = View.OnClickListener {
        // mOverlayLayout?.visibility = View.VISIBLE
    }
    //endregion
}
