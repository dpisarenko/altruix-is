package cc.altruix.is1.telegram.cmd.radar.metrics

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.cmd.radar.AbstractRecordCountMetric
import cc.altruix.is1.telegram.cmd.radar.RadarChartMetric
import cc.altruix.is1.telegram.rawdata.MovCmd
import java.time.LocalDate

/**
 * Created by pisarenko on 16.05.2017.
 */
object MoviesWatched : AbstractRecordCountMetric(
        MovCmd.MongoCollection,
        "MOV",
        "Movies watched",
        "Pieces",
        80
) {

}