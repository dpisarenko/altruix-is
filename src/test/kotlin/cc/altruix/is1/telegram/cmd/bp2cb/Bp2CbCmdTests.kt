package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by 1 on 25.02.2017.
 */
class Bp2CbCmdTests {
    @Test
    fun executeSunnyDay() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = spy(Bp2CbCmd(jena, tu))
        val persona = "arg"
        val text = "${Bp2CbCmd.Name} $persona"
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val userId = 2502
        val personaUcase = persona.toUpperCase()
        doReturn(true).`when`(sut).personaValid(personaUcase)
        val automaton = mock<Bp2CbCmdAutomaton>()
        doReturn(automaton).`when`(sut).createAutomaton(bot, chatId, jena, personaUcase)
        val inOrder = inOrder(jena, tu, sut, bot, automaton)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2CbCmd.Name)
        inOrder.verify(sut).personaValid(personaUcase)
        inOrder.verify(sut).createAutomaton(bot, chatId, jena, personaUcase)
        inOrder.verify(bot).subscribe(automaton)
        inOrder.verify(automaton).start()
    }
    @Test
    fun executeNoParameters() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp2CbCmd(jena, tu))
        val text = Bp2CbCmd.Name
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val userId = 2502
        val automaton = mock<Bp2CbCmdAutomaton>()
        val inOrder = inOrder(jena, tu, sut, bot, automaton)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2CbCmd.Name)
        inOrder.verify(tu).sendTextMessage(ITelegramUtils.OneParameterOnlyAllowed, chatId, bot)
    }
    @Test
    fun executeInvalidPersona() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = spy(Bp2CbCmd(jena, tu))
        val persona = "arg"
        val personaUcase = persona.toUpperCase()
        val text = "${Bp2CbCmd.Name} $persona"
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val userId = 2502
        doReturn(false).`when`(sut).personaValid(personaUcase)
        val automaton = mock<Bp2CbCmdAutomaton>()
        doReturn(automaton).`when`(sut).createAutomaton(bot, chatId, jena, personaUcase)
        val inOrder = inOrder(jena, tu, sut, bot, automaton)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp2CbCmd.Name)
        inOrder.verify(sut).personaValid(personaUcase)
        inOrder.verify(tu).sendTextMessage(Bp2CbCmd.WrongPersona, chatId, bot)
    }
    @Test
    fun personaValid() {
        personaValidTestLogic("DP", true)
        personaValidTestLogic("FD", true)
        personaValidTestLogic("abc", false)
        personaValidTestLogic("", false)
        personaValidTestLogic("dp", false)
        personaValidTestLogic("fd", false)

    }
    @Test
    fun nameAndHelp() {
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = spy(Bp2CbCmd(jena, tu))
        assertThat(sut.name()).isEqualTo(Bp2CbCmd.Name)
        assertThat(sut.helpText()).isEqualTo(Bp2CbCmd.Help)
    }
    private fun personaValidTestLogic(persona: String, expRes: Boolean) {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = spy(Bp2CbCmd(jena, tu))

        // Run method under test
        val actRes = sut.personaValid(persona)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}