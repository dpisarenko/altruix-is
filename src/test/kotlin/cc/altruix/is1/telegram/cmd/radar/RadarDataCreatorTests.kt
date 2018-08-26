package cc.altruix.is1.telegram.cmd.radar

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * Created by pisarenko on 22.05.2017.
 */
class RadarDataCreatorTests {
    @Test
    fun mondaySunday() {
        mondaySundayTestLogic(LocalDate.of(2017, 5, 22),
                LocalDate.of(2017, 5, 22),
                LocalDate.of(2017, 5, 28))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 23),
                LocalDate.of(2017, 5, 22),
                LocalDate.of(2017, 5, 28))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 24),
                LocalDate.of(2017, 5, 22),
                LocalDate.of(2017, 5, 28))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 25),
                LocalDate.of(2017, 5, 22),
                LocalDate.of(2017, 5, 28))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 26),
                LocalDate.of(2017, 5, 22),
                LocalDate.of(2017, 5, 28))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 27),
                LocalDate.of(2017, 5, 22),
                LocalDate.of(2017, 5, 28))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 28),
                LocalDate.of(2017, 5, 22),
                LocalDate.of(2017, 5, 28))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 29),
                LocalDate.of(2017, 5, 29),
                LocalDate.of(2017, 6, 4))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 30),
                LocalDate.of(2017, 5, 29),
                LocalDate.of(2017, 6, 4))
        mondaySundayTestLogic(LocalDate.of(2017, 5, 31),
                LocalDate.of(2017, 5, 29),
                LocalDate.of(2017, 6, 4))
        mondaySundayTestLogic(LocalDate.of(2017, 6, 1),
                LocalDate.of(2017, 5, 29),
                LocalDate.of(2017, 6, 4))
        mondaySundayTestLogic(LocalDate.of(2017, 6, 2),
                LocalDate.of(2017, 5, 29),
                LocalDate.of(2017, 6, 4))
        mondaySundayTestLogic(LocalDate.of(2017, 6, 3),
                LocalDate.of(2017, 5, 29),
                LocalDate.of(2017, 6, 4))
        mondaySundayTestLogic(LocalDate.of(2017, 6, 4),
                LocalDate.of(2017, 5, 29),
                LocalDate.of(2017, 6, 4))
    }

    private fun mondaySundayTestLogic(now: LocalDate, monday: LocalDate, sunday: LocalDate) {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = RadarDataCreator(mongo)

        // Run method under test
        val (mondayAct, sundayAct) = sut.sundayMonday(now)

        // Verify
        assertThat(mondayAct).isEqualTo(monday)
        assertThat(sundayAct).isEqualTo(sunday)
    }
}