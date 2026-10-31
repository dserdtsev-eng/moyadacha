package ru.sntnadezhda.dacha.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sntnadezhda.dacha.data.model.DateUtils
import ru.sntnadezhda.dacha.data.model.MoneyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    data: DashboardData,
    onLogout: () -> Unit,
    onAbout: () -> Unit,
    onRefresh: () -> Unit
) {
    val tabs = listOf("Долги", "Платежи", "Показания", "Электро", "QR", "Сроки")
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = data.plot.owner.ifBlank { "Участок №${data.plotNumber}" },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${data.plot.street} ${data.plot.house}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Text("🔄", fontSize = 20.sp)
                    }
                    IconButton(onClick = onAbout) {
                        Text("ℹ️", fontSize = 20.sp)
                    }
                    TextButton(onClick = onLogout) {
                        Text("Выйти", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> DebtsTab(data)
                    1 -> PaymentsTab(data)
                    2 -> ReadingsTab(data)
                    3 -> ElectricityTab(data)
                    4 -> QrPaymentScreen(data = data, onBack = {})
                    5 -> DeadlinesTab(data)
                }
            }
        }
    }
}

@Composable
private fun DebtsTab(data: DashboardData) {
    SectionTitle("Задолженности")

    if (data.totalDebt == 0.0) {
        SuccessCard("Задолженностей по взносам нет")
        return
    }

    if (data.memberDebt > 0.01 || data.targetDebt > 0.01) {
        SectionTitle("Текущий период", small = true)
        if (data.memberDebt > 0.01) DebtRow("Членские взносы", data.memberDebt)
        if (data.targetDebt > 0.01) DebtRow("Целевые взносы", data.targetDebt)
        TotalRow("Итого текущий", data.totalCurrentDebt)
    }

    if (data.prevMemberDebt > 0.01 || data.prevTargetDebt > 0.01) {
        Spacer(Modifier.height(12.dp))
        SectionTitle("Прошлый период", small = true)
        if (data.prevMemberDebt > 0.01) DebtRow("Членские взносы", data.prevMemberDebt)
        if (data.prevTargetDebt > 0.01) DebtRow("Целевые взносы", data.prevTargetDebt)
        TotalRow("Итого прошлый", data.totalPrevDebt)
    }

    Spacer(Modifier.height(12.dp))
    TotalRow("Всего к оплате", data.totalDebt, bold = true)
}

@Composable
private fun PaymentsTab(data: DashboardData) {
    SectionTitle("История платежей")

    if (data.payments.isEmpty()) {
        InfoCard("Платежей пока нет")
        return
    }

    data.payments.sortedByDescending { it.date }.forEach { p ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        DateUtils.toRuDate(p.date),
                        fontWeight = FontWeight.Medium
                    )
                    if (p.description.isNotBlank()) {
                        Text(
                            p.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val periodLabel = when (p.period) {
                        "prev" -> "прошлый период"
                        "current" -> "текущий период"
                        else -> null
                    }
                    if (periodLabel != null) {
                        Text(
                            periodLabel,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = MoneyUtils.formatRub(p.amount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    TotalRow("Всего оплачено", data.payments.sumOf { it.amount }, bold = true)
}

@Composable
private fun ReadingsTab(data: DashboardData) {
    SectionTitle("Показания счётчиков")

    if (data.readings.isEmpty()) {
        InfoCard("Показаний пока нет")
        return
    }

    data.readings.sortedByDescending { it.date }.forEach { r ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        DateUtils.toRuDate(r.date),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (r.replaced) {
                        Text(
                            "🔄 замена",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                data.meters.forEach { m ->
                    val v = r.values[m.id.toString()]
                    if (v != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(m.name, fontSize = 13.sp)
                            Text(
                                text = MoneyUtils.formatMeter(v),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElectricityTab(data: DashboardData) {
    SectionTitle("Электричество")

    if (data.lastReplacementDate.isNotEmpty()) {
        Text(
            text = "Период: с ${DateUtils.toRuDate(data.lastReplacementDate)} " +
                    "(после замены счётчика)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    val balance = data.kwhBalance
    val balanceColor = when {
        balance > 0.01 -> MaterialTheme.colorScheme.primary
        balance < -0.01 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Баланс", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Оплачено, кВт·ч", modifier = Modifier.weight(1f))
                Text(MoneyUtils.formatMeter(data.paidKwh))
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Израсходовано, кВт·ч", modifier = Modifier.weight(1f))
                Text(MoneyUtils.formatMeter(data.consumedKwh))
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Итог",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = (if (balance > 0) "+" else "") + MoneyUtils.formatMeter(balance),
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }

            if (balance > 0.01) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Переплата",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (balance < -0.01) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Недоплата",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (data.paidDetails.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        SectionTitle("Оплаченные кВт·ч", small = true)
        data.paidDetails.sortedByDescending { it.date }.forEach { d ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            DateUtils.toRuDate(d.date),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${MoneyUtils.format(d.amount)} ₽ ÷ ${"%.2f".format(d.price)} ₽/кВт·ч",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        MoneyUtils.formatMeter(d.kwh),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (data.consumptionDetails.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        SectionTitle("Расход по показаниям", small = true)
        data.consumptionDetails.sortedByDescending { it.toDate }.forEach { d ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${DateUtils.toRuDate(d.fromDate)} → ${DateUtils.toRuDate(d.toDate)}",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${MoneyUtils.formatMeter(d.fromValue)} → ${MoneyUtils.formatMeter(d.toValue)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        MoneyUtils.formatMeter(d.delta),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (data.paidDetails.isEmpty() && data.consumptionDetails.isEmpty()) {
        Spacer(Modifier.height(16.dp))
        InfoCard("Нет данных по электричеству")
    }
}

@Composable
private fun DeadlinesTab(data: DashboardData) {
    SectionTitle("Сроки оплаты")

    DeadlineRow(
        "Годовой членский взнос",
        data.memberFee,
        data.memberDeadline
    )
    DeadlineRow(
        "Годовой целевой взнос",
        data.targetFee,
        data.targetDeadline
    )
    DeadlineRow(
        "Электроэнергия",
        null,
        "до ${data.electricDay} числа каждого месяца"
    )

    if (data.feeStartDate.isNotBlank()) {
        Spacer(Modifier.height(16.dp))
        SectionTitle("Период взносов", small = true)
        val endIso = endOfFeePeriod(data.feeStartDate)
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "С ${DateUtils.toRuDate(data.feeStartDate)} по ${DateUtils.toRuDate(endIso)}",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

private fun endOfFeePeriod(start: String): String {
    return try {
        val d = java.time.LocalDate.parse(start)
        d.plusYears(1).minusDays(1).toString()
    } catch (e: Exception) {
        ""
    }
}

// ── Вспомогательные компоненты ──

@Composable
private fun SectionTitle(text: String, small: Boolean = false) {
    Text(
        text = text,
        fontSize = if (small) 14.sp else 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun DebtRow(label: String, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(8.dp))
            Text(
                MoneyUtils.formatRub(amount),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun TotalRow(label: String, amount: Double, bold: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                modifier = Modifier.weight(1f),
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.width(8.dp))
            Text(
                MoneyUtils.formatRub(amount),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SuccessCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = "✓ $text",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(text, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun DeadlineRow(label: String, amount: Double?, deadline: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            if (amount != null) {
                Text("Сумма: ${MoneyUtils.formatRub(amount)}", fontSize = 14.sp)
            }
            val dl = if (deadline.isBlank()) "не указан" else DateUtils.toRuDate(deadline)
            Text(
                "Срок: $dl",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}