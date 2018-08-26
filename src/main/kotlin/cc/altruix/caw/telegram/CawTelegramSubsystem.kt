package cc.altruix.caw.telegram

import cc.altruix.common.AbstractTelegramSubsystem
import cc.altruix.is1.App
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.telegram.Authenticator
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.exceptions.TelegramApiException

/**
 * Created by pisarenko on 02.05.2017.
 */
open class CawTelegramSubsystem(
        val botApi: TelegramBotsApi = TelegramBotsApi(),
        val mongo: IMongoSubsystem
) : AbstractTelegramSubsystem() {
    val logger = LoggerFactory.getLogger(App.LoggerName)
    open fun init() {
        try {
            initApiContextInitializer()
            val wolf = createWolf()
            val everett = createEverett(wolf)
            val bots = arrayListOf(
                    everett
            )
            bots.forEach { bot -> botApi.registerBot(bot) }
        } catch (exception: TelegramApiException) {
            logger.error("", exception)
        }

    }

    open fun createEverett(auth: Authenticator): EverettBot = EverettBot(auth)
}