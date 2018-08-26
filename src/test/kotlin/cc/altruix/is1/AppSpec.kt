package cc.altruix.is1

import org.jetbrains.spek.api.Spek
import cc.altruix.is1.App
import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.mongo.IMongoSubsystem
import org.mockito.Mockito
import org.mockito.Mockito.inOrder
import cc.altruix.is1.telegram.TelegramSubsystem
import cc.altruix.mock
import org.jetbrains.spek.api.dsl.*
import org.slf4j.Logger
import org.telegram.telegrambots.TelegramBotsApi

class AppSpec : Spek({
	describe("App") {
		val logger = mock<Logger>()
		val sut = Mockito.spy(App(logger))
		val telegramSubsystem = mock<TelegramSubsystem>()
		Mockito.doReturn(telegramSubsystem).`when`(sut).createTelegramSubsystem(
				mock<TelegramBotsApi>(),
				mock<ICapsuleCrmSubsystem>(),
				mock<IJenaSubsystem>(),
				mock<IAltruixIs1MongoSubsystem>()
		)
		val inOrder = inOrder(logger)
		on("run") {
			sut.run()
			it("should write a message before start") {
				inOrder.verify(logger).info("Starting Altruix IS v. ${App.Version}")
			}
			it("should create the telegram subsystem") {
				Mockito.verify(sut).createTelegramSubsystem(
						mock<TelegramBotsApi>(),
						mock<ICapsuleCrmSubsystem>(),
						mock<IJenaSubsystem>(),
						mock<IAltruixIs1MongoSubsystem>()
				)
			}
			it("should start the telegram subsystem") {
				Mockito.verify(telegramSubsystem).init()
			}
			it("should write logging messages before stop") {
				inOrder.verify(logger).info("Shutting down Altruix IS")
			}
		}
	}
})