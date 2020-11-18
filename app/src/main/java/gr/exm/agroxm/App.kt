package gr.exm.agroxm

import android.app.Application
import gr.exm.agroxm.util.CrashReportingTree
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Setup debug logs
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Setup crash reporting
        Timber.plant(CrashReportingTree())
    }

}