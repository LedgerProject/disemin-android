package gr.exm.agroxm.util

import android.content.Context
import android.content.SharedPreferences

object Settings {

    private const val NAME = "settings"
    private const val MODE = Context.MODE_PRIVATE

    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }
}