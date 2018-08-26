package cc.altruix.is1.telegram.cmd.radar.metrics

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.cmd.radar.AbstractRecordCountMetric
import cc.altruix.is1.telegram.cmd.radar.RadarChartMetric
import cc.altruix.is1.telegram.rawdata.EtbCmd
import java.time.LocalDate

/**
 * Created by pisarenko on 17.05.2017.
 */
object EveningTeethBrushings : AbstractRecordCountMetric(
        EtbCmd.MongoCollection,
        "ETB",
        "Teeth brooshings in the evening",
        "Pieces",
        90
) {

}