package cc.altruix.is1.telegram.cmd.radar

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import java.time.LocalDate

/**
 * Created by pisarenko on 16.05.2017.
 */
abstract class RadarChartMetric(
        val abbr:String, // Acronym
        val name:String, // Full name
        val unit:String,
        val sortOrder:Int
) {
    abstract fun calculate(
            mongo: IAltruixIs1MongoSubsystem,
            start: LocalDate,
            end: LocalDate
    ):Double
}