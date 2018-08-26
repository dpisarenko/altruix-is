package cc.altruix.is1.telegram.cmd.radar.metrics

import cc.altruix.is1.telegram.cmd.radar.AbstractRecordCountMetric
import cc.altruix.is1.telegram.rawdata.SsrCmd

/**
 * Created by pisarenko on 19.05.2017.
 */
object ShortStoriesRead: AbstractRecordCountMetric(
        SsrCmd.MongoCollection,
        "SSR",
        "Short stories read",
        "Pieces",
        45
) {

}