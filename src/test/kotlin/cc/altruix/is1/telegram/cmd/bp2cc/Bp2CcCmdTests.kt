package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCmdAutomaton
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by pisarenko on 15.03.2017.
 */
class Bp2CcCmdTests {
    @Test
    fun executeSunnyDay() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val batchId = 1
        val text = "${Bp2CcCmd.Name} $batchId"
        val bot = mock<IResponsiveBot>()
        val chatId = 1503L
        val userId = 2017
        val sut = spy(
                Bp2CcCmd(
                        jena,
                        mock<ICapsuleCrmSubsystem>(),
                        tu
                )
        )
        val automaton = mock<Bp2CcCmdAutomaton>()
        val inOrder = inOrder(jena, tu, bot, sut, automaton)
        doReturn(automaton).`when`(sut).createAutomaton(bot, chatId, batchId)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2CcCmd.Name)
        inOrder.verify(sut).createAutomaton(bot, chatId, batchId)
        inOrder.verify(bot).subscribe(automaton)
        inOrder.verify(automaton).start()
    }
    @Test
    fun executeNoArgs() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val batchId = 1
        val text = "${Bp2CcCmd.Name}"
        val bot = mock<IResponsiveBot>()
        val chatId = 1503L
        val userId = 2017
        val sut = spy(
                Bp2CcCmd(
                        jena,
                        mock<ICapsuleCrmSubsystem>(),
                        tu
                )
        )
        val automaton = mock<Bp2CcCmdAutomaton>()
        val inOrder = inOrder(jena, tu, bot, sut, automaton)
        doReturn(automaton).`when`(sut).createAutomaton(bot, chatId, batchId)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2CcCmd.Name)
        inOrder.verify(tu).sendTextMessage(ITelegramUtils.OneParameterOnlyAllowed, chatId, bot)
        inOrder.verify(sut, never()).createAutomaton(bot, chatId, batchId)
        inOrder.verify(bot, never()).subscribe(automaton)
        inOrder.verify(automaton, never()).start()
    }
    @Test
    fun executeNonNumericBatchNumber() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val batchId = 1
        val text = "${Bp2CcCmd.Name} bla"
        val bot = mock<IResponsiveBot>()
        val chatId = 1503L
        val userId = 2017
        val sut = spy(
                Bp2CcCmd(
                        jena,
                        mock<ICapsuleCrmSubsystem>(),
                        tu
                )
        )
        val automaton = mock<Bp2CcCmdAutomaton>()
        val inOrder = inOrder(jena, tu, bot, sut, automaton)
        doReturn(automaton).`when`(sut).createAutomaton(bot, chatId, batchId)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2CcCmd.Name)
        inOrder.verify(tu).sendTextMessage(Bp2CcCmd.NonNumericBatchNumber, chatId, bot)
        inOrder.verify(sut, never()).createAutomaton(bot, chatId, batchId)
        inOrder.verify(bot, never()).subscribe(automaton)
        inOrder.verify(automaton, never()).start()
    }
    @Test
    fun createAutomaton() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val batchId = 1
        val bot = mock<IResponsiveBot>()
        val chatId = 1503L
        val sut = spy(
                Bp2CcCmd(
                        jena,
                        mock<ICapsuleCrmSubsystem>(),
                        tu
                )
        )

        // Run method under test
        val actRes = sut.createAutomaton(bot, chatId, batchId)

        // Verify
        assertThat(actRes.bot).isSameAs(bot)
        assertThat(actRes.chatId).isEqualTo(chatId)
        assertThat(actRes.jena).isSameAs(jena)
    }
    @Test
    fun nameAndHelp() {
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = spy(
                Bp2CcCmd(
                        jena,
                        mock<ICapsuleCrmSubsystem>(),
                        tu
                )
        )
        assertThat(sut.name()).isEqualTo(Bp2CcCmd.Name)
        assertThat(sut.helpText()).isEqualTo(Bp2CcCmd.Help)
    }
}