package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.telegram.ITelegramUtils
import org.junit.Test
import cc.altruix.mock
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by 1 on 19.03.2017.
 */
class WAITING_FOR_CONTACT_TEXT_AND_NOTE_HandlerTests {
    @Test
    fun handleIncomingMessageCancelCommand() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                WAITING_FOR_CONTACT_TEXT_AND_NOTE_Handler(
                        parent,
                        tu
                )
        )
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
    fun handleIncomingMessageEmptyText() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                WAITING_FOR_CONTACT_TEXT_AND_NOTE_Handler(
                        parent,
                        tu
                )
        )
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        `when`(msg.text).thenReturn(" ")
        doNothing().`when`(sut).printMessage(WAITING_FOR_CONTACT_TEXT_AND_NOTE_Handler.EmptyMessageErrorText)
        val inOrder = inOrder(parent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(msg).text
        inOrder.verify(sut).printMessage(WAITING_FOR_CONTACT_TEXT_AND_NOTE_Handler.EmptyMessageErrorText)
        Bp2CcCmdState.values().forEach {
            inOrder.verify(parent, never()).goToStateIfPossible(it)
        }
    }
    @Test
    fun handleIncomingMessageSunnyDay() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                WAITING_FOR_CONTACT_TEXT_AND_NOTE_Handler(
                        parent,
                        tu
                )
        )
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        val contactNoteAndText = "bla"
        `when`(msg.text).thenReturn(contactNoteAndText)
        val inOrder = inOrder(parent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(msg).text
        inOrder.verify(parent).setContactTextAndNote(contactNoteAndText)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.SAVING_DATA)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.SAVING_DATA }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
}