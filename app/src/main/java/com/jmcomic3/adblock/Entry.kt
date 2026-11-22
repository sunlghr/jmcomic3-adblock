package com.jmcomic3.adblock

import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/** libxposed API 102 entry point, loaded from META-INF/xposed/java_init.list. */
class Entry : XposedModule() {

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        super.onModuleLoaded(param)
        instance = this
        log(
            Log.INFO,
            TAG,
            "module loaded, api=${getApiVersion()}, framework=${getFrameworkName()} ${getFrameworkVersion()}",
        )
    }

    override fun onPackageReady(param: PackageReadyParam) {
        super.onPackageReady(param)
        if (param.packageName != TARGET_PACKAGE) return

        val processName = param.applicationInfo?.processName
        if (processName != null && processName != TARGET_PACKAGE) return

        instance = this
        log(Log.INFO, TAG, "target ready: ${param.packageName}, first=${param.isFirstPackage}")
        try {
            AdBlockHook.install(this, param.classLoader)
        } catch (t: Throwable) {
            log(Log.ERROR, TAG, "failed to install WebView hook", t)
        }
    }

    companion object {
        const val TAG = "JMAdBlock"
        const val TARGET_PACKAGE = "com.JMComic3.app"

        @Volatile
        var instance: Entry? = null
            private set
    }
}
