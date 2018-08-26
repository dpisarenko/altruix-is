package cc.altruix.is1.telegram

import cc.altruix.common.AbstractTelegramSubsystem
import cc.altruix.is1.App
import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.bots.herrKarl.HerrKarl
import cc.altruix.is1.telegram.bots.herrKarl.HerrKarlCommandRegistry
import cc.altruix.is1.telegram.bots.knackal.Knackal
import cc.altruix.is1.telegram.bots.knackal.KnackalCommandRegistry
import cc.altruix.is1.telegram.bots.knackal.KommissarRex
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.generics.LongPollingBot

open class TelegramSubsystem(
		val botApi: TelegramBotsApi = TelegramBotsApi(),
		val capsule: ICapsuleCrmSubsystem,
		val jena: IJenaSubsystem,
		val mongo: IAltruixIs1MongoSubsystem
) : AbstractTelegramSubsystem() {
	val logger = LoggerFactory.getLogger(App.LoggerName)
	open fun init() {
		try {
			initApiContextInitializer()
			val rex = createAuthenticator(jena)
			val protocol = createProtocolLogger()
			val herrKarlCmdReg = createHerrKarlCmdRegistry(capsule, jena)
            herrKarlCmdReg.init()
			val wolf = createWolf()
            val herrKarl = createHerrKarl(protocol, wolf, herrKarlCmdReg)
            val knackalCmdReg = createKnackalCommandRegistry(capsule, jena, herrKarl)
            knackalCmdReg.init()
			val bots = arrayListOf(
					herrKarl,
					createFrauKnackal(protocol, rex, knackalCmdReg)
			)
			bots.forEach { bot -> botApi.registerBot(bot) }
		} catch (exception: TelegramApiException) {
			logger.error("", exception)
		}
	}

	open fun createHerrKarlCmdRegistry(capsule: ICapsuleCrmSubsystem, jena: IJenaSubsystem): ITelegramCommandRegistry =
			HerrKarlCommandRegistry(
                    capsule,
                    jena,
					mongo
            )

	open fun createKnackalCommandRegistry(
            capsule: ICapsuleCrmSubsystem,
            jena: IJenaSubsystem,
            boss: IResponsiveBot):ITelegramCommandRegistry =
			KnackalCommandRegistry(capsule, jena, boss)

	open fun createAuthenticator(jena:IJenaSubsystem):Authenticator = KommissarRex(jena)

	open fun createFrauKnackal(
            protocol: Logger,
            auth: Authenticator,
            commandRegistry: ITelegramCommandRegistry
	):LongPollingBot = Knackal(protocol, this.logger, auth, commandRegistry)

	open fun createHerrKarl(
			protocol: Logger,
			auth: Authenticator, cmdRegistry: ITelegramCommandRegistry
	): HerrKarl = HerrKarl(protocol, this.logger, auth, cmdRegistry)
	
	open fun createProtocolLogger():Logger {
		val protocol = getLogger("protocol")
		if (protocol != null) {
			return protocol
		}
		logger.error("Can't find protocol logging object. Quitting.")
		throw RuntimeException("Can't find protocol logging object.")
	}
	
	open fun getLogger(name:String):Logger? = LoggerFactory.getLogger(name)
}