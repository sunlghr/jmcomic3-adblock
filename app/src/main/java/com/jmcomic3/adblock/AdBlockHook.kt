package com.jmcomic3.adblock

import android.content.Context
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Capacitor asks androidx.webkit to install its JS injector during
 * Bridge.loadWebView(), immediately before loadUrl(). Appending to that same
 * document-start script is the only reliable way to clear ad caches before the
 * React app reads localStorage, and to wrap fetch before HttpUtil issues ads.
 */
object AdBlockHook {

    private val installed = AtomicBoolean(false)

    fun install(module: XposedModule, classLoader: ClassLoader) {
        if (!installed.compareAndSet(false, true)) return
        try {
            val webViewClass = Class.forName("android.webkit.WebView", false, classLoader)
            val compatClass = Class.forName("androidx.webkit.WebViewCompat", false, classLoader)
            val method = compatClass.getDeclaredMethod(
                "addDocumentStartJavaScript",
                webViewClass,
                String::class.java,
                Set::class.java,
            )

            module.hook(method)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept { chain ->
                    val original = chain.args.getOrNull(1) as? String
                    if (original == null || original.contains(AdBlockScript.MARKER)) {
                        module.log(Log.INFO, Entry.TAG, "document-start hook skipped: null or already injected")
                        return@intercept chain.proceed()
                    }

                    val context = currentApplication()
                    val config = Prefs.load(module, context)
                    val injection = if (config.enabled) {
                        AdBlockScript.build(config)
                    } else {
                        AdBlockScript.cleanup()
                    }
                    val args = chain.args.toMutableList()
                    args[1] = original + "\n;" + injection + "\n"
                    module.log(
                        Log.INFO,
                        Entry.TAG,
                        "inject into document-start: original=${original.length} chars, " +
                            "context=${context?.packageName}, enabled=${config.enabled}, " +
                            "home=${config.blockHome}, pages=${config.blockPages}, " +
                            "media=${config.blockMedia}",
                    )
                    chain.proceed(args.toTypedArray())
                }

            module.log(Log.INFO, Entry.TAG, "hooked WebViewCompat.addDocumentStartJavaScript")
        } catch (t: Throwable) {
            installed.set(false)
            throw t
        }
    }

    /**
     * The hook runs inside the JMComic3 process while its Activity is being created, so
     * ActivityThread.currentApplication() is already available.
     */
    private fun currentApplication(): Context? = runCatching {
        val activityThread = Class.forName("android.app.ActivityThread")
        activityThread.getMethod("currentApplication").invoke(null) as? Context
    }.getOrNull()
}
