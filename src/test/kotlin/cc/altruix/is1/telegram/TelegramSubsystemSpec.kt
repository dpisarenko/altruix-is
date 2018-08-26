package cc.altruix.is1.telegram

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.bots.herrKarl.HerrKarl
import cc.altruix.is1.telegram.bots.knackal.KommissarRex
import cc.altruix.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.slf4j.Logger
import org.telegram.telegrambots.TelegramBotsApi

class TelegramSubsystemSpec : Spek({
	describe("TelegramSubsystem") {
		on("start") {
			val botApi = mock<TelegramBotsApi>()
			val capsule = mock<ICapsuleCrmSubsystem>()
			val jena = mock<IJenaSubsystem>()
			val sut = Mockito.spy(TelegramSubsystem(botApi, capsule, jena, mock<IAltruixIs1MongoSubsystem>()))
			val protocol = mock<Logger>()
			doReturn(protocol).`when`(sut).createProtocolLogger()
			Mockito.doNothing().`when`(sut).initApiContextInitializer()
			val bot = mock<HerrKarl>()
			val auth = mock<Authenticator>()
			doReturn(auth).`when`(sut).createAuthenticator(mock<IJenaSubsystem>())
			Mockito.doReturn(bot).`when`(sut).createHerrKarl(protocol, auth, mock<ITelegramCommandRegistry>())

			sut.init()
			it("should start ApiContextInitializer") {
				Mockito.verify(sut).initApiContextInitializer()
			}
			it("should create HerrKarl") {
				Mockito.verify(sut).createHerrKarl(Mockito.verify(sut).createProtocolLogger(), auth, mock<ITelegramCommandRegistry>())
			}
			it("should register HerrKarl") {
				Mockito.verify(botApi).registerBot(bot)
			}
		}
		on("createHerrKarl") {
			val botApi = mock<TelegramBotsApi>()
			val capsule = mock<ICapsuleCrmSubsystem>()
			val jena = mock<IJenaSubsystem>()
			val sut = Mockito.spy(TelegramSubsystem(botApi, capsule, jena, mock<IAltruixIs1MongoSubsystem>()))
			val bot = mock<HerrKarl>()
			val logger = mock<Logger>()
			Mockito.doReturn(logger).`when`(sut).createProtocolLogger()
			Mockito.doReturn(bot).`when`(sut).createHerrKarl(logger, KommissarRex(jena), mock<ITelegramCommandRegistry>())
			val auth = mock<Authenticator>()
			doReturn(auth).`when`(sut).createAuthenticator(mock<IJenaSubsystem>())

			val actRes = sut.createHerrKarl(logger, auth, mock<ITelegramCommandRegistry>())
			it("should pass the result of createProtocolLogger to constructor of HerrKarl") {
				Mockito.verify(sut).createHerrKarl(logger, KommissarRex(mock<IJenaSubsystem>()), mock<ITelegramCommandRegistry>())
				Assert.assertSame(bot, actRes)
			}
		}
		given("non-null response from getLogger") {
			val botApi = mock<TelegramBotsApi>()
			val capsule = mock<ICapsuleCrmSubsystem>()
			val jena = mock<IJenaSubsystem>()
			val sut = Mockito.spy(TelegramSubsystem(botApi, capsule, jena, mock<IAltruixIs1MongoSubsystem>()))
			val protocolLogger = mock<Logger>()
			Mockito.doReturn(protocolLogger).`when`(sut).getLogger("protocol")
			on("createProtocolLogger") {
				val actRes = sut.createProtocolLogger()
				it("should return the result of getLogger") {
					Mockito.verify(sut).getLogger("protocol")
					Assert.assertSame(protocolLogger, actRes)
				}
			}
		}
		given("null response from getLogger") {
			val botApi = mock<TelegramBotsApi>()
			val capsule = mock<ICapsuleCrmSubsystem>()
			val jena = mock<IJenaSubsystem>()
			val sut = Mockito.spy(TelegramSubsystem(botApi, capsule, jena, mock<IAltruixIs1MongoSubsystem>()))
			Mockito.doReturn(null).`when`(sut).getLogger("protocol")
			on("createProtocolLogger") {
				it("should throw an exception") {
					try {
						sut.createProtocolLogger()
						Assert.fail("Expected exception not thrown")
					} catch (exception:RuntimeException) {
						Assert.assertEquals("Can't find protocol logging object.", exception.message)
					}
				}
			}
		}
	}
}) 