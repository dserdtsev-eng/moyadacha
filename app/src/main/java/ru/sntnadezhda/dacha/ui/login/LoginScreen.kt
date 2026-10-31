package ru.sntnadezhda.dacha.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginUiState,
    onStreetSelected: (String) -> Unit,
    onHouseSelected: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberMeChanged: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Моя дача",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "СНТ «Надежда»",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            when {
                state.isLoading -> LoadingBlock()
                state.sntData == null -> ErrorBlock(
                    message = state.errorMessage ?: "Нет данных",
                    onRetry = onRetryClick
                )
                else -> LoginForm(
                    state = state,
                    onStreetSelected = onStreetSelected,
                    onHouseSelected = onHouseSelected,
                    onPasswordChanged = onPasswordChanged,
                    onRememberMeChanged = onRememberMeChanged,
                    onLoginClick = onLoginClick
                )
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    CircularProgressIndicator()
    Spacer(Modifier.height(16.dp))
    Text("Загрузка данных…", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Ошибка",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onRetry) {
        Text("Повторить")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginForm(
    state: LoginUiState,
    onStreetSelected: (String) -> Unit,
    onHouseSelected: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberMeChanged: (Boolean) -> Unit,
    onLoginClick: () -> Unit
) {
    // ── Улица ──
    DropdownField(
        label = "Улица",
        selected = state.selectedStreet,
        options = state.streets,
        onSelect = onStreetSelected
    )

    Spacer(Modifier.height(12.dp))

    // ── Дом ──
    DropdownField(
        label = "Дом",
        selected = state.selectedHouse,
        options = state.houses,
        enabled = state.selectedStreet != null,
        onSelect = onHouseSelected
    )

    Spacer(Modifier.height(12.dp))

    // ── Пароль ──
    OutlinedTextField(
        value = state.password,
        onValueChange = onPasswordChanged,
        label = { Text("Пароль (4 цифры)") },
        singleLine = true,
        isError = state.isPasswordError,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        supportingText = {
            if (state.isPasswordError) {
                Text("Неверный пароль", color = MaterialTheme.colorScheme.error)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))

    // ── Запомнить мой выбор ──
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = state.rememberMe,
            onCheckedChange = onRememberMeChanged
        )
        Text(
            text = "Запомнить мой выбор",
            modifier = Modifier.padding(start = 4.dp)
        )
    }

    Spacer(Modifier.height(16.dp))

    // ── Войти ──
    val canLogin = state.selectedStreet != null
            && state.selectedHouse != null
            && state.password.length == 4
    Button(
        onClick = onLoginClick,
        enabled = canLogin,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text("Войти", fontSize = 16.sp)
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = "Пароль выдаёт председатель СНТ",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selected: String?,
    options: List<String>,
    enabled: Boolean = true,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                )
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("— нет вариантов —") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}