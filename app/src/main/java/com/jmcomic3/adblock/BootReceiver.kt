package com.jmcomic3.adblock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * URI grants are not persisted across reboots. Recreate the read grant so JMComic3 can
 * call ConfigProvider before the user opens the settings screen again.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i("JMAdBlock", "boot grant receiver: ${intent?.action}")
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_USER_UNLOCKED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> Prefs.ensureTargetGrant(context)
        }
        ConfigJobScheduler.schedule(context)
    }
}
