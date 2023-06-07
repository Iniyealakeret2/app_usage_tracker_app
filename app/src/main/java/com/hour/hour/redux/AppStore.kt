package com.hour.hour.redux

import android.content.Context
import com.hour.hour.helper.Logger
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import org.json.JSONObject
import redux.api.Store

@Suppress("unused", "UNUSED_PARAMETER")
class AppStore {
    var view = ViewStore.createStore()

    //region Pesistence
    fun load(context: Context) {

        val pref = context.getSharedPreferences("redux", Context.MODE_PRIVATE)
        try {
            ViewStore.load(JSONObject(pref.getString("view", "")))?.let { dispatch(ViewStore.Action._PresistenceRestore(it)) }
        } catch (e: Exception) {
            Logger.e("redux", "persistence load viewState: ${e.message} - ${e.localizedMessage}")
        }
    }

    fun save(context: Context) {
        val pref = context.getSharedPreferences("redux", Context.MODE_PRIVATE).edit()
        try {
            pref.putString("view", ViewStore.save(view.state).toString())
        } catch (e: Exception) {
            Logger.e("redux", "persistence load viewState: ${e.message} - ${e.localizedMessage}")
        }
        pref.apply()
    }
    //endregion

    //region Redux glue code
    fun dispatch(action: Any) {
        when (action) {
            is ViewStore.Action -> view.dispatch(action)
        }
    }

    fun <S> observe(store: Store<S>): Observable<S> {
        return Observable.create(object : ObservableOnSubscribe<S> {
            private var mSubscribe: Store.Subscription? = null
            @Throws(Exception::class)
            override fun subscribe(emitter: ObservableEmitter<S>) {
                mSubscribe = store.subscribe({ emitter.onNext(store.state) })
                emitter.setCancellable {
                    mSubscribe?.let {
                        it.unsubscribe()
                        mSubscribe = null
                    }
                }
                // Sync initial state
                emitter.onNext(store.state)
            }
        })
    }
    //endregion
}
