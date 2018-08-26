package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify

/**
 * Created by pisarenko on 07.02.2017.
 */
class Bp1UActCmdTests {
    @Test
    fun executeSunnyDay() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val telegramUtils = mock<ITelegramUtils>()
        val sut = Bp1UActCmd(jena, telegramUtils)
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1333L
        val userId = 702
        val args = "dp@altruix.co"
        `when`(telegramUtils.extractArgs(text, Bp1UActCmd.Name)).thenReturn(args)
        `when`(telegramUtils.moreThanOneParameter(args)).thenReturn(false)
        val res = ValidationResult(true, "")
        `when`(jena.activateUser(args)).thenReturn(res)
        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(telegramUtils).extractArgs(text, Bp1UActCmd.Name)
        verify(telegramUtils).moreThanOneParameter(args)
        verify(jena).activateUser(args)
        verify(telegramUtils).sendTextMessage("User activated. Now notify him via e-mail '${args}'", chatId, bot)
    }
    @Test
    fun executeDatabaseError() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val telegramUtils = mock<ITelegramUtils>()
        val sut = Bp1UActCmd(jena, telegramUtils)
        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1333L
        val userId = 702
        val args = "dp@altruix.co"
        `when`(telegramUtils.extractArgs(text, Bp1UActCmd.Name)).thenReturn(args)
        `when`(telegramUtils.moreThanOneParameter(args)).thenReturn(false)
        val res = ValidationResult(false, "Database error")
        `when`(jena.activateUser(args)).thenReturn(res)
        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(telegramUtils).extractArgs(text, Bp1UActCmd.Name)
        verify(telegramUtils).moreThanOneParameter(args)
        verify(jena).activateUser(args)
        verify(telegramUtils).displayError("Database error", chatId, bot)
    }
    @Test
    fun nameAndHelp() {
        val jena = mock<IJenaSubsystem>()
        val telegramUtils = mock<ITelegramUtils>()
        val sut = Bp1UActCmd(jena, telegramUtils)
        assertThat(sut.name()).isEqualTo(Bp1UActCmd.Name)
        assertThat(sut.helpText()).isEqualTo(Bp1UActCmd.Help)
    }
}