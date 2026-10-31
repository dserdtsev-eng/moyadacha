package ru.sntnadezhda.dacha.data.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyUtils {

    private val fmt: DecimalFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("ru")))

    /** 14500.0 → «14 500,00» */
    fun format(amount: Double): String = fmt.format(amount)

    /** 14500.0 → «14 500,00 ₽» */
    fun formatRub(amount: Double): String = "${fmt.format(amount)} ₽"

    /** Для счётчиков: 100000.0 → «100 000», 123.45 → «123,45» */
    fun formatMeter(v: Double): String {
        return if (v == v.toLong().toDouble()) {
            DecimalFormat("#,##0", DecimalFormatSymbols(Locale("ru"))).format(v)
        } else {
            fmt.format(v)
        }
    }
}