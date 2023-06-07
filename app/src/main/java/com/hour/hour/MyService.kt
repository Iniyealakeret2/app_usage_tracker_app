package com.hour.hour

import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.hour.hour.client.UpdateServerTask
import com.hour.hour.helper.*
import com.hour.hour.helper.UsageStatsHelper.queryTodayUsage
import com.hour.hour.model.UsageRecord
import com.hour.hour.redux.ViewStore
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/*
 * different method to handle this class for Android version < 26 and >= 26
 *
 * Android version < 26
 *      START_STICKY & restart using pending intent after service is killed
 *
 * Android version >= 26
 *      register for foreground service
 *      live-time register for Screen Unlock Receiver
 */

class MyService : Service() {
    class MyBinder(myService: MyService) : Binder() {
        val service = WeakReference(myService)
    }

    private val binder = MyBinder(this)
    private var mLastQueryTime: Long = 0
    private var mTimer: Timer? = null
    private var mTodayUsages = HashMap<String, Long>()
    private var mNotTrackingList: List<String>? = null
    private var mToday: String = ""
    var isReminderOn: Boolean = false
    var isStrictModeOn: Boolean = false
    var usageLimit: Int = 30

    //region Life cycle
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground()
            registerScreenUnlockReceiver()
        } else {
            try {
                if (getSharedPreferences("redux", Context.MODE_PRIVATE).getString("view", "")?.let { JSONObject(it).getBoolean("isForegroundOn") } == true) {
                    startForeground()
                }
            } catch (_: Exception) {
            }
        }

        loadRedux()
        loadUsages()
        loadNotTrackingList()

        startForegroundListener()
        Logger.d("MyService", "onCreate")
    }

    override fun onDestroy() {
        Logger.d("MyService", "onDestroy")
        mTimer?.cancel()
        sendBroadcast(Intent(this, ServiceEndReceiver::class.java))
        try {
            unregisterReceiver(ScreenUnlockReceiver())
        } catch (_: Exception) {
        }

        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder {
        Logger.d("MyService", "onBind")
        return binder
    }

    override fun onRebind(intent: Intent?) {
        Logger.d("MyService", "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.d("MyService", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onTaskRemoved(rootIntent: Intent?) {
        Logger.d("MyService", "onTaskRemoved")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val restartService = Intent(applicationContext,
                    this.javaClass)
            restartService.`package` = packageName
            val restartServicePI = PendingIntent.getService(
                    applicationContext, 1, restartService,
                    PendingIntent.FLAG_ONE_SHOT)
            val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3000, restartServicePI)
        }
        super.onTaskRemoved(rootIntent)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun startForeground() {
        val channelId = "default"
        val notificationId = 1 // Choose any random notification ID
        // Create a PendingIntent for the MainActivity
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                FLAG_UPDATE_CURRENT
            )
        }
        // Create a notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        // Build the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Tracking App Usage")
            .setSmallIcon(R.drawable.ic_timeline)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()

        // Start the service as a foreground service with the notification
        startForeground(notificationId, notification)


    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun stopForeground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    private fun registerScreenUnlockReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.USER_PRESENT")
        registerReceiver(ScreenUnlockReceiver(), intentFilter)
    }
    // endregion

    // Events
    private fun startForegroundListener() {
        val timer = Timer(true)
        var lastForegroundPackageName: String? = null
        val usageStatsManager = getSystemService(AppCompatActivity.USAGE_STATS_SERVICE) as UsageStatsManager

        mTimer = timer
        mLastQueryTime = getSharedPreferences("MyService", Context.MODE_PRIVATE).getLong("mLastQueryTime", 1000L)

        val monitoringTask = object : TimerTask() {
            val lock = ReentrantLock()
            override fun run() {
                if (lock.tryLock()) {
                    val result = UsageStatsHelper.getLatestEvent(usageStatsManager, mLastQueryTime, System.currentTimeMillis())
                    val foregroundEvent = result.foregroundPackageName

                    if (result.lastEndTime != 0L) {
                        mLastQueryTime = result.lastEndTime
                        val prefEdit = getSharedPreferences("MyService", Context.MODE_PRIVATE).edit()
                        prefEdit.putLong("mLastQueryTime", mLastQueryTime)
                        prefEdit.apply()
                    }

                    addUsages(result.records)
                    if (foregroundEvent != null && !foregroundEvent.contains("hour")) {
                        if (lastForegroundPackageName != foregroundEvent) {
                            onAppSwitch(foregroundEvent)
                        }
                        lastForegroundPackageName = foregroundEvent
                    }
                }
                lock.unlock()
            }
        }
        val updateServerTask = UpdateServerTask(this)
        timer.schedule(monitoringTask, 0, AppConfig.TIMER_CHECK_PERIOD)
        timer.schedule(updateServerTask, 30000, AppConfig.TIMER_UPDATE_SERVER_PERIOD)
    }

    private fun onAppSwitch(packageName: String) {
        val t = mTodayUsages[packageName] ?: 0
        Logger.d("onAppSwitch", "$packageName - usage: ${t
                / 60000}min  limit: $usageLimit  In NotTrackingList: ${mNotTrackingList?.contains(packageName)}")

        if (t >= usageLimit * 60000 && mNotTrackingList?.contains(packageName) == false) {
            if (isReminderOn) {
                // TODO: add this app into exception
                if (!isStrictModeOn) {
                    NotificationHelper.show(
                            this,
                            "${PackageHelper.getAppName(this, packageName)} - ${CalendarHelper.toReadableDuration(t)}",
                                    "Why not take a break?",
//                            packageName.hashCode()
                            1
                    )
                } else {
                    val intent = Intent(this, BlockerActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }

    private fun loadRedux() {
        val pref = getSharedPreferences("redux", Context.MODE_PRIVATE)
        val store = try {
            ViewStore.load(pref.getString("view", "")?.let { JSONObject(it) }) ?: return
        } catch (e: Exception) {
            Logger.e("loadRedux", "${e.message}, use default value")
            ViewStore.State()
        }
        isReminderOn = store.isReminderOn
        isStrictModeOn = store.isStrictModeOn
        usageLimit = store.usageLimit
    }

    fun loadNotTrackingList() {
        mNotTrackingList = NotTrackingListHelper.loadNotTrackingList(this)
    }

    private fun loadUsages() {
        val records = queryTodayUsage()
        mTodayUsages = HashMap()
        mToday = CalendarHelper.getDate(System.currentTimeMillis())
        for (r in records) {
            mTodayUsages[r.packageName] = (mTodayUsages[r.packageName] ?: 0) + r.duration
        }
    }

    private fun addUsages(records: List<UsageRecord>) {
        if (records.isNotEmpty() && CalendarHelper.getDate(records.first().starTime) != mToday) {
            loadUsages()
        }
        for (r in records) {
            mTodayUsages[r.packageName] = (mTodayUsages[r.packageName] ?: 0) + r.duration
        }
    }
    //endregion
}
