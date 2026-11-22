package com.jmcomic3.adblock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

enum class SettingsGlyph {
    Shield,
    Play,
    Layers,
    Popup,
    Home,
    Feed,
    Sparkle,
    Update,
    File,
    Lock,
    Info,
}

@Composable
fun GlyphBadge(
    glyph: SettingsGlyph,
    tint: Color,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 34.dp,
    iconSize: Dp = 20.dp,
) {
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(RoundedCornerShape(badgeSize * 0.28f))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(iconSize)) {
            drawSettingsGlyph(glyph, tint)
        }
    }
}

private fun DrawScope.drawSettingsGlyph(glyph: SettingsGlyph, color: Color) {
    val unit = min(size.width, size.height) / 24f
    val left = (size.width - 24f * unit) / 2f
    val top = (size.height - 24f * unit) / 2f

    fun p(x: Float, y: Float) = Offset(left + x * unit, top + y * unit)

    fun Path.m(x: Float, y: Float) {
        val point = p(x, y)
        moveTo(point.x, point.y)
    }

    fun Path.l(x: Float, y: Float) {
        val point = p(x, y)
        lineTo(point.x, point.y)
    }

    fun Path.c(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        val a = p(x1, y1)
        val b = p(x2, y2)
        val c = p(x3, y3)
        cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
    }

    fun Path.q(x1: Float, y1: Float, x2: Float, y2: Float) {
        val a = p(x1, y1)
        val b = p(x2, y2)
        quadraticTo(a.x, a.y, b.x, b.y)
    }

    val line = Stroke(
        width = 1.7f * unit,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )

    when (glyph) {
        SettingsGlyph.Shield -> {
            val shield = Path().apply {
                m(12f, 3f)
                l(20f, 6.3f)
                l(20f, 11.3f)
                c(20f, 16.4f, 16.4f, 19.6f, 12f, 21f)
                c(7.6f, 19.6f, 4f, 16.4f, 4f, 11.3f)
                l(4f, 6.3f)
                close()
            }
            drawPath(shield, color, style = line)
            val check = Path().apply {
                m(8.5f, 11.9f)
                l(11f, 14.3f)
                l(15.8f, 9.4f)
            }
            drawPath(check, color, style = line)
        }

        SettingsGlyph.Play -> {
            drawRoundRect(
                color = color,
                topLeft = p(5f, 2.8f),
                size = Size(14f * unit, 18.4f * unit),
                cornerRadius = CornerRadius(2.8f * unit),
                style = line,
            )
            val triangle = Path().apply {
                m(10.2f, 8.4f)
                l(10.2f, 15.6f)
                l(15.4f, 12f)
                close()
            }
            drawPath(triangle, color, style = line)
            drawLine(
                color = color,
                start = p(9.8f, 19f),
                end = p(14.2f, 19f),
                strokeWidth = 1.5f * unit,
                cap = StrokeCap.Round,
            )
        }

        SettingsGlyph.Layers -> {
            drawRoundRect(
                color = color,
                topLeft = p(4f, 4.2f),
                size = Size(16f * unit, 5.2f * unit),
                cornerRadius = CornerRadius(2.1f * unit),
                style = line,
            )
            drawRoundRect(
                color = color,
                topLeft = p(2.6f, 9.4f),
                size = Size(18.8f * unit, 5.2f * unit),
                cornerRadius = CornerRadius(2.1f * unit),
                style = line,
            )
            drawRoundRect(
                color = color,
                topLeft = p(4f, 14.6f),
                size = Size(16f * unit, 5.2f * unit),
                cornerRadius = CornerRadius(2.1f * unit),
                style = line,
            )
        }

        SettingsGlyph.Popup -> {
            drawRoundRect(
                color = color,
                topLeft = p(3f, 4f),
                size = Size(18f * unit, 16f * unit),
                cornerRadius = CornerRadius(3f * unit),
                style = line,
            )
            drawLine(
                color = color,
                start = p(3f, 9f),
                end = p(21f, 9f),
                strokeWidth = 1.4f * unit,
            )
            drawCircle(color, radius = 0.8f * unit, center = p(6.2f, 6.5f))
            drawLine(
                color = color,
                start = p(7f, 13.4f),
                end = p(17f, 13.4f),
                strokeWidth = 1.5f * unit,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = p(7f, 16.4f),
                end = p(14f, 16.4f),
                strokeWidth = 1.5f * unit,
                cap = StrokeCap.Round,
            )
        }

        SettingsGlyph.Home -> {
            val roof = Path().apply {
                m(3.5f, 11f)
                l(12f, 3.7f)
                l(20.5f, 11f)
            }
            drawPath(roof, color, style = line)
            drawLine(color, p(5.7f, 9.8f), p(5.7f, 20f), 1.7f * unit, StrokeCap.Round)
            drawLine(color, p(18.3f, 9.8f), p(18.3f, 20f), 1.7f * unit, StrokeCap.Round)
            drawLine(color, p(5.7f, 20f), p(18.3f, 20f), 1.7f * unit, StrokeCap.Round)
            drawRoundRect(
                color = color,
                topLeft = p(10f, 14f),
                size = Size(4f * unit, 6f * unit),
                cornerRadius = CornerRadius(1.1f * unit),
                style = line,
            )
        }

        SettingsGlyph.Feed -> {
            listOf(6f, 12f, 18f).forEach { y ->
                drawCircle(color, radius = 0.9f * unit, center = p(4.2f, y))
                drawLine(
                    color = color,
                    start = p(7.2f, y),
                    end = p(20f, y),
                    strokeWidth = 1.5f * unit,
                    cap = StrokeCap.Round,
                )
            }
        }

        SettingsGlyph.Sparkle -> {
            fun starPath(cx: Float, cy: Float, radius: Float): Path = Path().apply {
                m(cx, cy - radius)
                q(cx + radius * 0.22f, cy - radius * 0.22f, cx + radius, cy)
                q(cx + radius * 0.22f, cy + radius * 0.22f, cx, cy + radius)
                q(cx - radius * 0.22f, cy + radius * 0.22f, cx - radius, cy)
                q(cx - radius * 0.22f, cy - radius * 0.22f, cx, cy - radius)
                close()
            }
            drawPath(starPath(11.2f, 11.4f, 8.2f), color, style = line)
            drawPath(starPath(19f, 18.6f, 3.1f), color)
        }

        SettingsGlyph.Update -> {
            drawArc(
                color = color,
                startAngle = 45f,
                sweepAngle = 285f,
                useCenter = false,
                topLeft = p(4f, 4f),
                size = Size(16f * unit, 16f * unit),
                style = line,
            )
            drawLine(color, p(17.2f, 3.4f), p(18.2f, 7.1f), 1.7f * unit, StrokeCap.Round)
            drawLine(color, p(18.2f, 7.1f), p(14.5f, 7.8f), 1.7f * unit, StrokeCap.Round)
        }

        SettingsGlyph.File -> {
            val document = Path().apply {
                m(6f, 3f)
                l(14f, 3f)
                l(19f, 8f)
                l(19f, 21f)
                l(6f, 21f)
                close()
                m(14f, 3f)
                l(14f, 8f)
                l(19f, 8f)
            }
            drawPath(document, color, style = line)
            drawLine(color, p(9f, 12.5f), p(16f, 12.5f), 1.5f * unit, StrokeCap.Round)
            drawLine(color, p(9f, 16f), p(14f, 16f), 1.5f * unit, StrokeCap.Round)
        }

        SettingsGlyph.Lock -> {
            drawRoundRect(
                color = color,
                topLeft = p(5f, 10f),
                size = Size(14f * unit, 11f * unit),
                cornerRadius = CornerRadius(2.4f * unit),
                style = line,
            )
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = p(8f, 4f),
                size = Size(8f * unit, 10f * unit),
                style = line,
            )
            drawCircle(color, radius = 0.9f * unit, center = p(12f, 14.7f))
            drawLine(color, p(12f, 15.5f), p(12f, 17.5f), 1.5f * unit, StrokeCap.Round)
        }

        SettingsGlyph.Info -> {
            drawCircle(color, radius = 9f * unit, center = p(12f, 12f), style = line)
            drawCircle(color, radius = 0.9f * unit, center = p(12f, 8.1f))
            drawLine(color, p(12f, 11f), p(12f, 16.5f), 1.7f * unit, StrokeCap.Round)
        }
    }
}

