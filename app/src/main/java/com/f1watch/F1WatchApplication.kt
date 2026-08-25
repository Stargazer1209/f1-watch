package com.f1watch

import android.app.Application
import com.f1watch.worker.F1SyncWorker

class F1WatchApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Schedule background periodic sync for F1 schedule and widgets
        F1SyncWorker.schedulePeriodicSync(this)
    }
}
