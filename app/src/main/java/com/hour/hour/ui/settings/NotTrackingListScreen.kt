package com.hour.hour.ui.settings

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.hour.hour.MainApplication
import com.hour.hour.MainApplication.Companion.store
import com.hour.hour.R
import com.hour.hour.helper.LP
import com.hour.hour.helper.NotTrackingListHelper
import com.hour.hour.helper.ResourceHelper
import com.hour.hour.helper.ResourceHelper.dp
import com.hour.hour.helper.Touchable
import com.hour.hour.model.NotTrackingRecord
import com.hour.hour.redux.ViewStore
import com.hour.hour.ui.base.BaseController
import com.hour.hour.widget.ActionBar
import com.hour.hour.widget.NotTrackingListAdapter

class NotTrackingListScreen : BaseController() {
    private var mRecyclerView: RecyclerView? = null
    private var mNotTrackingRecords: List<NotTrackingRecord>? = null

    override fun onCreateView(context: Context): View {
        return setup(context)
    }

    private fun setup(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        val actionBar = ActionBar(context)
        val recyclerView = RecyclerView(context)

        mRecyclerView = recyclerView

        actionBar.run {
            val title = TextView(context).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                typeface = ResourceHelper.font(R.font.barlow_condensed_medium)
                textSize = 22f
                setTextColor(ResourceHelper.color(R.color.primary))
                text = "Not Tracking List"
                Touchable.make(this@apply)
            }

            elevation = 2f
            setBackgroundColor(Color.WHITE)

            addLeftButton(R.drawable.ic_arrow_back_24dp, onBackClick)
            addLeftView(title)
            addRightButton(R.drawable.ic_info_24dp, onInfoClick)
        }

        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
            isVerticalScrollBarEnabled = true

            val list = NotTrackingListHelper.getList(context)
            list.sortedByDescending { it.isIgnored }
            mNotTrackingRecords = list
            mRecyclerView?.adapter = NotTrackingListAdapter(list, onCheckChange)
        }

        layout.apply {
            orientation = LinearLayout.VERTICAL
            addView(actionBar, LP.MATCH_PARENT, dp(50))
            addView(recyclerView, LP.MATCH_PARENT, LP.MATCH_PARENT)
        }

        return layout
    }

    private val onBackClick = View.OnClickListener {
        popController()
    }

    private val onCheckChange: ((record: NotTrackingRecord, isChecked: Boolean) -> Unit) =
            { record: NotTrackingRecord, isChecked: Boolean ->
                activity?.let {
                    record.isIgnored = isChecked

                    if (isChecked) {
                        NotTrackingListHelper.addRecords(it, listOf(record))
                    } else {
                        NotTrackingListHelper.removeRecords(it, listOf(record))
                    }

                    val l = NotTrackingListHelper.loadNotTrackingList(applicationContext ?: return@let)
                    MainApplication.store().dispatch(ViewStore.Action.SetNotTrackingList(l))
                }
            }

    private val onInfoClick = View.OnClickListener {
        activity?.let {
            val builder = AlertDialog.Builder(it, android.R.style.Theme_Material_Dialog_NoActionBar)
            builder.setTitle("Information")
                    .setMessage("Add your productivity apps into not tracking list. Apps added will not be count into usage.")
                    .setPositiveButton(android.R.string.ok, { _, _ -> })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        }
    }
}
