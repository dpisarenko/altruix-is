package cc.altruix.is1.telegram.bots.herrKarl

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.Authenticator
import cc.altruix.is1.telegram.ITelegramCommandRegistry
import cc.altruix.is1.telegram.bots.knackal.KommissarRex
import cc.altruix.mock
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class HerrKarlSpec : Spek({
	describe("AltruixIs") {
		given("null update") {
			val sut = HerrKarlForTesting()
			on("onUpdateReceived") {
				sut.onUpdateReceived(null)
				it("should not call handleIncomingMessage") {
					// If it does, an exception will be thrown
				}
			}
		}
		given("update without message") {
			val sut = HerrKarlForTesting()
			val update = mock<Update>()
			`when`(update.hasMessage()).thenReturn(false)
			on("onUpdateReceived") {
				sut.onUpdateReceived(update)
				it("should not call handleIncomingMessage") {
					// If it does, an exception will be thrown
				}
			}
		}
		given("update with null message") {
			val sut = HerrKarlForTesting()
			val update = mock<Update>()
			`when`(update.hasMessage()).thenReturn(true)
			`when`(update.message).thenReturn(null)
			on("onUpdateReceived") {
				sut.onUpdateReceived(update)
				it("should not call handleIncomingMessage") {
					// If it does, an exception will be thrown
				}
			}
		}
		given("update with message with null content") {
			val sut = spy(
					HerrKarl(
							mock<Logger>(),
							mock<Logger>(),
							KommissarRex(mock<IJenaSubsystem>()
							),
							mock<ITelegramCommandRegistry>()
					)
			)
			val update = mock<Update>()
			`when`(update.hasMessage()).thenReturn(true)
			val msg = mock<Message>()
			`when`(msg.text).thenReturn(null)
			`when`(update.message).thenReturn(msg)
			doNothing().`when`(sut).handleIncomingMessage(msg)
			on("onUpdateReceived") {
				sut.onUpdateReceived(update)
				it("should call not handleIncomingMessage with update.message") {
					verify(sut, never()).handleIncomingMessage(msg)
				}
			}
		}
		given("update with message with empty content") {
			val sut = spy(HerrKarl(mock<Logger>(), mock<Logger>(), KommissarRex(mock<IJenaSubsystem>()), mock<ITelegramCommandRegistry>()))
			val update = mock<Update>()
			`when`(update.hasMessage()).thenReturn(true)
			val msg = mock<Message>()
			`when`(msg.text).thenReturn(StringUtils.EMPTY)
			`when`(update.message).thenReturn(msg)
			doNothing().`when`(sut).handleIncomingMessage(msg)
			on("onUpdateReceived") {
				sut.onUpdateReceived(update)
				it("should call not handleIncomingMessage with update.message") {
					verify(sut, never()).handleIncomingMessage(msg)
				}
			}
		}
		given("update with message") {
			val sut = spy(HerrKarl(mock<Logger>(), mock<Logger>(), KommissarRex(mock<IJenaSubsystem>()), mock<ITelegramCommandRegistry>()))
			val update = mock<Update>()
			`when`(update.hasMessage()).thenReturn(true)
			val msg = mock<Message>()
			`when`(msg.text).thenReturn("abc")
			`when`(update.message).thenReturn(msg)
			doNothing().`when`(sut).handleIncomingMessage(msg)
			on("onUpdateReceived") {
				sut.onUpdateReceived(update)
				it("should call handleIncomingMessage with update.message") {
					verify(sut).handleIncomingMessage(msg)
				}
			}
		}
		given("message") {
			val protocol = mock<Logger>()
			val sut = spy(HerrKarl(protocol, mock<Logger>(), KommissarRex(mock<IJenaSubsystem>()), mock<ITelegramCommandRegistry>()))
			val msg = mock<Message>()
			val infoTxt = "text"
			doReturn(infoTxt).`when`(sut).composeMessageToPrint(msg)
			val ackReply = mock<SendMessage>()
			doReturn(ackReply).`when`(sut).createAckReply(msg)
			doNothing().`when`(sut).sendTelegramMessage(ackReply)
			on("handleIncomingMessage") {
				sut.handleIncomingMessage(msg)
				it("should write the result of composeMessageToPrint to protocol") {
					verify(protocol).info(infoTxt)
				}
				it("should send a message created with createTestReply") {
					verify(sut).sendTelegramMessage(ackReply)
				}
			}
		}
		given("message with a certain text") {
			val sut = spy(HerrKarl(mock<Logger>(), mock<Logger>(), KommissarRex(mock<IJenaSubsystem>()), mock<ITelegramCommandRegistry>()))
			val localTime = Date.from(LocalDateTime.of(2016, 11, 22, 12, 15, 10).atZone(ZoneId.of("Europe/Moscow")).toInstant())
			doReturn(ZoneId.of("Europe/Moscow")).`when`(sut).systemTimeZone()
			doReturn(localTime).`when`(sut).now()
			doReturn("\n").`when`(sut).lineSeparator()
			val msg = mock<Message>()
			`when`(msg.text).thenReturn("This is a test with Umlauts. Erwägung. Grüß Gott! Öl. Überflüssig.")
			on("composeMessageToPrint") {
				val actRes = sut.composeMessageToPrint(msg)
				it("should return this string") {
					val expRes = IOUtils.toString(javaClass.classLoader.getResourceAsStream("cc/altruix/is1/telegram/AltruixIsBotSpec.01.txt"), "UTF-8")
					Assert.assertEquals(expRes, actRes)
				}
			}
		}
		given("message with a certain text") {
			val sut = spy(HerrKarl(mock<Logger>(), mock<Logger>(), KommissarRex(mock<IJenaSubsystem>()), mock<ITelegramCommandRegistry>()))
			val chatId = 42L
			val req = mock<Message>()
			`when`(req.text).thenReturn("Erwägung. Grüß Gott! Öl. Überflüssig.")
			`when`(req.chatId).thenReturn(chatId)
			on("createAckReply") {
				val actRes = sut.createAckReply(req)
				it("should return SendMessage with this text") {
					Assert.assertEquals("ACK 'Erwägun...'", actRes.text)
				}
				it("should return SendMessage with the chat ID of the original message") {
					Assert.assertEquals(chatId.toString(), actRes.chatId)
				}
			}
		}
		given("update from the right user") {
			val auth = mock<Authenticator>()
			val sut = spy(HerrKarl(mock<Logger>(), mock<Logger>(), auth, mock<ITelegramCommandRegistry>()))
			val update = mock<Update>()
			`when`(update.hasMessage()).thenReturn(true)
			val msg = mock<Message>()
			`when`(update.message).thenReturn(msg)
			`when`(msg.text).thenReturn("Cars drive faster because they have brakes")
			doReturn(true).`when`(auth).rightUser(update)
			doNothing().`when`(sut).logInvalidAccessAttempt(update, msg)
			doNothing().`when`(sut).handleIncomingMessage(msg)
			on("onUpdateReceived") {
				sut.onUpdateReceived(update)
				it("should call rightUser") {
					verify(auth).rightUser(update)
				}
				it("should not log invalid access attempt") {
					verify(sut, never()).logInvalidAccessAttempt(update, msg)
				}
				it("should call handleIncomingMessage") {
					verify(sut).handleIncomingMessage(msg)
				}
			}
		}
		given("update from the wrong user") {
			val auth = mock<Authenticator>()
			val sut = spy(HerrKarl(mock<Logger>(), mock<Logger>(), auth, mock<ITelegramCommandRegistry>()))
			val update = mock<Update>()
			`when`(update.hasMessage()).thenReturn(true)
			val msg = mock<Message>()
			`when`(update.message).thenReturn(msg)
			`when`(msg.text).thenReturn("Cars drive faster because they have brakes")
			doReturn(true).`when`(auth).rightUser(update)
			doNothing().`when`(sut).logInvalidAccessAttempt(update, msg)
			doNothing().`when`(sut).handleIncomingMessage(msg)
			on("onUpdateReceived") {
				sut.onUpdateReceived(update)
				it("should call rightUser") {
					verify(auth).rightUser(update)
				}
				it("should log invalid access attempt") {
					verify(sut, never()).logInvalidAccessAttempt(update, msg)
				}
				it("should not call handleIncomingMessage") {
					verify(sut).handleIncomingMessage(msg)
				}
			}
		}
	}
}) 