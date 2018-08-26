package cc.altruix.is1.telegram

import cc.altruix.is1.telegram.cmd.Bp1SuCmd
import cc.altruix.is1.telegram.cmd.Bp1UActCmd
import org.junit.Test
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 10.02.2017.
 */
class AbstractBotTests {
    @Test
    fun extractCmdName() {
        extractCmdNameTestLogic("${Bp1SuCmd.Name}", Bp1SuCmd.Name)
        extractCmdNameTestLogic("${Bp1UActCmd.Name} dp@altruix.co", Bp1UActCmd.Name)
    }
    @Test
    fun subscribeUnsubscribe() {
        // Create object under test
        val sut = AbstractBotForTesting()

        val automaton = mock<ITelegramCmdAutomaton>()
        `when`(automaton.waitingForResponse()).thenReturn(true)

        // Subscribe
        sut.subscribe(automaton)

        // Verify that automatonWaitingForResponse finds it
        val actRes1 = sut.automatonWaitingForResponse()
        assertThat(actRes1).isSameAs(automaton)

        // Unsubscribe
        sut.unsubscribe(automaton)

        // Verify that automatonWaitingForResponse doesn't find it
        val actRes2 = sut.automatonWaitingForResponse()
        assertThat(actRes2).isNull()
    }
    @Test
    fun automatonWaitingForResponse() {
        val waitingAutomaton1 = mockAutomaton(true)
        val waitingAutomaton2 = mockAutomaton(true)
        val idleAutomaton = mockAutomaton(false)

        automatonWaitingForResponseTestLogic(emptyList<ITelegramCmdAutomaton>(), null)
        automatonWaitingForResponseTestLogic(listOf(idleAutomaton), null)
        automatonWaitingForResponseTestLogic(listOf(idleAutomaton, waitingAutomaton1), waitingAutomaton1)
        automatonWaitingForResponseTestLogic(listOf(idleAutomaton, waitingAutomaton1, waitingAutomaton2),
                waitingAutomaton1)
        automatonWaitingForResponseTestLogic(listOf(idleAutomaton, waitingAutomaton2, waitingAutomaton1),
                waitingAutomaton2)
        automatonWaitingForResponseTestLogic(listOf(waitingAutomaton2, waitingAutomaton1),
                waitingAutomaton2)
    }

    private fun mockAutomaton(waiting: Boolean): ITelegramCmdAutomaton {
        val res = mock<ITelegramCmdAutomaton>()
        `when`(res.waitingForResponse()).thenReturn(waiting)
        return res
    }

    private fun automatonWaitingForResponseTestLogic(
            automata: List<ITelegramCmdAutomaton>,
            expectedResult: ITelegramCmdAutomaton?) {
        // Prepare
        val sut = AbstractBotForTesting()

        automata.forEach { sut.subscribe(it) }

        // Run method under test
        val actRes = sut.automatonWaitingForResponse()

        // Verify
        assertThat(actRes).isEqualTo(expectedResult)
    }

    private fun extractCmdNameTestLogic(msgTxt: String, expectedResult: String) {
        // Prepare
        val sut = AbstractBotForTesting()
        val msg = mock<Message>()
        `when`(msg.text).thenReturn(msgTxt)

        // Run method under test
        val actRes = sut.extractCmdName(msg)

        // Verify
        verify(msg).text
        assertThat(actRes).isEqualTo(expectedResult)
    }
}