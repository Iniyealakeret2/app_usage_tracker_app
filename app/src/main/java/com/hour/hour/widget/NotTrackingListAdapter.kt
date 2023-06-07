package com.hour.hour.widget

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.hour.hour.model.NotTrackingRecord
import com.hour.hour.ui.settings.NotTrackingListView

class NotTrackingListAdapter(
        private var items: List<NotTrackingRecord>,
        private var onCheckChange: ((NotTrackingRecord, Boolean) -> Unit)?
) : RecyclerView.Adapter<NotTrackingListAdapter.ViewHolder>() {
    class ViewHolder(val view: NotTrackingListView) : RecyclerView.ViewHolder(view) {
        fun bind(u: NotTrackingRecord) {
            view.bind(u)
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = NotTrackingListView(p0.context, onCheckChange)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(items[p1])
    }
}
