package cc.altruix.is1.telegram.cmd.radar

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.cmd.radar.metrics.CafeVisits
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.jfree.data.category.DefaultCategoryDataset
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger

/**
 * Created by pisarenko on 16.05.2017.
 */
class RadarThreadTests {
    @Test
    @Ignore
    fun testImageCreation() {
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1001L
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = RadarThread(mongo, bot, chatId, tu, logger)
        sut.run()
    }
    @Test
    fun insertActualDataPercentPair() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1001L
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(RadarThread(mongo, bot, chatId, tu, logger))

        val actualPercent = mock<RadarChartData>()
        val metric = CafeVisits
        val dataSet = mock<DefaultCategoryDataset>()
        val actual = 102.0
        doReturn(actual).`when`(sut).capAt100Percent(actualPercent, metric)
        val target = 100.0

        // Run method under test
        sut.insertActualDataPercentPair(
                actualPercent,
                metric,
                dataSet
        )

        // Verify
        verify(sut).capAt100Percent(actualPercent, metric)
        verify(dataSet).addValue(actual, "Actual", metric.abbr)
        verify(dataSet).addValue(target, "Target", metric.abbr)
    }
    @Test
    fun capAt100Percent() {
        capAt100PercentTestLogic(0.0, 0.0)
        capAt100PercentTestLogic(50.0, 50.0)
        capAt100PercentTestLogic(99.0, 99.0)
        capAt100PercentTestLogic(100.0, 100.0)
        capAt100PercentTestLogic(100.1, 100.0)
    }

    private fun capAt100PercentTestLogic(orig: Double, expRes: Double) {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1001L
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(RadarThread(mongo, bot, chatId, tu, logger))

        val metric = CafeVisits
        val actualPercent = RadarChartData(
                mapOf(
                        metric to orig
                )
        )

        // Run method under test
        val actRes = sut.capAt100Percent(actualPercent, metric)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}