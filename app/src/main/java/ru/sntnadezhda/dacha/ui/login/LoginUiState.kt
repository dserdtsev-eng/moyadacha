package ru.sntnadezhda.dacha.ui.login

import ru.sntnadezhda.dacha.data.model.SntData

/**
 * Состояние экрана входа.
 * Compose перерисовывает экран, когда состояние меняется.
 */
data class LoginUiState(
    val isLoading: Boolean = false,        // идёт загрузка JSON
    val errorMessage: String? = null,      // текст ошибки
    val sntData: SntData? = null,          // загруженная база
    val streets: List<String> = emptyList(),
    val houses: List<String> = emptyList(),
    val selectedStreet: String? = null,
    val selectedHouse: String? = null,
    val password: String = "",
    val rememberMe: Boolean = false,
    val loginSuccess: Boolean = false,
    val isPasswordError: Boolean = false
)