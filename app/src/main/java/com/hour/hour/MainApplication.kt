package com.hour.hour

import android.app.Application
import android.content.Context
import com.hour.hour.helper.GraphHelper
import com.hour.hour.helper.ResourceHelper
import com.hour.hour.helper.UsageStatsHelper
import com.hour.hour.redux.AppStore

class MainApplication : Application() {
    //region Lifecycle
    //---------------------------------------------------------------
    override fun onCreate() {
        super.onCreate()
        ResourceHelper.setup(applicationContext)
        UsageStatsHelper.setup(applicationContext)
        GraphHelper.setup(applicationContext)
    }
    //---------------------------------------------------------------
    //endregion

    //region Redux Store
    //---------------------------------------------------------------
    companion object {
        private var sStoreRef: Int = 0
        private var sStore: AppStore? = null
        fun retainStore(context: Context) {
            if (sStoreRef == 0) {
                sStore = AppStore()
                sStore?.load(context)
            }
            sStoreRef++
        }

        fun releaseStore(context: Context) {
            sStoreRef--
            if (sStoreRef == 0) {
                sStore?.save(context)
                sStore = null
            }
        }

        fun store(): AppStore {
            return sStore!!
        }
    }
    //---------------------------------------------------------------
    //endregion
}
