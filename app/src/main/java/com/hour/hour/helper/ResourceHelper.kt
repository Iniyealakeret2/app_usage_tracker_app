package com.hour.hour.helper

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import android.util.SparseArray
import android.util.TypedValue
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

@Suppress("unused")
object ResourceHelper {
    private var context: WeakReference<Context>? = null
    fun setup(c: Context) {
        context = WeakReference(c)
    }
    fun dp(value: Float): Int {
        val c = context?.get() ?: return 0
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, c.resources.displayMetrics).roundToInt()
    }
    fun dp(value: Int): Int = dp(value.toFloat())
    fun dimen(@DimenRes resId: Int): Int {
        return context?.get()?.resources?.getDimensionPixelOffset(resId) ?: 0
    }
    fun color(@ColorRes resId: Int): Int {
        val c = context?.get() ?: return 0
        return ContextCompat.getColor(c, resId)
    }

    private var sFontCache: SparseArray<Typeface> = SparseArray()
    fun font(@FontRes resId: Int): Typeface? {
        return if (sFontCache.indexOfKey(resId) >= 0) {
            sFontCache.get(resId)
        } else {
            val con = context?.get()
            val cache = if (con != null) ResourcesCompat.getFont(con, resId) else null
            if (cache != null) sFontCache.put(resId, cache)
            cache
        }
    }
}
