package cc.altruix.is1.telegram.cmd.r

import cc.altruix.is1.validation.FailableOperationResult
import java.util.*

/**
 * Created by pisarenko on 13.04.2017.
 */
interface IReportDataCreator {
    fun createData(start:Date):FailableOperationResult<List<DailyMetricValues>>
}