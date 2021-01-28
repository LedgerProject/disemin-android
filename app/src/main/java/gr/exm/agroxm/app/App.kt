package gr.exm.agroxm.app

import android.app.Application
import gr.exm.agroxm.BuildConfig
import gr.exm.agroxm.data.io.ApiService
import gr.exm.agroxm.data.AuthHelper
import gr.exm.agroxm.util.ResourcesHelper
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Setup debug logs
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Init helpers
        // TODO Replace with DI in the future
        AuthHelper.init(this)
        ResourcesHelper.init(this)

        // Init Api Service
        ApiService.init(this)
    }
}