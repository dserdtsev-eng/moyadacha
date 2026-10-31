package ru.sntnadezhda.dacha.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val ruFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))

    /** ISO-дата из JSON → LocalDate. */
    fun parse(s: String?): LocalDate? {
        if (s.isNullOrBlank()) return null
        return try {
            LocalDate.parse(s, isoFmt)
        } catch (e: Exception) {
            null
        }
    }

    /** LocalDate → ISO-строка (для записи). */
    fun format(d: LocalDate?): String {
        return d?.format(isoFmt) ?: ""
    }

    /** ISO-строка → «дд.мм.гггг» для показа пользователю. */
    fun toRuDate(iso: String?): String {
        val d = parse(iso) ?: return iso ?: ""
        return d.format(ruFmt)
    }

    /** Период взносов: 12 месяцев с даты начала. */
    fun periodEnd(start: LocalDate?): LocalDate? {
        return start?.plusYears(1)?.minusDays(1)
    }
}