package cc.altruix.is1.telegram.rawdata.wordcount

import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCmdAutomaton
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.forms.IAutomatonFactory
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by 1 on 09.04.2017.
 */
class WordCountCommandTests {
    @Test
    fun executeSunnyDay() {
        // Prepare
        val af = mock<IAutomatonFactory>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WordCountCommand(af, tu))
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1317L
        val userId = 904
        val automaton = mock<ITelegramCmdAutomaton>()
        val res = FailableOperationResult<ITelegramCmdAutomaton>(
                true,
                "",
                automaton
        )
        `when`(af.createAutomaton(WordCountCommand.Form, bot, chatId)).thenReturn(res)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(af).createAutomaton(WordCountCommand.Form, bot, chatId)
        verify(bot).subscribe(automaton)
        verify(automaton).start()
    }
    @Test
    fun executeRainyDay() {
        // Prepare
        val af = mock<IAutomatonFactory>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WordCountCommand(af, tu))
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1317L
        val userId = 904
        val res = FailableOperationResult<ITelegramCmdAutomaton>(
                false,
                "IDs not unique",
                null
        )
        `when`(af.createAutomaton(WordCountCommand.Form, bot, chatId)).thenReturn(res)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(af).createAutomaton(WordCountCommand.Form, bot, chatId)
        verify(tu).displayError("Internal error", chatId, bot)
    }
    @Test
    fun nameAndHelp() {
        val sut = WordCountCommand(mock<IAutomatonFactory>())
        assertThat(sut.name()).isEqualTo(WordCountCommand.Name)
        assertThat(sut.helpText()).isEqualTo(WordCountCommand.Help)
    }
}