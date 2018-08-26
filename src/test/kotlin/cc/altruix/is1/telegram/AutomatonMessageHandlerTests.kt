package cc.altruix.is1.telegram

import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmdState
import cc.altruix.is1.telegram.cmd.bp1add.IParentBp1AddCmdAutomaton
import org.junit.Test
import cc.altruix.mock
import org.mockito.Mockito.verify

/**
 * Created by pisarenko on 15.02.2017.
 */
class AutomatonMessageHandlerTests {
    @Test
    fun printMessage() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val sut = AutomatonMessageHandler<Bp1AddCmdState>(parent)
        val msg = "msg"

        // Run method under test
        sut.printMessage(msg)

        // Verify
        verify(parent).printMessage(msg)
    }
}