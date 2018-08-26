package cc.altruix.is1.telegram.cmd.radar

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import java.time.LocalDate

/**
 * Created by pisarenko on 17.05.2017.
 */
abstract class AbstractRecordCountMetric(
        val coll:String,
        abbr:String, // Acronym
        name:String, // Full name
        unit:String,
        sortOrder:Int) : RadarChartMetric(abbr, name, unit, sortOrder) {

    override fun calculate(
            mongo: IAltruixIs1MongoSubsystem,
            start: LocalDate,
            end: LocalDate
    ): Double {
        val res = mongo.recordsCount(coll, start, end)
        val rc = res.result
        if (res.success && (rc != null)) {
            return rc.toDouble()
        }
        return 0.0
    }
}