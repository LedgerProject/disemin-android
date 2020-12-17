package gr.exm.agroxm.util

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.res.ResourcesCompat

object ResourcesHelper {

    private lateinit var resources: Resources

    fun init(context: Context) {
        resources = context.resources
    }

    @ColorInt
    fun getColor(@ColorRes colorResId: Int): Int {
        return ResourcesCompat.getColor(resources, colorResId, null)
    }

    fun getDimension(@DimenRes dimenResId: Int): Float {
        return resources.getDimension(dimenResId)
    }
}