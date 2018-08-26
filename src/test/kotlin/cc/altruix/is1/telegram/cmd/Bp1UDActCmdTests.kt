package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.validation.ValidationResult
import org.junit.Test
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.mockito.Mockito.*

/**
 * Created by pisarenko on 06.03.2017.
 */
class Bp1UDActCmdTests {
    @Test
    fun executeSunnyDay() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = spy(Bp1UDActCmd(jena, tu))
        val text = "/bp1udact dp@altruix.co"
        val bot = mock<IResponsiveBot>()
        val chatId = 1313L
        val userId = 31131
        val res = ValidationResult(true, "")
        `when`(jena.deActivateUser("dp@altruix.co")).thenReturn(res)
        val inOrder = inOrder(jena, tu, sut)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(jena).deActivateUser("dp@altruix.co")
        inOrder.verify(tu).sendTextMessage("User 'dp@altruix.co' de-activated.", chatId, bot)
    }
    @Test
    fun executeRainyDay() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = spy(Bp1UDActCmd(jena, tu))
        val text = "/bp1udact dp@altruix.co"
        val bot = mock<IResponsiveBot>()
        val chatId = 1313L
        val userId = 31131
        val res = ValidationResult(false, "error")
        `when`(jena.deActivateUser("dp@altruix.co")).thenReturn(res)
        val inOrder = inOrder(jena, tu, sut)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(jena).deActivateUser("dp@altruix.co")
        inOrder.verify(tu).displayError("error", chatId, bot)
    }
    @Test
    fun nameAndHelp() {
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = Bp1UDActCmd(jena, tu)
        assertThat(sut.name()).isEqualTo(Bp1UDActCmd.Name)
        assertThat(sut.helpText()).isEqualTo(Bp1UDActCmd.Help)
    }
}