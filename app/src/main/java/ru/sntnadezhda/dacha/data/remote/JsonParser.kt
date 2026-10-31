package ru.sntnadezhda.dacha.data.remote

import kotlinx.serialization.json.Json
import ru.sntnadezhda.dacha.data.model.SntData

object JsonParser {

    val json: Json = Json {
        ignoreUnknownKeys = true   // не падать на незнакомых полях
        isLenient = true           // терпимость к формату
        coerceInputValues = true   // null → значение по умолчанию
        explicitNulls = false      // не требовать null-поля
    }

    fun parse(jsonString: String): SntData {
        return json.decodeFromString<SntData>(jsonString)
    }
}