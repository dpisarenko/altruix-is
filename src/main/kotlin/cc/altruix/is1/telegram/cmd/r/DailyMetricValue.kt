package cc.altruix.is1.telegram.cmd.r

import java.util.*

/**
 * Created by pisarenko on 13.04.2017.
 */
data class DailyMetricValue(
        val day: Date,
        val abs:Double,
        val percent:Double
)