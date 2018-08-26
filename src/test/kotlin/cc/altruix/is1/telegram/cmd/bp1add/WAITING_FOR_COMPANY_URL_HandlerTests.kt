package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 13.02.2017.
 */
class WAITING_FOR_COMPANY_URL_HandlerTests {
    @Test
    fun handleIncomingMessageDoesNothingIfCancelCommandReceived() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_COMPANY_URL_Handler(parent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(true)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(sut).printMessage(ITelegramUtils.CancelMessage)
        verify(parent).goToStateIfPossible(Bp1AddCmdState.CANCELING)
        verify(sut, never()).extractUrl(msg)
    }

    @Test
    fun handleIncomingMessageHandlesRightUrlCorrectly() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_COMPANY_URL_Handler(parent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        val url = "url"
        doReturn(url).`when`(sut).extractUrl(msg)
        doReturn(true).`when`(sut).urlCorrect(url)
        doNothing().`when`(sut).printMessage(
                WAITING_FOR_COMPANY_URL_Handler.CorrectUrlMessage
        )

        val inOrder = inOrder(parent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut).extractUrl(msg)
        inOrder.verify(sut).urlCorrect(url)
        inOrder.verify(sut).printMessage(
                WAITING_FOR_COMPANY_URL_Handler.CorrectUrlMessage
        )
        inOrder.verify(parent).saveMainUrl(url)
        inOrder.verify(parent).goToStateIfPossible(Bp1AddCmdState.WAITING_FOR_CONTACT_DATA_TYPE)
    }
    @Test
    fun handleIncomingMessageHandlesWrongUrlCorrectly() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_COMPANY_URL_Handler(parent, tu))
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        val url = "url"
        doReturn(url).`when`(sut).extractUrl(msg)
        doReturn(false).`when`(sut).urlCorrect(url)
        doNothing().`when`(sut).printMessage(
                WAITING_FOR_COMPANY_URL_Handler.IncorrectUrlMessage
        )

        val inOrder = inOrder(parent, tu, sut, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut).extractUrl(msg)
        inOrder.verify(sut).urlCorrect(url)
        inOrder.verify(sut).printMessage(
                WAITING_FOR_COMPANY_URL_Handler.IncorrectUrlMessage
        )
        Bp1AddCmdState.values().forEach { state ->
            inOrder.verify(parent, never()).goToStateIfPossible(state)
        }
    }
    @Test
    fun urlCorrect() {
        urlCorrectTestLogic("", false)
        urlCorrectTestLogic("bla", false)
        urlCorrectTestLogic("http://stackoverflow.com/questions/1600291/validating-url-in-java", true)
        urlCorrectTestLogic("https://stackoverflow.com/questions/1600291/validating-url-in-java", true)
        urlCorrectTestLogic("h ttp://stackoverflow.com/questions/1600291/validating-url-in-java", false)
        urlCorrectTestLogic("https://stackoverflow.com/questions/1600291/validating-u rl-in-java", false)
    }
    @Test
    fun extractUrl() {
        extractUrlTestLogic("", "")
        extractUrlTestLogic(null, "")
        extractUrlTestLogic(" abc ", "abc")
    }
    private fun extractUrlTestLogic(input: String?, expectedResult: String) {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_COMPANY_URL_Handler(parent, tu))
        val msg = mock<Message>()
        `when`(msg.text).thenReturn(input)

        // Run method under test
        val actRes = sut.extractUrl(msg)

        // Verify
        assertThat(actRes).isEqualTo(expectedResult)
    }

    private fun urlCorrectTestLogic(input: String, expectedResult: Boolean) {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(WAITING_FOR_COMPANY_URL_Handler(parent, tu))

        // Run method under test
        val actRes = sut.urlCorrect(input)

        // Verify
        assertThat(actRes).isEqualTo(expectedResult)
    }
}