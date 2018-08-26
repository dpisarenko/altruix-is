package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.utils.composeWeekText2
import org.telegram.telegrambots.api.methods.send.SendMessage
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by pisarenko on 11.05.2017.
 */
open class DaCmd(
        val mongo:IAltruixIs1MongoSubsystem,
        val tu:ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/da"
        val Help = "Distraction analysis"
    }

    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val dsRes = mongo.distractionStats()
        val ds = dsRes.result
        if (!dsRes.success || (ds == null)) {
            tu.displayError("Could not get data from Mongo ('${dsRes.error}').", chatId, bot)
            return
        }
        executeLogic(ds, chatId, bot)
    }

    open fun executeLogic(ds: List<Date>, chatId: Long, bot: IResponsiveBot) {
        val now = now()
        val distractionsPerHourTotal = createHourlyMap()
        val distractionsPerHourWeek = createHourlyMap()
        calculateDistractionsPerHourWeek(ds, now, distractionsPerHourWeek)
        calculateDistractionsPerHourTotal(ds, distractionsPerHourTotal)
        val maxWeek = calcMax(distractionsPerHourWeek)
        val maxTotal = calcMax(distractionsPerHourTotal)
        val sb = composeMsgTxt(
                distractionsPerHourTotal,
                distractionsPerHourWeek,
                maxTotal,
                maxWeek
        )
        val msg = createSendMessage(chatId, sb.toString())
        bot.sendTelegramMessage(msg)
    }

    open fun now() = Date()

    open fun createSendMessage(chatId: Long, txt: String): SendMessage {
        val msg = createSendMessage()
        msg.enableMarkdown(true)
        msg.chatId = chatId.toString()
        msg.text = txt
        return msg
    }

    open fun calculateDistractionsPerHourTotal(
            ds: List<Date>,
            distractionsPerHourTotal: MutableMap<Int, AtomicInteger>
    ) {
        ds.map { extractHour(it) }.forEach { hour ->
            distractionsPerHourTotal[hour]?.incrementAndGet()
        }
    }

    open fun calculateDistractionsPerHourWeek(
            ds: List<Date>,
            now: Date,
            distractionsPerHourWeek: MutableMap<Int, AtomicInteger>
    ) {
        ds.filter { sameWeek(it, composeWeekText2(now)) }.map { extractHour(it) }.forEach { hour ->
            distractionsPerHourWeek[hour]?.incrementAndGet()
        }
    }

    open fun createHourlyMap(): MutableMap<Int, AtomicInteger> {
        val distractionsPerHourTotal = HashMap<Int, AtomicInteger>()
        for (hour in 0..23) {
            distractionsPerHourTotal[hour] = AtomicInteger(0)
        }
        return distractionsPerHourTotal
    }

    open fun composeMsgTxt(
            distractionsPerHourTotal: MutableMap<Int, AtomicInteger>,
            distractionsPerHourWeek: MutableMap<Int, AtomicInteger>,
            maxTotal: Double,
            maxWeek: Double
    ): StringBuilder {
        val sb = StringBuilder()
        sb.append("*Distraction analysis*\n\n")
        for (hour in 0..23) {
            val totalCount = distractionsPerHourTotal[hour]?.toInt() ?: 0
            val weekCount = distractionsPerHourWeek[hour]?.toInt() ?: 0
            if ((totalCount == 0) && (weekCount == 0)) {
                continue
            }
            appendHourlyStats(totalCount, maxTotal, weekCount, maxWeek, sb, hour)
        }
        return sb
    }

    open fun calcMax(data: Map<Int, AtomicInteger>) =
            data.values.map { it.get() }.max()?.toDouble() ?: 1.0

    open fun appendHourlyStats(
            totalCount: Int,
            maxTotal: Double,
            weekCount: Int,
            maxWeek: Double,
            sb: StringBuilder,
            hour: Int
    ) {
        val totalPercent = percent(totalCount, maxTotal)
        val weekPercent = percent(weekCount, maxWeek)
        if (weekCount.toDouble() == maxWeek) {
            sb.append("*")
        }
        sb.append("$hour:00 -- $hour:59: ")
        if (weekCount.toDouble() == maxWeek) {
            sb.append("*")
        }
        val weekPercentTxt = String.format("%2.0f", weekPercent)
        val totalPercentTxt = String.format("%2.0f", totalPercent)
        sb.append("Week: $weekCount ($weekPercentTxt %) ")
        sb.append("Total: $totalCount ($totalPercentTxt %)")
        sb.append("\n")
    }

    open fun percent(value: Int, max: Double) = value.toDouble() * 100.0 / max

    open fun createSendMessage() = SendMessage()

    open fun extractHour(day: Date): Int {
        val cal = GregorianCalendar()
        cal.time = day
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    open fun sameWeek(day: Date, curWeek: String): Boolean =
            (composeWeekText2(day) == curWeek)

    override fun name(): String = Name

    override fun helpText(): String = Help
}