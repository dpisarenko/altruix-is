package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.mock
import org.junit.Test
import org.mockito.Mockito

/**
 * Created by pisarenko on 15.03.2017.
 */
class CANCELING_HandlerTests {
    @Test
    fun fire() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = Mockito.spy(CANCELING_Handler(parent))
        val inOrder = Mockito.inOrder(parent, tu, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).unsubscribe()
        inOrder.verify(sut).printMessage(ITelegramUtils.CanceledMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.END)
    }
}