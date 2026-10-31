package ru.sntnadezhda.dacha.ui.dashboard

import ru.sntnadezhda.dacha.data.model.Meter
import ru.sntnadezhda.dacha.data.model.MeterReading
import ru.sntnadezhda.dacha.data.model.Payment
import ru.sntnadezhda.dacha.data.model.Plot
import ru.sntnadezhda.dacha.data.repository.ElectricCalculator

/**
 * Всё, что нужно садоводу для отображения — собрано в одном месте.
 * Заполняется один раз после успешного логина.
 */
data class DashboardData(
    val plotNumber: String,
    val plot: Plot,

    // Долги
    val memberDebt: Double,
    val targetDebt: Double,
    val prevMemberDebt: Double,
    val prevTargetDebt: Double,

    // Платежи
    val payments: List<Payment>,

    // Показания
    val meters: List<Meter>,
    val readings: List<MeterReading>,

    // Сроки оплаты
    val memberDeadline: String,
    val targetDeadline: String,
    val electricDay: Int,
    val feeStartDate: String,
    val memberFee: Double,
    val targetFee: Double,

    // Электричество
    val lastReplacementDate: String,
    val paidKwh: Double,
    val consumedKwh: Double,
    val paidDetails: List<ElectricCalculator.PaidDetail>,
    val consumptionDetails: List<ElectricCalculator.ConsumptionDetail>,

    // Тариф на сегодня (для расчёта оплаты по QR)
    val tariff: Double,

    // Реквизиты СНТ для QR-оплаты (приходят из snt_data.json)
    val reqName: String,
    val reqPersonalAcc: String,
    val reqBankName: String,
    val reqBic: String,
    val reqCorrespAcc: String,
    val reqInn: String,
    val reqKpp: String
) {
    val totalCurrentDebt: Double get() = memberDebt + targetDebt
    val totalPrevDebt: Double get() = prevMemberDebt + prevTargetDebt
    val totalDebt: Double get() = totalCurrentDebt + totalPrevDebt

    /** Положительный — переплата, отрицательный — недоплата. */
    val kwhBalance: Double get() = paidKwh - consumedKwh
}