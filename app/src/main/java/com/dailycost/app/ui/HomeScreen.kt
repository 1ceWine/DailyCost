package com.dailycost.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycost.app.data.AppStore
import com.dailycost.app.data.ItemCategory
import com.dailycost.app.data.PurchasedItem
import java.time.format.DateTimeFormatter

enum class SortBy(val label: String) {
    DAILY("日均"),
    PRICE("价格"),
    DATE("时间"),
    NAME("名称")
}

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    store: AppStore,
    contentPadding: PaddingValues,
    onOpenItem: (Long) -> Unit
) {
    val items by store.items.collectAsState()
    val categories by store.categories.collectAsState()
    val settings by store.settings.collectAsState()
    val symbol = settings.currency

    var query by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf<Long?>(null) }
    var sortBy by remember { mutableStateOf(SortBy.DAILY) }
    val focus = LocalFocusManager.current

    val activeItems = items.filter { it.isActive }
    val retiredCount = items.size - activeItems.size
    val countByCat = items.groupingBy { it.categoryId }.eachCount()
    val visible = items.filter { item ->
        (settings.showRetired || item.isActive) &&
            (selectedCat == null || item.categoryId == selectedCat) &&
            (query.isBlank() || item.name.contains(query, true) || item.note.contains(query, true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 4.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item("hero") {
            HeroCard(
                total = items.sumOf { it.price },
                daily = activeItems.sumOf { it.dailyCost },
                activeCount = activeItems.size,
                retiredCount = retiredCount,
                symbol = symbol
            )
        }

        item("search") {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("搜索物品或备注", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item("cats") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCat == null,
                        onClick = { selectedCat = null },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("全部")
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${items.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
                items(
                    categories.filter { c -> items.any { it.categoryId == c.id } },
                    key = { it.id }
                ) { cat ->
                    FilterChip(
                        selected = selectedCat == cat.id,
                        onClick = { selectedCat = if (selectedCat == cat.id) null else cat.id },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.name)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${countByCat[cat.id] ?: 0}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedCat == cat.id) MaterialTheme.colorScheme.onSecondaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(Color(cat.colorArgb), CircleShape)
                            )
                        }
                    )
                }
            }
        }

        item("sort") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SortBy.entries.toList(), key = { it.name }) { s ->
                        FilterChip(
                            selected = sortBy == s,
                            onClick = { sortBy = s },
                            label = { Text(s.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                if (retiredCount > 0) {
                    FilterChip(
                        selected = settings.showRetired,
                        onClick = {
                            store.updateSettings(settings.copy(showRetired = !settings.showRetired))
                        },
                        label = { Text("含退役") }
                    )
                }
            }
        }

        if (visible.isEmpty()) {
            item("empty") {
                EmptyState(
                    title = if (items.isEmpty()) "还没有记录" else "没有符合条件的物品",
                    subtitle = if (items.isEmpty())
                        "点「记一笔」添加，或在设置里导入 CSV / 填充示例数据"
                    else "换个关键词或分类试试"
                )
            }
        } else {
            val sorted = when (sortBy) {
                SortBy.DAILY -> visible.sortedByDescending { it.dailyCost }
                SortBy.PRICE -> visible.sortedByDescending { it.price }
                SortBy.DATE -> visible.sortedByDescending { it.purchaseDate }
                SortBy.NAME -> visible.sortedBy { it.name }
            }
            val grouped = sorted.groupBy { it.categoryId }
            val orderedCats = categories
                .filter { grouped.containsKey(it.id) }
                .sortedByDescending { cat -> grouped[cat.id]!!.sumOf { it.dailyCost } }

            orderedCats.forEach { cat ->
                val catItems = grouped[cat.id]!!
                item(key = "h_${cat.id}") {
                    CategoryHeader(cat, catItems, symbol)
                }
                items(catItems, key = { "i_${it.id}" }) { item ->
                    ItemRow(item = item, categories = categories, symbol = symbol, onClick = { onOpenItem(item.id) })
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    total: Double,
    daily: Double,
    activeCount: Int,
    retiredCount: Int,
    symbol: String
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(primary, primary.copy(alpha = 0.86f), secondary.copy(alpha = 0.92f))
                    )
                )
                .padding(20.dp)
        ) {
            // 装饰圆
            Box(
                Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 0.dp)
                    .background(Color.White.copy(alpha = 0.07f), CircleShape)
            )
            Column {
                Text(
                    "总投入",
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    money(total, symbol),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "每天成本",
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            money(daily, symbol) + "/天",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "在用 / 退役",
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            "$activeCount 件 / $retiredCount 件",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(cat: ItemCategory, catItems: List<PurchasedItem>, symbol: String) {
    val subtotal = catItems.sumOf { it.price }
    val dailyTotal = catItems.filter { it.isActive }.sumOf { it.dailyCost }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .background(Color(cat.colorArgb), CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            cat.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "${catItems.size} 件",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Text(
            "${money(dailyTotal, symbol)}/天 · 合计 ${money(subtotal, symbol)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun ItemRow(
    item: PurchasedItem,
    categories: List<ItemCategory>,
    symbol: String,
    onClick: () -> Unit
) {
    val cat = categories.find { it.id == item.categoryId }
    val catColor = cat?.let { Color(it.colorArgb) } ?: MaterialTheme.colorScheme.primary

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(catColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (cat?.name ?: "其").take(1),
                        color = catColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (!item.isActive) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "已退役",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        buildString {
                            append("购于 ${item.purchaseDate.format(DATE_FMT)}")
                            if (!item.isActive) append(" · 退役 ${item.retiredDate?.format(DATE_FMT)}")
                            append(" · 用了 ${item.daysHeld} 天")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        money(item.price, symbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        money(item.dailyCost, symbol) + "/天",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isActive) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item.lifeProgress?.let { p ->
                Spacer(Modifier.height(10.dp))
                ThinBar(fraction = p, color = catColor)
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        "回本 ${(p * 100).toInt()}% · 已用成本 ${money(item.amortized, symbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        run {
                            val left = item.daysLeft
                            when {
                                left == null -> ""
                                left >= 0 -> "还差 $left 天"
                                else -> "超出 ${-left} 天"
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🗂", fontSize = 30.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
