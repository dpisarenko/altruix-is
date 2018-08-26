package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.mock
import org.fest.assertions.Assertions
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import java.io.File

/**
 * Created by 1 on 25.02.2017.
 */
class Bp2CbCmdAutomatonTests {
    @Test
    fun startSunnyDay() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1445L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(
                Bp2CbCmdAutomaton(bot, chatId, jena, "", tu, logger)
        )
        val handlers = emptyMap<Bp2CbCmdState, AutomatonMessageHandler<Bp2CbCmdState>>()
        doReturn(handlers).`when`(sut).createHandlers()
        doNothing().`when`(sut).changeState(Bp2CbCmdState.WAITING_FOR_FILE_UPLOAD)

        // Run method under test
        sut.start()

        // Verify
        verify(sut).createHandlers()
        verify(sut).canChangeState(Bp2CbCmdState.WAITING_FOR_FILE_UPLOAD)
        verify(sut).changeState(Bp2CbCmdState.WAITING_FOR_FILE_UPLOAD)
    }
    @Test
    fun createHandlers() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1445L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(
                Bp2CbCmdAutomaton(bot, chatId, jena, "", tu, logger)
        )

        // Run method under test
        val actRes = sut.createHandlers()

        // Verify
        Bp2CbCmdState.values()
                .filter { !it.terminalState && !it.initialState }
                .forEach { state ->
                    Assertions.assertThat(actRes[state]).isNotNull
                }
    }
    @Test
    fun unsubscribe() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1445L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(
                Bp2CbCmdAutomaton(bot, chatId, jena, "", tu, logger)
        )

        // Run method under test
        sut.unsubscribe()

        // Verify
        verify(bot).unsubscribe(sut)
    }
    @Test
    fun printMessage() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1445L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(
                Bp2CbCmdAutomaton(bot, chatId, jena, "", tu, logger)
        )
        val msg = "msg"

        // Run method under test
        sut.printMessage(msg)

        // Verify
        verify(tu).sendTextMessage(msg, chatId, bot)
    }
    @Test
    fun startPrintsFileUploadPrompt() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1445L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(
                Bp2CbCmdAutomaton(bot, chatId, jena, "", tu, logger)
        )
        doReturn(true).`when`(sut).canChangeState(Bp2CbCmdState.WAITING_FOR_FILE_UPLOAD)
        doNothing().`when`(sut).printMessage(Bp2CbCmdAutomaton.FileUploadPrompt)

        // Run method under test
        sut.start()

        // Verify
        verify(sut).printMessage(Bp2CbCmdAutomaton.FileUploadPrompt)
    }
    @Test
    fun readFileContents() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1445L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(
                Bp2CbCmdAutomaton(bot, chatId, jena, "", tu, logger)
        )
        val fileId = "fileId"
        val file = mock<File>()
        `when`(bot.readFileContents(fileId)).thenReturn(file)

        // Run method under test
        val actRes = sut.readFileContents(fileId)

        // Verify
        verify(bot).readFileContents(fileId)
        assertThat(actRes).isSameAs(file)
    }
    @Test
    fun persona() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1445L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val persona = "persona"
        val sut = Bp2CbCmdAutomaton(bot, chatId, jena, persona, tu, logger)

        // Run method under test
        val actRes = sut.persona()

        // Verify
        assertThat(actRes).isEqualTo(persona)
    }
}