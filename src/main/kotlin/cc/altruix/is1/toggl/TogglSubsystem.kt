package cc.altruix.is1.toggl

import cc.altruix.is1.tpt.TotalProductiveTime
import cc.altruix.is1.validation.FailableOperationResult
import ch.simas.jtoggl.JToggl
import ch.simas.jtoggl.TimeEntry
import org.apache.commons.lang3.time.DateUtils
import java.util.*

/**
 * Created by pisarenko on 24.04.2017.
 */
open class TogglSubsystem : ITogglSubsystem {
    companion object {
        val ApiToken = "010de6be75f62f14a8523ab6775177eb"
        val WritingFiction = 8867446L
        val Editing = 36812892L
        val Marketing = 9385725L
        val ReadingNonFiction = 6216054L
        val ReadingFiction = 6198601L
        val SecondsInHour = 3600.0
        val SvWorldBuilding = 43634417L
        val ScreenWritingMarketing = 42417610L
    }
    var toggl:JToggl? = null

    override fun init() {
        toggl = createJToggl(ApiToken, "api_token")
    }

    open fun createJToggl(user: String, password: String) = JToggl(user, password)

    override fun close() {
    }
    override fun totalProductiveTime(day: Date): FailableOperationResult<TotalProductiveTime> {
        val tgl = toggl
        if (tgl == null) {
            return FailableOperationResult<TotalProductiveTime>(false, "Internal error", null)
        }
        val startTime = calculateStartOfDay(day)
        val endTime = calculateEndOfDay(day)
        val timeEntries = tgl.getTimeEntries(startTime, endTime)
        val total = calculateTotal(timeEntries)
        val writing = calculateWritingTime(timeEntries)
        val editing = calculateEditingTime(timeEntries)
        val marketing = calculateMarketingTime(timeEntries)
        val readingFiction = calculateReadingFictionTime(timeEntries)
        val readingNonFiction = calculateReadingNonFictionTime(timeEntries)

        val screenWritingMarketing = calculateScreenWritingMarketingTime(timeEntries)
        val svWorldBuilding = calculatesvWorldBuildingTime(timeEntries)

        return FailableOperationResult(true, "",
                TotalProductiveTime(startTime,
                        endTime,
                        total,
                        writing,
                        editing,
                        marketing,
                        readingFiction,
                        readingNonFiction,
                        screenWritingMarketing,
                        svWorldBuilding
                )
        )
    }

    private fun calculatesvWorldBuildingTime(timeEntries: List<TimeEntry>): Double
            = toHours(
                    timeEntries
                            .filter { isSvWorldBuildingEntry(it) }
                            .map { timeEntry -> timeEntry.duration }
                            .sum()
                            .toDouble()
            )

    private fun isSvWorldBuildingEntry(timeEntry: TimeEntry): Boolean
        = extractProjectId(timeEntry) == SvWorldBuilding

    private fun calculateScreenWritingMarketingTime(timeEntries: List<TimeEntry>): Double             = toHours(
            timeEntries
                    .filter { isScreenWritingMarketingEntry(it) }
                    .map { timeEntry -> timeEntry.duration }
                    .sum()
                    .toDouble()
    )

    private fun isScreenWritingMarketingEntry(timeEntry: TimeEntry): Boolean =
            extractProjectId(timeEntry) == ScreenWritingMarketing


    open fun toHours(seconds:Double):Double = seconds / SecondsInHour

    open fun calculateReadingNonFictionTime(timeEntries: List<TimeEntry>): Double =
            toHours(
                    timeEntries
                            .filter { isReadingNonFictionEntry(it) }
                            .map { timeEntry -> timeEntry.duration }
                            .sum()
                            .toDouble()
            )

    open fun calculateReadingFictionTime(timeEntries: List<TimeEntry>): Double =
        toHours(
                timeEntries
                        .filter { isReadingFictionEntry(it) }
                        .map { timeEntry -> timeEntry.duration }
                        .sum()
                        .toDouble()
        )

    open fun calculateMarketingTime(timeEntries: List<TimeEntry>) =
            toHours(
                    timeEntries
                            .filter { isMarketingEntry(it) }
                            .map { timeEntry -> timeEntry.duration }
                            .sum()
                            .toDouble()
            )

    open fun calculateEditingTime(timeEntries: List<TimeEntry>) =
        toHours(
                timeEntries
                        .filter { isEditingEntry(it) }
                        .map { timeEntry -> timeEntry.duration }
                        .sum()
                        .toDouble()
        )


    open fun calculateWritingTime(timeEntries: List<TimeEntry>) =
        toHours(
                timeEntries
                        .filter { isWritingEntry(it) }
                        .map { timeEntry -> timeEntry.duration }
                        .sum()
                        .toDouble()
        )


    open fun calculateTotal(timeEntries: List<TimeEntry>) =
        toHours(
                timeEntries
                        .map { timeEntry -> timeEntry.duration }
                        .sum()
                        .toDouble()
        )

    open fun isReadingNonFictionEntry(timeEntry: TimeEntry): Boolean =
            extractProjectId(timeEntry) == ReadingNonFiction

    open fun isReadingFictionEntry(timeEntry: TimeEntry): Boolean =
            extractProjectId(timeEntry) == ReadingFiction

    open fun isMarketingEntry(timeEntry:TimeEntry):Boolean = extractProjectId(timeEntry) == Marketing

    open fun isWritingEntry(timeEntry:TimeEntry):Boolean = extractProjectId(timeEntry) == WritingFiction

    open fun isEditingEntry(timeEntry:TimeEntry):Boolean = extractProjectId(timeEntry) == Editing

    open fun extractProjectId(timeEntry: TimeEntry):Long {
        if (timeEntry.pid == null) {
            return -1L
        }
        return timeEntry.pid
    }

    open fun calculateEndOfDay(day: Date): Date {
        var res = DateUtils.setHours(day, 23)
        res = DateUtils.setMinutes(res, 59)
        res = DateUtils.setSeconds(res, 59)
        return res
    }
    open fun calculateStartOfDay(day: Date): Date {
        var res = DateUtils.setHours(day, 0)
        res = DateUtils.setMinutes(res, 0)
        res = DateUtils.setSeconds(res, 0)
        return res
    }
}