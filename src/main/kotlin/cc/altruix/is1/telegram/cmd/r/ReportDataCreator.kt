package cc.altruix.is1.telegram.cmd.r

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.cmd.r.RCmd.Companion.MetricWordCount
import cc.altruix.is1.telegram.cmd.r.RCmd.Companion.UnitWords
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.utils.round4
import cc.altruix.utils.toDate
import java.time.LocalDate
import java.util.*

/**
 * Created by pisarenko on 13.04.2017.
 */
open class ReportDataCreator(
        val mongo: IAltruixIs1MongoSubsystem
) : IReportDataCreator {
    override fun createData(start: Date): FailableOperationResult<List<DailyMetricValues>> {
        val wctRes = createWordCountMetric(start)
        val wctData = wctRes.result
        if (!wctRes.success || (wctData == null)) {
            return FailableOperationResult(false, "Word count statistics couldn't be calculated", null)
        }
        return FailableOperationResult(true, "", listOf(wctData))
    }

    open fun createWordCountMetric(start: Date): FailableOperationResult<DailyMetricValues> {
        val itemsRes = mongo.readWritingStats()
        val items = itemsRes.result
        if (!itemsRes.success || (items == null)) {
            return FailableOperationResult(
                    false,
                    "Word count data couldn't be retrieved ('${itemsRes.error}').",
                    null
            )
        }
        return calculateWordCountDifferences(items, start)
    }

    open fun calculateWordCountDifferences(
            items: List<WritingStatRow>, start: Date):
            FailableOperationResult<DailyMetricValues> {
        if (items.isEmpty()) {
            return FailableOperationResult(true, "",
                    DailyMetricValues(
                            MetricWordCount,
                            UnitWords,
                            emptyList()
                    )
            )
        }
        val wordCountByWorkAndScene = createWordCountByWorkAndScene()
        val wordCountByDay = createWordCountByDay()
        val days:MutableList<LocalDate> = createDaysList()
        processWritingStatRows(items, wordCountByWorkAndScene, wordCountByDay, days)
        val max = calculateMax(wordCountByDay)
        val values = createDailyMetricValues(days, wordCountByDay, max)
        return FailableOperationResult(
                true,
                "",
                DailyMetricValues(
                        RCmd.MetricWordCount,
                        RCmd.UnitWords,
                        values
                )
        )
    }

    open fun createDailyMetricValues(
            days: List<LocalDate>,
            wordCountByDay: Map<LocalDate, Int>,
            max: Double
    ): List<DailyMetricValue> = days
            .map {
                createDailyMetricValue(wordCountByDay, it, max)
            }
            .toList()

    open fun processWritingStatRows(
            items: List<WritingStatRow>,
            wordCountByWorkAndScene: MutableMap<WorkPart, Int>,
            wordCountByDay: MutableMap<LocalDate, Int>,
            days: MutableList<LocalDate>
    ) {
        items.forEach { curItem ->
            processWritingStatRow(
                    curItem,
                    wordCountByWorkAndScene,
                    wordCountByDay,
                    days
            )
        }
    }

    open fun createDaysList(): MutableList<LocalDate> = ArrayList()

    open fun createWordCountByWorkAndScene():MutableMap<WorkPart, Int> =
            HashMap<WorkPart, Int>()

    open fun createWordCountByDay():MutableMap<LocalDate, Int> =
            HashMap<LocalDate, Int>()

    open fun processWritingStatRow(
            curItem: WritingStatRow,
            wordCountByWorkAndScene: MutableMap<WorkPart, Int>,
            wordCountByDay: MutableMap<LocalDate, Int>,
            days: MutableList<LocalDate>
    ) {
        val workPart = createWorkPart(curItem)
        val wordsWritten =
                calculateWordsWritten(curItem, wordCountByWorkAndScene, workPart)
        val day = extractDay(curItem.timestamp)
        updateDailyWordCount(day, wordCountByDay, wordsWritten)
        wordCountByWorkAndScene[workPart] = curItem.wordCount
        addIfNotContains(days, day)
    }

    open fun createWorkPart(curItem: WritingStatRow) =
            WorkPart(curItem.work, curItem.part)

    open fun calculateMax(wordCountByDay: Map<LocalDate, Int>) =
            wordCountByDay.values.max()?.toDouble() ?: 0.0

    open fun addIfNotContains(days: MutableList<LocalDate>, day: LocalDate) {
        if (!days.contains(day)) {
            days.add(day)
        }
    }

    open fun createDailyMetricValue(
            wordCountByDay: Map<LocalDate, Int>,
            day: LocalDate,
            max: Double
    ): DailyMetricValue {
        val wct = wordCountByDay[day]?.toDouble() ?: 0.0
        if (max == 0.0) {
            return DailyMetricValue(
                    toDate(day),
                    wct,
                    0.0
            )
        }
        return DailyMetricValue(
                toDate(day),
                wct,
                (wct / max).round4()
        )
    }

    open fun toDate(day: LocalDate) = day.toDate()

    open fun updateDailyWordCount(
            day: LocalDate,
            wordCountByDay: MutableMap<LocalDate, Int>,
            wordsWritten: Int
    ) {
        var oldDailyWordCount = wordCountByDay[day]
        if (oldDailyWordCount == null) {
            oldDailyWordCount = 0
        }
        val newDailyWordCount = oldDailyWordCount + wordsWritten
        wordCountByDay[day] = newDailyWordCount
    }

    open fun calculateWordsWritten(
            curItem: WritingStatRow,
            wordCountByWorkAndScene: MutableMap<WorkPart, Int>,
            workPart: WorkPart
    ): Int {
        var oldWordCount: Int? = wordCountByWorkAndScene[workPart]
        if (oldWordCount == null) {
            oldWordCount = 0
        }
        val newWordCount = curItem.wordCount
        val wordsWritten = newWordCount - oldWordCount
        return wordsWritten
    }

    open fun extractDay(timestamp: Date): LocalDate {
        val cal = Calendar.getInstance()
        cal.time = timestamp
        val day = cal.get(Calendar.DATE)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        return LocalDate.of(year, month, day)
    }
}