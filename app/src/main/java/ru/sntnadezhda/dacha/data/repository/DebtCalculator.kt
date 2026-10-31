package ru.sntnadezhda.dacha.data.repository

import ru.sntnadezhda.dacha.data.model.DateUtils
import ru.sntnadezhda.dacha.data.model.Payment
import ru.sntnadezhda.dacha.data.model.Plot
import ru.sntnadezhda.dacha.data.model.Settings

object DebtCalculator {

    private const val ELECTRIC_KEYWORD = "электр"
    private const val MEMBER_KEYWORD = "членск"
    private const val TARGET_KEYWORD = "целев"

    private fun isElectric(desc: String) = ELECTRIC_KEYWORD in desc.lowercase()
    private fun isMember(desc: String) = MEMBER_KEYWORD in desc.lowercase()
    private fun isTarget(desc: String) = TARGET_KEYWORD in desc.lowercase()

    /** Округление до копеек — чтобы не показывать «0,00 ₽» из-за мусора в double. */
    private fun round2(v: Double): Double = Math.round(v * 100.0) / 100.0

    private fun paymentPeriod(p: Payment, s: Settings): String? {
        val explicit = p.period.trim().lowercase()
        if (explicit == "prev" || explicit == "current") return explicit

        val pd = DateUtils.parse(p.date) ?: return null
        val curStart = DateUtils.parse(s.feeStartDate)
        val curEnd = DateUtils.periodEnd(curStart)
        if (curStart != null && curEnd != null &&
            !pd.isBefore(curStart) && !pd.isAfter(curEnd)) return "current"

        val prevStart = DateUtils.parse(s.prevFeeStartDate)
        val prevEnd = DateUtils.periodEnd(prevStart)
        if (prevStart != null && prevEnd != null &&
            !pd.isBefore(prevStart) && !pd.isAfter(prevEnd)) return "prev"

        if (curStart == null) return "current"
        return null
    }

    private fun sum(
        payments: List<Payment>,
        period: String,
        predicate: (String) -> Boolean,
        s: Settings
    ): Double = payments
        .filter { predicate(it.description) && paymentPeriod(it, s) == period }
        .sumOf { it.amount }

    fun memberDebt(plotNumber: String, plot: Plot, allPayments: List<Payment>, s: Settings): Double {
        if (s.memberFee <= 0 || DateUtils.parse(s.feeStartDate) == null) return 0.0
        val paid = sum(allPayments.filter { it.plot == plotNumber }, "current", ::isMember, s)
        return round2((s.memberFee - plot.memberPromo - paid).coerceAtLeast(0.0))
    }

    fun targetDebt(plotNumber: String, allPayments: List<Payment>, s: Settings): Double {
        if (s.targetFee <= 0 || DateUtils.parse(s.feeStartDate) == null) return 0.0
        val paid = sum(allPayments.filter { it.plot == plotNumber }, "current", ::isTarget, s)
        return round2((s.targetFee - paid).coerceAtLeast(0.0))
    }

    fun prevMemberDebt(plotNumber: String, allPayments: List<Payment>, s: Settings): Double {
        if (s.prevMemberFee <= 0 || DateUtils.parse(s.prevFeeStartDate) == null) return 0.0
        val paid = sum(allPayments.filter { it.plot == plotNumber }, "prev", ::isMember, s)
        return round2((s.prevMemberFee - paid).coerceAtLeast(0.0))
    }

    fun prevTargetDebt(plotNumber: String, allPayments: List<Payment>, s: Settings): Double {
        if (s.prevTargetFee <= 0 || DateUtils.parse(s.prevFeeStartDate) == null) return 0.0
        val paid = sum(allPayments.filter { it.plot == plotNumber }, "prev", ::isTarget, s)
        return round2((s.prevTargetFee - paid).coerceAtLeast(0.0))
    }
}