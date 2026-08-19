package com.example.app

import android.app.Application
import timber.log.Timber

class LinkXApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
        
        Timber.i("LinkX Remote Pro Android Client starting up...")

        // Setup global uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Timber.e(exception, "A fatal unhandled exception occurred on thread ${thread.name}")
            // Log to disk or crashlytics here if needed
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}
