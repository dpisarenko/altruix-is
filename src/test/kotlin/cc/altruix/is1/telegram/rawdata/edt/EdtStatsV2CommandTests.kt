package cc.altruix.is1.telegram.rawdata.edt

import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCmdAutomaton
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.forms.IAutomatonFactory
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import cc.altruix.utils.allIdsUnique
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito

/**
 * Created by pisarenko on 19.04.2017.
 */
class EdtStatsV2CommandTests {
    companion object {
        val FormUnderTest = EdtStatsV2Command.Form
    }
    @Test
    fun formIdsUnique() {
        assertThat(FormUnderTest.allIdsUnique()).isTrue()
    }
    @Test
    fun executeSunnyDay() {
        // Prepare
        val af = mock<IAutomatonFactory>()
        val tu = mock<ITelegramUtils>()
        val sut = Mockito.spy(EdtStatsV2Command(af, tu))
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
        Mockito.`when`(af.createAutomaton(FormUnderTest, bot, chatId)).thenReturn(res)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        Mockito.verify(af).createAutomaton(FormUnderTest, bot, chatId)
        Mockito.verify(bot).subscribe(automaton)
        Mockito.verify(automaton).start()
    }
    @Test
    fun executeRainyDay() {
        // Prepare
        val af = mock<IAutomatonFactory>()
        val tu = mock<ITelegramUtils>()
        val sut = Mockito.spy(EdtStatsV2Command(af, tu))
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1317L
        val userId = 904
        val res = FailableOperationResult<ITelegramCmdAutomaton>(
                false,
                "IDs not unique",
                null
        )
        Mockito.`when`(af.createAutomaton(FormUnderTest, bot, chatId)).thenReturn(res)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        Mockito.verify(af).createAutomaton(FormUnderTest, bot, chatId)
        Mockito.verify(tu).displayError("Internal error", chatId, bot)
    }
    @Test
    fun nameAndHelp() {
        val sut = EdtStatsV2Command(mock<IAutomatonFactory>())
        assertThat(sut.name()).isEqualTo(EdtStatsV2Command.Name)
        assertThat(sut.helpText()).isEqualTo(EdtStatsV2Command.Help)
    }
}