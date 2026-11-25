package com.dailycost.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycost.app.data.AppStore
import com.dailycost.app.data.PurchasedItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailSheet(
    store: AppStore,
    item: PurchasedItem,
    symbol: String,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories by store.categories.collectAsState()
    val cat = categories.find { it.id == item.categoryId }
    var confirmDelete by remember { mutableStateOf(false) }

    // 持有期日均成本曲线：第 1 天 = 价格，之后逐日摊薄
    val series = remember(item.id, item.daysHeld) {
        val totalDays = item.daysHeld
        val samples = if (totalDays <= 1) 1 else 13
        (0 until samples).map { idx ->
            val day = if (samples == 1) 1 else 1 + (totalDays - 1) * idx / (samples - 1)
            val date = item.purchaseDate.plusDays((day - 1).toLong())
            "${date.monthValue}/${date.dayOfMonth}" to item.price / maxOf(1, day)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = cat?.let { Color(it.colorArgb).copy(alpha = 0.14f) }
                                ?: MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                cat?.name ?: "其他",
                                style = MaterialTheme.typography.labelMedium,
                                color = cat?.let { Color(it.colorArgb) } ?: MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (!item.isActive) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "已退役",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row {
                Column(Modifier.weight(1f)) {
                    Text("购买价", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        money(item.price, symbol),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("日均花费", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        money(item.dailyCost, symbol),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("/天", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(Modifier.weight(1f)) {
                    Text("已持有", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${item.daysHeld}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("天", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 日均成本随持有时间下降的折线图（覆盖购买至今 / 至退役）
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "日均成本走势",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (item.isActive) "持有 ${item.daysHeld} 天（持续中）" else "持有 ${item.daysHeld} 天（已退役）",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                "第 1 天 ${money(item.price, symbol)}/天 → 现在 ${money(item.dailyCost, symbol)}/天",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            LineChart(
                entries = series,
                color = MaterialTheme.colorScheme.primary,
                symbol = symbol,
                height = 150.dp,
                labelEvery = 2
            )

            item.lifeProgress?.let { p ->
                Spacer(Modifier.height(18.dp))
                Row {
                    Text("回本进度", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${(p * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))
                ThinBar(fraction = p, color = MaterialTheme.colorScheme.primary, height = 10)
                Spacer(Modifier.height(6.dp))
                Text(
                    "已用成本 ${money(item.amortized, symbol)} / 预期 ${item.lifeDays} 天",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(10.dp))

            DetailRow("购买日期", item.purchaseDate.format(FMT))
            if (item.retiredDate != null) {
                DetailRow("退役日期", item.retiredDate.format(FMT))
                DetailRow("计价截止", "按 ${item.daysHeld} 天封账")
            } else {
                DetailRow("计价截止", "持续计算中（每天更新）")
            }
            if (item.note.isNotBlank()) DetailRow("备注", item.note)

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("编辑") }
                OutlinedButton(
                    onClick = {
                        if (item.isActive) store.retireItem(item.id, LocalDate.now())
                        else store.restoreItem(item.id)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(if (item.isActive) "标记退役" else "恢复在用") }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("删除这件物品") }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除物品") },
            text = { Text("确定删除「${item.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    store.deleteItem(item.id)
                    confirmDelete = false
                    onDismiss()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(76.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
