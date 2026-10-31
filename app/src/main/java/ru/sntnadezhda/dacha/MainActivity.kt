package ru.sntnadezhda.dacha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sntnadezhda.dacha.ui.dashboard.AboutScreen
import ru.sntnadezhda.dacha.ui.dashboard.DashboardScreen
import ru.sntnadezhda.dacha.ui.login.LoginScreen
import ru.sntnadezhda.dacha.ui.login.LoginViewModel
import ru.sntnadezhda.dacha.ui.theme.МояДачаTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            МояДачаTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val vm: LoginViewModel = viewModel()
    val state by vm.state.collectAsState()

    // dashboard берём из ViewModel — она переживает поворот экрана
    val dashboard by vm.dashboard.collectAsState()

    // rememberSaveable тоже переживает поворот
    var showAbout by rememberSaveable { mutableStateOf(false) }

    when {
        showAbout -> {
            AboutScreen(onBack = { showAbout = false })
        }

        dashboard == null -> {
            LoginScreen(
                state = state,
                onStreetSelected = vm::onStreetSelected,
                onHouseSelected = vm::onHouseSelected,
                onPasswordChanged = vm::onPasswordChanged,
                onRememberMeChanged = vm::onRememberMeChanged,
                onLoginClick = vm::login,
                onRetryClick = vm::loadData
            )
        }

        else -> {
            DashboardScreen(
                data = dashboard!!,
                onLogout = { vm.onLogout() },
                onAbout = { showAbout = true },
                onRefresh = { vm.loadData() }
            )
        }
    }
}