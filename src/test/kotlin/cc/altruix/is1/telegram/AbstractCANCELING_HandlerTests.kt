package cc.altruix.is1.telegram

import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmdState
import cc.altruix.is1.telegram.cmd.bp1add.CANCELING_Handler
import cc.altruix.is1.telegram.cmd.bp1add.IParentBp1AddCmdAutomaton
import cc.altruix.is1.telegram.cmd.bp2cb.Bp2CbCmdState
import cc.altruix.is1.telegram.cmd.bp2cb.IParentBp2CbCmdAutomaton
import cc.altruix.is1.telegram.forms.TeleformElementState
import cc.altruix.is1.telegram.forms.TeleformCancellingHandler
import cc.altruix.mock
import org.junit.Test
import org.mockito.Mockito

/**
 * Created by pisarenko on 03.03.2017.
 */
class AbstractCANCELING_HandlerTests {
    @Test
    fun fire1() {
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        // Prepare
        val sut = Mockito.spy(CANCELING_Handler(parent))
        val inOrder = Mockito.inOrder(parent, tu, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).unsubscribe()
        inOrder.verify(sut).printMessage(ITelegramUtils.CanceledMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp1AddCmdState.END)
    }
    @Test
    fun fire2() {
        val parent = mock<IParentBp2CbCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        // Prepare
        val sut = Mockito.spy(cc.altruix.is1.telegram.cmd.bp2cb.CANCELING_Handler(parent))
        val inOrder = Mockito.inOrder(parent, tu, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).unsubscribe()
        inOrder.verify(sut).printMessage(ITelegramUtils.CanceledMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp2CbCmdState.END)
    }
    @Test
    fun fire3() {
        val parent = mock<IParentAutomaton<TeleformElementState>>()
        val tu = mock<ITelegramUtils>()
        val endState = mock<TeleformElementState>()
        // Prepare
        val sut = Mockito.spy(TeleformCancellingHandler(
                endState,
                parent))
        val inOrder = Mockito.inOrder(parent, tu, sut, endState)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).unsubscribe()
        inOrder.verify(sut).printMessage(ITelegramUtils.CanceledMessage)
        inOrder.verify(parent).goToStateIfPossible(endState)
    }
}