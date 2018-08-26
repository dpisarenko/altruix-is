package cc.altruix.is1.telegram.cmd.r

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import cc.altruix.utils.round4
import cc.altruix.utils.toDate
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.verification.VerificationMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


/**
 * Created by pisarenko on 13.04.2017.
 */
class ReportDataCreatorTests {
    @Test
    fun createWordCountMetric() {
        val t0 = LocalDateTime.of(2017, 4, 13, 17, 49, 0)
        val t1 = t0.plusMinutes(1)
        val t2 = t1.plusMinutes(1)
        val t3 = t1.plusMinutes(1)
        val t4 = t1.plusMinutes(1)
        val t5 = LocalDateTime.of(2017, 4, 14, 17, 49, 0)

        val rawData = listOf<WritingStatRow>(
                WritingStatRow(t0.toDate(), "SS-1", "S1", 1200),
                WritingStatRow(t1.toDate(), "SS-2", "S1", 0),
                WritingStatRow(t2.toDate(), "SS-1", "S2", 1300),
                WritingStatRow(t3.toDate(), "SS-1", "S1", 1500),
                WritingStatRow(t4.toDate(), "SS-3", "S1", 20),
                WritingStatRow(t5.toDate(), "SS-3", "S1", 130)
        )
        val day1 = LocalDateTime.of(2017, 4, 13, 0, 0, 0)
        val day2 = LocalDateTime.of(2017, 4, 14, 0, 0, 0)
        val day1WordCount = 1200 + 1300 + (1500 - 1200) + 20
        val expRes = DailyMetricValues(
                        RCmd.MetricWordCount,
                        RCmd.UnitWords,
                       listOf(
                               DailyMetricValue(
                                       day1.toDate(),
                                       day1WordCount.toDouble(),
                                       1.0
                               ),
                               DailyMetricValue(
                                       day2.toDate(),
                                       (130 - 20).toDouble(),
                                       (110.0 / day1WordCount.toDouble()).round4()
                               )
                       )
                )
        createWordCountMetricTestLogic(expRes, rawData)
    }
    @Test
    fun calculateWordCountDifferencesEmptyInput() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val start = startDate()

        // Run method under test
        val actRes = sut.calculateWordCountDifferences(
                emptyList(),
                start
        )

        // Verify
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo(
                DailyMetricValues(
                        RCmd.MetricWordCount,
                        RCmd.UnitWords,
                        emptyList()
                )
        )
    }
    @Test
    fun createDataWctFailure() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val msg = "msg"
        val wctRes = FailableOperationResult<DailyMetricValues>(false, msg, null)
        val start = startDate()
        doReturn(wctRes).`when`(sut).createWordCountMetric(start)

        // Run method under test
        val actRes = sut.createData(start)

        // Verify
        verify(sut).createWordCountMetric(start)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Word count statistics couldn't be calculated")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createDataSunnydDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val res = DailyMetricValues(RCmd.MetricWordCount, RCmd.UnitWords, emptyList())
        val wctRes = FailableOperationResult<DailyMetricValues>(true, "", res)
        val start = startDate()
        doReturn(wctRes).`when`(sut).createWordCountMetric(start)

        // Run method under test
        val actRes = sut.createData(start)

        // Verify
        verify(sut).createWordCountMetric(start)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo(listOf(res))
    }
    @Test
    fun extractDay() {
        extractDayTestLogic(2017, 1, 1)
        extractDayTestLogic(2017, 1, 31)
        extractDayTestLogic(2017, 2, 1)
        extractDayTestLogic(2017, 2, 28)
        extractDayTestLogic(2017, 3, 1)
        extractDayTestLogic(2017, 3, 31)
        extractDayTestLogic(2017, 4, 1)
        extractDayTestLogic(2017, 4, 30)
        extractDayTestLogic(2017, 5, 1)
        extractDayTestLogic(2017, 5, 31)
        extractDayTestLogic(2017, 6, 1)
        extractDayTestLogic(2017, 6, 30)
        extractDayTestLogic(2017, 7, 1)
        extractDayTestLogic(2017, 7, 31)
        extractDayTestLogic(2017, 8, 1)
        extractDayTestLogic(2017, 8, 30)
        extractDayTestLogic(2017, 9, 1)
        extractDayTestLogic(2017, 9, 30)
        extractDayTestLogic(2017, 10, 1)
        extractDayTestLogic(2017, 10, 30)
        extractDayTestLogic(2017, 11, 1)
        extractDayTestLogic(2017, 11, 30)
        extractDayTestLogic(2017, 12, 1)
        extractDayTestLogic(2017, 12, 31)
        extractDayTestLogic(2017, 4, 14)
    }
    @Test
    fun createWordCountMetricMongoFault() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val start = startDate()
        val msg = "msg"
        val itemsRes = FailableOperationResult<List<WritingStatRow>>(false, msg, null)
        `when`(mongo.readWritingStats()).thenReturn(itemsRes)

        // Run method under test
        val actRes = sut.createWordCountMetric(start)

        // Verify
        verify(mongo).readWritingStats()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Word count data couldn't be retrieved ('$msg').")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createWordCountMetricSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val start = startDate()
        val itemsStuff = emptyList<WritingStatRow>()
        val itemsRes = FailableOperationResult(true, "", itemsStuff)
        `when`(mongo.readWritingStats()).thenReturn(itemsRes)
        val diffs = DailyMetricValues(RCmd.MetricWordCount, RCmd.UnitWords, emptyList())
        val diffRes = FailableOperationResult(true, "", diffs)
        doReturn(diffRes).`when`(sut).calculateWordCountDifferences(itemsStuff, start)

        // Run method under test
        val actRes = sut.createWordCountMetric(start)

        // Verify
        verify(mongo).readWritingStats()
        verify(sut).calculateWordCountDifferences(itemsStuff, start)
        assertThat(actRes).isSameAs(diffRes)
    }
    @Test
    fun calculateWordCountDifferencesSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val items = mock<List<WritingStatRow>>()
        val start = mock<Date>()

        `when`(items.isEmpty()).thenReturn(false)
        val wordCountByWorkAndScene = mock<MutableMap<WorkPart,Int>>()
        val wordCountByDay = mock<MutableMap<LocalDate,Int>>()
        doReturn(wordCountByDay).`when`(sut).createWordCountByDay()
        val days = mock<MutableList<LocalDate>>()
        doReturn(days).`when`(sut).createDaysList()
        doNothing().`when`(sut).processWritingStatRows(
                items, wordCountByWorkAndScene, wordCountByDay, days
        )
        val max = 31.331
        doReturn(max).`when`(sut).calculateMax(wordCountByDay)
        val values = mock<List<DailyMetricValue>>()
        doReturn(values).`when`(sut).createDailyMetricValues(days,
                wordCountByDay, max)

        doReturn(wordCountByWorkAndScene).`when`(sut).createWordCountByWorkAndScene()

        val inOrder = inOrder(mongo, sut, items, start)

        // Run method under test
        val actRes = sut.calculateWordCountDifferences(items, start)

        // Verify
        inOrder.verify(items).isEmpty()
        inOrder.verify(sut).createWordCountByWorkAndScene()
        inOrder.verify(sut).createWordCountByDay()
        inOrder.verify(sut).createDaysList()
        inOrder.verify(sut).processWritingStatRows(items, wordCountByWorkAndScene,
                wordCountByDay, days)
        inOrder.verify(sut).calculateMax(wordCountByDay)
        inOrder.verify(sut).createDailyMetricValues(days, wordCountByDay, max)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNotNull
        assertThat(actRes.result?.metric).isEqualTo(RCmd.MetricWordCount)
        assertThat(actRes.result?.unit).isEqualTo(RCmd.UnitWords)
        assertThat(actRes.result?.dailyValues).isSameAs(values)
    }
    @Test
    fun addIfNotContains() {
        addIfNotContainsTestLogic(false, times(1))
        addIfNotContainsTestLogic(true, never())
    }
    @Test
    fun calculateMaxNullMax() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val wordCountByDay = mock<Map<LocalDate, Int>>()
        val values:Collection<Int> = emptyList()
        `when`(wordCountByDay.values).thenReturn(values)

        // Run method under test
        val actRes = sut.calculateMax(wordCountByDay)

        // Verify
        verify(wordCountByDay).values
        assertThat(actRes).isEqualTo(0.0)
    }
    @Test
    fun calculateMaxSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val wordCountByDay = mock<Map<LocalDate, Int>>()
        val values = listOf(1, 2, 3)
        `when`(wordCountByDay.values).thenReturn(values)

        // Run method under test
        val actRes = sut.calculateMax(wordCountByDay)

        // Verify
        verify(wordCountByDay).values
        assertThat(actRes).isEqualTo(3.0)
    }

    @Test
    fun createDailyMetricValues() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))

        val date1 = LocalDate.now()
        val date2 = date1.plusDays(1)

        val days = listOf(date1, date2)
        val wordCountByDay = mock<Map<LocalDate, Int>>()
        val max = 31.331

        val dmv1 = DailyMetricValue(mock<Date>(), 1.0, 1.0)
        val dmv2 = DailyMetricValue(mock<Date>(), 0.5, 0.5)
        doReturn(dmv1).`when`(sut).createDailyMetricValue(
                wordCountByDay,
                date1,
                max
        )
        doReturn(dmv2).`when`(sut).createDailyMetricValue(
                wordCountByDay,
                date2,
                max
        )

        // Run method under test
        val actRes = sut.createDailyMetricValues(days, wordCountByDay, max)

        // Verify
        verify(sut).createDailyMetricValue(wordCountByDay, date1, max)
        verify(sut).createDailyMetricValue(wordCountByDay, date1, max)
        assertThat(actRes.size).isEqualTo(2)
        assertThat(actRes[0]).isSameAs(dmv1)
        assertThat(actRes[1]).isSameAs(dmv2)
    }
    @Test
    fun processWritingStatRows() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))

        val wsr1 = WritingStatRow(mock<Date>(), "SS-1", "S-2", 10)
        val wsr2 = WritingStatRow(mock<Date>(), "SS-1", "S-3", 20)

        val items = listOf<WritingStatRow>(wsr1, wsr2)
        val wordCountByWorkAndScene = mock<MutableMap<WorkPart, Int>>()
        val wordCountByDay = mock<MutableMap<LocalDate, Int>>()
        val days = mock<MutableList<LocalDate>>()

        doNothing().`when`(sut).processWritingStatRow(
                wsr1,
                wordCountByWorkAndScene,
                wordCountByDay,
                days
        )
        doNothing().`when`(sut).processWritingStatRow(
                wsr2,
                wordCountByWorkAndScene,
                wordCountByDay,
                days
        )

        // Run method under test
        sut.processWritingStatRows(items, wordCountByWorkAndScene,
                wordCountByDay, days)

        // Verify
        verify(sut).processWritingStatRow(
                wsr1,
                wordCountByWorkAndScene,
                wordCountByDay,
                days
        )
        verify(sut).processWritingStatRow(
                wsr2,
                wordCountByWorkAndScene,
                wordCountByDay,
                days
        )
    }
    @Test
    fun processWritingStatRow() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val timestamp = mock<Date>()
        val wordCount = 10
        val curItem = WritingStatRow(timestamp, "SS-1", "S-2", wordCount)
        val wordCountByWorkAndScene = mock<MutableMap<WorkPart, Int>>()
        val wordCountByDay = mock<MutableMap<LocalDate, Int>>()
        val days = mock<MutableList<LocalDate>>()
        val workPart = WorkPart("SS-1", "S-2")
        doReturn(workPart).`when`(sut).createWorkPart(curItem)
        val wordsWritten = 2100
        doReturn(wordsWritten).`when`(sut).calculateWordsWritten(
                curItem,
                wordCountByWorkAndScene,
                workPart
        )
        val day = LocalDate.now()
        doReturn(day).`when`(sut).extractDay(timestamp)
        doNothing().`when`(sut).updateDailyWordCount(
                day,
                wordCountByDay,
                wordsWritten
        )
        doNothing().`when`(sut).addIfNotContains(days, day)

        val inOrder = inOrder(mongo, sut, wordCountByWorkAndScene,
                wordCountByDay, days)

        // Run method under test
        sut.processWritingStatRow(
                curItem,
                wordCountByWorkAndScene,
                wordCountByDay,
                days
        )

        // Verify
        inOrder.verify(sut).createWorkPart(curItem)
        inOrder.verify(sut).calculateWordsWritten(curItem,
                wordCountByWorkAndScene, workPart)
        inOrder.verify(sut).extractDay(timestamp)
        inOrder.verify(sut).updateDailyWordCount(day, wordCountByDay, wordsWritten)
        inOrder.verify(wordCountByWorkAndScene).put(workPart, wordCount)
        inOrder.verify(sut).addIfNotContains(days, day)
    }
    @Test
    fun createWorkPart() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val work = "SS-1"
        val part = "S-2"
        val curItem = WritingStatRow(mock<Date>(), work, part, 1200)

        // Run method under test
        val actRes = sut.createWorkPart(curItem)

        // Verify
        assertThat(actRes.work).isEqualTo(work)
        assertThat(actRes.part).isEqualTo(part)
    }
    @Test
    fun createDailyMetricValueNullDayData() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))

        val wordCountByDay = mock<Map<LocalDate, Int>>()
        val day = LocalDate.now()
        val max = 31.331
        `when`(wordCountByDay.get(day)).thenReturn(null)
        val date = mock<Date>()
        doReturn(date).`when`(sut).toDate(day)

        // Run method under test
        val actRes = sut.createDailyMetricValue(wordCountByDay, day, max)

        // Verify
        verify(wordCountByDay).get(day)
        verify(sut).toDate(day)
        assertThat(actRes.day).isSameAs(date)
        assertThat(actRes.abs).isEqualTo(0.0)
        assertThat(actRes.percent).isEqualTo((0.0 / max).round4())
    }
    @Test
    fun createDailyMetricValueSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))

        val wordCountByDay = mock<Map<LocalDate, Int>>()
        val day = LocalDate.now()
        val max = 31.331
        val wct = 10
        `when`(wordCountByDay.get(day)).thenReturn(wct)
        val date = mock<Date>()
        doReturn(date).`when`(sut).toDate(day)

        // Run method under test
        val actRes = sut.createDailyMetricValue(wordCountByDay, day, max)

        // Verify
        verify(wordCountByDay).get(day)
        verify(sut).toDate(day)
        assertThat(actRes.day).isSameAs(date)
        assertThat(actRes.abs).isEqualTo(wct.toDouble())
        assertThat(actRes.percent).isEqualTo((wct.toDouble() / max).round4())
    }
    @Test
    fun createDailyMetricValueZeroMax() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))

        val wordCountByDay = mock<Map<LocalDate, Int>>()
        val day = LocalDate.now()
        val max = 0.0
        val wct = 10
        `when`(wordCountByDay.get(day)).thenReturn(wct)
        val date = mock<Date>()
        doReturn(date).`when`(sut).toDate(day)

        // Run method under test
        val actRes = sut.createDailyMetricValue(wordCountByDay, day, max)

        // Verify
        verify(wordCountByDay).get(day)
        verify(sut).toDate(day)
        assertThat(actRes.day).isSameAs(date)
        assertThat(actRes.abs).isEqualTo(wct.toDouble())
        assertThat(actRes.percent).isEqualTo(0.0)
    }

    private fun addIfNotContainsTestLogic(
            contains: Boolean,
            addCount: VerificationMode
    ) {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))
        val days = mock<MutableList<LocalDate>>()
        val day = LocalDate.now()
        `when`(days.contains(day)).thenReturn(contains)

        // Run method under test
        sut.addIfNotContains(days, day)

        // Verify
        verify(days).contains(day)
        verify(days, addCount).add(day)
    }

    private fun extractDayTestLogic(year: Int, month: Int, day: Int) {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = ReportDataCreator(mongo)
        val hour = 0
        val min = 0
        val sec = 0
        val x = LocalDateTime.of(year, month, day, hour, min, sec)

        // Run method under test
        val actRes = sut.extractDay(x.toDate())

        // Verify
        assertThat(actRes.dayOfMonth).isEqualTo(day)
        assertThat(actRes.monthValue).isEqualTo(month)
        assertThat(actRes.year).isEqualTo(year)
    }

    private fun createWordCountMetricTestLogic(
            expRes: DailyMetricValues,
            rawData: List<WritingStatRow>
    ) {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(ReportDataCreator(mongo))

        val itemsRes = FailableOperationResult<List<WritingStatRow>>(true, "", rawData)
        `when`(mongo.readWritingStats()).thenReturn(itemsRes)
        val start = startDate()
        // Run method under test
        val actRes = sut.createWordCountMetric(start)

        // Verify
        verify(mongo).readWritingStats()
        verify(sut).calculateWordCountDifferences(rawData, start)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNotNull
        assertThat(actRes.result).isEqualTo(expRes)
    }

    private fun startDate(): Date {
        return ReportThread(
                mock<IAltruixIs1MongoSubsystem>(),
                mock<IResponsiveBot>(),
                0L,
                mock<ITelegramUtils>(),
                mock<IReportDataCreator>()
        ).createStartDate()
    }
}