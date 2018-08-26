package cc.altruix.is1.telegram.forms

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

/**
 * Created by 1 on 09.04.2017.
 */
class InputFieldTests {
    @Test
    fun id() {
        // Prepare
        val id = "id"
        val sut = InputFieldForTesting(id)

        // Run method under test
        val actRes = sut.id()

        // Verify
        assertThat(actRes).isEqualTo(id)
    }
}