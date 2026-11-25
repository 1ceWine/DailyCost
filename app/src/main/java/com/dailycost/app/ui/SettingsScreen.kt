package com.dailycost.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycost.app.data.AppStore
import com.dailycost.app.data.ItemCategory
import com.dailycost.app.ui.theme.AccentPalettes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private val CURRENCIES = listOf("¥", "$", "€", "£", "₩")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    store: AppStore,
    contentPadding: PaddingValues,
    onMessage: (String) -> Unit
) {
    val settings by store.settings.collectAsState()
    val items by store.items.collectAsState()
    val categories by store.categories.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingCsv by remember { mutableStateOf("") }
    var pendingJson by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    var deleteCatTarget by remember { mutableStateOf<ItemCategory?>(null) }

    fun writeTo(uri: android.net.Uri, text: String) {
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(text.toByteArray(Charsets.UTF_8))
                    }
                    true
                }.getOrDefault(false)
            }
            onMessage(if (ok) "导出成功" else "导出失败，请重试")
        }
    }

    fun readFrom(uri: android.net.Uri, done: (String?) -> Unit) {
        scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                }.getOrNull()
            }
            done(text)
        }
    }

    val importCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            readFrom(it) { text ->
                if (text.isNullOrBlank()) {
                    onMessage("无法读取所选文件")
                } else {
                    scope.launch {
                        val r = store.importCsv(text)
                        onMessage("导入 ${r.imported} 件物品" + if (r.skipped > 0) "，跳过 ${r.skipped} 行" else "")
                    }
                }
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { writeTo(it, pendingCsv) }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { writeTo(it, pendingJson) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            readFrom(it) { text ->
                if (text.isNullOrBlank()) {
                    onMessage("无法读取备份文件")
                } else {
                    val n = runCatching { store.restoreFromJson(text) }.getOrNull()
                    onMessage(if (n == null) "备份文件格式不正确" else "已恢复 $n 件物品")
                }
            }
        }
    }

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
        item {
            SettingsCard(title = "外观") {
                Text("深色模式", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("跟随系统", "浅色", "深色").forEachIndexed { i, label ->
                        SegmentedButton(
                            selected = settings.darkMode == i,
                            onClick = { store.updateSettings(settings.copy(darkMode = i)) },
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = 3)
                        ) { Text(label, style = MaterialTheme.typography.labelMedium) }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("主题色", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    AccentPalettes.forEachIndexed { i, palette ->
                        val selected = settings.accent == i
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(palette.lightPrimary, CircleShape)
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { store.updateSettings(settings.copy(accent = i)) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = palette.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsCard(title = "通用") {
                Text("货币符号", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CURRENCIES.forEach { c ->
                        FilterChip(
                            selected = settings.currency == c,
                            onClick = { store.updateSettings(settings.copy(currency = c)) },
                            label = { Text(c) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("列表中显示已退役物品", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "关闭后只统计在用物品",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.showRetired,
                        onCheckedChange = { store.updateSettings(settings.copy(showRetired = it)) }
                    )
                }
            }
        }

        item {
            SettingsCard(title = "分类排序") {
                Text(
                    "调整「添加物品」时分类下拉框的显示顺序，也可删除不需要的分类",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                categories.forEachIndexed { index, cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(16.dp)
                        )
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(Color(cat.colorArgb), CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(cat.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(
                            "${items.count { it.categoryId == cat.id }} 件",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { store.moveCategory(cat.id, -1) },
                            enabled = index > 0,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Filled.KeyboardArrowUp,
                                contentDescription = "上移 ${cat.name}",
                                tint = if (index > 0) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(
                            onClick = { store.moveCategory(cat.id, 1) },
                            enabled = index < categories.lastIndex,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Filled.KeyboardArrowDown,
                                contentDescription = "下移 ${cat.name}",
                                tint = if (index < categories.lastIndex) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(
                            onClick = { deleteCatTarget = cat },
                            enabled = categories.size > 1,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "删除分类 ${cat.name}",
                                tint = if (categories.size > 1)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsCard(title = "数据") {
                SettingAction(
                    icon = { Icon(Icons.Filled.FileUpload, contentDescription = null) },
                    title = "导入 CSV",
                    subtitle = "名称,价格,日期,分类（可带表头）",
                    onClick = { importCsvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*")) }
                )
                SettingAction(
                    icon = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                    title = "导出 CSV",
                    subtitle = "导出全部物品，可用于 Excel 编辑",
                    onClick = {
                        pendingCsv = store.exportCsv()
                        exportCsvLauncher.launch("dailycost-${LocalDate.now()}.csv")
                    }
                )
                SettingAction(
                    icon = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                    title = "导出备份",
                    subtitle = "包含分类、设置与全部物品（JSON）",
                    onClick = {
                        pendingJson = store.exportJson()
                        exportJsonLauncher.launch("dailycost-backup-${LocalDate.now()}.json")
                    }
                )
                SettingAction(
                    icon = { Icon(Icons.Filled.Restore, contentDescription = null) },
                    title = "从备份恢复",
                    subtitle = "会覆盖当前全部数据",
                    onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
                )
                SettingAction(
                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                    title = "填充示例数据",
                    subtitle = "快速体验统计效果（与现有数据共存）",
                    onClick = {
                        store.loadSampleData()
                        onMessage("已添加示例数据")
                    }
                )
                SettingAction(
                    icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    title = "清空所有物品",
                    subtitle = "保留分类与设置，操作不可撤销",
                    danger = true,
                    onClick = { confirmClear = true }
                )
            }
        }

        item {
            SettingsCard(title = "关于") {
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("物尽其用", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "把一次性支出摊到每一天，看清每件东西真正的成本。",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("v2.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
                Row {
                    Text(
                        "物品 ${items.size} 件 · 分类 ${categories.size} 个 · 日均计算含购买当天",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "数据仅保存在本机，建议定期用「导出备份」保存到网盘。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "CSV 日期支持 2024-03-01 / 2024/3/1 / 2024年3月1日",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }

    deleteCatTarget?.let { target ->
        val count = items.count { it.categoryId == target.id }
        val fallback = categories.firstOrNull { it.id != target.id }
        AlertDialog(
            onDismissRequest = { deleteCatTarget = null },
            title = { Text("删除分类") },
            text = {
                Text(
                    if (count > 0)
                        "「${target.name}」下还有 $count 件物品，删除后这些物品会移到「${fallback?.name ?: "其他"}」。此操作不可撤销。"
                    else
                        "确定删除分类「${target.name}」吗？此操作不可撤销。"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    store.deleteCategory(target.id)
                    deleteCatTarget = null
                    onMessage("已删除分类「${target.name}」")
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCatTarget = null }) { Text("取消") }
            }
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清空所有物品") },
            text = { Text("将删除 ${items.size} 件物品，分类和设置会保留。此操作不可撤销，建议先导出备份。") },
            confirmButton = {
                TextButton(onClick = {
                    store.clearAllItems()
                    confirmClear = false
                    onMessage("已清空物品")
                }) { Text("确认清空", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingAction(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (danger) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
