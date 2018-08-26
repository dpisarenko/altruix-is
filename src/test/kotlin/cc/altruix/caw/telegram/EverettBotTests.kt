package cc.altruix.caw.telegram

import cc.altruix.is1.telegram.Authenticator
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup

/**
 * Created by pisarenko on 05.05.2017.
 */
class EverettBotTests {
    @Test
    fun onUpdateReceivedNullUpdate() {
        // Prepare
        val auth = mock<Authenticator>()
        val logger = mock<Logger>()
        val sut = spy(EverettBot(auth, logger))

        // Run method under test
        sut.onUpdateReceived(null)

        // Verify
        verify(logger).error("Null update")
    }
    @Test
    fun onUpdateReceivedNoMessage() {
        // Prepare
        val auth = mock<Authenticator>()
        val logger = mock<Logger>()
        val sut = spy(EverettBot(auth, logger))
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(false)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(logger).error("No message")
    }
    @Test
    fun onUpdateSunnyDay() {
        // Prepare
        val auth = mock<Authenticator>()
        val logger = mock<Logger>()
        val sut = spy(EverettBot(auth, logger))
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        doNothing().`when`(sut).handleIncomingMessage(msg)
        `when`(msg.hasText()).thenReturn(true)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(msg).hasText()
        verify(sut).handleIncomingMessage(msg)
    }
    @Test
    fun onUpdateException() {
        // Prepare
        val auth = mock<Authenticator>()
        val logger = mock<Logger>()
        val sut = spy(EverettBot(auth, logger))
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(msg.hasText()).thenReturn(true)
        val throwable = RuntimeException("error")
        doThrow(throwable).`when`(sut).handleIncomingMessage(msg)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(sut).handleIncomingMessage(msg)
        verify(logger).error("EverettBot.onUpdateReceived", throwable)
    }
    @Test
    fun handleIncomingMessage() {
        // Prepare
        val auth = mock<Authenticator>()
        val logger = mock<Logger>()
        val sut = spy(EverettBot(auth, logger))
        val msg = mock<Message>()
        val sendMsg = mock<SendMessage>()
        doReturn(sendMsg).`when`(sut).defaultMsg(msg)
        doNothing().`when`(sut).sendMessage2(sendMsg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(sut).defaultMsg(msg)
        verify(sut).sendMessage2(sendMsg)
    }
    @Test
    fun defaultMsg() {
        // Prepare
        val auth = mock<Authenticator>()
        val logger = mock<Logger>()
        val sut = spy(EverettBot(auth, logger))
        val kbd = mock<ReplyKeyboardMarkup>()
        doReturn(kbd).`when`(sut).createMainMenuKeyboard()
        val sendMessage = mock<SendMessage>()
        doReturn(sendMessage).`when`(sut).createSendMessage()

        val msg = mock<Message>()
        val chatId = 31331L
        `when`(msg.chatId).thenReturn(chatId)
        val messageId = 123
        `when`(msg.messageId).thenReturn(messageId)

        // Run method under test
        val actRes = sut.defaultMsg(msg)

        // Verify
        verify(sut).createMainMenuKeyboard()
        verify(sendMessage).enableMarkdown(true)
        verify(sendMessage).setChatId(chatId)
        verify(sendMessage).setReplyToMessageId(messageId)
        verify(sendMessage).setReplyMarkup(kbd)
        verify(sendMessage).setText("Hi there")
        assertThat(actRes).isSameAs(sendMessage)
    }
    @Test
    fun createMainMenuKeyboard() {
        // Prepare
        val auth = mock<Authenticator>()
        val logger = mock<Logger>()
        val sut = spy(EverettBot(auth, logger))

        // Run method under test
        val actRes = sut.createMainMenuKeyboard()

        // Verify
        assertThat(actRes.selective).isTrue()
        assertThat(actRes.resizeKeyboard).isTrue()
        assertThat(actRes.oneTimeKeyboad).isFalse()
        assertThat(actRes.keyboard.size).isEqualTo(1)
        val row = actRes.keyboard[0]
        assertThat(row.size).isEqualTo(1)
        assertThat(row[0].text).isEqualTo("/test")
    }
}