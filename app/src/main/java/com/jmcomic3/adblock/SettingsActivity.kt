package com.jmcomic3.adblock

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

private const val TARGET_PACKAGE = "com.JMComic3.app"
private const val MODULE_VERSION = "1.1.3"
private const val LSPOSED_PACKAGE = "org.lsposed.manager"
private const val LSPOSED_MAIN = "org.lsposed.manager.ui.activity.MainActivity"

private val ToneBlue = Color(0xFF3482FF)
private val ToneGreen = Color(0xFF22C55E)
private val ToneOrange = Color(0xFFFF6900)
private val TonePurple = Color(0xFF8B5CF6)
private val ToneTeal = Color(0xFF14B8A6)
private val ToneGrey = Color(0xFF8E8E93)

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Prefs.ensureTargetGrant(this)
        ConfigJobScheduler.schedule(this)
        setContent {
            val dark = isSystemInDarkTheme()
            val colors = if (dark) {
                darkColorScheme(
                    primary = Color(0xFF4C9BFF),
                    primaryVariant = Color(0xFF3482FF),
                    background = Color(0xFF0B0B0D),
                    surface = Color(0xFF0B0B0D),
                    surfaceContainer = Color(0xFF1C1C1E),
                    onBackground = Color(0xFFF4F4F5),
                    onSurface = Color(0xFFF4F4F5),
                    onSurfaceVariantSummary = Color(0xFFA6A6AD),
                    dividerLine = Color(0xFF2C2C2E),
                    secondaryVariant = Color(0xFF2C2C2E),
                    onSecondaryVariant = Color(0xFFF4F4F5),
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF3482FF),
                    primaryVariant = Color(0xFF2B6FE0),
                    background = Color(0xFFF5F5F7),
                    surface = Color(0xFFF5F5F7),
                    surfaceContainer = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF191919),
                    onSurface = Color(0xFF191919),
                    onSurfaceVariantSummary = Color(0xFF7A7A80),
                    dividerLine = Color(0xFFE8E8ED),
                    secondaryVariant = Color(0xFFFFFFFF),
                    onSecondaryVariant = Color(0xFF191919),
                )
            }
            MiuixTheme(colors = colors) {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    val initial = remember { Prefs.load(context) }
    var state by remember { mutableStateOf(initial) }
    val targetVersion = remember { targetVersionName(context) }
    val scrollBehavior = MiuixScrollBehavior()

    fun update(next: BlockConfig) {
        state = next
        Prefs.save(context, next)
        Toast.makeText(context, "已保存，强制停止 JMComic3 后生效", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = MiuixTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = "广告拦截",
                subtitle = "JMComic3 $targetVersion · LSPosed",
                scrollBehavior = scrollBehavior,
                color = MiuixTheme.colorScheme.background,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            StatusCard(state) { update(state.copy(enabled = it)) }

            SectionTitle("拦截范围")
            SettingsCard {
                ToggleRow(
                    glyph = SettingsGlyph.Home,
                    tone = ToneBlue,
                    title = "首页信息流",
                    summary = "接管 ad_content_all，清空推荐位与首页广告数据",
                    checked = state.blockHome,
                    enabled = state.enabled,
                    onCheckedChange = { update(state.copy(blockHome = it)) },
                )
                ItemDivider()
                ToggleRow(
                    glyph = SettingsGlyph.Layers,
                    tone = TonePurple,
                    title = "开屏 / 封面 / 详情页",
                    summary = "接管 advertise_all，返回空数据壳，避免页面读取广告字段时出错",
                    checked = state.blockPages,
                    enabled = state.enabled,
                    onCheckedChange = { update(state.copy(blockPages = it)) },
                )
                ItemDivider()
                ToggleRow(
                    glyph = SettingsGlyph.Play,
                    tone = ToneGreen,
                    title = "影视 / 游戏横幅",
                    summary = "接管旧版 advertise、ad_content 接口中的 banner 数据",
                    checked = state.blockMedia,
                    enabled = state.enabled,
                    onCheckedChange = { update(state.copy(blockMedia = it)) },
                )
            }

            SectionTitle("工具")
            SettingsCard {
                ActionRow(
                    glyph = SettingsGlyph.Update,
                    tone = ToneOrange,
                    title = "应用信息（强制停止）",
                    summary = "修改开关后在这里停止 JMComic3，再重新打开",
                    onClick = { openAppInfo(context) },
                )
                ItemDivider()
                ActionRow(
                    glyph = SettingsGlyph.Shield,
                    tone = ToneBlue,
                    title = "打开 LSPosed",
                    summary = "确认模块已启用，并勾选 com.JMComic3.app 作用域",
                    onClick = { openLsposed(context) },
                )
                ItemDivider()
                ActionRow(
                    glyph = SettingsGlyph.File,
                    tone = ToneTeal,
                    title = "复制诊断信息",
                    summary = "模块版本、目标版本与当前拦截开关",
                    onClick = { copyDiagnostics(context, state, targetVersion) },
                )
            }

            SectionTitle("说明")
            Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
                Text(
                    text = "模块使用 document-start 脚本在页面加载前清空广告缓存，并把四个广告接口替换成空的 AES 响应，不依赖删除 iframe。\n\n首次使用：在 LSPosed 中启用“JM 广告净化”，勾选 com.JMComic3.app，然后强制停止 JMComic3 再打开。若页面出现异常，请关闭总开关并重启目标应用。",
                    modifier = Modifier.padding(18.dp),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "JM 广告净化 $MODULE_VERSION · Kotlin + Miuix",
                modifier = Modifier.fillMaxWidth(),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun StatusCard(state: BlockConfig, onEnabledChange: (Boolean) -> Unit) {
    val accent = if (state.enabled) ToneBlue else ToneGrey
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlyphBadge(
                    glyph = SettingsGlyph.Shield,
                    tint = accent,
                    badgeSize = 48.dp,
                    iconSize = 27.dp,
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.enabled) "广告拦截已开启" else "广告拦截已关闭",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state.enabled) {
                            "下次启动 JMComic3 时接管广告接口"
                        } else {
                            "JMComic3 将恢复原始广告请求"
                        },
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        fontSize = 13.sp,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(checked = state.enabled, onCheckedChange = onEnabledChange)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MiuixTheme.colorScheme.dividerLine)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BoxDot(if (state.enabled) ToneGreen else ToneGrey)
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "LSPosed API 102 · 作用域 com.JMComic3.app",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun BoxDot(color: Color) {
    Spacer(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, top = 18.dp, bottom = 8.dp),
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
        Column(content = content)
    }
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp, end = 16.dp),
        color = MiuixTheme.colorScheme.dividerLine,
    )
}

@Composable
private fun ToggleRow(
    glyph: SettingsGlyph,
    tone: Color,
    title: String,
    summary: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphBadge(glyph = glyph, tint = tone)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MiuixTheme.colorScheme.onSurface
                } else {
                    MiuixTheme.colorScheme.disabledOnSurface
                },
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = summary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun ActionRow(
    glyph: SettingsGlyph,
    tone: Color,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphBadge(glyph = glyph, tint = tone)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = summary,
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = MiuixIcons.Basic.ArrowRight,
            contentDescription = null,
            modifier = Modifier.width(9.dp).height(15.dp),
            tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
        )
    }
}

private fun targetVersionName(context: Context): String {
    return runCatching {
        context.packageManager.getPackageInfo(TARGET_PACKAGE, 0).versionName
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: "未安装"
}

private fun openAppInfo(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$TARGET_PACKAGE")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "无法打开应用信息", Toast.LENGTH_SHORT).show() }
}

private fun openLsposed(context: Context) {
    val direct = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        component = ComponentName(LSPOSED_PACKAGE, LSPOSED_MAIN)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (runCatching { context.startActivity(direct) }.isSuccess) return

    val launch = context.packageManager.getLaunchIntentForPackage(LSPOSED_PACKAGE)
    if (launch != null) {
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (runCatching { context.startActivity(launch) }.isSuccess) return
    }
    Toast.makeText(context, "未找到 LSPosed 管理器", Toast.LENGTH_SHORT).show()
}

private fun copyDiagnostics(context: Context, state: BlockConfig, targetVersion: String) {
    val text = buildString {
        appendLine("JM 广告净化 $MODULE_VERSION")
        appendLine("目标: $TARGET_PACKAGE $targetVersion")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("总开关: ${state.enabled}")
        appendLine("首页: ${state.blockHome}")
        appendLine("开屏/封面/详情: ${state.blockPages}")
        appendLine("影视/游戏: ${state.blockMedia}")
        append("详细日志: ${state.verbose}")
    }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("JM 广告净化", text))
    Toast.makeText(context, "诊断信息已复制", Toast.LENGTH_SHORT).show()
}
