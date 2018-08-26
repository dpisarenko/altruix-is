package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.ITelegramUtils
import org.junit.Test
import cc.altruix.mock
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.User

/**
 * Created by pisarenko on 14.02.2017.
 */
class WAITING_FOR_NOTE_HandlerTests {
    @Test
    fun handleIncomingMessageCancelCommand() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_NOTE_Handler(parent, tu))
        val msgText = "msgText"
        val msg = mock<Message>()
        `when`(msg.text).thenReturn(msgText)
        `when`(tu.cancelCommand(msg)).thenReturn(true)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(tu).cancelCommand(msg)
        verify(parent).goToStateIfPossible(Bp1AddCmdState.CANCELING)
        verify(parent, never()).saveNote(msgText)
        Bp1AddCmdState.values().filter { it != Bp1AddCmdState.CANCELING }.forEach { state ->
            verify(parent, never()).goToStateIfPossible(state)
        }
    }
    @Test
    fun handleIncomingMessageBlankText() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_NOTE_Handler(parent, tu))
        val msgText = ""
        val msg = mock<Message>()
        `when`(msg.text).thenReturn(msgText)
        val from = mock<User>()
        `when`(msg.from).thenReturn(from)
        `when`(tu.cancelCommand(msg)).thenReturn(false)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(tu).cancelCommand(msg)
        verify(parent, never()).saveNote(msgText)
        verify(parent).saveAgent(from)
        verify(parent).unsubscribe()
        verify(parent).goToStateIfPossible(Bp1AddCmdState.SAVING_DATA_IN_CAPSULE)
        Bp1AddCmdState.values()
                .filter { state -> state != Bp1AddCmdState.SAVING_DATA_IN_CAPSULE }
                .forEach { state ->
                    verify(parent, never()).goToStateIfPossible(state)
                }
    }
    @Test
    fun handleIncomingMessageSunnyDay() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_NOTE_Handler(parent, tu))
        val msgText = "msgText"
        val msg = mock<Message>()
        val from = mock<User>()
        `when`(msg.text).thenReturn(msgText)
        `when`(msg.from).thenReturn(from)
        `when`(tu.cancelCommand(msg)).thenReturn(false)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(tu).cancelCommand(msg)
        verify(parent).saveNote(msgText)
        verify(parent).saveAgent(from)
        verify(parent).unsubscribe()
        verify(parent).goToStateIfPossible(Bp1AddCmdState.SAVING_DATA_IN_CAPSULE)
        Bp1AddCmdState.values()
                .filter { state -> state != Bp1AddCmdState.SAVING_DATA_IN_CAPSULE }
                .forEach { state ->
                    verify(parent, never()).goToStateIfPossible(state)
                }
    }
}