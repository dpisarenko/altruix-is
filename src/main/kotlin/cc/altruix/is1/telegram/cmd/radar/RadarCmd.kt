package cc.altruix.is1.telegram.cmd.radar

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by pisarenko on 16.05.2017.
 */
class RadarCmd(val mongo: IAltruixIs1MongoSubsystem,
               val tu: ITelegramUtils = TelegramUtils()) : ITelegramCommand {
    companion object {
        val Name = "/radar"
        val Help = "Radar chart"
    }

    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val thread = RadarThread(mongo, bot, chatId, tu)
        thread.start()
        tu.sendTextMessage("Radar chart generation started", chatId, bot)
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}