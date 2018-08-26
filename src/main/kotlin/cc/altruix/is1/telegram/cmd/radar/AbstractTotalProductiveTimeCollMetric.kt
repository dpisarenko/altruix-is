package cc.altruix.is1.telegram.cmd.radar

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.tpt.TotalProductiveTimeJob
import java.time.LocalDate

/**
 * Created by pisarenko on 25.05.2017.
 */
abstract class AbstractTotalProductiveTimeCollMetric(
        val column:String,
        abbr:String, // Acronym
        name:String, // Full name
        unit:String,
        sortOrder:Int) : RadarChartMetric(abbr, name, unit, sortOrder) {
    override fun calculate(
            mongo: IAltruixIs1MongoSubsystem,
            start: LocalDate,
            end: LocalDate
    ): Double {
        val res = mongo.totalProductiveTime(start, end, column)
        val time = res.result
        if (res.success && (time != null)) {
            return time
        }
        return 0.0
    }
}