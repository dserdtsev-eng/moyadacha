package ru.sntnadezhda.dacha.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sntnadezhda.dacha.BuildConfig
import ru.sntnadezhda.dacha.data.model.DateUtils
import ru.sntnadezhda.dacha.data.model.SntData
import ru.sntnadezhda.dacha.data.repository.DebtCalculator
import ru.sntnadezhda.dacha.data.repository.ElectricCalculator
import ru.sntnadezhda.dacha.data.repository.SelectionStorage
import ru.sntnadezhda.dacha.data.repository.SntRepository
import ru.sntnadezhda.dacha.ui.dashboard.DashboardData
import java.time.LocalDate

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val storage = SelectionStorage(app)
    private val repository = SntRepository(
        email = BuildConfig.MAILRU_EMAIL,
        password = BuildConfig.MAILRU_PASSWORD
    )

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    // Dashboard храним здесь — ViewModel переживает поворот экрана
    private val _dashboard = MutableStateFlow<DashboardData?>(null)
    val dashboard: StateFlow<DashboardData?> = _dashboard.asStateFlow()

    // Номер участка, открытого в кабинете (null = никто не вошёл)
    private var currentPlotNumber: String? = null

    init {
        storage.load()?.let { saved ->
            _state.update {
                it.copy(
                    selectedStreet = saved.street,
                    selectedHouse = saved.house,
                    rememberMe = true
                )
            }
        }
        loadData()
    }

    /** Загрузка JSON из облака Mail.ru. */
    fun loadData() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.loadData()
                .onSuccess { data -> onDataLoaded(data) }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Не удалось загрузить данные: ${e.message ?: "неизвестная ошибка"}"
                        )
                    }
                }
        }
    }

    private fun onDataLoaded(data: SntData) {
        val streets = repository.listStreets(data)
        val currentStreet = _state.value.selectedStreet
        val houses = currentStreet?.let { repository.listHouses(data, it) } ?: emptyList()

        val validStreet = currentStreet?.takeIf { it in streets }
        val validHouse = _state.value.selectedHouse?.takeIf { it in houses }

        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                sntData = data,
                streets = streets,
                houses = houses,
                selectedStreet = validStreet,
                selectedHouse = validHouse
            )
        }

        // Если пользователь уже в кабинете — пересобрать dashboard для его участка
        val pn = currentPlotNumber
        if (pn != null) {
            val plot = data.plots[pn]
            if (plot != null) {
                _dashboard.value = buildDashboard(data, pn, plot)
            }
        }
    }

    /** Собирает DashboardData из свежего SntData. */
    private fun buildDashboard(
        data: SntData,
        plotNumber: String,
        plot: ru.sntnadezhda.dacha.data.model.Plot
    ): DashboardData {
        val settings = data.settings
        val plotPayments = data.payments.filter { it.plot == plotNumber }

        val meterIds = plot.meters.map { it.id.toString() }
        val lastReplacement = ElectricCalculator.lastReplacementDate(
            plot.meterReadings, meterIds
        )
        val paidPair = ElectricCalculator.paidKwh(
            plotPayments, data.tariffs, lastReplacement
        )
        val consumedPair = ElectricCalculator.consumptionKwh(
            plot.meterReadings, meterIds
        )

        // Тариф, действующий на сегодняшнюю дату (для расчёта оплаты по QR)
        val todayIso = DateUtils.format(LocalDate.now())
        val todayTariff = ElectricCalculator.tariffForDate(data.tariffs, todayIso)?.price ?: 0.0

        return DashboardData(
            plotNumber = plotNumber,
            plot = plot,
            memberDebt = DebtCalculator.memberDebt(plotNumber, plot, data.payments, settings),
            targetDebt = DebtCalculator.targetDebt(plotNumber, data.payments, settings),
            prevMemberDebt = DebtCalculator.prevMemberDebt(plotNumber, data.payments, settings),
            prevTargetDebt = DebtCalculator.prevTargetDebt(plotNumber, data.payments, settings),
            payments = plotPayments,
            meters = plot.meters,
            readings = plot.meterReadings,
            memberDeadline = settings.memberDeadline,
            targetDeadline = settings.targetDeadline,
            electricDay = settings.electricDay,
            feeStartDate = settings.feeStartDate,
            memberFee = settings.memberFee,
            targetFee = settings.targetFee,
            lastReplacementDate = lastReplacement,
            paidKwh = paidPair.first,
            consumedKwh = consumedPair.first,
            paidDetails = paidPair.second,
            consumptionDetails = consumedPair.second,
            tariff = todayTariff,

            // Реквизиты СНТ — приходят из snt_data.json (settings.requisites)
            reqName = settings.requisites.name,
            reqPersonalAcc = settings.requisites.personalAcc,
            reqBankName = settings.requisites.bankName,
            reqBic = settings.requisites.bic,
            reqCorrespAcc = settings.requisites.correspAcc,
            reqInn = settings.requisites.inn,
            reqKpp = settings.requisites.kpp
        )
    }

    fun onStreetSelected(street: String) {
        val data = _state.value.sntData ?: return
        val houses = repository.listHouses(data, street)
        _state.update {
            it.copy(
                selectedStreet = street,
                selectedHouse = null,
                houses = houses,
                isPasswordError = false
            )
        }
    }

    fun onHouseSelected(house: String) {
        _state.update { it.copy(selectedHouse = house, isPasswordError = false) }
    }

    fun onPasswordChanged(pwd: String) {
        val filtered = pwd.filter { it.isDigit() }.take(4)
        _state.update { it.copy(password = filtered, isPasswordError = false) }
    }

    fun onRememberMeChanged(checked: Boolean) {
        _state.update { it.copy(rememberMe = checked) }
        if (!checked) {
            storage.clear()
        }
    }

    /** Попытка входа. */
    fun login() {
        val s = _state.value
        val data = s.sntData ?: return
        val street = s.selectedStreet ?: return
        val house = s.selectedHouse ?: return
        val pwd = s.password

        if (pwd.length != 4) {
            _state.update { it.copy(isPasswordError = true) }
            return
        }

        val found = repository.findPlot(data, street, house)
        if (found == null) {
            _state.update { it.copy(errorMessage = "Участок не найден в базе") }
            return
        }
        val (plotNumber, plot) = found

        if (!repository.verifyPassword(plot, pwd)) {
            _state.update { it.copy(isPasswordError = true) }
            return
        }

        if (s.rememberMe) {
            storage.save(street, house)
        } else {
            storage.clear()
        }

        currentPlotNumber = plotNumber
        _dashboard.value = buildDashboard(data, plotNumber, plot)
        _state.update { it.copy(loginSuccess = true) }
        _onLoginSuccess?.invoke(_dashboard.value!!)
    }

    var _onLoginSuccess: ((DashboardData) -> Unit)? = null
    fun setOnLoginSuccess(cb: (DashboardData) -> Unit) { _onLoginSuccess = cb }

    fun consumeLoginSuccess() {
        _state.update { it.copy(loginSuccess = false) }
    }

    /** Выход из личного кабинета. Пароль не сохраняем. */
    fun onLogout() {
        currentPlotNumber = null
        _dashboard.value = null
        _state.update {
            it.copy(
                password = "",
                isPasswordError = false,
                loginSuccess = false
            )
        }
    }
}