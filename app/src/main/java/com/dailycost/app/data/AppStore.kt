package com.dailycost.app.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TAG = "AppStore"

/** CSV 导入结果 */
data class ImportResult(val imported: Int, val skipped: Int)

/**
 * 数据仓库：物品、分类与设置，本地 JSON 持久化。
 */
class AppStore private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("dailycost", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _categories = MutableStateFlow<List<ItemCategory>>(emptyList())
    val categories: StateFlow<List<ItemCategory>> = _categories.asStateFlow()

    private val _items = MutableStateFlow<List<PurchasedItem>>(emptyList())
    val items: StateFlow<List<PurchasedItem>> = _items.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private var nextId = 1L
    private var nextCatId = 1L

    init {
        load()
    }

    // ---------- 增删改 ----------

    fun addItem(
        name: String,
        price: Double,
        date: LocalDate,
        categoryId: Long,
        note: String,
        retiredDate: LocalDate? = null,
        lifeDays: Int? = null
    ) {
        val item = PurchasedItem(
            id = nextId++,
            name = name,
            price = price,
            purchaseDate = date,
            categoryId = categoryId,
            note = note,
            retiredDate = retiredDate,
            lifeDays = lifeDays
        )
        _items.update { it + item }
        persist()
    }

    fun updateItem(item: PurchasedItem) {
        _items.update { list -> list.map { if (it.id == item.id) item else it } }
        persist()
    }

    fun deleteItem(id: Long) {
        _items.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    /** 标记退役（停止继续累积日均） */
    fun retireItem(id: Long, date: LocalDate = LocalDate.now()) {
        _items.update { list ->
            list.map { if (it.id == id) it.copy(retiredDate = date) else it }
        }
        persist()
    }

    /** 恢复为在用 */
    fun restoreItem(id: Long) {
        _items.update { list ->
            list.map { if (it.id == id) it.copy(retiredDate = null) else it }
        }
        persist()
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        persist()
    }

    /** 新建分类；重名时直接返回已有分类 */
    fun addCategory(name: String): ItemCategory {
        val trimmed = name.trim()
        _categories.value.find { it.name.equals(trimmed, ignoreCase = true) }?.let { return it }
        val id = nextCatId++
        val cat = ItemCategory(id, trimmed, CATEGORY_PALETTE[(id % CATEGORY_PALETTE.size).toInt()])
        _categories.update { it + cat }
        persist()
        return cat
    }

    fun deleteCategory(id: Long) {
        if (_categories.value.size <= 1) return
        val fallback = _categories.value.first { it.id != id }
        _items.update { list -> list.map { if (it.categoryId == id) it.copy(categoryId = fallback.id) else it } }
        _categories.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    /**
     * 调整分类显示顺序（决定新增条目时分类下拉框 / 筛选条的顺序）。
     * @param delta -1 上移，+1 下移
     */
    fun moveCategory(id: Long, delta: Int): Boolean {
        val list = _categories.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx < 0) return false
        val target = idx + delta
        if (target !in list.indices) return false
        val moved = list.removeAt(idx)
        list.add(target, moved)
        _categories.value = list
        persist()
        return true
    }

    // ---------- CSV / 备份 ----------

    /**
     * 导入格式（逗号或中文逗号分隔，可带表头）：
     *   名称,价格[,日期,分类]
     * 日期支持 yyyy-MM-dd / yyyy/M/d / yyyy.MM.dd / yyyy年M月d日，缺省为今天。
     */
    suspend fun importCsv(text: String): ImportResult = withContext(Dispatchers.IO) {
        var imported = 0
        var skipped = 0
        val newItems = mutableListOf<PurchasedItem>()
        text.replace("\uFEFF", "").split("\r\n", "\n").forEach { raw ->
            val line = raw.trim()
            if (line.isEmpty()) return@forEach
            val cols = splitCsvLine(line)
            if (cols.size < 2) { skipped++; return@forEach }
            val name = cols[0]
            val price = cols[1].filter { it in "0123456789." || it == '-' }.toDoubleOrNull()
            if (name.isEmpty() || price == null || price <= 0.0) { skipped++; return@forEach }
            val date = parseDate(cols.getOrNull(2)) ?: LocalDate.now()
            val catName = cols.getOrNull(3)?.takeIf { it.isNotEmpty() } ?: "其他"
            val cat = addCategory(catName)
            newItems += PurchasedItem(nextId++, name, price, date, cat.id)
            imported++
        }
        if (newItems.isNotEmpty()) {
            _items.update { it + newItems }
            persist()
        }
        Log.i(TAG, "importCsv: imported=$imported skipped=$skipped")
        ImportResult(imported, skipped)
    }

    private fun splitCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                (ch == ',' || ch == '，') && !inQuotes -> {
                    out += sb.toString().trim()
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        out += sb.toString().trim()
        return out
    }

    fun exportCsv(): String {
        val names = _categories.value.associate { it.id to it.name }
        val sb = StringBuilder("名称,价格,购买日期,分类,备注,退役日期,预期天数\n")
        _items.value.sortedBy { it.purchaseDate }.forEach { i ->
            sb.append(
                listOf(
                    escapeCsv(i.name),
                    String.format(Locale.CHINA, "%.2f", i.price),
                    i.purchaseDate.toString(),
                    escapeCsv(names[i.categoryId] ?: "其他"),
                    escapeCsv(i.note),
                    i.retiredDate?.toString() ?: "",
                    i.lifeDays?.toString() ?: ""
                ).joinToString(",")
            ).append('\n')
        }
        return sb.toString()
    }

    private fun escapeCsv(s: String): String =
        if (s.contains(',') || s.contains('"') || s.contains('\n')) {
            "\"" + s.replace("\"", "\"\"") + "\""
        } else s

    /** 导出完整备份 JSON */
    fun exportJson(): String = buildRoot().toString(2)

    /** 供 Web 界面读取的完整状态（与备份同构） */
    fun stateJson(): String = buildRoot().toString()

    /** Web 界面整体覆盖数据 */
    fun saveStateFromWeb(json: String) {
        applyRoot(JSONObject(json))
        persist()
    }

    /** 从备份 JSON 恢复，返回恢复的物品数 */
    fun restoreFromJson(text: String): Int {
        val root = JSONObject(text)
        applyRoot(root)
        persist()
        return _items.value.size
    }

    fun clearAllItems() {
        _items.value = emptyList()
        nextId = 1L
        persist()
    }

    /** 生成示例数据，方便上手体验 */
    fun loadSampleData() {
        val today = LocalDate.now()
        val cFood = addCategory("餐饮食品").id
        val cCloth = addCategory("服饰鞋包").id
        val cElec = addCategory("电子产品").id
        val cHome = addCategory("家居日用").id
        val cSport = addCategory("运动户外").id
        val samples = listOf(
            PurchasedItem(nextId++, "iPhone 15 Pro", 8999.0, today.minusDays(398), cElec, "首发入手，主力机", lifeDays = 1095),
            PurchasedItem(nextId++, "降噪耳机 AirPods Pro", 1899.0, today.minusDays(520), cElec, "通勤必备", lifeDays = 1095),
            PurchasedItem(nextId++, "机械键盘", 549.0, today.minusDays(240), cElec, "", lifeDays = 730),
            PurchasedItem(nextId++, "冬季羽绒服", 899.0, today.minusDays(300), cCloth, "很抗冻", lifeDays = 1095),
            PurchasedItem(nextId++, "跑鞋", 699.0, today.minusDays(150), cSport, "每周三次", lifeDays = 500),
            PurchasedItem(nextId++, "登山包 38L", 780.0, today.minusDays(88), cSport, ""),
            PurchasedItem(nextId++, "电饭煲", 499.0, today.minusDays(700), cHome, ""),
            PurchasedItem(nextId++, "人体工学椅", 1599.0, today.minusDays(410), cHome, "腰不酸了", lifeDays = 1825),
            PurchasedItem(nextId++, "电动牙刷", 329.0, today.minusDays(200), cHome, ""),
            PurchasedItem(nextId++, "旧笔记本电脑", 6299.0, today.minusDays(1500), cElec, "已更换", retiredDate = today.minusDays(200)),
            PurchasedItem(nextId++, "雨伞", 59.0, today.minusDays(45), cHome, "丢了", retiredDate = today.minusDays(3)),
            PurchasedItem(nextId++, "咖啡月卡", 199.0, today.minusDays(12), cFood, "每天一杯"),
            PurchasedItem(nextId++, "空气炸锅", 359.0, today.minusDays(95), cHome, "用得不多"),
            PurchasedItem(nextId++, "冲锋衣", 1299.0, today.minusDays(58), cCloth, "防水")
        )
        _items.update { it + samples }
        persist()
    }

    private fun parseDate(s: String?): LocalDate? {
        val v = s?.trim().takeUnless { it.isNullOrEmpty() } ?: return null
        return runCatching { LocalDate.parse(v) }.getOrElse {
            listOf("yyyy/M/d", "yyyy.MM.dd", "yyyy年M月d日", "yyyy/MM/dd").firstNotNullOfOrNull { p ->
                runCatching { LocalDate.parse(v, DateTimeFormatter.ofPattern(p)) }.getOrNull()
            }
        }
    }

    // ---------- 持久化 ----------

    private fun buildRoot(): JSONObject {
        val cats = JSONArray()
        _categories.value.forEach { c ->
            cats.put(JSONObject().put("id", c.id).put("name", c.name).put("color", c.colorArgb))
        }
        val arr = JSONArray()
        _items.value.forEach { i ->
            val o = JSONObject()
                .put("id", i.id)
                .put("name", i.name)
                .put("price", i.price)
                .put("date", i.purchaseDate.toString())
                .put("cat", i.categoryId)
                .put("note", i.note)
            i.retiredDate?.let { o.put("retired", it.toString()) }
            i.lifeDays?.let { o.put("life", it) }
            i.icon?.let { o.put("icon", it) }
            i.exitType?.let { o.put("exit", it) }
            i.resalePrice?.let { o.put("resale", it) }
            arr.put(o)
        }
        val s = _settings.value
        val set = JSONObject()
            .put("darkMode", s.darkMode)
            .put("accent", s.accent)
            .put("currency", s.currency)
            .put("showRetired", s.showRetired)
        return JSONObject()
            .put("version", 2)
            .put("categories", cats)
            .put("items", arr)
            .put("settings", set)
            .put("nextId", nextId)
            .put("nextCatId", nextCatId)
    }

    private fun persist() {
        val root = buildRoot()
        scope.launch {
            runCatching { prefs.edit().putString("data", root.toString()).apply() }
                .onFailure { Log.e(TAG, "persist failed: ${it.message}") }
        }
    }

    private fun load() {
        runCatching {
            val json = prefs.getString("data", null)
            if (json == null) {
                defaultData()
                return
            }
            applyRoot(JSONObject(json))
        }.onFailure {
            Log.e(TAG, "load failed, using defaults: ${it.message}")
            defaultData()
        }
    }

    private fun applyRoot(root: JSONObject) {
        val cats = mutableListOf<ItemCategory>()
        val catsArr = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until catsArr.length()) {
            val o = catsArr.getJSONObject(i)
            cats += ItemCategory(o.getLong("id"), o.getString("name"), o.optLong("color", 0xFF94A3B8))
        }
        val items = mutableListOf<PurchasedItem>()
        val arr = root.optJSONArray("items") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            items += PurchasedItem(
                id = o.getLong("id"),
                name = o.getString("name"),
                price = o.getDouble("price"),
                purchaseDate = LocalDate.parse(o.getString("date")),
                categoryId = o.getLong("cat"),
                note = o.optString("note", ""),
                retiredDate = o.optString("retired", "").takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) },
                lifeDays = if (o.has("life") && !o.isNull("life")) o.getInt("life") else null,
                icon = o.optString("icon", "").takeIf { it.isNotEmpty() },
                exitType = o.optString("exit", "").takeIf { it.isNotEmpty() },
                resalePrice = if (o.has("resale") && !o.isNull("resale")) o.getDouble("resale") else null
            )
        }
        root.optJSONObject("settings")?.let { s ->
            _settings.value = AppSettings(
                darkMode = s.optInt("darkMode", 0),
                accent = s.optInt("accent", 0),
                currency = s.optString("currency", "¥"),
                showRetired = s.optBoolean("showRetired", true)
            )
        }
        _categories.value = if (cats.isEmpty()) defaultCategories() else cats
        _items.value = items
        nextId = root.optLong("nextId", (items.maxOfOrNull { it.id } ?: 0L) + 1)
        nextCatId = root.optLong("nextCatId", (_categories.value.maxOfOrNull { it.id } ?: 0L) + 1)
    }

    private fun defaultCategories(): List<ItemCategory> {
        val names = listOf(
            "餐饮食品" to 0xFFF59E0B,
            "服饰鞋包" to 0xFF8B5CF6,
            "电子产品" to 0xFF3B82F6,
            "家居日用" to 0xFF10B981,
            "交通出行" to 0xFF64748B,
            "医疗健康" to 0xFFEF4444,
            "娱乐爱好" to 0xFFEC4899,
            "其他" to 0xFF94A3B8
        )
        return names.mapIndexed { idx, (n, c) -> ItemCategory(idx + 1L, n, c) }
    }

    private fun defaultData() {
        _categories.value = defaultCategories()
        nextCatId = _categories.value.size + 1L
        _items.value = emptyList()
        nextId = 1L
        persist()
    }

    companion object {
        private val CATEGORY_PALETTE = longArrayOf(
            0xFFF59E0B, 0xFF8B5CF6, 0xFF3B82F6, 0xFF10B981,
            0xFF64748B, 0xFFEF4444, 0xFFEC4899, 0xFF14B8A6,
            0xFFEAB308, 0xFFF97316, 0xFF06B6D4, 0xFFA855F7
        )

        @Volatile
        private var instance: AppStore? = null

        fun get(context: Context): AppStore =
            instance ?: synchronized(this) {
                instance ?: AppStore(context).also { instance = it }
            }
    }
}
