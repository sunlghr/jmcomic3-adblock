package com.jmcomic3.adblock

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

/**
 * Read-only bridge for the hooked JMComic3 process.
 *
 * API 102 remote preferences are unreliable on Android 17 because the settings file is
 * created with 0660 permissions and is not readable across the app sandbox. This provider
 * exposes only the five booleans already shown in the Miuix UI, never app data.
 */
class ConfigProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method != METHOD_GET) return null
        val ctx: Context = context ?: return null
        return Bundle().apply {
            putString(KEY_JSON, Prefs.toJson(Prefs.load(ctx)))
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        const val AUTHORITY = "com.jmcomic3.adblock.config"
        val URI: Uri = Uri.parse("content://$AUTHORITY/config")
        const val METHOD_GET = "get"
        const val KEY_JSON = "json"
    }
}
