package cc.altruix.is1.telegram.forms

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

/**
 * Created by 1 on 09.04.2017.
 */
class TeleformElementStateTests {
    @Test
    fun ctor() {
        ctorTestLogic(true, false, false)
        ctorTestLogic(false, true, false)
        ctorTestLogic(false, false, true)
    }

    private fun ctorTestLogic(
            waiting: Boolean,
            initial: Boolean,
            terminal: Boolean
    ) {
        // Run method under test
        val sut = TeleformElementState(waiting, initial, terminal)
        // Verify
        assertThat(sut.waitingState()).isEqualTo(waiting)
        assertThat(sut.terminalState()).isEqualTo(terminal)
        assertThat(sut.initialState()).isEqualTo(initial)
    }
}