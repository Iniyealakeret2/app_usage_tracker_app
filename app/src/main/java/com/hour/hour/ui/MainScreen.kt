package com.hour.hour.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.cardview.widget.CardView
import androidx.appcompat.widget.SwitchCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.hour.hour.AppConfig
import com.hour.hour.MainApplication.Companion.store
import io.reactivex.disposables.CompositeDisposable
import com.hour.hour.ui.base.BaseController
import com.hour.hour.R
import com.hour.hour.R.attr.tabIndicatorColor
import com.hour.hour.helper.CalendarHelper
import com.hour.hour.helper.GraphHelper
import com.hour.hour.helper.GraphHelper.plot7DayBarChart
import com.hour.hour.helper.LP
import com.hour.hour.helper.Logger
import com.hour.hour.helper.ResourceHelper.color
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.helper.Touchable
import com.hour.hour.helper.UsageStatsHelper
import com.hour.hour.helper.UsageStatsHelper.HOUR_24
import com.hour.hour.helper.UsageStatsHelper.getAverageUsageTime
import com.hour.hour.model.UsageDigest
import com.hour.hour.redux.ViewStore
import com.hour.hour.ui.settings.SettingScreen
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.hour.hour.helper.CalendarHelper.toReadableDuration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import java.util.Timer
import java.util.TimerTask

@SuppressLint("ResourceType")
class MainScreen : BaseController() {
    private val mSubscriptions = CompositeDisposable()
    private var mOverviewText: TextView? = null
    private var mAverageText: TextView? = null
    private var mUnlockOverview: TextView? = null
    private var mReminderTitle: TextView? = null
    private var mUsageOverview: TextView? = null
    private var mReminderSwitch: SwitchCompat? = null
    private var mOldTime = 0L
    private var mLineChartYesterday: LineChart? = null
    private var mBarChart7Day: BarChart? = null
    private var mTimer = Timer()

    //region Life Cycle
    override fun onCreateView(context: Context): View {
        return setup(context)
    }

    private fun setup(context: Context): View {
        val baseLayout = FrameLayout(context)
        val scrollable = ScrollView(context)
        val contentLayout = LinearLayout(context)
        val todayText = TextView(context)
        val dayText = TextView(context)
        val settingBtn = setupSettingBtn(context)
        val graphBtn = setupGraphBtn(context)
        val todayOverviewLayout = setupTodayOverviewLayout(context)
        val reminderLayout = setupReminderLayout(context)
        val divider = View(context)
        val yesterdayText = TextView(context)
        val yesterdayOverviewLayout = setupYesterdayOverview(context)
        val lineChartYesterday = GraphHelper.plotDailyLineChart(context)
        val divider2 = View(context)
        val overviewText = TextView(context)
        val barChart7Day = plot7DayBarChart(context)

        mLineChartYesterday = lineChartYesterday
        mBarChart7Day = barChart7Day

        todayText.apply {
            text = "TODAY"
            textSize = 20f
            setTextColor(Color.parseColor("#484c63"))
            setPadding(0, 0, 0, dp(5))
        }

        dayText.apply {
            text = CalendarHelper.getDateLong(System.currentTimeMillis())
            isAllCaps = true
            textSize = 15f
            setTextColor(Color.parseColor("#8996a9"))
        }

        divider.apply {
            setBackgroundColor(color(R.color.dark))
            alpha = 0.15f
            elevation = 5f
        }

        yesterdayText.apply {
            text = "YESTERDAY"
            setTextColor(color(R.color.dark))
            alpha = 0.6f
            textSize = 16f
        }

        divider2.apply {
            setBackgroundColor(color(R.color.dark))
            alpha = 0.15f
            elevation = 5f
        }

        overviewText.apply {
            text = "THIS WEEK'S USAGE"
            setTextColor(color(R.color.dark))
            alpha = 0.6f
            textSize = 16f
        }

        contentLayout.apply {
            setBackgroundColor(color(R.color.accent))
            orientation = LinearLayout.VERTICAL
            addView(todayText, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .setGravity(Gravity.CENTER_HORIZONTAL)
                    .setMargins(0, dp(22), 0, 0)
                    .build())
            addView(dayText, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .setGravity(Gravity.CENTER_HORIZONTAL)
                    .build())
            addView(todayOverviewLayout, LP.linear(dp(220), dp(220))
                    .setGravity(Gravity.CENTER_HORIZONTAL)
                    .setMargins(0, dp(30), 0, dp(20))
                    .build())
            addView(reminderLayout, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .setGravity(Gravity.CENTER_HORIZONTAL)
                    .setMargins(0, dp(10), 0, dp(20))
                    .build())
            addView(divider, LP.linear(LP.MATCH_PARENT, dp(3))
                    .setMargins(dp(60), dp(80), 0, dp(10))
                    .build())
            addView(yesterdayText, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .setMargins(dp(60), 0, 0, 0)
                    .build())
            addView(yesterdayOverviewLayout, LP.linear(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .setMargins(0, dp(20), 0, dp(0))
                    .build())
            addView(lineChartYesterday, LP.linear(LP.MATCH_PARENT, dp(200))
                    .setMargins(dp(40), dp(20), dp(30), 0)
                    .build())
            addView(divider2, LP.linear(LP.MATCH_PARENT, dp(3))
                    .setMargins(dp(60), dp(100), 0, dp(10))
                    .build())
            addView(overviewText, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .setMargins(dp(60), 0, 0, 0)
                    .build())
            addView(barChart7Day, LP.linear(LP.MATCH_PARENT, dp(200))
                    .setMargins(dp(40), dp(15), dp(30), dp(100))
                    .build())
        }

        scrollable.apply {
            isVerticalScrollBarEnabled = false
            addView(contentLayout)
        }

        baseLayout.apply {
            addView(graphBtn, LP.frame(dp(55), dp(55), Gravity.END).build())
            addView(settingBtn, LP.frame(dp(55), dp(55)).build())
            addView(scrollable, LP.frame(LP.MATCH_PARENT, LP.MATCH_PARENT).build())
        }

        return baseLayout
    }

    private fun setupSettingBtn(context: Context): FrameLayout {
        return FrameLayout(context).apply {
            elevation = 10f
            setBackgroundColor(color(R.color.light))
            Touchable.make(this@apply)
            setOnClickListener(onSettingClick)

            val img = ImageView(context).apply {
                setImageResource(R.drawable.ic_settings_white)
            }
            addView(img, LP.frame(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .setGravity(Gravity.CENTER)
                    .build())
        }
    }

    private fun setupGraphBtn(context: Context): FrameLayout {
        return FrameLayout(context).apply {
            elevation = 10f
            setBackgroundColor(color(R.color.light))
            Touchable.make(this@apply)
            setOnClickListener(onGraphClick)

            val img = ImageView(context).apply {
                setImageResource(R.drawable.ic_assessment_white)
            }
            addView(img, LP.frame(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .setGravity(Gravity.CENTER)
                    .build())
        }
    }

    private fun setupTodayOverviewLayout(context: Context): LinearLayout {
        val todayOverviewText = TextView(context)
        val averageText = TextView(context)

        mOverviewText = todayOverviewText
        mAverageText = averageText

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = resources.getDrawable(R.drawable.rounded_overview, null)
            elevation = 5f

            addView(todayOverviewText, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL)
                    .setMargins(0, dp(58), 0, dp(5))
                    .build())
            addView(averageText, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL)
                    .build())

            setOnTouchListener { v, event ->
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    v.elevation = 15f
                } else if (event?.action == MotionEvent.ACTION_CANCEL || event?.action == MotionEvent.ACTION_UP) {
                    v.elevation = 5f
                }
                false
            }
            setOnClickListener {
                pushController(UsageSummaryScreen())
            }
        }
    }

    private fun setupReminderLayout(context: Context): CardView {
        val container = LinearLayout(context)
        val title = TextView(context)
        val switch = SwitchCompat(context)

        mReminderTitle = title
        mReminderSwitch = switch

        return CardView(context).apply {
            radius = 30f

            title.apply {
                text = "REMINDER ON"
                setTextColor(Color.WHITE)
                setPadding(dp(30), dp(10), dp(40), dp(10))
                textSize = 13f
            }
            switch.apply {
                trackTintList = ColorStateList(
                        arrayOf(intArrayOf(android.R.attr.state_checked),
                                intArrayOf(-android.R.attr.state_checked)),
                        intArrayOf(color(R.color.dark),
                                Color.parseColor("#A3B0BA")))
                setOnCheckedChangeListener(onReminderSwitchChange)
            }
            container.apply {
                addView(title)
                addView(switch, LP.linear(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER_VERTICAL)
                        .setMargins(dp(10), 0, dp(10), 0)
                        .build())
            }

            addView(container)
            setOnTouchListener(onReminderTouch)
        }
    }

    private fun setupYesterdayOverview(context: Context): LinearLayout {
        val usageOverview = TextView(context)
        val unlockOverview = TextView(context)

        mUnlockOverview = unlockOverview
        mUsageOverview = usageOverview

        return LinearLayout(context).apply {
            val d1 = View(context).apply { setBackgroundColor(Color.parseColor("#ffcc66")) }
            val d2 = View(context).apply { setBackgroundColor(Color.parseColor("#ff9999")) }

            addView(d1, LP.linear(dp(3), LP.MATCH_PARENT)
                    .setMargins(dp(60), 0, dp(10), 0)
                    .build())
            addView(unlockOverview, LP.linear(0, LP.WRAP_CONTENT)
                    .setWeight(1)
                    .build())
            addView(d2, LP.linear(dp(3), LP.MATCH_PARENT)
                    .setMargins(0, 0, dp(10), 0)
                    .build())
            addView(usageOverview, LP.linear(0, LP.WRAP_CONTENT)
                    .setMargins(0, 0, dp(20), 0)
                    .setWeight(1)
                    .build())
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        mTimer = Timer()
        mTimer.schedule(object : TimerTask() {
            override fun run() {
                updateView()
            }
        }, 0, AppConfig.MAIN_SCREEN_UPDATEVIEW_TIMER)

        mSubscriptions.add(store().observe(store().view)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mapReminder)
                .distinctUntilChanged()
                .subscribe(consumeReminder))
    }

    override fun onDetach(view: View) {
        mTimer.cancel()
        mOldTime = 0
        super.onDetach(view)
        mSubscriptions.clear()
    }
    //---------------------------------------------------------------
    //endregion

    //region UI Events
    private fun updateView() {
        val usageTime = UsageStatsHelper.getTodayUsageTime(true)
        if (usageTime == mOldTime) return

        mOldTime = usageTime
        activity?.let {
            it.runOnUiThread {
                setUsage(usageTime)
                setAverage(usageTime)
                setYesterday(it)
                GraphHelper.updateChart(mLineChartYesterday
                        ?: return@runOnUiThread, CalendarHelper.getDateCondensed(System.currentTimeMillis() - UsageStatsHelper.HOUR_24))
                GraphHelper.updateChart(mBarChart7Day ?: return@runOnUiThread)
            }
        }
        Logger.d("text", "update view")
    }

    private fun setYesterday(context: Context) {
        val yesterdayDigest = UsageDigest.loadFiltered(context, CalendarHelper.getDateCondensed(System.currentTimeMillis() - HOUR_24))

        mUnlockOverview?.apply {
            val str = CalendarHelper.toReadableDuration(yesterdayDigest.totalTime)
            val suffix = "\nUSAGE TIME"
            val span = SpannableString(str + suffix)
            span.setSpan(RelativeSizeSpan(2f), 0, str.length, 0)
            span.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, str.length, 0)
            span.setSpan(ForegroundColorSpan(Color.parseColor("#484c63")), 0, str.length, 0)
            span.setSpan(ForegroundColorSpan(Color.parseColor("#b5bdc9")), str.length, span.length, 0)
            text = span
        }
        mUsageOverview?.apply {
            val str = yesterdayDigest.unlockCount.toString() ?: ""
            val suffix = "\nSCREEN UNLOCK"
            val span = SpannableString(str + suffix)
            span.setSpan(RelativeSizeSpan(2f), 0, str.length, 0)
            span.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, str.length, 0)
            span.setSpan(ForegroundColorSpan(Color.parseColor("#484c63")), 0, str.length, 0)
            span.setSpan(ForegroundColorSpan(Color.parseColor("#b5bdc9")), str.length, span.length, 0)
            text = span
        }
    }

    private fun setUsage(t: Long) {
        if (t / 60000 < 60) {
            val span = SpannableString(CalendarHelper.toReadableDuration(t))
            span.setSpan(RelativeSizeSpan(4.3f), 0, span.length - 4, 0)
            span.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, span.length - 4, 0)
            span.setSpan(RelativeSizeSpan(1.8f), span.length - 4, span.length, 0)
            mOverviewText?.text = span
        } else {
            val hour = (t / 3600000).toString()
            val min = ((t % 3600000) / 60000).toString()
            val span = SpannableString("${hour}h  ${min}m")
            span.setSpan(RelativeSizeSpan(4.3f), 0, hour.length, 0)
            span.setSpan(RelativeSizeSpan(4.3f), hour.length + 3, hour.length + 3 + min.length, 0)
            span.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, span.length, 0)
            span.setSpan(RelativeSizeSpan(1.8f), hour.length, hour.length + 3, 0)
            span.setSpan(RelativeSizeSpan(1.8f), span.length - 1, span.length, 0)
            mOverviewText?.text = span
        }

        mOverviewText?.setTextColor(color(R.color.primary))
    }

    private fun setAverage(time: Long) {
        val average = getAverageUsageTime(true)
//        val span = SpannableString("${if (average == 0L) 100 else time * 100 / average}% of your average")
        val span = SpannableString(
                if (time >= average) "${toReadableDuration(time - average)} above your average"
                else "${toReadableDuration(average - time)} to your average"
        )
        span.setSpan(StyleSpan(android.graphics.Typeface.ITALIC), 0, span.length, 0)
        mAverageText?.text = span
        if (time >= average && average != 0L) {
            mAverageText?.setTextColor(Color.parseColor("#ff1a00"))
        } else {
            mAverageText?.setTextColor(tabIndicatorColor)
        }
    }

    private val onSettingClick = View.OnClickListener {
        pushController(SettingScreen(), FadeChangeHandler(), FadeChangeHandler())
    }

    private val onGraphClick = View.OnClickListener {
        pushController(UsageSummaryScreen())
    }

    private val onReminderSwitchChange = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        store().view.dispatch(ViewStore.Action.SetReminder(isChecked))
    }
    private val onReminderTouch = View.OnTouchListener { v, event ->
        if (event?.action == MotionEvent.ACTION_DOWN) {
            v.elevation = 8f
        } else if (event?.action == MotionEvent.ACTION_CANCEL || event?.action == MotionEvent.ACTION_UP) {
            v.elevation = 2f
        }
        if (event?.action == MotionEvent.ACTION_UP) {
            mReminderSwitch?.isChecked = !(mReminderSwitch?.isChecked
                    ?: return@OnTouchListener true)
            return@OnTouchListener false
        }
        true
    }
    //endregion

    //region redux
    private val mapReminder = Function<ViewStore.State, Boolean> { state ->
        state.isReminderOn
    }
    private val consumeReminder = Consumer<Boolean> { isChecked ->
        mReminderSwitch?.isChecked = isChecked
        mReminderTitle?.setBackgroundColor(
                if (isChecked) {
                    color(R.color.dark)
                } else {
                    Color.parseColor("#A3B0BA")
                })
    }
    //endregion
}
