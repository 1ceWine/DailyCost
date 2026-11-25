package com.dailycost.app.ui

import java.util.Locale

/** 金额格式化，例如 ¥1,299.00 */
fun money(v: Double, symbol: String = "¥"): String =
    String.format(Locale.CHINA, "%s%,.2f", symbol, v)

/** 紧凑金额，例如 ¥1.3万 */
fun moneyCompact(v: Double, symbol: String = "¥"): String = when {
    v >= 100_000_000 -> String.format(Locale.CHINA, "%s%.2f亿", symbol, v / 100_000_000)
    v >= 10_000 -> String.format(Locale.CHINA, "%s%.2f万", symbol, v / 10_000)
    else -> String.format(Locale.CHINA, "%s%,.0f", symbol, v)
}

/** 日均显示，保留到角 */
fun dailyText(v: Double, symbol: String): String = String.format(Locale.CHINA, "%s%.2f/天", symbol, v)
