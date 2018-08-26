package cc.altruix.is1.telegram.forms

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

/**
 * Created by 1 on 09.04.2017.
 */
class IntegerInputTests {
    @Test
    fun parseRainyDay() {
        parseRainyDayTestLogic("", "Blank text")
        parseRainyDayTestLogic(" ", "Blank text")
        parseRainyDayTestLogic("-1.2a", "Text not numeric")
        parseRainyDayTestLogic("-1.2", "Text not numeric")
        parseRainyDayTestLogic("-12", "Text not numeric")
    }
    @Test
    fun parseSunnyDay() {
        parseSunnyDayTestLogic(" 15 ", 15)
        parseSunnyDayTestLogic(" 162 ", 162)
    }

    private fun parseSunnyDayTestLogic(input: String, expRes: Int) {
        // Prepare
        val sut = IntegerInput("1", "prop", "msg")

        // Run method under test
        val actRes = sut.parse(input)

        // Verify
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo(expRes)
    }

    private fun parseRainyDayTestLogic(input: String, expMsg: String) {
        // Prepare
        val sut = IntegerInput("1", "prop", "msg")

        // Run method under test
        val actRes = sut.parse(input)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(expMsg)
        assertThat(actRes.result).isNull()
    }
}
