package com.f1watch.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.f1watch.data.local.PreferencesManager
import com.f1watch.data.repository.F1Repository
import com.f1watch.widget.F1CompactWidgetReceiver
import com.f1watch.widget.F1ScheduleWidgetReceiver
import java.util.concurrent.TimeUnit

class F1SyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = F1Repository(appContext)
            val prefs = PreferencesManager(appContext)

            // Force refresh schedule
            val races = repository.getRaces(forceRefresh = true)
            val next = repository.getNextOrCurrentRace()

            // Update standings
            repository.getDriverStandings(forceRefresh = true)
            repository.getConstructorStandings(forceRefresh = true)

            // Update timestamp
            prefs.updateLastSyncTime()

            // Trigger Desktop Widget Refresh
            F1CompactWidgetReceiver.updateAll(appContext)
            F1ScheduleWidgetReceiver.updateAll(appContext)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "f1_schedule_sync_worker"

        fun schedulePeriodicSync(context: Context, intervalHours: Long = 48) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<F1SyncWorker>(intervalHours, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
