package cc.altruix.is1.tpt

import java.util.Date
/**
 * Created by pisarenko on 24.04.2017.
 */
data class TotalProductiveTime(
        val startTime:Date,
        val endTime:Date,
        val total:Double,
        val writing:Double,
        val editing:Double,
        val marketing:Double,
        val readingFiction:Double,
        val readingNonFiction:Double,
        val screenWritingMarketing:Double,
        val svWorldBuilding:Double)