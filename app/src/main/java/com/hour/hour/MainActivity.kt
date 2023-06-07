package com.hour.hour

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.FrameLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.hour.hour.MainApplication.Companion.store
import com.hour.hour.helper.NotTrackingListHelper
import io.reactivex.disposables.CompositeDisposable
import com.hour.hour.ui.StartScreen
import com.hour.hour.helper.PermissionHelper
import com.hour.hour.redux.ViewStore
import com.hour.hour.ui.MainScreen
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function

@Suppress("ConstantConditionIf")
class MainActivity : AppCompatActivity() {
    private val mSubscriptions = CompositeDisposable()
    private var mRouter: Router? = null
    private var mStoreRetained: Boolean = false
    private var mBinder: MyService.MyBinder? = null

    private val con = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            mBinder = p1 as? MyService.MyBinder
        }
    }

    //region Lifecycle
    //---------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!mStoreRetained) {
            mStoreRetained = true
            MainApplication.retainStore(this)
        }

        startService(Intent(this, MyService::class.java))

        val layout = FrameLayout(this)
        setContentView(layout)
        mRouter = Conductor.attachRouter(this, layout, savedInstanceState)
        if (!PermissionHelper.hasAppUsagePermission(this) || !store().view.state.agreeTermsConditions) {
            mRouter?.setRoot(RouterTransaction.with(StartScreen()))
        } else {
            mRouter?.setRoot(RouterTransaction.with(MainScreen()))
        }
    }

    override fun onStart() {
        // NOTE: We need to get store ready before super.onStart(),
        //       otherwise Conductor will re-create our view and cause NPE upon using store
        if (!mStoreRetained) {
            mStoreRetained = true
            MainApplication.retainStore(this)
        }
        val l = NotTrackingListHelper.loadNotTrackingList(this)
        store().dispatch(ViewStore.Action.SetNotTrackingList(l))

        bindService(Intent(this, MyService::class.java), con, 0)

        mSubscriptions.add(
            store().observe(store().view)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mapReminder)
                .distinctUntilChanged()
                .subscribe(consumeReminder))
        mSubscriptions.add(
            store().observe(store().view)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mapStrictMode)
                .distinctUntilChanged()
                .subscribe(consumeStrictMode))
        mSubscriptions.add(
            store().observe(store().view)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mapUsageLimit)
                .distinctUntilChanged()
                .subscribe(consumeUsageLimit))
        mSubscriptions.add(
            store().observe(store().view)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mapForeground)
                .distinctUntilChanged()
                .subscribe(consumeForeground))
        mSubscriptions.add(
            store().observe(store().view)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mapNotTracking)
                .distinctUntilChanged()
                .subscribe(consumeNotTracking))

        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        mSubscriptions.clear()
        try {
            unbindService(con)
        } catch (_: Exception) { }
    }

    override fun onStop() {
        super.onStop()
        if (mStoreRetained) {
            mStoreRetained = false
            MainApplication.releaseStore(this)
        }
    }
    //---------------------------------------------------------------
    //endregion

    //region UI Events
    //---------------------------------------------------------------
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mRouter?.handleBack() != true) {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        }
    }
    //---------------------------------------------------------------
    //endregion

    //region redux
    private val mapReminder = Function<ViewStore.State, Boolean> { state ->
        state.isReminderOn
    }
    private val consumeReminder = Consumer<Boolean> { isChecked ->
        mBinder?.service?.get()?.isReminderOn = isChecked
    }
    private val mapStrictMode = Function<ViewStore.State, Boolean> { state ->
        state.isStrictModeOn
    }
    private val consumeStrictMode = Consumer<Boolean> { isChecked ->
        mBinder?.service?.get()?.isStrictModeOn = isChecked
    }
    private val mapUsageLimit = Function<ViewStore.State, Int> { state ->
        state.usageLimit
    }
    private val consumeUsageLimit = Consumer<Int> { limit ->
        mBinder?.service?.get()?.usageLimit = limit
    }
    private val mapForeground = Function<ViewStore.State, Boolean> { state ->
        state.isForegroundOn
    }
    private val consumeForeground = Consumer<Boolean> { on ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return@Consumer

        if (on) {
            mBinder?.service?.get()?.startForeground()
        } else {
            mBinder?.service?.get()?.stopForeground()
        }
    }
    private val mapNotTracking = Function<ViewStore.State, Int> { state ->
        state.notTrackingList.size
    }
    private val consumeNotTracking = Consumer<Int> { 
        mBinder?.service?.get()?.loadNotTrackingList()
        store().dispatch(ViewStore.Action.UpdateNotTrackingListComplete())
    }
    //endregion
}
