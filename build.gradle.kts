plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20" apply false
    id("org.jetbrains.compose") version "1.10.3" apply false
}

tasks.register("buildModule") {
    group = "build"
    description = "Builds the unsigned release APK for the JMComic3 ad-block LSPosed module."
    dependsOn(":app:assembleRelease")
}
