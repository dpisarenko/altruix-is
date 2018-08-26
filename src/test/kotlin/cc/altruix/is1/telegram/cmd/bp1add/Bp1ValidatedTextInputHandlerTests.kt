package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.ITelegramUtils
import org.junit.Test
import cc.altruix.mock
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 14.02.2017.
 */
class Bp1ValidatedTextInputHandlerTests {
    @Test
    fun handleIncomingMessageCancel() {
        // Prepare
        val state = Bp1AddCmdState.WAITING_FOR_EMAIL
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val errorMsg = "errorMsg"
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp1ValidatedTextInputHandlerForTesting(state, parent, errorMsg, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(true)
        doNothing().`when`(sut).printMessage(ITelegramUtils.CancelMessage)
        val msgTxt = "msgTxt"
        `when`(msg.text).thenReturn(msgTxt)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(tu).cancelCommand(msg)
        verify(sut).printMessage(ITelegramUtils.CancelMessage)
        verify(parent).goToStateIfPossible(Bp1AddCmdState.CANCELING)
        Bp1AddCmdState.values().filter { it != Bp1AddCmdState.CANCELING }.forEach {
            verify(parent, never()).goToStateIfPossible(it)
        }
        verify(sut, never()).saveData(msgTxt)
    }
    @Test
    fun handleIncomingMessageInvalidInput() {
        // Prepare
        val state = Bp1AddCmdState.WAITING_FOR_EMAIL
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val errorMsg = "errorMsg"
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp1ValidatedTextInputHandlerForTesting(state, parent, errorMsg, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        doNothing().`when`(sut).printMessage(ITelegramUtils.CancelMessage)
        val msgTxt = "msgTxt"
        `when`(msg.text).thenReturn(msgTxt)
        doReturn(false).`when`(sut).inputValid(msgTxt)
        doNothing().`when`(sut).printMessage(errorMsg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(tu).cancelCommand(msg)
        verify(sut).inputValid(msgTxt)
        verify(sut).printMessage(errorMsg)
        verify(sut, never()).saveData(msgTxt)
        Bp1AddCmdState.values().forEach {
            verify(parent, never()).goToStateIfPossible(it)
        }
    }
    @Test
    fun handleIncomingMessageSunnyDay() {
        // Prepare
        val state = Bp1AddCmdState.WAITING_FOR_EMAIL
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val errorMsg = "errorMsg"
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp1ValidatedTextInputHandlerForTesting(state, parent, errorMsg, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        doNothing().`when`(sut).printMessage(ITelegramUtils.CancelMessage)
        val msgTxt = "msgTxt"
        `when`(msg.text).thenReturn(msgTxt)
        doReturn(true).`when`(sut).inputValid(msgTxt)
        doNothing().`when`(sut).printMessage(errorMsg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(tu).cancelCommand(msg)
        verify(sut).inputValid(msgTxt)
        verify(sut, never()).printMessage(errorMsg)
        verify(sut).saveData(msgTxt)
        verify(parent).goToStateIfPossible(Bp1AddCmdState.WAITING_FOR_NOTE)
        Bp1AddCmdState.values().filter { it != Bp1AddCmdState.WAITING_FOR_NOTE } .forEach {
            verify(parent, never()).goToStateIfPossible(it)
        }
    }
}