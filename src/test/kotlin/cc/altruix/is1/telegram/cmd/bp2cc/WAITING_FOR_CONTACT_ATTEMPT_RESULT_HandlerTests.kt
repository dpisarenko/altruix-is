package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.telegram.ITelegramUtils
import org.junit.Test
import cc.altruix.mock
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by 1 on 19.03.2017.
 */
class WAITING_FOR_CONTACT_ATTEMPT_RESULT_HandlerTests {
    @Test
    fun handleIncomingMessageCancel() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_CONTACT_ATTEMPT_RESULT_Handler(parent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(true)
        doNothing().`when`(sut).printMessage(ITelegramUtils.CancelMessage)
        val inOrder = inOrder(parent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut).printMessage(ITelegramUtils.CancelMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.CANCELING)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.CANCELING }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
    @Test
    fun handleIncomingMessageSunnyDay() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_CONTACT_ATTEMPT_RESULT_Handler(parent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        `when`(msg.text).thenReturn("y")
        doNothing().`when`(sut).printMessage(WAITING_FOR_CONTACT_ATTEMPT_RESULT_Handler.SuccessMessage)
        val inOrder = inOrder(parent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut).printMessage(WAITING_FOR_CONTACT_ATTEMPT_RESULT_Handler.SuccessMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.WAITING_FOR_CONTACT_TEXT_AND_NOTE)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.WAITING_FOR_CONTACT_TEXT_AND_NOTE }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
    @Test
    fun handleIncomingMessageRainyDay() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_CONTACT_ATTEMPT_RESULT_Handler(parent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        `when`(msg.text).thenReturn("n")
        doNothing().`when`(sut).printMessage(WAITING_FOR_CONTACT_ATTEMPT_RESULT_Handler.SuccessMessage)
        val inOrder = inOrder(parent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.CANCELING)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.CANCELING }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }

    }
}