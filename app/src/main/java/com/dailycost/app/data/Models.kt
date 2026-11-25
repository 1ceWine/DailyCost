package com.dailycost.app.data

import java.time.LocalDate

/** 物品分类 */
data class ItemCategory(
    val id: Long,
    val name: String,
    val colorArgb: Long
)

/** 一件购入的物品 */
data class PurchasedItem(
    val id: Long,
    val name: String,
    val price: Double,
    val purchaseDate: LocalDate,
    val categoryId: Long,
    val note: String = "",
    /** 退役（丢弃/送人/报废）日期，null 表示仍在用 */
    val retiredDate: LocalDate? = null,
    /** 预期使用天数，可选，用于回本进度 */
    val lifeDays: Int? = null,
    /** 图标 key，为空时用名称首字 */
    val icon: String? = null,
    /** 离开方式：sold = 已售出，其余（含 null）视为报废 */
    val exitType: String? = null,
    /** 二手售出价格，用于抵扣成本 */
    val resalePrice: Double? = null
) {
    val isActive: Boolean get() = retiredDate == null

    /** 计价截止日：在用算到今天，退役算到退役日 */
    val endDate: LocalDate get() = retiredDate ?: LocalDate.now()

    /** 已持有天数（含购买当天，至少 1 天） */
    val daysHeld: Int
        get() = maxOf(
            1,
            (endDate.toEpochDay() - purchaseDate.toEpochDay()).toInt() + 1
        )

    /** 平均每日花费 */
    val dailyCost: Double get() = price / daysHeld

    /** 回本进度 0~1（未设置预期寿命时为 null） */
    val lifeProgress: Float?
        get() = lifeDays?.takeIf { it > 0 }?.let { (daysHeld.toFloat() / it).coerceIn(0f, 1f) }

    /** 距离预期寿命还剩多少天（可为负） */
    val daysLeft: Int?
        get() = lifeDays?.let { it - daysHeld }

    /** 已用成本（按回本进度累计） */
    val amortized: Double
        get() = price * (lifeProgress ?: 0f)
}

/** 应用设置 */
data class AppSettings(
    /** 0 跟随系统 / 1 浅色 / 2 深色 */
    val darkMode: Int = 0,
    /** 主题配色索引 */
    val accent: Int = 0,
    /** 货币符号 */
    val currency: String = "¥",
    /** 列表中是否显示已退役物品 */
    val showRetired: Boolean = true
)
