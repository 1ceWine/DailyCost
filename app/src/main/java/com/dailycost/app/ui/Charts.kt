package com.dailycost.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DonutSlice(val label: String, val value: Double, val color: Color)

/** 环形占比图 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 20.dp,
    centerTitle: String = "",
    centerValue: String = ""
) {
    val total = slices.sumOf { it.value }
    val track = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height)
            val topLeft = Offset(
                (size.width - diameter) / 2 + stroke / 2,
                (size.height - diameter) / 2 + stroke / 2
            )
            val arcSize = Size(diameter - stroke, diameter - stroke)

            if (total <= 0.0) {
                drawArc(
                    color = track,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
                return@Canvas
            }
            var start = -90f
            slices.forEach { s ->
                val sweep = (360f * (s.value / total)).toFloat()
                if (sweep > 0f) {
                    drawArc(
                        color = s.color,
                        startAngle = start + 1.5f,
                        sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Butt)
                    )
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                centerValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                centerTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 柱状图：数值行 / 柱体区 / 标签行三段分离，保证所有标签严格同一基线
 */
@Composable
fun MonthBarChart(
    entries: List<Pair<String, Double>>,
    color: Color,
    modifier: Modifier = Modifier,
    barHeight: Int = 140,
    symbol: String = "¥",
    showValues: Boolean = entries.size <= 7
) {
    if (entries.isEmpty()) {
        Box(modifier.fillMaxWidth().height(barHeight.dp), contentAlignment = Alignment.Center) {
            Text("暂无数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val max = entries.maxOfOrNull { it.second } ?: 0.0
    val maxIndex = entries.indices.maxByOrNull { entries[it].second } ?: -1
    val labelEvery = if (entries.size <= 12) 1 else (entries.size + 11) / 12
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    // 柱顶金额：大额去小数（¥15.2万），小额取整（¥2,100），保证格子内放得下
    fun valueText(v: Double): String = when {
        v >= 100_000_000 -> String.format(java.util.Locale.CHINA, "%s%.1f亿", symbol, v / 100_000_000)
        v >= 10_000 -> String.format(java.util.Locale.CHINA, "%s%.1f万", symbol, v / 10_000)
        else -> String.format(java.util.Locale.CHINA, "%s%,.0f", symbol, v)
    }

    Column(modifier.fillMaxWidth()) {
        // 1. 数值行（固定高度，居中，与柱子无关）
        Row(Modifier.fillMaxWidth().height(15.dp)) {
            entries.forEachIndexed { index, (_, value) ->
                val text = when {
                    value <= 0 -> ""
                    showValues || index == maxIndex -> valueText(value)
                    else -> ""
                }
                Text(
                    text = text,
                    modifier = Modifier.weight(1f),
                    fontSize = 8.sp,
                    color = muted,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        // 2. 柱体区（固定高度，底部对齐）
        Row(
            modifier = Modifier.fillMaxWidth().height(barHeight.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            entries.forEach { (_, value) ->
                val frac = if (max <= 0.0) 0f else (value / max).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (value > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.66f)
                                .fillMaxHeight(frac.coerceAtLeast(0.012f))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(color, color.copy(alpha = 0.55f))
                                    ),
                                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                    }
                }
            }
        }

        // 3. 基线 + 标签行（固定高度，公共基线）
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth().height(14.dp)) {
            entries.forEachIndexed { index, (label, _) ->
                val show = labelEvery == 1 || index % labelEvery == 0 || index == entries.lastIndex
                Text(
                    text = if (show) label else "",
                    modifier = Modifier.weight(1f),
                    fontSize = 10.sp,
                    color = muted,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/** 简易横向进度条 */
@Composable
fun ThinBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Int = 8) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(height.dp / 2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color, RoundedCornerShape(height.dp / 2))
        )
    }
}

/** 图例行 */
@Composable
fun LegendRow(
    color: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(10.dp)
                .height(10.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

/**
 * 折线图：平滑曲线 + 渐变填充 + 横向网格 + 纵轴刻度
 */
@Composable
fun LineChart(
    entries: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 170.dp,
    symbol: String = "¥",
    showXLabels: Boolean = true,
    /** 每隔几个点显示一个横轴标签，1 表示全部显示 */
    labelEvery: Int = 1
) {
    if (entries.isEmpty()) {
        Box(modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
            Text("暂无数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val maxValue = entries.maxOf { it.second }.coerceAtLeast(1.0)
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(height)) {
            Column(
                modifier = Modifier.width(46.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(moneyCompact(maxValue, symbol), fontSize = 9.sp, color = labelColor)
                Text(moneyCompact(maxValue / 2, symbol), fontSize = 9.sp, color = labelColor)
                Text("0", fontSize = 9.sp, color = labelColor)
            }
            Spacer(Modifier.width(8.dp))
            Canvas(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val w = size.width
                val h = size.height
                val topPad = h * 0.06f
                val plotH = h - topPad * 1.4f

                // 网格
                listOf(0f, 0.5f, 1f).forEach { f ->
                    val y = topPad + plotH * f
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val stepX = if (entries.size > 1) w / (entries.size - 1) else w
                val pts = entries.mapIndexed { i, e ->
                    Offset(
                        x = stepX * i,
                        y = topPad + plotH * (1f - (e.second / (maxValue * 1.02)).toFloat())
                    )
                }

                if (pts.size == 1) {
                    drawCircle(color, radius = 4.dp.toPx(), center = pts[0])
                } else {
                    val line = Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) {
                            val prev = pts[i - 1]
                            val cur = pts[i]
                            val midX = (prev.x + cur.x) / 2f
                            cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                        }
                    }
                    val area = Path().apply {
                        addPath(line)
                        lineTo(pts.last().x, h)
                        lineTo(pts.first().x, h)
                        close()
                    }
                    drawPath(
                        area,
                        Brush.verticalGradient(
                            listOf(color.copy(alpha = 0.32f), color.copy(alpha = 0.02f))
                        )
                    )
                    drawPath(
                        line,
                        color = color,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    pts.forEach { p ->
                        drawCircle(color = Color.White, radius = 3.6.dp.toPx(), center = p)
                        drawCircle(color = color, radius = 2.4.dp.toPx(), center = p)
                    }
                }
            }
        }

        if (showXLabels) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 54.dp, top = 6.dp)
            ) {
                entries.forEachIndexed { i, entry ->
                    val show = labelEvery <= 1 || i % labelEvery == 0 || i == entries.lastIndex
                    Text(
                        if (show) entry.first else "",
                        modifier = Modifier.weight(1f),
                        fontSize = 9.sp,
                        color = labelColor,
                        maxLines = 1,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
