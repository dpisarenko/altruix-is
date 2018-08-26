package cc.altruix.is1.telegram

import cc.altruix.is1.telegram.cmd.AbstractAutomatonForTesting
import cc.altruix.is1.telegram.cmd.AbstractAutomatonWithAllowedTransitions
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmdAutomaton
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmdState
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.verification.VerificationMode
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.slf4j.Logger
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 13.02.2017.
 */
class AbstractAutomatonTests {
    @Test
    fun goToStateIfPossible() {
        goToStateIfPossibleTestLogic(true, Mockito.times(1))
        goToStateIfPossibleTestLogic(false, never())
    }

    @Test
    fun start() {
        // Prepare
        val sut = spy(AbstractAutomatonForTesting())
        val handlers = mock<Map<Bp1AddCmdState, AutomatonMessageHandler<Bp1AddCmdState>>>()
        doReturn(handlers).`when`(sut).createHandlers()

        // Run method under test
        sut.start()

        // Verify
        verify(sut).createHandlers()
        assertThat(sut.handlers).isSameAs(handlers)
    }

    @Test
    fun changeStateNoHandlers() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AbstractAutomatonForTesting(logger))
        doReturn(null).`when`(sut).createHandlers()

        // Run method under test
        sut.start()
        sut.changeState(Bp1AddCmdState.WAITING_FOR_CONTACT_DATA_TYPE)

        // Verify
        verify(logger).error("No handlers")
        assertThat(sut.state).isEqualTo(Bp1AddCmdState.NEW)
    }
    @Test
    fun changeStateNoRightHandler() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AbstractAutomatonForTesting(logger))
        val handlers = emptyMap<Bp1AddCmdState, AutomatonMessageHandler<Bp1AddCmdState>>()
        doReturn(handlers).`when`(sut).createHandlers()

        // Run method under test
        sut.start()
        sut.changeState(Bp1AddCmdState.WAITING_FOR_CONTACT_DATA_TYPE)

        // Verify
        verify(logger).error("There is no handler for state 'WAITING_FOR_CONTACT_DATA_TYPE'")
        assertThat(sut.state).isEqualTo(Bp1AddCmdState.NEW)
    }
    @Test
    fun changeStateTransitionToWaitingState() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AbstractAutomatonForTesting(logger))
        val handler = mock<AutomatonMessageHandler<Bp1AddCmdState>>()
        val newState = Bp1AddCmdState.WAITING_FOR_CONTACT_DATA_TYPE
        val handlers = mapOf(
                newState to handler
        )
        doReturn(handlers).`when`(sut).createHandlers()

        // Run method under test
        sut.start()
        sut.changeState(newState)

        // Verify
        verify(logger, never()).error("There is no handler for state 'WAITING_FOR_CONTACT_DATA_TYPE'")
        verify(handler, never()).fire()
        assertThat(sut.state).isEqualTo(newState)
    }
    @Test
    fun changeStateTransitionToNonWaitingState() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AbstractAutomatonForTesting(logger))
        val handler = mock<AutomatonMessageHandler<Bp1AddCmdState>>()
        val newState = Bp1AddCmdState.CANCELING
        val handlers = mapOf(
                newState to handler
        )
        doReturn(handlers).`when`(sut).createHandlers()

        // Run method under test
        sut.start()
        sut.changeState(newState)

        // Verify
        verify(logger, never()).error("There is no handler for state 'WAITING_FOR_CONTACT_DATA_TYPE'")
        verify(handler).fire()
        assertThat(sut.state).isEqualTo(newState)
    }
    @Test
    fun canChangeState() {
        canChangeStateTestLogic(Bp1AddCmdState.WAITING_FOR_COMPANY_URL, true)
        canChangeStateTestLogic(Bp1AddCmdState.CANCELING, false)
    }
    @Test
    fun handleIncomingMessageNoHandler() {
        // Prepare
        val logger = mock<Logger>()
        val allowedTransitions = emptyMap<Bp1AddCmdState, List<Bp1AddCmdState>>()
        val sut = spy(
                AbstractAutomatonWithAllowedTransitions(
                        allowedTransitions,
                        logger
                )
        )
        assertThat(sut.state).isEqualTo(Bp1AddCmdState.NEW)

        val msg = mock<Message>()

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(logger).error("No handler for state 'NEW'")
    }
    @Test
    fun handleIncomingMessageSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val handler = mock<AutomatonMessageHandler<Bp1AddCmdState>>()
        val handlers = mapOf(
                Bp1AddCmdState.NEW to handler
        )
        val allowedTransitions = mapOf<Bp1AddCmdState, List<Bp1AddCmdState>>(
                Bp1AddCmdState.NEW to listOf(Bp1AddCmdState.WAITING_FOR_COMPANY_URL)
        )
        val sut = spy(
                AbstractAutomatonWithAllowedTransitions(
                        allowedTransitions,
                        logger
                )
        )
        val msg = mock<Message>()
        doReturn(handlers).`when`(sut).createHandlers()

        assertThat(sut.state).isEqualTo(Bp1AddCmdState.NEW)
        // Run method under test
        sut.start()
        sut.handleIncomingMessage(msg)

        // Verify
        verify(handler).handleIncomingMessage(msg)
    }

    private fun canChangeStateTestLogic(newState: Bp1AddCmdState, expectedResult: Boolean) {
        // Prepare
        val sut = spy(AbstractAutomatonWithAllowedTransitions(Bp1AddCmdAutomaton.AllowedTransitions))

        // Run method under test
        val actRes = sut.canChangeState(newState)

        // Verify
        assertThat(actRes).isEqualTo(expectedResult)
    }

    private fun goToStateIfPossibleTestLogic(
            canChangeState: Boolean, changeStateInvocations: VerificationMode) {
        // Prepare
        val sut = spy(AbstractAutomatonForTesting())
        val target = Bp1AddCmdState.WAITING_FOR_CONTACT_DATA_TYPE
        doNothing().`when`(sut).changeState(target)
        doReturn(canChangeState).`when`(sut).canChangeState(target)

        // Run method under test
        sut.goToStateIfPossible(target)

        // Verify
        verify(sut).canChangeState(target)
        verify(sut, changeStateInvocations).changeState(target)
    }
}