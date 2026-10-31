package ru.sntnadezhda.dacha.data.model

/**
 * Что сохраняем в SharedPreferences, когда пользователь ставит
 * галочку «Запомнить мой выбор».
 * Пароль НЕ сохраняется.
 */
data class SavedSelection(
    val street: String,
    val house: String
)