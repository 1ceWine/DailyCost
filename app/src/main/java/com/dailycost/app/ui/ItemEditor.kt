package com.dailycost.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailycost.app.data.AppStore
import com.dailycost.app.data.PurchasedItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorDialog(
    store: AppStore,
    editing: PurchasedItem?,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit = {}
) {
    val categories = store.categories.collectAsState().value
    val settings = store.settings.collectAsState().value

    var name by remember { mutableStateOf(editing?.name ?: "") }
    var priceText by remember {
        mutableStateOf(
            editing?.price?.let { if (it % 1.0 == 0.0) "%.0f".format(it) else "$it" } ?: ""
        )
    }
    var date by remember { mutableStateOf(editing?.purchaseDate ?: LocalDate.now()) }
    var selectedCatId by remember { mutableStateOf(editing?.categoryId ?: categories.firstOrNull()?.id) }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var lifeText by remember { mutableStateOf(editing?.lifeDays?.toString() ?: "") }
    var retired by remember { mutableStateOf(editing?.retiredDate != null) }
    var retiredDate by remember { mutableStateOf(editing?.retiredDate ?: LocalDate.now()) }

    var catMenuOpen by remember { mutableStateOf(false) }
    var creatingCat by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var purchasePickerOpen by remember { mutableStateOf(false) }
    var retirePickerOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val price = priceText.toDoubleOrNull()
    val lifeDays = lifeText.trim().toIntOrNull()
    val inputValid = name.isNotBlank() && price != null && price > 0.0 && selectedCatId != null

    fun save() {
        val catId = selectedCatId ?: return
        if (!inputValid) return
        val retireAt = if (retired) maxOf(retiredDate, date) else null
        if (editing == null) {
            store.addItem(
                name = name.trim(),
                price = price!!,
                date = date,
                categoryId = catId,
                note = note.trim(),
                retiredDate = retireAt,
                lifeDays = lifeDays?.takeIf { it > 0 }
            )
        } else {
            store.updateItem(
                editing.copy(
                    name = name.trim(),
                    price = price!!,
                    purchaseDate = date,
                    categoryId = catId,
                    note = note.trim(),
                    retiredDate = retireAt,
                    lifeDays = lifeDays?.takeIf { it > 0 }
                )
            )
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "添加物品" else "编辑物品") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { v -> priceText = v.filter { it.isDigit() || it == '.' } },
                    label = { Text("价格（${settings.currency}）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = date.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("购买日期") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { purchasePickerOpen = true }, modifier = Modifier.padding(start = 6.dp)) {
                        Text("选择")
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = catMenuOpen,
                    onExpandedChange = { catMenuOpen = it }
                ) {
                    val selectedName = if (creatingCat && newCatName.isNotBlank()) {
                        newCatName
                    } else {
                        categories.find { it.id == selectedCatId }?.name ?: ""
                    }
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分类") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catMenuOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catMenuOpen,
                        onDismissRequest = { catMenuOpen = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCatId = cat.id
                                    creatingCat = false
                                    catMenuOpen = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("＋ 新建分类…") },
                            onClick = {
                                creatingCat = true
                                catMenuOpen = false
                            }
                        )
                    }
                }

                if (creatingCat) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            label = { Text("新分类名称") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                if (newCatName.isNotBlank()) {
                                    val cat = store.addCategory(newCatName)
                                    selectedCatId = cat.id
                                    creatingCat = false
                                    newCatName = ""
                                }
                            },
                            modifier = Modifier.padding(start = 6.dp)
                        ) { Text("添加") }
                    }
                }

                OutlinedTextField(
                    value = lifeText,
                    onValueChange = { v -> lifeText = v.filter { it.isDigit() }.take(5) },
                    label = { Text("预期使用天数（可选）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "填写后可在列表里看到回本进度，例如 3 年 ≈ 1095 天",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("已退役", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "退役后停止计算新的日均",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = retired, onCheckedChange = { retired = it })
                }

                if (retired) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = retiredDate.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("退役日期") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { retirePickerOpen = true }, modifier = Modifier.padding(start = 6.dp)) {
                            Text("选择")
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { save() }, enabled = inputValid) { Text("保存") }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (editing != null) {
                    IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "删除该物品",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )

    if (purchasePickerOpen) {
        DatePickerModal(
            initial = date,
            onDismiss = { purchasePickerOpen = false },
            onConfirm = {
                date = it
                purchasePickerOpen = false
            }
        )
    }
    if (retirePickerOpen) {
        DatePickerModal(
            initial = retiredDate,
            onDismiss = { retirePickerOpen = false },
            onConfirm = {
                retiredDate = it
                retirePickerOpen = false
            }
        )
    }

    if (confirmDelete && editing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除物品") },
            text = { Text("确定删除「${editing.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    store.deleteItem(editing.id)
                    confirmDelete = false
                    onDeleted()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { ms ->
                    onConfirm(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate())
                } ?: onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    ) {
        DatePicker(state = state)
    }
}
