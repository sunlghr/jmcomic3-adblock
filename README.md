# JMComic3 去广告

JM 广告净化是一个基于 Kotlin 的 LSPosed 模块，面向 `com.JMComic3.app` 2.0.14。设置页使用 [Miuix](https://github.com/compose-miuix-ui/miuix)。当前源码版本为 **1.1.3**（versionCode 14）。本仓库只包含模块源码，不分发目标应用。

## 功能

- 屏蔽开屏广告，以及首页、详情页和阅读器中的广告位。
- 隐藏首页顶部原本为广告预留的空白区域。
- 设置页提供总开关和不同场景的开关。更改设置后，建议强制停止并重新启动目标应用。

原项目曾在 OnePlus 13T、Android 17 和 LSPosed 2.2.1（API 102）上验证。其他应用版本或 ROM 不保证兼容。

## 构建

准备 JDK 17、Gradle 9.9.2 和 Android SDK（平台 API 37，Build Tools 37.0.0），设置 `JAVA_HOME`、`ANDROID_HOME`，并让 `gradle` 可从命令行调用。也可以使用 Android Studio 打开项目，由 IDE 配置 SDK 路径。

```sh
gradle :app:assembleDebug
```

输出为 `app/build/outputs/apk/debug/app-debug.apk`，使用 Android 默认调试签名。`gradle :app:assembleRelease` 生成未签名的 Release APK，需要使用自己的密钥签名后才能安装。若设备上已有不同签名的旧版模块，不能直接覆盖安装。

## 启用

1. 在已安装兼容 LSPosed 的设备上安装自行构建的模块 APK。
2. 在 LSPosed 管理器中启用“JM 广告净化”，确认作用域为 `com.JMComic3.app`。
3. 完全退出并重新打开目标应用；必要时重启设备。

在部分 ColorOS 版本上，重启后可能需要先打开一次模块设置页，再启动目标应用，才能同步修改后的设置。

## 源码结构

- `Entry.kt`、`AdBlockHook.kt`：模块入口与目标应用 WebView 中的广告拦截入口。
- `AdBlockScript.kt`：页面侧广告过滤逻辑。
- `Prefs.kt`、`ConfigProvider.kt`：保存模块设置并向目标进程提供配置。
- `SettingsActivity.kt`、`SettingsGlyphs.kt`：模块设置页和图标。
