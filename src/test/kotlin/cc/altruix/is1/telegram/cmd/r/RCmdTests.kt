package cc.altruix.is1.telegram.cmd.r

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by pisarenko on 13.04.2017.
 */
class RCmdTests {
    @Test
    fun execute() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(RCmd(mongo, tu))
        val thread = mock<ReportThread>()
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1304L
        val userId = 1423
        doReturn(thread).`when`(sut).createReportThread(bot, chatId)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(sut).createReportThread(bot, chatId)
        verify(thread).start()
        verify(tu).sendTextMessage("Report generation started", chatId, bot)
    }
    @Test
    fun nameAndHelp() {
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = RCmd(mongo, tu)
        assertThat(sut.name()).isEqualTo(RCmd.Name)
        assertThat(sut.helpText()).isEqualTo(RCmd.Help)
    }
}