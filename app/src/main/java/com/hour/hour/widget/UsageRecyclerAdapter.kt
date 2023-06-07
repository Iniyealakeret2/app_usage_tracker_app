package com.hour.hour.widget

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.hour.hour.model.UsageSummary
import com.hour.hour.ui.UsageSummaryView

class UsageRecyclerAdapter(
        private var items: List<UsageSummary>,
        private var onClickListener: View.OnClickListener
) : RecyclerView.Adapter<UsageRecyclerAdapter.ViewHolder>() {
    var maxUsage = if (items.isNotEmpty()) items[0].useTimeTotal else 1

    class ViewHolder(val view: UsageSummaryView) : RecyclerView.ViewHolder(view) {
        fun bind(u: UsageSummary, maxUsage: Long) {
            view.bind(u, (u.useTimeTotal * 115 / maxUsage).toInt())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = UsageSummaryView(parent.context, onClickListener)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], maxUsage)
    }
}
