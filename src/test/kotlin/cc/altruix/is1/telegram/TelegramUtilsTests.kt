package cc.altruix.is1.telegram

import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 02.02.2017.
 */
class TelegramUtilsTests {
    @Test
    fun sendInvalidArgsMessage() {
        // Prepare
        val sut = Mockito.spy(TelegramUtils())
        val msg = mock<SendMessage>()
        val msgTxt = "msgTxt"
        val bot = mock<IResponsiveBot>()
        val chatId = 2036L
        Mockito.doReturn(msg).`when`(sut).createSendTextMessage(msgTxt, chatId)

        // Run method under test
        sut.sendTextMessage(msgTxt, chatId, bot)

        // Verify
        Mockito.verify(sut).createSendTextMessage(msgTxt, chatId)
        Mockito.verify(bot).sendTelegramMessage(msg)
    }
    @Test
    fun createInvalidArgsMessage() {
        // Prepare
        val sut = Mockito.spy(TelegramUtils())
        val msg = mock<SendMessage>()
        Mockito.doReturn(msg).`when`(sut).createSendMessage()
        val msgTxt = "msgTxt"
        val chatId = 2036L

        // Run method under test
        sut.createSendTextMessage(msgTxt, chatId)

        // Verify
        Mockito.verify(sut).createSendMessage()
        Mockito.verify(msg).setText(msgTxt)
        Mockito.verify(msg).setChatId(chatId.toString())
        Mockito.verify(msg).enableMarkdown(false)
    }
    @Test
    fun displayError() {
        // Prepare
        val sut = Mockito.spy(TelegramUtils())
        val bot = mock<IResponsiveBot>()
        val chatId = 2022L
        val errorMsg = "LooseWienerException"
        val sendMsg = mock<SendMessage>()
        Mockito.doReturn(sendMsg).`when`(sut).createErrorResponse(errorMsg, chatId)

        // Run method under test
        sut.displayError(errorMsg, chatId, bot)

        // Verify
        Mockito.verify(sut).createErrorResponse(errorMsg, chatId)
        Mockito.verify(bot).sendTelegramMessage(sendMsg)
    }
    @Test
    fun createErrorResponse() {
        // Prepare
        val sut = Mockito.spy(TelegramUtils())
        val chatId = 2022L
        val errorMsg = "LooseWienerException"

        // Run method under test
        val actRes = sut.createErrorResponse(errorMsg, chatId)

        // Verify
        assertThat(actRes.text).isEqualTo("An error occured ('LooseWienerException')")
        assertThat(actRes.chatId).isEqualTo(chatId.toString())
    }
    @Test
    fun createSendMessageWithText() {
        // Prepare
        val sut = Mockito.spy(TelegramUtils())
        val chatId = 2022L
        val txt = "txt"

        // Run method under test
        val actRes = sut.createSendTextMessage(txt, chatId)

        // Verify
        assertThat(actRes.text).isEqualTo(txt)
        assertThat(actRes.chatId).isEqualTo(chatId.toString())
    }
    @Test
    fun cancelCommand() {
        cancelCommandTestLogic("/cancel", false, false)
        cancelCommandTestLogic("/can cel", true, false)
        cancelCommandTestLogic("/cancel", true, true)
        cancelCommandTestLogic("/cAncel", true, true)
        cancelCommandTestLogic("/CANCEL", true, true)
        cancelCommandTestLogic("/CAnCeL", true, true)
        cancelCommandTestLogic("/kusch", false, false)
        cancelCommandTestLogic("/kusch", true, true)
    }
    private fun cancelCommandTestLogic(
            msgText: String, isCommand: Boolean, expRes: Boolean) {
        // Prepare
        val sut = TelegramUtils()
        val msg = mock<Message>()
        `when`(msg.text).thenReturn(msgText)
        `when`(msg.isCommand).thenReturn(isCommand)

        // Run method under test
        val actRes = sut.cancelCommand(msg)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}