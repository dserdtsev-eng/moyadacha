package ru.sntnadezhda.dacha.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SntData(
    val plots: Map<String, Plot> = emptyMap(),
    val payments: List<Payment> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    @SerialName("other_income") val otherIncome: List<OtherIncome> = emptyList(),
    val tariffs: List<Tariff> = emptyList(),
    val settings: Settings = Settings()
)

@Serializable
data class Plot(
    val owner: String = "",
    val street: String = "",
    val house: String = "",
    val note: String = "",
    val phone: String = "",
    val residence: String = "—",
    @SerialName("member_promo") val memberPromo: Double = 0.0,
    @SerialName("member_promo_note") val memberPromoNote: String = "",
    @SerialName("passport_hash") val passportHash: String = "",
    @SerialName("passport_salt") val passportSalt: String = "",
    val meters: List<Meter> = emptyList(),
    @SerialName("meter_readings") val meterReadings: List<MeterReading> = emptyList()
)

@Serializable
data class Meter(
    val id: Int = 1,
    val name: String = "Счётчик"
)

@Serializable
data class MeterReading(
    val date: String = "",
    val values: Map<String, Double> = emptyMap(),
    val replaced: Boolean = false
)

@Serializable
data class Payment(
    val plot: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val period: String = ""
)

@Serializable
data class Expense(
    val id: Int = 0,
    val date: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = ""
)

@Serializable
data class OtherIncome(
    val id: Int = 0,
    val date: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = ""
)

@Serializable
data class Tariff(
    val date: String = "",
    val price: Double = 0.0,
    val note: String = ""
)

@Serializable
data class Requisites(
    val name: String = "",
    @SerialName("personal_acc") val personalAcc: String = "",
    @SerialName("bank_name") val bankName: String = "",
    val bic: String = "",
    @SerialName("corresp_acc") val correspAcc: String = "",
    val inn: String = "",
    val kpp: String = ""
)

@Serializable
data class Settings(
    @SerialName("member_fee") val memberFee: Double = 0.0,
    @SerialName("target_fee") val targetFee: Double = 0.0,
    @SerialName("fee_start_date") val feeStartDate: String = "",
    @SerialName("member_deadline") val memberDeadline: String = "",
    @SerialName("target_deadline") val targetDeadline: String = "",
    @SerialName("electric_day") val electricDay: Int = 20,
    @SerialName("prev_member_fee") val prevMemberFee: Double = 0.0,
    @SerialName("prev_target_fee") val prevTargetFee: Double = 0.0,
    @SerialName("prev_fee_start_date") val prevFeeStartDate: String = "",
    val requisites: Requisites = Requisites()
)