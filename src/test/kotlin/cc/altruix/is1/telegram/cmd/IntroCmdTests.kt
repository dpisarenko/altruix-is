package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.AbstractCommandRegistry
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.cmd.IntroCmd
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import org.fest.assertions.Assertions
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import kotlin.test.assertTrue

/**
 * Created by pisarenko on 02.02.2017.
 */
class IntroCmdTests {
    @Test
    fun executeCorrectlyReactsToSuccesfulDbInteraction() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val boss = mock<IResponsiveBot>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(IntroCmd(jena, boss, tu))
        val nick = "dp"
        val email = "dp@altruix.co"
        val text = "/intro ${nick} ${email}"
        val bot = mock<IResponsiveBot>()
        val chatId = 1240L
        val userId = 343
        val res = ValidationResult(true, "")
        `when`(jena.createNewUser(nick, email, userId, chatId)).thenReturn(res)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(sut).argCountCorrect(listOf(nick, email))
        verify(sut).isAlphanumeric(nick)
        verify(sut).isValidEmail(email)
        verify(jena).createNewUser(nick, email, userId, chatId)
        verify(tu).sendTextMessage(
                "User created. My boss will contact you via e-mail within 72 hours.",
                chatId,
                bot
        )
        verify(boss).sendBroadcast("Knackal: Mir haben an Neuen: dp ('dp@altruix.co') hinzugefügt.")
    }
    @Test
    fun executeCorrectlyReactsFailedDbInteraction() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val boss = mock<IResponsiveBot>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(IntroCmd(jena, boss, tu))
        val nick = "dp"
        val email = "dp@altruix.co"
        val text = "/intro ${nick} ${email}"
        val bot = mock<IResponsiveBot>()
        val chatId = 1240L
        val userId = 343
        val errorMsg = "OutOfPunchCardsException"
        val res = ValidationResult(false, errorMsg)
        `when`(jena.createNewUser(nick, email, userId, chatId)).thenReturn(res)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(sut).argCountCorrect(listOf(nick, email))
        verify(sut).isAlphanumeric(nick)
        verify(sut).isValidEmail(email)
        verify(jena).createNewUser(nick, email, userId, chatId)
        verify(tu).displayError(
                errorMsg,
                chatId,
                bot
        )
    }
    @Test
    fun argCountCorrect() {
        argCountTestLogic(emptyList(), false)
        argCountTestLogic(listOf("a", "b"), true)
        argCountTestLogic(listOf("a", "b", "c"), false)
    }
    @Test
    fun nameAndHelp() {
        val jena = mock<IJenaSubsystem>()
        val boss = mock<IResponsiveBot>()
        val tu = mock<ITelegramUtils>()
        val sut = IntroCmd(jena, boss, tu)
        assertThat(sut.name()).isEqualTo(IntroCmd.Name)
        assertThat(sut.helpText()).isEqualTo(IntroCmd.Help)
    }

    private fun argCountTestLogic(parts: List<String>, expectedResult: Boolean) {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val boss = mock<IResponsiveBot>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(IntroCmd(jena, boss, tu))

        // Run method under test
        val actRes = sut.argCountCorrect(parts)

        // Verify
        assertThat(actRes).isEqualTo(expectedResult)
    }
}