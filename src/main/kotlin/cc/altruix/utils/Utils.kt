package cc.altruix.utils

import cc.altruix.is1.telegram.cmd.r.ReportThread
import cc.altruix.is1.telegram.forms.Teleform
import org.apache.commons.lang3.StringUtils
import java.time.*
import java.util.*

/**
 * Created by pisarenko on 09.02.2017.
 */
fun String?.isBlank():Boolean = StringUtils.isBlank(this)

fun StringBuilder.appendQuoted(txt:String) {
    this.append("\"")
    this.append(txt)
    this.append("\"")
}

fun StringBuilder.appendKeyValuePair(key: String, value: String) {
    this.appendQuoted(key)
    this.append(":")
    this.appendQuoted(value)
}

fun String?.isNumeric():Boolean = StringUtils.isNumeric(this)

fun Teleform.allIdsUnique():Boolean {
    val allIds = this.elements.map { it.id() }
    val distinctIds = allIds.distinct()
    return (allIds.size == distinctIds.size)

}

fun timestamp(): ZonedDateTime {
    val now = Instant.now()
    val zoneId = ZoneId.of("Europe/Moscow")
    val moscowTime: ZonedDateTime = ZonedDateTime.ofInstant(now, zoneId)
    return moscowTime
}


fun LocalDateTime.toDate() = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())

fun LocalDate.toDate():Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

fun Double.round4():Double = Math.round(this * 10000.0)/10000.0

fun Date.toLocalDate():LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun composeWeekText2(day: Date): String {
    val ld = day.toLocalDate()
    val weekBasedYear = ld.get(ReportThread.AltruixISWeekFields.weekBasedYear())
    val calendarYear = ld.year
    val year = Math.max(weekBasedYear, calendarYear)
    val weekNr = ld.get(ReportThread.AltruixISWeekFields.weekOfYear())
    val week = String.format("%04d-%02d", year, weekNr)
    return week
}

fun StringBuilder.nl() {
    this.append("\n")
}