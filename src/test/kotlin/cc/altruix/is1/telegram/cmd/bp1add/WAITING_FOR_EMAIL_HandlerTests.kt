package cc.altruix.is1.telegram.cmd.bp1add

import org.junit.Test
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

/**
 * Created by pisarenko on 14.02.2017.
 */
class WAITING_FOR_EMAIL_HandlerTests {
    @Test
    fun saveData() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val sut = spy(WAITING_FOR_EMAIL_Handler(parent))
        val email = "email"

        // Run method under test
        sut.saveData(email)

        // Verify
        verify(parent).saveEmail(email)
    }
    @Test
    fun inputValid() {
        inputValidTestLogic("dp @ altruix.co", false)
        inputValidTestLogic("dp@altruix.co", true)
    }

    private fun inputValidTestLogic(input: String, expRes: Boolean) {
        // Prepare
        val sut = WAITING_FOR_EMAIL_Handler(mock<IParentBp1AddCmdAutomaton>())

        // Run method under test
        val actRes = sut.inputValid(input)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}