package com.yandex.retailmedia.sample.presentation.common

import java.util.Locale

object Money {

    fun format(amount: Double, currencyId: String): String {
        val symbol = when (currencyId.uppercase(Locale.ROOT)) {
            "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            else -> currencyId
        }
        val number = if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%,d", amount.toLong())
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
        return "${number.replace(',', ' ')} $symbol"
    }
}
