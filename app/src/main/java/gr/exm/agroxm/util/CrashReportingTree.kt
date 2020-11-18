package gr.exm.agroxm.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        // Only log errors and warnings
        if (priority == Log.ERROR || priority == Log.WARN) {
            // Log message
            FirebaseCrashlytics.getInstance().log(message)

            // Log exception stacktrace
            if (throwable != null) {
                FirebaseCrashlytics.getInstance().recordException(throwable)
            }
        }
    }

    init {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }
}