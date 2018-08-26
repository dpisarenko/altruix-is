package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.ITelegramUtils
import org.junit.Test
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 14.02.2017.
 */
class WAITING_FOR_CONTACT_DATA_TYPE_HandlerTests {
    @Test
    fun extractContactType() {
        extractContactTypeTestLogic("e", ContactDataType.EMAIL)
        extractContactTypeTestLogic(" E  ", ContactDataType.EMAIL)
        extractContactTypeTestLogic("c", ContactDataType.CONTACT_FORM)
        extractContactTypeTestLogic("   C   ", ContactDataType.CONTACT_FORM)
        extractContactTypeTestLogic(" ", ContactDataType.UNKNOWN)
        extractContactTypeTestLogic("ec", ContactDataType.UNKNOWN)
        extractContactTypeTestLogic("bla", ContactDataType.UNKNOWN)
        extractContactTypeTestLogic(null, ContactDataType.UNKNOWN)
    }
    @Test
    fun determineContactTextAndTargetState() {
        determineContactTextAndTargetStateTestLogic(
                ContactDataType.UNKNOWN,
                Pair("?", Bp1AddCmdState.CANCELING)
        )
        determineContactTextAndTargetStateTestLogic(
                ContactDataType.EMAIL,
                Pair("e-mail", Bp1AddCmdState.WAITING_FOR_EMAIL)
        )
        determineContactTextAndTargetStateTestLogic(
                ContactDataType.CONTACT_FORM,
                Pair("URL of the contact form", Bp1AddCmdState.WAITING_FOR_CONTACT_FORM_URL)
        )
    }
    @Test
    fun handleIncomingMessageCancel() {
        // Prepare
        val customParent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_CONTACT_DATA_TYPE_Handler(customParent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(true)
        doNothing().`when`(sut).printMessage(ITelegramUtils.CancelMessage)

        val inOrder = inOrder(customParent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut).printMessage(ITelegramUtils.CancelMessage)
        inOrder.verify(customParent).goToStateIfPossible(Bp1AddCmdState.CANCELING)
        Bp1AddCmdState.values().filter { it !=  Bp1AddCmdState.CANCELING }.forEach {
            inOrder.verify(customParent, never()).goToStateIfPossible(it)
        }
    }
    @Test
    fun handleIncomingMessageUnknownType() {
        // Prepare
        val customParent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_CONTACT_DATA_TYPE_Handler(customParent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        val msgText = "msgText"
        `when`(msg.text).thenReturn(msgText)
        val type = ContactDataType.UNKNOWN
        doReturn(type).`when`(sut).extractContactType(msgText)
        doNothing().`when`(sut).printMessage(WAITING_FOR_CONTACT_DATA_TYPE_Handler.WrongMessage)

        val inOrder = inOrder(customParent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut).extractContactType(msgText)
        inOrder.verify(sut).printMessage(WAITING_FOR_CONTACT_DATA_TYPE_Handler.WrongMessage)
        inOrder.verify(customParent, never()).saveContactDataType(type)
        Bp1AddCmdState.values().forEach {
            inOrder.verify(customParent, never()).goToStateIfPossible(it)
        }
    }
    @Test
    fun handleIncomingMessageSunnyDay() {
        handleIncomingMessageSunnyDayTestLogic(ContactDataType.EMAIL, "contactMechanism", Bp1AddCmdState.WAITING_FOR_EMAIL)
        handleIncomingMessageSunnyDayTestLogic(ContactDataType.CONTACT_FORM, "contactMechanism", Bp1AddCmdState.WAITING_FOR_CONTACT_FORM_URL)
    }

    private fun handleIncomingMessageSunnyDayTestLogic(type: ContactDataType, contactMechanism: String, targetState: Bp1AddCmdState) {
        // Prepare
        val customParent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_CONTACT_DATA_TYPE_Handler(customParent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        val msgText = "msgText"
        `when`(msg.text).thenReturn(msgText)
        doReturn(type).`when`(sut).extractContactType(msgText)
        doReturn(Pair(contactMechanism, targetState)).`when`(sut).determineContactTextAndTargetState(type)
        doNothing().`when`(sut).printMessage("Now please enter ${contactMechanism} of the company.")
        val inOrder = inOrder(customParent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut).extractContactType(msgText)
        inOrder.verify(customParent).saveContactDataType(type)
        inOrder.verify(sut).determineContactTextAndTargetState(type)
        inOrder.verify(sut).printMessage("Now please enter ${contactMechanism} of the company.")
        inOrder.verify(customParent).goToStateIfPossible(targetState)
        Bp1AddCmdState.values().filter { it != targetState }.forEach {
            inOrder.verify(customParent, never()).goToStateIfPossible(it)
        }
    }

    private fun determineContactTextAndTargetStateTestLogic(type: ContactDataType, expRes: Pair<String, Bp1AddCmdState>) {
        // Prepare
        val customParent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_CONTACT_DATA_TYPE_Handler(customParent, tu))

        // Run method under test
        val actRes = sut.determineContactTextAndTargetState(type)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun extractContactTypeTestLogic(msgText: String?, expRes: ContactDataType) {
        // Prepare
        val customParent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_CONTACT_DATA_TYPE_Handler(customParent, tu))

        // Run method under test
        val actRes = sut.extractContactType(msgText)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}