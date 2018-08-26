package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.AbstractCommandRegistry
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.bots.herrKarl.HerrKarlCommandRegistry
import cc.altruix.mock
import org.apache.commons.io.IOUtils
import org.fest.assertions.Assertions
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.methods.send.SendMessage

/**
 * Created by pisarenko on 10.05.2017.
 */
class HCmdTests {
    @Test
    fun nameAndHelp() {
        val sut = HCmd(mock<AbstractCommandRegistry>())
        Assertions.assertThat(sut.name()).isEqualTo(HCmd.Name)
        Assertions.assertThat(sut.helpText()).isEqualTo(HCmd.Help)
    }
    @Test
    fun execute() {
        // Prepare
        val cmdReg = mock<AbstractCommandRegistry>()
        val sut = spy(HCmd(cmdReg))
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1542L
        val userId = 1543

        val txt = "txt"
        doReturn(txt).`when`(sut).composeHelpText()
        val msg = mock<SendMessage>()
        doReturn(msg).`when`(sut).createSendMessage(chatId, txt)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(sut).composeHelpText()
        verify(sut).createSendMessage(chatId, txt)
        verify(bot).sendTelegramMessage(msg)
    }
    @Test
    fun composeCmdText() {
        // Prepare
        val cmdReg = mock<AbstractCommandRegistry>()
        val sut = spy(HCmd(cmdReg))
        val cmdEntry = mock<MutableMap.MutableEntry<String, ITelegramCommand>>()
        val cmd = mock<ITelegramCommand>()
        `when`(cmdEntry.value).thenReturn(cmd)
        `when`(cmd.name()).thenReturn("name")
        `when`(cmd.helpText()).thenReturn("helpText")

        // Run method under test
        val actRes = sut.composeCmdText(cmdEntry)

        // Verify
        verify(cmdEntry).value
        verify(cmd).name()
        verify(cmd).helpText()
        assertThat(actRes).isEqualTo("name: helpText")
    }
    @Test
    fun composeHelpText() {
        // Prepare
        val cmdReg = HerrKarlCommandRegistry(
                mock<ICapsuleCrmSubsystem>(),
                mock<IJenaSubsystem>(),
                mock<IAltruixIs1MongoSubsystem>()
        )
        cmdReg.init()
        val sut = HCmd(cmdReg)

        // Run method under test
        val actRes = sut.composeHelpText()

        // Verify
        assertThat(actRes).isEqualTo(readFile("HCmdTests.txt"))
    }
    @Test
    fun createSendMessage() {
        // Prepare
        val cmdReg = mock<AbstractCommandRegistry>()
        val sut = spy(HCmd(cmdReg))
        val chatId = 1507L
        val txt = "txt"
        val msg = mock<SendMessage>()
        doReturn(msg).`when`(sut).createSendMessage()


        // Run method under test
        val actRes = sut.createSendMessage(chatId, txt)

        // Verify
        verify(sut).createSendMessage()
        verify(msg).enableMarkdown(true)
        verify(msg).setChatId(chatId.toString())
        verify(msg).setText(txt)
        assertThat(actRes).isSameAs(msg)
    }
    private fun readFile(file: String) = IOUtils.toString(javaClass.classLoader.getResourceAsStream("cc/altruix/is1/telegram/cmd/" + file), "UTF-8")
}