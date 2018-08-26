package cc.altruix.is1.telegram.forms

import org.fest.assertions.Assertions.assertThat
import org.junit.Test

/**
 * Created by 1 on 30.04.2017.
 */
class DoubleInputTests {
    @Test
    fun parseBlankText() {
        parseBlankTextTestLogic("")
        parseBlankTextTestLogic(" ")
        parseBlankTextTestLogic("   ")
    }
    @Test
    fun parseNonnumericInput() {
        parseNonnumericInputTestLogic("A")
        parseNonnumericInputTestLogic("10,40")

        parseNonnumericInputTestLogic("a10.40f")
        parseNonnumericInputTestLogic("1e30.40f")
        parseNonnumericInputTestLogic("- 10.40")
        parseNonnumericInputTestLogic("- 12.55")
        parseNonnumericInputTestLogic("-  1.55e-4")
        parseNonnumericInputTestLogic("  -  12.55")
        parseNonnumericInputTestLogic(" -    1.55e-4")
        parseNonnumericInputTestLogic("- 12.55 ")
        parseNonnumericInputTestLogic("-   1.55e-4  ")
    }
    @Test
    fun parseNegativeNumber() {
        parseNegativeNumberTestLogic("-12.55")
        parseNegativeNumberTestLogic("-1.55e-4")
        parseNegativeNumberTestLogic("  -12.55")
        parseNegativeNumberTestLogic(" -1.55e-4")
        parseNegativeNumberTestLogic("-12.55 ")
        parseNegativeNumberTestLogic("-1.55e-4  ")
    }

    private fun parseNegativeNumberTestLogic(input: String) {
        // Prepare
        val sut = DoubleInput(
                "4",
                "totalTime",
                "Total time for editing this scene?"
        )

        // Run method under test
        val actRes = sut.parse(input)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Negative number")
        assertThat(actRes.result).isNull()
    }

    @Test
    fun parseSunnyDay() {
        parseSunnyDayTestLogic("12.55", 12.55)
        parseSunnyDayTestLogic("1.55e-4", 1.55e-4)
        parseSunnyDayTestLogic("  12.55", 12.55)
        parseSunnyDayTestLogic(" 1.55e-4", 1.55e-4)
        parseSunnyDayTestLogic("12.55 ", 12.55)
        parseSunnyDayTestLogic("1.55e-4  ", 1.55e-4)
        parseSunnyDayTestLogic("10.40f", 10.40)
    }

    private fun parseSunnyDayTestLogic(input: String, expRes: Double) {
        // Prepare
        val sut = DoubleInput(
                "4",
                "totalTime",
                "Total time for editing this scene?"
        )

        // Run method under test
        val actRes = sut.parse(input)

        // Verify
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo(expRes)
    }

    private fun parseNonnumericInputTestLogic(input: String) {
        // Prepare
        val sut = DoubleInput(
                "4",
                "totalTime",
                "Total time for editing this scene?"
        )

        // Run method under test
        val actRes = sut.parse(input)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Wrong number format")
        assertThat(actRes.result).isNull()
    }

    private fun parseBlankTextTestLogic(text: String) {
        // Prepare
        val sut = DoubleInput("4", "totalTime", "Total time for editing this scene?")

        // Run method under test
        val actRes = sut.parse(text)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Blank text")
        assertThat(actRes.result).isNull()
    }
}