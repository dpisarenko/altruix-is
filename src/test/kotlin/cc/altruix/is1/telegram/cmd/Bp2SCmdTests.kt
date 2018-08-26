package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by 1 on 19.03.2017.
 */
class Bp2SCmdTests {
    @Test
    fun executeNullArgs() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp2SCmd(jena, tu))
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1814L
        val userId = 1903
        val args = null
        `when`(tu.extractArgs(text, Bp2SCmd.Name)).thenReturn(args)
        val inOrder = inOrder(jena, tu, sut, bot)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2SCmd.Name)
        inOrder.verify(tu).sendTextMessage(
                ITelegramUtils.OneParameterOnlyAllowed,
                chatId,
                bot
        )
    }
    @Test
    fun executeNonNumericBatchId() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp2SCmd(jena, tu))
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1814L
        val userId = 1903
        val args = "a13"
        `when`(tu.extractArgs(text, Bp2SCmd.Name)).thenReturn(args)
        val inOrder = inOrder(jena, tu, sut, bot)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2SCmd.Name)
        inOrder.verify(tu).sendTextMessage(
                "Non-numeric batch ID. Fuck you!",
                chatId,
                bot
        )
    }
    @Test
    fun executeJenaFailure() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp2SCmd(jena, tu))
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1814L
        val userId = 1903
        val batchId = 13
        val args = batchId.toString()
        `when`(tu.extractArgs(text, Bp2SCmd.Name)).thenReturn(args)
        val statusRes = FailableOperationResult<Bp2BatchStatus>(false, "error", null)
        `when`(jena.batchStatus(batchId)).thenReturn(statusRes)
        val inOrder = inOrder(jena, tu, sut, bot)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2SCmd.Name)
        inOrder.verify(jena).batchStatus(batchId)
        inOrder.verify(tu).sendTextMessage(
                "Database error ('error')",
                chatId,
                bot
        )
    }
    @Test
    fun executeSunnyDay() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp2SCmd(jena, tu))
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1814L
        val userId = 1903
        val batchId = 13
        val args = batchId.toString()
        `when`(tu.extractArgs(text, Bp2SCmd.Name)).thenReturn(args)
        val status = Bp2BatchStatus(batchId, 34)
        val statusRes = FailableOperationResult<Bp2BatchStatus>(true, "", status)
        `when`(jena.batchStatus(batchId)).thenReturn(statusRes)
        val inOrder = inOrder(jena, tu, sut, bot)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2SCmd.Name)
        inOrder.verify(jena).batchStatus(batchId)
        inOrder.verify(tu).sendTextMessage(
                "There are 34 companies in batch $batchId.",
                chatId,
                bot
        )
    }
    @Test
    fun nameAndHelp() {
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = Bp2SCmd(jena, tu)
        assertThat(sut.name()).isEqualTo(Bp2SCmd.Name)
        assertThat(sut.helpText()).isEqualTo(Bp2SCmd.Help)
    }
}