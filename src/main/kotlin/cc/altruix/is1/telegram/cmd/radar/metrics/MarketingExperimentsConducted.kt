package cc.altruix.is1.telegram.cmd.radar.metrics

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.cmd.radar.AbstractRecordCountMetric
import cc.altruix.is1.telegram.cmd.radar.RadarChartMetric
import cc.altruix.is1.telegram.rawdata.MxCmd
import java.time.LocalDate

/**
 * Created by pisarenko on 17.05.2017.
 */
object MarketingExperimentsConducted : AbstractRecordCountMetric(
        MxCmd.MongoCollection,
        "MX",
        "Marketing experiments conducted",
        "Pieces",
        20
) {
}