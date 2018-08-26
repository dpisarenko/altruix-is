package cc.altruix.caw

import cc.altruix.caw.mongo.CawMongoSubsystem
import cc.altruix.caw.telegram.CawTelegramSubsystem
import cc.altruix.is1.App
import cc.altruix.is1.mongo.IMongoSubsystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.TelegramBotsApi

/**
 * Created by pisarenko on 02.05.2017.
 */
open class CawApp(val logger: Logger = LoggerFactory.getLogger(App.LoggerName)) {
    fun run() {
        logger.info("Starting CAW v. ${App.Version}")
        val mongo = createMongo()
        val mongoStatus = mongo.init()
        if (!mongoStatus.success) {
            logger.error("Mongo hat an Patsch'n ('${mongoStatus.error}').")
            logger.error("Shutting down after a failed start.")
            return
        }
        val botApi = createTelegramBotsApi()
        val telegram = createTelegramSubsystem(botApi, mongo)
        telegram.init()
    }

    open fun createTelegramSubsystem(
            botApi: TelegramBotsApi,
            mongo: IMongoSubsystem): CawTelegramSubsystem =
            CawTelegramSubsystem(botApi, mongo)

    open fun createTelegramBotsApi()  = TelegramBotsApi()
    open fun createMongo(): IMongoSubsystem {
        return CawMongoSubsystem()
    }

}

fun main(args : Array<String>) {
    val app = CawApp()
    app.run()
}