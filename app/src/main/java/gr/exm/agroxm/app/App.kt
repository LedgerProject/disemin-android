package gr.exm.agroxm.app

import android.app.Application
import gr.exm.agroxm.BuildConfig
import gr.exm.agroxm.data.modules
import gr.exm.agroxm.util.ResourcesHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Setup DI
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@App)
            modules(modules)
        }

        // Setup debug logs
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Init helpers
        // TODO Replace with DI in the future
        ResourcesHelper.init(this)
    }
}