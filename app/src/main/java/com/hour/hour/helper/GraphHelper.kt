@file:Suppress("DEPRECATION")

package com.hour.hour.helper

import android.content.Context
import android.graphics.Color
import androidx.appcompat.content.res.AppCompatResources
import com.hour.hour.MainApplication.Companion.store
import com.hour.hour.R
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.helper.UsageStatsHelper.HOUR_24
import com.hour.hour.model.UsageDigest
import com.hour.hour.model.UsageSummary
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.io.File
import java.lang.ref.WeakReference
import java.util.Calendar
import kotlin.math.roundToInt

@Suppress("MemberVisibilityCanBePrivate")
object GraphHelper {
    private var context: WeakReference<Context>? = null
    fun setup(c: Context) {
        context = WeakReference(c)
    }

    fun getCumulative(filename: String): List<Entry> {
        val context = context?.get() ?: return listOf()
        val path = File(context.filesDir.path + "/" + filename)
        val ntList = store().view.state.notTrackingList
        val records = CsvHelper.read(path).filter { !ntList.contains(it.packageName) }.sortedBy { it.starTime }
        if (records.isEmpty()) {
            return listOf()
        }
        val l = hashMapOf<Float, Float>()
        var culDuration = 0L
        val cal = Calendar.getInstance()
        cal.timeInMillis = records.first().starTime
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        for (r in records) {
            l[(r.starTime - startOfDay) / 60000f] = culDuration / 3600000f
            culDuration += r.duration
            l[(r.starTime + r.duration - startOfDay) / 60000f] = culDuration / 3600000f
        }
        l[0f] = 0f
        return l.toSortedMap().map { Entry(it.key, it.value) }
    }

    fun updateChart(lineChart: LineChart, day: String) {
        val context = context?.get() ?: return
        val entries = getCumulative(day)
        if (entries.isEmpty()) {
            return
        }
        val dataSet = LineDataSet(entries, "Usage Time")
        val lineData = LineData(dataSet)
        dataSet.apply {
            color = Color.parseColor("#00d0ff")
            setCircleColor(Color.parseColor("#00d0ff"))
            setDrawCircleHole(false)
            setDrawCircles(false)
            lineWidth = 2f
            fillDrawable = AppCompatResources.getDrawable(context, R.drawable.fade_background)
            setDrawFilled(true)
            fillAlpha
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return ""
                }
            }
        }
        lineChart.data = lineData
        lineChart.animateX(50)
    }

    fun plotDailyLineChart(context: Context): LineChart {
        return LineChart(context).apply {
            xAxis.apply {
                textSize = 13f
                position = XAxis.XAxisPosition.BOTTOM
                axisMinimum = 0f
                axisMaximum = HOUR_24 / 60000f
                labelRotationAngle = 30f
                axisLineWidth = 3f
                setLabelCount(5, true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%02d:%02d", value.toInt() / 60, value.toInt() % 60)
                    }
                }
            }

            axisLeft.apply {
                textSize = 13f
                axisMinimum = 0f
                setLabelCount(5, false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value == 0f) {
                            "0"
                        } else if (value > .98 && (value + 0.02) % 1 < 0.04) {
                            String.format("%dh", value.roundToInt())
                        } else if (value > .98) {
                            String.format("%dh%02dm", value.toInt(), (((value - value.toInt()) * 60)).roundToInt())
                        } else {
                            String.format("%dm", ((value - value.toInt()) * 60).roundToInt())
                        }
                    }
                }
                axisLineWidth = 3f
            }

            isDoubleTapToZoomEnabled = false
            axisRight.isEnabled = false
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = true
            description.isEnabled = false
            legend.isEnabled = false
        }
    }


    fun get7DaySummary(endTime: Long): List<BarEntry> {
        val context = context?.get() ?: return listOf()
        val series = arrayListOf<BarEntry>()
        var i = 1f
        val ntList = store().view.state.notTrackingList
        for (t in (endTime - 6 * HOUR_24)..endTime step HOUR_24) {
            val d = UsageDigest.loadFiltered(context, CalendarHelper.getDateCondensed(t))
            series.add(BarEntry(i, (d.totalTime) / 3600000f))
            i += 1
        }
        return series
    }

    fun updateChart(barChart: BarChart) {
        val time = System.currentTimeMillis()
        val entries = get7DaySummary(time)
        if (entries.isEmpty()) {
            return
        }
        val dataSet = BarDataSet(entries, "Usage Time")
        val barData = BarData(dataSet)
        dataSet.apply {
            valueTextSize = 11f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float, axis: AxisBase): String {
                    return if (value != 0f) {
                        String.format("%d:%02d", value.toInt(), ((value - value.toInt()) * 60).toInt())
                    } else {
                        ""
                    }
                }
            }
        }
        barChart.data = barData
        barChart.animateX(50)
    }

    fun plot7DayBarChart(context: Context): BarChart {
        return BarChart(context).apply {
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textSize = 13f
                labelRotationAngle = 30f

                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val t = System.currentTimeMillis() - (7 - value.toLong()) * HOUR_24
                        return CalendarHelper.getMonthDay(t)
                    }
                }
            }

            axisLeft.apply {
                axisMinimum = 0f
                setLabelCount(5, false)
                textSize = 13f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value == 0f) {
                            "0"
                        } else if (value > .98 && (value + 0.02) % 1 < 0.04) {
                            String.format("%dh", value.roundToInt())
                        } else if (value > .98) {
                            String.format("%dh%02dm", value.toInt(), (((value - value.toInt()) * 60)).roundToInt())
                        } else {
                            String.format("%dm", ((value - value.toInt()) * 60).roundToInt())
                        }
                    }
                }
            }

            isDoubleTapToZoomEnabled = false
            axisRight.isEnabled = false
            isDragEnabled = true
            isScaleXEnabled = false
            isScaleYEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
        }
    }

    fun plotSummaryPieChart(context: Context): PieChart {
        return PieChart(context).apply {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = false

            setDrawCenterText(true)
            centerText = "Total Usage Time"
            setCenterTextColor(Color.parseColor("#2b83bd"))
            setCenterTextSize(15f)

            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)

            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)

            holeRadius = 55f
            transparentCircleRadius = 63f

            setEntryLabelColor(Color.parseColor("#cc444444"))
            setEntryLabelTextSize(13f)
        }
    }

    fun setData(pieChart: PieChart, usageSummary: List<UsageSummary>) {
        val entries = arrayListOf<PieEntry>()
        val sum = usageSummary.sumOf { it.useTimeTotal }
        var cul = 0L
        for (i in usageSummary) {
            if (cul > 0.85 * sum || i.useTimeTotal < 0.05 * sum) {
                entries.add(PieEntry((sum - cul).toFloat(), "Others"))
                break
            } else {
                cul += i.useTimeTotal
                entries.add(PieEntry(i.useTimeTotal.toFloat(), i.appName))
            }
        }
        val colors = arrayListOf(
                Color.parseColor("#99ff33"),
                Color.parseColor("#fff133"),
                Color.parseColor("#ffb133"),
                Color.parseColor("#33d3ff")
        )
        val pieDataSet = PieDataSet(entries, "Total Usage Time").apply {
            sliceSpace = dp(3).toFloat()
            selectionShift = dp(5).toFloat()
            setColors(colors)
        }
        val pieData = PieData(pieDataSet).apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(15f)
            setValueTextColor(Color.parseColor("#555555"))
        }
        pieChart.data = pieData
        pieChart.animateY(1200, Easing.EaseInOutQuad)
    }
}
