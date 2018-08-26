package cc.altruix.is1.telegram.cmd.bp1add

import org.junit.Test
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

/**
 * Created by pisarenko on 14.02.2017.
 */
class WAITING_FOR_CONTACT_FORM_URL_HandlerTests {
    @Test
    fun saveData() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val sut = spy(WAITING_FOR_CONTACT_FORM_URL_Handler(parent))
        val url = "url"

        // Run method under test
        sut.saveData(url)

        // Verify
        verify(parent).saveContactFormUrl(url)
    }
    @Test
    fun inputValid() {
        inputValidTestLogic("http://altruix.cc", true)
        inputValidTestLogic("http://altru ix.cc", false)
        inputValidTestLogic(null, false)
    }

    private fun inputValidTestLogic(input: String?, expRes: Boolean) {
        // Prepare
        val sut = spy(WAITING_FOR_CONTACT_FORM_URL_Handler(mock<IParentBp1AddCmdAutomaton>()))

        // Run method under test
        val actRes = sut.inputValid(input)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}