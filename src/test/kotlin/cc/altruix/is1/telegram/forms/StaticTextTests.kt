package cc.altruix.is1.telegram.forms

import org.fest.assertions.Assertions
import org.junit.Test

/**
 * Created by 1 on 09.04.2017.
 */
class StaticTextTests {
    @Test
    fun id() {
        // Prepare
        val id = "id"
        val sut = StaticText(id, "")

        // Run method under test
        val actRes = sut.id()

        // Verify
        Assertions.assertThat(actRes).isEqualTo(id)
    }
}