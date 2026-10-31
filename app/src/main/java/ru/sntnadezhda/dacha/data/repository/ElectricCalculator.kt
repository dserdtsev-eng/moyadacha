package ru.sntnadezhda.dacha.data.repository

import ru.sntnadezhda.dacha.data.model.MeterReading
import ru.sntnadezhda.dacha.data.model.Payment
import ru.sntnadezhda.dacha.data.model.Tariff

object ElectricCalculator {

    private const val ELECTRIC_KEYWORD = "электр"

    fun isElectric(desc: String) = ELECTRIC_KEYWORD in desc.lowercase()

    /** Находит тариф, действовавший на указанную дату. */
    fun tariffForDate(tariffs: List<Tariff>, dateIso: String): Tariff? {
        if (dateIso.isBlank()) return null
        var current: Tariff? = null
        for (t in tariffs.sortedBy { it.date }) {
            if (t.date <= dateIso) current = t else break
        }
        return current
    }

    /**
     * Самая поздняя дата замены счётчика по всем счётчикам участка.
     * Если замен не было — возвращает пустую строку.
     */
    fun lastReplacementDate(
        readings: List<MeterReading>,
        meterIds: List<String>
    ): String {
        var latest = ""
        for (r in readings) {
            if (!r.replaced) continue
            // Проверяем, что запись относится хотя бы к одному из счётчиков участка
            val belongs = meterIds.any { r.values.containsKey(it) }
            if (!belongs) continue
            if (r.date > latest) latest = r.date
        }
        return latest
    }

    /** Оплачено кВт·ч по платежам после указанной даты (включительно). */
    fun paidKwh(
        payments: List<Payment>,
        tariffs: List<Tariff>,
        sinceDateIso: String
    ): Pair<Double, List<PaidDetail>> {
        var total = 0.0
        val details = mutableListOf<PaidDetail>()
        for (p in payments) {
            if (!isElectric(p.description)) continue
            if (sinceDateIso.isNotEmpty() && p.date < sinceDateIso) continue
            val t = tariffForDate(tariffs, p.date) ?: continue
            if (t.price <= 0) continue
            val kwh = p.amount / t.price
            total += kwh
            details += PaidDetail(p.date, p.amount, t.price, kwh)
        }
        return total to details
    }

    /**
     * Израсходовано кВт·ч по показаниям.
     *
     * Логика: считаем только дельты ПОСЛЕ последней замены счётчика.
     * Всё, что было до неё, отбрасывается.
     */
    fun consumptionKwh(
        readings: List<MeterReading>,
        meterIds: List<String>
    ): Pair<Double, List<ConsumptionDetail>> {
        var total = 0.0
        val details = mutableListOf<ConsumptionDetail>()

        for (mid in meterIds) {
            val rows = readings
                .filter { it.values.containsKey(mid) }
                .sortedBy { it.date }

            if (rows.isEmpty()) continue

            // Индекс последнего показания с replaced = true.
            var lastReplacedIndex = -1
            for ((idx, r) in rows.withIndex()) {
                if (r.replaced) lastReplacedIndex = idx
            }

            val startIdx = if (lastReplacedIndex >= 0) lastReplacedIndex else 0

            var prev: Double? = null
            var prevDate: String? = null

            for (i in startIdx until rows.size) {
                val r = rows[i]
                val v = r.values[mid] ?: continue

                if (prev != null && !r.replaced) {
                    val delta = v - prev!!
                    if (delta > 0) {
                        total += delta
                        details += ConsumptionDetail(
                            meterId = mid,
                            fromDate = prevDate ?: "",
                            toDate = r.date,
                            fromValue = prev!!,
                            toValue = v,
                            delta = delta
                        )
                    }
                }

                prev = v
                prevDate = r.date
            }
        }
        return total to details
    }

    data class PaidDetail(
        val date: String,
        val amount: Double,
        val price: Double,
        val kwh: Double
    )

    data class ConsumptionDetail(
        val meterId: String,
        val fromDate: String,
        val toDate: String,
        val fromValue: Double,
        val toValue: Double,
        val delta: Double
    )
}