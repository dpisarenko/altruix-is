package cc.altruix.is1.telegram.cmd.r

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by pisarenko on 13.04.2017.
 */
open class RCmd(
        val mongo: IAltruixIs1MongoSubsystem,
        val tu:ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/r"
        val Help = "Creates and sends a report"
        val MetricWordCount = "Daily word count"
        val UnitWords = "Words"
    }

    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val thread = createReportThread(bot, chatId)
        thread.start()
        tu.sendTextMessage("Report generation started", chatId, bot)
    }

    open fun createReportThread(bot: IResponsiveBot, chatId: Long) =
            ReportThread(mongo, bot, chatId, tu)

    override fun name(): String = Name

    override fun helpText(): String = Help
}