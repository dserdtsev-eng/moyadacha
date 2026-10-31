package ru.sntnadezhda.dacha.data.repository

import ru.sntnadezhda.dacha.data.model.Plot
import ru.sntnadezhda.dacha.data.model.SntData
import ru.sntnadezhda.dacha.data.remote.JsonParser
import ru.sntnadezhda.dacha.data.remote.WebDavClient
import ru.sntnadezhda.dacha.security.PasswordVerifier

class SntRepository(
    private val email: String,
    private val password: String
) {
    private val client = WebDavClient(email, password)

    /**
     * Скачивает и парсит базу.
     * Возвращает готовый SntData или ошибку.
     */
    suspend fun loadData(): Result<SntData> {
        return client.downloadJson().mapCatching { jsonString ->
            JsonParser.parse(jsonString)
        }
    }

    /**
     * Ищет участок по улице и дому.
     * Возвращает пару (номер участка, объект) или null.
     */
    fun findPlot(data: SntData, street: String, house: String): Pair<String, Plot>? {
        val key = "${street.trim().lowercase()}|${house.trim().lowercase()}"
        for ((plotNumber, plot) in data.plots) {
            val plotKey = "${plot.street.trim().lowercase()}|${plot.house.trim().lowercase()}"
            if (plotKey == key) {
                return plotNumber to plot
            }
        }
        return null
    }

    /**
     * Проверяет пароль для выбранного участка.
     */
    fun verifyPassword(plot: Plot, password: String): Boolean {
        return PasswordVerifier.verify(
            password = password,
            storedHash = plot.passportHash,
            storedSalt = plot.passportSalt
        )
    }

    /**
     * Уникальные отсортированные улицы из базы.
     */
    fun listStreets(data: SntData): List<String> {
        return data.plots.values
            .map { it.street.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    /**
     * Уникальные отсортированные дома для выбранной улицы.
     */
    fun listHouses(data: SntData, street: String): List<String> {
        return data.plots.values
            .filter { it.street.trim().equals(street.trim(), ignoreCase = true) }
            .map { it.house.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
    }
}