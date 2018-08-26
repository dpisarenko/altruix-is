package cc.altruix.is1.mongo

import cc.altruix.is1.telegram.cmd.r.WritingStatRow
import cc.altruix.is1.telegram.cmd.radar.RadarChartData
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import java.time.LocalDate
import java.util.*

/**
 * Created by 1 on 08.05.2017.
 */
interface IAltruixIs1MongoSubsystem : IMongoSubsystem {
    fun readWritingStats(): FailableOperationResult<List<WritingStatRow>>
    fun distractionStats(): FailableOperationResult<List<Date>>
    fun totalProductiveTime(start: LocalDate, end: LocalDate, col: String): FailableOperationResult<Double>
    fun recordsCount(collName: String,
                          start: LocalDate,
                          end: LocalDate) : FailableOperationResult<Int>
    fun saveRadarData(
            now: LocalDate,
            targetsAbs: RadarChartData,
            actualAbs: RadarChartData):ValidationResult
}