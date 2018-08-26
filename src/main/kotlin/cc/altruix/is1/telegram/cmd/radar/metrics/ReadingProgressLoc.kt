package cc.altruix.is1.telegram.cmd.radar.metrics

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.cmd.radar.RadarChartMetric
import java.time.LocalDate

/**
 * Created by pisarenko on 16.05.2017.
 */
object ReadingProgressLoc : RadarChartMetric(
        "RPL",
        "Reading progress",
        "Kindle locations",
        50
) {
    override fun calculate(mongo: IAltruixIs1MongoSubsystem, start: LocalDate, end: LocalDate): Double {
        // TODO: Implement
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}