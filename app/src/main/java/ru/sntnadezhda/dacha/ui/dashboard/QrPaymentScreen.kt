package ru.sntnadezhda.dacha.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.sntnadezhda.dacha.data.model.MoneyUtils
import java.io.OutputStream
import kotlin.math.roundToLong

@Composable
fun QrPaymentScreen(
    data: DashboardData,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var paymentType by remember { mutableStateOf("member") }
    var meterInput by remember { mutableStateOf("") }

    // Сумма долга по взносам
    val totalDebt = data.totalCurrentDebt

    // Последнее показание счётчика
    val lastReading: Double = data.readings
        .sortedByDescending { it.date }
        .firstOrNull()
        ?.values?.values?.firstOrNull() ?: 0.0

    val tariff = data.tariff

    // Расчёт электро
    val meterValue = meterInput.replace(',', '.').toDoubleOrNull() ?: 0.0
    val electricKwh = if (meterValue > lastReading) meterValue - lastReading else 0.0
    val electricSum = electricKwh * tariff

    // Итоговая сумма
    val finalAmount = when (paymentType) {
        "member" -> totalDebt
        else -> electricSum
    }

    // Назначение платежа (для банка — без точек с запятой)
    val purpose: String = when (paymentType) {
        "member" -> "${data.plot.street} ${data.plot.house}, членские взносы"
        else -> "${data.plot.street} ${data.plot.house}, показание ${
            meterValue.toLong()
        }, электроэнергия"
    }

    // Сумма в копейках для формата ST00012
    val sumKopecks = (finalAmount * 100).roundToLong()

    // Строка для QR в формате ST00012 (ГОСТ Р 56042-2014)
    val qrPayload: String = buildString {
        append("ST00012")
        append("|Name=").append(data.reqName)
        append("|PersonalAcc=").append(data.reqPersonalAcc)
        append("|BankName=").append(data.reqBankName)
        append("|BIC=").append(data.reqBic)
        append("|CorrespAcc=").append(data.reqCorrespAcc)
        append("|INN=").append(data.reqInn)
        append("|KPP=").append(data.reqKpp)
        append("|PayeeINN=").append(data.reqInn)
        append("|Sum=").append(sumKopecks)
        append("|Purpose=").append(purpose)
    }

    val qrPainter = rememberQrCodePainter(
        data = qrPayload,
        errorCorrectionLevel = QrErrorCorrectionLevel.Medium
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Информация об участке ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Участок: ${data.plot.street} ${data.plot.house}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Владелец: ${data.plot.owner}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Тип платежа ──
        Text(
            text = "За что платим?",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = paymentType == "member",
                onClick = { paymentType = "member" },
                label = { Text("Взносы") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = paymentType == "electric",
                onClick = { paymentType = "electric" },
                label = { Text("Электро") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Ввод показания ──
        if (paymentType == "electric") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Текущее показание счётчика:", fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = meterInput,
                        onValueChange = {
                            meterInput = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
                        },
                        label = { Text("Показание") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Предыдущее: ${MoneyUtils.formatMeter(lastReading)}",
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Расход: ${MoneyUtils.formatMeter(electricKwh)} кВт·ч",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    if (tariff > 0) {
                        Text(
                            "Тариф: ${"%.2f".format(tariff)} ₽/кВт·ч",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "⚠ Тариф не задан",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Сумма к оплате ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Сумма к оплате", fontSize = 13.sp)
                Text(
                    text = MoneyUtils.formatRub(finalAmount),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (paymentType == "electric" && electricKwh <= 0) {
                    Text(
                        "Введите показание больше предыдущего",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── QR-код ──
        if (finalAmount > 0.01) {
            Text(
                "QR-код для оплаты",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = qrPainter,
                        contentDescription = "QR для оплаты",
                        modifier = Modifier.size(280.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Откройте приложение банка → «Оплата по QR» → наведите камеру",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // ── Кнопки ──
            Button(
                onClick = {
                    scope.launch {
                        val ok = saveQrToGallery(
                            context,
                            qrPainter,
                            "qr_${data.plot.street}_${data.plot.house}_$paymentType"
                        )
                        Toast.makeText(
                            context,
                            if (ok) "QR сохранён в галерею" else "Не удалось сохранить",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💾 Сохранить QR в галерею")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("QR", qrPayload))
                    Toast.makeText(
                        context,
                        "Реквизиты скопированы",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋 Скопировать реквизиты")
            }

            Spacer(Modifier.height(16.dp))

            // ── Реквизиты текстом ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Реквизиты для перевода",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    InfoLine("Получатель", data.reqName)
                    InfoLine("ИНН", data.reqInn)
                    InfoLine("КПП", data.reqKpp)
                    InfoLine("Р/с", data.reqPersonalAcc)
                    InfoLine("Банк", data.reqBankName)
                    InfoLine("БИК", data.reqBic)
                    InfoLine("К/с", data.reqCorrespAcc)
                    InfoLine("Сумма", MoneyUtils.formatRub(finalAmount))
                    InfoLine("Назначение", purpose)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = if (paymentType == "member")
                        "Нет задолженности по взносам"
                    else
                        "Введите показание, чтобы рассчитать сумму",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Сохраняет QR-код в галерею.
 *
 * QR рисуется НЕ на весь кадр, а на 70% площади — по краям остаётся
 * белое поле (quiet zone). Это нужно, чтобы банковские приложения
 * корректно распознавали QR при загрузке из галереи.
 */
private suspend fun saveQrToGallery(
    context: Context,
    painter: Painter,
    fileName: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        // Итоговое разрешение PNG — 1536×1536 (хорошо для банков)
        val size = 1536

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val imageBitmap = bitmap.asImageBitmap()
        val composeCanvas: Canvas = Canvas(imageBitmap)

        val drawScope = CanvasDrawScope()
        val fullSize = Size(size.toFloat(), size.toFloat())

        // QR занимает 70% площади, по 15% белого поля с каждой стороны
        val qrSize = size * 0.7f
        val padding = (size - qrSize) / 2f

        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = composeCanvas,
            size = fullSize
        ) {
            // белый фон на всё поле
            drawRect(color = Color.White, size = fullSize)

            // рисуем QR в центре, со сдвигом на padding
            translate(left = padding, top = padding) {
                with(painter) {
                    draw(
                        size = Size(qrSize, qrSize),
                        colorFilter = null,
                        alpha = 1.0f
                    )
                }
            }
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/МояДача")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return@withContext false

        context.contentResolver.openOutputStream(uri)?.use { out: OutputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
        }

        bitmap.recycle()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}