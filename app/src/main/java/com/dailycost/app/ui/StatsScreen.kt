package com.dailycost.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycost.app.data.AppStore
import com.dailycost.app.data.ItemCategory
import com.dailycost.app.data.PurchasedItem
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

// ---------------- 时间范围 ----------------

enum class RangePreset(val label: String) {
    M3("3 个月"),
    M6("6 个月"),
    M12("1 年"),
    M24("2 年"),
    ALL("全部"),
    CUSTOM("自定义")
}

/** 聚合粒度：由所选周期自动决定，不提供手动开关 */
enum class Granularity(val label: String, val unit: String) {
    MONTH("按月", "月"),
    QUARTER("按季", "季度"),
    YEAR("按年", "年")
}

data class TimeRange(val preset: RangePreset, val start: YearMonth, val end: YearMonth) {
    val months: Int get() = (ChronoUnit.MONTHS.between(start, end).toInt() + 1).coerceAtLeast(1)

    fun contains(date: LocalDate): Boolean =
        !date.isBefore(start.atDay(1)) && !date.isAfter(end.atEndOfMonth())

    val summary: String
        get() = if (preset == RangePreset.ALL) {
            "全部时间 · ${start.year}年${start.monthValue}月起 · 共 ${months} 个月"
        } else {
            "${start.year}年${start.monthValue}月 - ${end.year}年${end.monthValue}月 · ${months} 个月"
        }
}

private data class Bucket(val label: String, val start: YearMonth, val end: YearMonth) {
    fun contains(date: LocalDate): Boolean =
        !date.isBefore(start.atDay(1)) && !date.isAfter(end.atEndOfMonth())
}

private fun quarterStart(ym: YearMonth): YearMonth =
    YearMonth.of(ym.year, ((ym.monthValue - 1) / 3) * 3 + 1)

private fun buildRange(
    preset: RangePreset,
    customStart: YearMonth,
    customEnd: YearMonth,
    earliest: YearMonth?,
    now: YearMonth
): TimeRange = when (preset) {
    RangePreset.M3 -> TimeRange(preset, now.minusMonths(2), now)
    RangePreset.M6 -> TimeRange(preset, now.minusMonths(5), now)
    RangePreset.M12 -> TimeRange(preset, now.minusMonths(11), now)
    RangePreset.M24 -> TimeRange(preset, now.minusMonths(23), now)
    RangePreset.ALL -> TimeRange(preset, earliest?.let { if (it.isAfter(now)) now else it } ?: now, now)
    RangePreset.CUSTOM -> {
        val s = if (customStart.isAfter(customEnd)) customEnd else customStart
        val e = if (customStart.isAfter(customEnd)) customStart else customEnd
        TimeRange(preset, s, e)
    }
}

private fun autoGranularity(range: TimeRange): Granularity = when {
    range.months <= 15 -> Granularity.MONTH
    range.months <= 40 -> Granularity.QUARTER
    else -> Granularity.YEAR
}

private fun buildBuckets(range: TimeRange, gran: Granularity): List<Bucket> {
    val crossYear = range.start.year != range.end.year
    return when (gran) {
        Granularity.MONTH -> generateSequence(range.start) { it.plusMonths(1) }
            .takeWhile { !it.isAfter(range.end) }
            .map { ym ->
                val label = when {
                    !crossYear -> "${ym.monthValue}月"
                    ym == range.start || ym.monthValue == 1 -> "${ym.year % 100}年"
                    else -> "${ym.monthValue}月"
                }
                Bucket(label, ym, ym)
            }.toList()

        Granularity.QUARTER -> generateSequence(quarterStart(range.start)) { it.plusMonths(3) }
            .takeWhile { !it.isAfter(range.end) }
            .map { qs ->
                val qe = qs.plusMonths(2)
                Bucket(
                    label = "${qs.year % 100}Q${(qs.monthValue - 1) / 3 + 1}",
                    start = qs,
                    end = if (qe.isAfter(range.end)) range.end else qe
                )
            }.toList()

        Granularity.YEAR -> (range.start.year..range.end.year).map { y ->
            val ye = YearMonth.of(y, 12)
            Bucket("${y}年", YearMonth.of(y, 1), if (ye.isAfter(range.end)) range.end else ye)
        }
    }
}

/** 某一天时，全部物品的每天成本合计（已退役的在退役后不再计入） */
private fun dailyTotalAt(items: List<PurchasedItem>, date: LocalDate): Double =
    items.sumOf { item ->
        if (item.purchaseDate.isAfter(date)) return@sumOf 0.0
        val retired = item.retiredDate
        if (retired != null && !retired.isAfter(date)) return@sumOf 0.0
        val days = (date.toEpochDay() - item.purchaseDate.toEpochDay()).toInt() + 1
        item.price / maxOf(1, days)
    }

// ---------------- 页面 ----------------

@Composable
fun StatsScreen(store: AppStore, contentPadding: PaddingValues) {
    val items by store.items.collectAsState()
    val categories by store.categories.collectAsState()
    val settings by store.settings.collectAsState()
    val symbol = settings.currency

    val today = LocalDate.now()
    val nowYm = YearMonth.from(today)
    val earliest = remember(items) { items.minOfOrNull { YearMonth.from(it.purchaseDate) } }

    var preset by remember { mutableStateOf(RangePreset.M12) }
    var customStart by remember { mutableStateOf(nowYm.minusMonths(5)) }
    var customEnd by remember { mutableStateOf(nowYm) }
    var showCustom by remember { mutableStateOf(false) }

    val range = buildRange(preset, customStart, customEnd, earliest, nowYm)
    val gran = autoGranularity(range)

    val rangeItems = items.filter { range.contains(it.purchaseDate) }
    val rangeTotal = rangeItems.sumOf { it.price }
    val activeDaily = items.filter { it.isActive }.sumOf { it.dailyCost }

    val catShare = categories.mapNotNull { cat ->
        val list = rangeItems.filter { it.categoryId == cat.id }
        if (list.isEmpty()) return@mapNotNull null
        Triple(cat, list.sumOf { it.price }, list.sumOf { it.dailyCost })
    }.sortedByDescending { it.second }

    val buckets = buildBuckets(range, gran)
    val barEntries = buckets.map { b ->
        b.label to rangeItems.filter { b.contains(it.purchaseDate) }.sumOf { it.price }
    }
    val dailyEntries = buckets.map { b ->
        val point = minOf(b.end.atEndOfMonth(), today)
        b.label to dailyTotalAt(items, point)
    }

    val peak = barEntries.maxByOrNull { it.second }
    val monthlyAvg = rangeTotal / range.months
    val active = items.filter { it.isActive }
    val chartLabelEvery = if (buckets.size <= 8) 1 else (buckets.size + 7) / 8

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 4.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 周期选择
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(RangePreset.values().toList(), key = { it.name }) { p ->
                    val selected = preset == p
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (p == RangePreset.CUSTOM) showCustom = true else preset = p
                        },
                        label = {
                            Text(
                                if (p == RangePreset.CUSTOM && selected)
                                    "${range.start.monthValue}月-${range.end.monthValue}月"
                                else p.label,
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }
        }

        item {
            OverviewCard(
                title = if (preset == RangePreset.ALL) "资产总投入" else "区间购入金额",
                amount = rangeTotal,
                subtitle = range.summary,
                leftLabel = "购入件数",
                leftValue = "${rangeItems.size} 件",
                rightLabel = "当前每天成本",
                rightValue = dailyText(activeDaily, symbol),
                symbol = symbol
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard(
                    "最高${gran.unit}",
                    peak?.let { if (it.second > 0) it.first else "-" } ?: "-",
                    peak?.let { money(it.second, symbol) } ?: money(0.0, symbol),
                    Modifier.weight(1f)
                )
                MiniStatCard(
                    "月均购入",
                    money(monthlyAvg, symbol),
                    "共 ${range.months} 个月",
                    Modifier.weight(1f)
                )
            }
        }

        // 分类占比
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("分类占比")
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${rangeItems.size} 件",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    if (catShare.isEmpty()) {
                        Text(
                            "该时间范围内暂无购入记录",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(
                                slices = catShare.map {
                                    DonutSlice(it.first.name, it.second, Color(it.first.colorArgb))
                                },
                                modifier = Modifier.size(132.dp),
                                centerTitle = "区间投入",
                                centerValue = moneyCompact(rangeTotal, symbol)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                catShare.take(5).forEach { (cat, sum, daily) ->
                                    LegendRow(
                                        color = Color(cat.colorArgb),
                                        title = cat.name,
                                        value = "${if (rangeTotal > 0) (sum / rangeTotal * 100).toInt() else 0}%",
                                        subtitle = "${money(sum, symbol)} · ${money(daily, symbol)}/天"
                                    )
                                }
                                if (catShare.size > 5) {
                                    Text(
                                        "还有 ${catShare.size - 5} 个分类…",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 购入金额柱状图
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("购入金额")
                        Spacer(Modifier.width(6.dp))
                        UnitBadge(gran.label)
                        Spacer(Modifier.weight(1f))
                        if (peak != null && peak.second > 0) {
                            Text(
                                "最高 ${peak.first} · ${money(peak.second, symbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        money(rangeTotal, symbol),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    MonthBarChart(
                        entries = barEntries,
                        color = MaterialTheme.colorScheme.primary,
                        barHeight = 142,
                        symbol = symbol
                    )
                }
            }
        }

        // 每天成本走势
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("每天成本走势")
                        Spacer(Modifier.width(6.dp))
                        UnitBadge(gran.label)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "今天 ${dailyText(activeDaily, symbol)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "每${gran.unit}末所有在用物品的日均合计：买入时上升，用得越久摊得越薄",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    LineChart(
                        entries = dailyEntries,
                        color = MaterialTheme.colorScheme.primary,
                        symbol = symbol,
                        height = 160.dp,
                        labelEvery = chartLabelEvery
                    )
                }
            }
        }

        // 日均排行
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("在用品 · 日均最贵 Top 5")
                        Spacer(Modifier.weight(1f))
                        Text(
                            "全部时间",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (active.isEmpty()) {
                        Text(
                            "暂无在用物品",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        active.sortedByDescending { it.dailyCost }.take(5).forEachIndexed { idx, item ->
                            if (idx > 0) HorizontalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            RankRow(rank = idx + 1, item = item, symbol = symbol, categories = categories)
                        }
                    }
                }
            }
        }

        // 持有最久
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("持有最久 Top 3")
                        Spacer(Modifier.weight(1f))
                        Text(
                            "全部时间",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (items.isEmpty()) {
                        Text(
                            "暂无数据",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        items.sortedByDescending { it.daysHeld }.take(3).forEachIndexed { idx, item ->
                            if (idx > 0) HorizontalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${item.daysHeld} 天",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${money(item.price, symbol)} → ${money(item.dailyCost, symbol)}/天",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "每天成本 = 所有「在用」物品每天的花费之和，也就是这些东西每天实际花了你多少钱。\n" +
                    "已退役物品按退役当天封账，之后不再计入。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }

    if (showCustom) {
        val minYear = minOf(earliest?.year ?: nowYm.year, nowYm.year - 1)
        CustomRangeDialog(
            start = if (preset == RangePreset.CUSTOM) range.start else nowYm.minusMonths(5),
            end = if (preset == RangePreset.CUSTOM) range.end else nowYm,
            minYear = minYear,
            maxYear = nowYm.year,
            onDismiss = { showCustom = false },
            onConfirm = { s, e ->
                customStart = s
                customEnd = e
                preset = RangePreset.CUSTOM
                showCustom = false
            }
        )
    }
}

// ---------------- 组件 ----------------

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun UnitBadge(text: String) {
    Box(
        Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun OverviewCard(
    title: String,
    amount: Double,
    subtitle: String,
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    symbol: String
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(primary, primary.copy(alpha = 0.85f), secondary.copy(alpha = 0.9f))),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(title, color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(2.dp))
            Text(money(amount, symbol), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(14.dp))
            Row {
                Column(Modifier.weight(1f)) {
                    Text(leftLabel, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelSmall)
                    Text(
                        leftValue,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(rightLabel, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelSmall)
                    Text(
                        rightValue,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(label: String, main: String, sub: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                main,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RankRow(rank: Int, item: PurchasedItem, symbol: String, categories: List<ItemCategory>) {
    val cat = categories.find { it.id == item.categoryId }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(24.dp)
                .background(
                    if (rank <= 3) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$rank",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${cat?.name ?: "其他"} · 用了 ${item.daysHeld} 天",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            money(item.dailyCost, symbol) + "/天",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------- 自定义区间弹窗 ----------------

@Composable
private fun CustomRangeDialog(
    start: YearMonth,
    end: YearMonth,
    minYear: Int,
    maxYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth, YearMonth) -> Unit
) {
    var sy by remember { mutableIntStateOf(start.year) }
    var sm by remember { mutableIntStateOf(start.monthValue) }
    var ey by remember { mutableIntStateOf(end.year) }
    var em by remember { mutableIntStateOf(end.monthValue) }

    val from = YearMonth.of(sy, sm)
    val to = YearMonth.of(ey, em)
    val valid = !from.isAfter(to)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择月份区间") },
        text = {
            Column {
                MonthPicker("起始", sy, sm, minYear, maxYear) { y, m -> sy = y; sm = m }
                Spacer(Modifier.height(14.dp))
                MonthPicker("结束", ey, em, minYear, maxYear) { y, m -> ey = y; em = m }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (valid) "${from.year}年${from.monthValue}月 - ${to.year}年${to.monthValue}月"
                    else "起始月份不能晚于结束月份",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onConfirm(from, to) }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun MonthPicker(
    label: String,
    year: Int,
    month: Int,
    minYear: Int,
    maxYear: Int,
    onChange: (Int, Int) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = { if (year > minYear) onChange(year - 1, month) },
                enabled = year > minYear,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "上一年", modifier = Modifier.size(18.dp))
            }
            Text(
                "$year 年",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            IconButton(
                onClick = { if (year < maxYear) onChange(year + 1, month) },
                enabled = year < maxYear,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "下一年", modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        listOf(1..6, 7..12).forEach { rowMonths ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowMonths.forEach { m ->
                    val selected = m == month
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onChange(year, m) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$m",
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
