package com.jmcomic3.adblock

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log

/**
 * Persisted JobScheduler job. Unlike a manifest BOOT_COMPLETED receiver, a persisted job
 * is restored by the system after reboot, including on ROMs that restrict autostart.
 * Running it starts the module process, opens the private prefs file, and refreshes the
 * URI grant so the target can call ConfigProvider before the settings UI is opened.
 */
class ConfigJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        val result = runCatching { Prefs.ensureTargetGrant(this) }
        Prefs.open(this).getBoolean(Prefs.ENABLED, true)
        appendLog("job ran, grant=$result")
        Log.i("JMAdBlock", "config job ran, grant=$result")
        jobFinished(params, false)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean = false

    private fun appendLog(line: String) {
        runCatching {
            openFileOutput("config_job.log", Context.MODE_APPEND).use {
                it.write("${System.currentTimeMillis()} $line\n".toByteArray())
            }
        }
    }
}

object ConfigJobScheduler {

    private const val JOB_ID = 0x4a4d

    fun schedule(context: Context) {
        val scheduler = context.getSystemService(JobScheduler::class.java) ?: return
        if (scheduler.getPendingJob(JOB_ID) != null) return
        val component = ComponentName(context, ConfigJobService::class.java)
        val job = JobInfo.Builder(JOB_ID, component)
            .setPersisted(true)
            .setMinimumLatency(1_000L)
            .setOverrideDeadline(5_000L)
            .build()
        val result = scheduler.schedule(job)
        Log.i("JMAdBlock", "config job schedule=$result")
    }
}
