package ru.sntnadezhda.dacha.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sntnadezhda.dacha.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О программе") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Моя дача",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "СНТ «Надежда»",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Версия ${BuildConfig.VERSION_NAME}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            InfoBlock(
                title = "Назначение",
                body = "Приложение для садоводов СНТ «Надежда». " +
                        "Показывает ваши взносы, платежи, показания счётчиков " +
                        "и баланс электроэнергии."
            )

            InfoBlock(
                title = "Возможности",
                body = "• Просмотр задолженностей по взносам\n" +
                        "• История платежей\n" +
                        "• Показания счётчиков\n" +
                        "• Баланс электроэнергии\n" +
                        "• Сроки оплаты\n" +
                        "• Оплата по QR-коду (вкладка «QR»)"
            )

            InfoBlock(
                title = "💳 Оплата по QR-коду",
                body = "Откройте вкладку «QR», чтобы оплатить взносы " +
                        "или электроэнергию через банковское приложение.\n\n" +
                        "1. Выберите «Взносы» или «Электро».\n" +
                        "2. Для электро введите текущее показание счётчика.\n" +
                        "3. Приложение сформирует QR с реквизитами СНТ " +
                        "и суммой платежа.\n" +
                        "4. Откройте приложение банка → «Оплата по QR» " +
                        "→ наведите камеру.\n" +
                        "5. Реквизиты и сумма подставятся автоматически — " +
                        "останется только нажать «Оплатить».\n\n" +
                        "QR можно сохранить в галерею телефона, " +
                        "а также скопировать реквизиты текстом — " +
                        "на случай, если банк не читает QR-код."
            )

            InfoBlock(
                title = "Как получить доступ",
                body = "Пароль выдаёт председатель СНТ. " +
                        "Пароль — 4 цифры, у каждого участка свой."
            )

            InfoBlock(
                title = "Вопросы и проблемы",
                body = "По вопросам работы приложения обращайтесь к Сердцеву Д.Н., Калиновая 23\n" +
                        "Телефон: +7 913 953-45-60"
            )

            InfoBlock(
                title = "Конфиденциальность",
                body = "Приложение не собирает личные данные и не передаёт их третьим лицам. " +
                        "Данные загружаются напрямую из защищённого облака СНТ."
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "© 2026 СНТ «Надежда»",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text(body, fontSize = 13.sp)
        }
    }
}