package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.ITelegramUtils
import org.junit.Test
import cc.altruix.mock
import org.mockito.Mockito.*

/**
 * Created by pisarenko on 15.02.2017.
 */
class CANCELING_HandlerTests {
    @Test
    fun fire() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(CANCELING_Handler(parent))
        val inOrder = inOrder(parent, tu, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).unsubscribe()
        inOrder.verify(sut).printMessage(ITelegramUtils.CanceledMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp1AddCmdState.END)
    }
}