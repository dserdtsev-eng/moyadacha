package ru.sntnadezhda.dacha.data.repository

import android.content.Context
import ru.sntnadezhda.dacha.data.model.SavedSelection

/**
 * Хранит выбор «улица + дом» в SharedPreferences.
 * Пароль не хранится никогда.
 */
class SelectionStorage(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("moya_dacha_prefs", Context.MODE_PRIVATE)

    fun save(street: String, house: String) {
        prefs.edit()
            .putString(KEY_STREET, street)
            .putString(KEY_HOUSE, house)
            .apply()
    }

    fun load(): SavedSelection? {
        val street = prefs.getString(KEY_STREET, null) ?: return null
        val house = prefs.getString(KEY_HOUSE, null) ?: return null
        return SavedSelection(street, house)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_STREET = "saved_street"
        private const val KEY_HOUSE = "saved_house"
    }
}