package cc.altruix.is1.telegram.cmd.radar

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.validation.FailableOperationResult
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters


/**
 * Created by pisarenko on 17.05.2017.
 */
open class RadarDataCreator(val mongo:IAltruixIs1MongoSubsystem) {
    val radarData = HashMap<RadarChartMetric,Double>()
    fun calculateRadarData(now:LocalDate):FailableOperationResult<RadarChartData> {
        val metrics = RadarThread.TargetsAbs.amountsByMetric.keys
        if (metricsIncorrect(metrics)) {
            return FailableOperationResult(false, "Metric IDs not unique, internal error", null)
        }
        val (monday, sunday) = sundayMonday(now)
        metrics
                .map { Pair(it, it.calculate(mongo, monday, sunday)) }
                .map { (metric, value) ->
                    radarData.put(metric, value)
                }
        return FailableOperationResult<RadarChartData>(true, "", RadarChartData(radarData))
    }

    open fun sundayMonday(now:LocalDate):Pair<LocalDate, LocalDate> {
        val monday = monday(now)
        val sunday = monday.plusDays(6)
        return Pair(monday, sunday)
    }

    open fun monday(now: LocalDate):LocalDate {
        if (now.dayOfWeek == DayOfWeek.MONDAY) {
            return now
        }
        return now.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
    }

    open fun metricsIncorrect(metrics: Set<RadarChartMetric>): Boolean {
        val uniqueAbbrs = metrics.map { it.abbr }.distinct()
        if (uniqueAbbrs.size != metrics.size) {
            return false
        }
        val metricsWithZeroTargetValue = metrics
                .map { RadarThread.TargetsAbs.amountsByMetric[it] }
                .filter { it != null }
                .filter { it == 0.0 }
                .count()
        return (metricsWithZeroTargetValue != 0)
    }
}