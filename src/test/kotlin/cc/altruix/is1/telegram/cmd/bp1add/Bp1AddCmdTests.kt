package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.IAutomatonFactory
import cc.altruix.is1.telegram.rawdata.RdCmd
import cc.altruix.mock
import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by pisarenko on 13.02.2017.
 */
class Bp1AddCmdTests {
    @Test
    fun executeSunnyDay() {
        // Prepare
        val tu = mock<ITelegramUtils>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(Bp1AddCmd(capsule, tu))
        val text = ""
        val bot = mock<IResponsiveBot>()
        val chatId = 1858L
        val userId = 1302
        val automaton = mock<Bp1AddCmdAutomaton>()
        doReturn(automaton).`when`(sut).createAutomaton(bot, chatId, capsule)

        `when`(tu.extractArgs(text, Bp1AddCmd.Name)).thenReturn(text)
        val inOrder = inOrder(sut, bot, tu, capsule, automaton)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(sut).createAutomaton(bot, chatId, capsule)
        inOrder.verify(bot).subscribe(automaton)
        inOrder.verify(automaton).start()
    }
    @Test
    fun executeBlankMsgText() {
        // Prepare
        val tu = mock<ITelegramUtils>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(Bp1AddCmd(capsule, tu))
        val text = "textA"
        val bot = mock<IResponsiveBot>()
        val chatId = 1858L
        val userId = 1302
        val automaton = mock<Bp1AddCmdAutomaton>()
        doReturn(automaton).`when`(sut).createAutomaton(bot, chatId, capsule)

        `when`(tu.extractArgs(text, Bp1AddCmd.Name)).thenReturn(text)

        val inOrder = inOrder(tu, sut, bot, capsule, automaton)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).sendTextMessage(ITelegramUtils.NoParametersAllowed, chatId, bot)
        inOrder.verify(sut, never()).createAutomaton(bot, chatId, capsule)
        inOrder.verify(bot, never()).subscribe(automaton)
        inOrder.verify(automaton, never()).start()
    }
    @Test
    fun nameAndHelp() {
        val tu = mock<ITelegramUtils>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(Bp1AddCmd(capsule, tu))
        Assertions.assertThat(sut.name()).isEqualTo(Bp1AddCmd.Name)
        Assertions.assertThat(sut.helpText()).isEqualTo(Bp1AddCmd.Help)
    }
}