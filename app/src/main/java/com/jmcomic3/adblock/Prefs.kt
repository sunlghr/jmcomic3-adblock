package com.jmcomic3.adblock

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import io.github.libxposed.api.XposedModule
import org.json.JSONObject

/** Switch state shared by the Miuix UI and the process running JMComic3. */
data class BlockConfig(
    val enabled: Boolean = true,
    val blockHome: Boolean = true,
    val blockPages: Boolean = true,
    val blockMedia: Boolean = true,
    val verbose: Boolean = false,
)

object Prefs {

    const val NAME = "settings"

    const val ENABLED = "enabled"
    const val BLOCK_HOME = "blockHome"
    const val BLOCK_PAGES = "blockPages"
    const val BLOCK_MEDIA = "blockMedia"
    const val VERBOSE = "verbose"

    /**
     * The module's own process can always open this file. Android 17 rejects
     * MODE_WORLD_READABLE, so cross-process reads go through ConfigProvider instead.
     */
    @Suppress("DEPRECATION")
    fun open(context: Context): SharedPreferences {
        return try {
            context.getSharedPreferences(NAME, Context.MODE_WORLD_READABLE)
        } catch (_: SecurityException) {
            context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        }
    }

    fun load(context: Context): BlockConfig = from(open(context))

    fun load(module: XposedModule): BlockConfig {
        val prefs = runCatching { module.getRemotePreferences(NAME) }.getOrNull()
        return from(prefs)
    }

    /**
     * Provider first: it reads the module's real file regardless of sandbox permissions.
     * Remote preferences remain a fallback for older LSPosed builds / provider failures.
     */
    fun load(module: XposedModule, context: Context?): BlockConfig {
        if (context != null) {
            loadFromProvider(context)?.let { return it }
        }
        return load(module)
    }

    private fun loadFromProvider(context: Context): BlockConfig? {
        return try {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    ConfigProvider.URI,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            val result = context.contentResolver.call(
                ConfigProvider.URI,
                ConfigProvider.METHOD_GET,
                null,
                null,
            )
            Log.i("JMAdBlock", "config provider result=$result")
            if (result == null) return null
            val json = result.getString(ConfigProvider.KEY_JSON)
            Log.i("JMAdBlock", "config provider json=$json")
            if (json == null) return null
            fromJson(JSONObject(json))
        } catch (t: Throwable) {
            Log.w("JMAdBlock", "config provider failed: ${t.javaClass.name}: ${t.message}", t)
            null
        }
    }

    private fun from(prefs: SharedPreferences?): BlockConfig {
        if (prefs == null) return BlockConfig()
        return BlockConfig(
            enabled = prefs.getBoolean(ENABLED, true),
            blockHome = prefs.getBoolean(BLOCK_HOME, true),
            blockPages = prefs.getBoolean(BLOCK_PAGES, true),
            blockMedia = prefs.getBoolean(BLOCK_MEDIA, true),
            verbose = prefs.getBoolean(VERBOSE, false),
        )
    }

    fun save(context: Context, config: BlockConfig) {
        open(context).edit()
            .putBoolean(ENABLED, config.enabled)
            .putBoolean(BLOCK_HOME, config.blockHome)
            .putBoolean(BLOCK_PAGES, config.blockPages)
            .putBoolean(BLOCK_MEDIA, config.blockMedia)
            .putBoolean(VERBOSE, config.verbose)
            .commit()
        ensureTargetGrant(context)
    }

    /**
     * Package visibility on Android 11+ hides the module's provider authority from
     * JMComic3. A URI grant lets the target resolve the exported provider without
     * declaring a <queries> entry in its own manifest.
     */
    fun ensureTargetGrant(context: Context) {
        val result = runCatching {
            context.grantUriPermission(
                "com.JMComic3.app",
                ConfigProvider.URI,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
            )
        }
        Log.i("JMAdBlock", "target grant result=$result")
    }

    fun toJson(config: BlockConfig): String = JSONObject().apply {
        put(ENABLED, config.enabled)
        put(BLOCK_HOME, config.blockHome)
        put(BLOCK_PAGES, config.blockPages)
        put(BLOCK_MEDIA, config.blockMedia)
        put(VERBOSE, config.verbose)
    }.toString()

    private fun fromJson(json: JSONObject): BlockConfig = BlockConfig(
        enabled = json.optBoolean(ENABLED, true),
        blockHome = json.optBoolean(BLOCK_HOME, true),
        blockPages = json.optBoolean(BLOCK_PAGES, true),
        blockMedia = json.optBoolean(BLOCK_MEDIA, true),
        verbose = json.optBoolean(VERBOSE, false),
    )
}
