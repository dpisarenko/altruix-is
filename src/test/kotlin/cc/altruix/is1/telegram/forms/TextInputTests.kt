package cc.altruix.is1.telegram.forms

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

/**
 * Created by 1 on 09.04.2017.
 */
class TextInputTests {
    @Test
    fun parseBlankInput() {
        parseBlankInputTestLogic("")
        parseBlankInputTestLogic(" ")
        parseBlankInputTestLogic("  ")
    }
    @Test
    fun parseSunnyDay() {
        // Prepare
        val sut = TextInput("1", "target", "msg")

        // Run method under test
        val actRes = sut.parse("test  ")

        // Verify
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo("test")
    }

    private fun parseBlankInputTestLogic(input: String) {
        // Prepare
        val sut = TextInput("1", "target", "msg")

        // Run method under test
        val actRes = sut.parse(input)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Blank text")
        assertThat(actRes.result).isNull()
    }
}