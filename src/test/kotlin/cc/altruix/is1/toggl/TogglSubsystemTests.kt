package cc.altruix.is1.toggl

import cc.altruix.mock
import ch.simas.jtoggl.JToggl
import ch.simas.jtoggl.TimeEntry
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * Created by pisarenko on 25.04.2017.
 */
class TogglSubsystemTests {
    @Test
    fun init() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val toggl = mock<JToggl>()
        doReturn(toggl).`when`(sut).createJToggl(
                TogglSubsystem.ApiToken,
                "api_token"
        )

        // Run method under test
        sut.init()

        // Verify
        verify(sut).createJToggl(TogglSubsystem.ApiToken, "api_token")
        assertThat(sut.toggl).isSameAs(toggl)
    }
    @Test
    fun totalProductiveTimeNotInitialized() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val now = Date()

        // Run method under test
        val actRes = sut.totalProductiveTime(now)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun totalProductiveTimeSunnyDay() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val day = mock<Date>()
        val startTime = mock<Date>()
        doReturn(startTime).`when`(sut).calculateStartOfDay(day)
        val endTime = mock<Date>()
        doReturn(endTime).`when`(sut).calculateEndOfDay(day)
        val tgl = mock<JToggl>()
        doReturn(tgl).`when`(sut).createJToggl(TogglSubsystem.ApiToken, "api_token")
        val timeEntries:List<TimeEntry> = emptyList()
        `when`(tgl.getTimeEntries(startTime, endTime)).thenReturn(timeEntries)
        val total = 1.0
        doReturn(total).`when`(sut).calculateTotal(timeEntries)
        val writing = 2.0
        doReturn(writing).`when`(sut).calculateWritingTime(timeEntries)
        val editing = 3.0
        doReturn(editing).`when`(sut).calculateEditingTime(timeEntries)
        val marketing = 4.0
        doReturn(marketing).`when`(sut).calculateMarketingTime(timeEntries)
        val readingFiction = 5.0
        doReturn(readingFiction).`when`(sut).calculateReadingFictionTime(timeEntries)
        val readingNonFiction = 6.0
        doReturn(readingNonFiction).`when`(sut).calculateReadingNonFictionTime(timeEntries)

        val inOrder = inOrder(sut, day, startTime, tgl)

        sut.init()
        // Run method under test
        val actRes = sut.totalProductiveTime(day)

        // Verify
        inOrder.verify(sut).calculateStartOfDay(day)
        inOrder.verify(sut).calculateEndOfDay(day)
        inOrder.verify(tgl).getTimeEntries(startTime, endTime)
        inOrder.verify(sut).calculateTotal(timeEntries)
        inOrder.verify(sut).calculateWritingTime(timeEntries)
        inOrder.verify(sut).calculateEditingTime(timeEntries)
        inOrder.verify(sut).calculateMarketingTime(timeEntries)
        inOrder.verify(sut).calculateReadingFictionTime(timeEntries)
        inOrder.verify(sut).calculateReadingNonFictionTime(timeEntries)

        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNotNull
        assertThat(actRes.result?.startTime).isSameAs(startTime)
        assertThat(actRes.result?.total).isEqualTo(total)
        assertThat(actRes.result?.writing).isEqualTo(writing)
        assertThat(actRes.result?.editing).isEqualTo(editing)
        assertThat(actRes.result?.marketing).isEqualTo(marketing)
        assertThat(actRes.result?.readingFiction).isEqualTo(readingFiction)
        assertThat(actRes.result?.readingNonFiction).isEqualTo(readingNonFiction)
    }
    @Test
    fun toHours() {
        toHoursLogic(0.0, 0.0)
        toHoursLogic(3600.0, 1.0)
    }
    @Test
    fun calculateReadingNonFictionTime() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val summand1 = 3600L
        val summand2 = 100L
        val timeEntries = listOf(
                createTimeEntry(TogglSubsystem.Editing, 1800L),
                createTimeEntry(TogglSubsystem.ReadingNonFiction, summand1),
                createTimeEntry(TogglSubsystem.ReadingNonFiction, summand2)
        )

        // Run method under test
        val actRes = sut.calculateReadingNonFictionTime(timeEntries)

        // Verify
        assertThat(actRes).isEqualTo((summand1 + summand2).toDouble() / TogglSubsystem.SecondsInHour)
    }
    @Test
    fun calculateReadingFictionTime() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val summand1 = 3600L
        val summand2 = 100L
        val timeEntries = listOf(
                createTimeEntry(TogglSubsystem.Editing, 1800L),
                createTimeEntry(TogglSubsystem.ReadingFiction, summand1),
                createTimeEntry(TogglSubsystem.ReadingFiction, summand2)
        )

        // Run method under test
        val actRes = sut.calculateReadingFictionTime(timeEntries)

        // Verify
        assertThat(actRes).isEqualTo((summand1 + summand2).toDouble() / TogglSubsystem.SecondsInHour)
    }
    @Test
    fun calculateMarketingTime() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val summand1 = 3600L
        val summand2 = 100L
        val timeEntries = listOf(
                createTimeEntry(TogglSubsystem.Editing, 1800L),
                createTimeEntry(TogglSubsystem.Marketing, summand1),
                createTimeEntry(TogglSubsystem.Marketing, summand2)
        )

        // Run method under test
        val actRes = sut.calculateMarketingTime(timeEntries)

        // Verify
        assertThat(actRes).isEqualTo((summand1 + summand2).toDouble() / TogglSubsystem.SecondsInHour)
    }
    @Test
    fun calculateEditingTime() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val summand1 = 3600L
        val summand2 = 100L
        val timeEntries = listOf(
                createTimeEntry(TogglSubsystem.Marketing, 1800L),
                createTimeEntry(TogglSubsystem.Editing, summand1),
                createTimeEntry(TogglSubsystem.Editing, summand2)
        )

        // Run method under test
        val actRes = sut.calculateEditingTime(timeEntries)

        // Verify
        assertThat(actRes).isEqualTo((summand1 + summand2).toDouble() / TogglSubsystem.SecondsInHour)
    }
    @Test
    fun calculateWritingTime() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val summand1 = 3600L
        val summand2 = 100L
        val timeEntries = listOf(
                createTimeEntry(TogglSubsystem.Marketing, 1800L),
                createTimeEntry(TogglSubsystem.WritingFiction, summand1),
                createTimeEntry(TogglSubsystem.WritingFiction, summand2)
        )

        // Run method under test
        val actRes = sut.calculateWritingTime(timeEntries)

        // Verify
        assertThat(actRes).isEqualTo((summand1 + summand2).toDouble() / TogglSubsystem.SecondsInHour)
    }
    @Test
    fun calculateTotal() {
        // Prepare
        val sut = spy(TogglSubsystem())
        val summand1 = 3600L
        val summand2 = 100L
        val summand3 = 1800L
        val timeEntries = listOf(
                createTimeEntry(TogglSubsystem.Marketing, summand3),
                createTimeEntry(TogglSubsystem.WritingFiction, summand1),
                createTimeEntry(TogglSubsystem.WritingFiction, summand2)
        )

        // Run method under test
        val actRes = sut.calculateTotal(timeEntries)

        // Verify
        assertThat(actRes).isEqualTo((summand1 + summand2 + summand3).toDouble() / TogglSubsystem.SecondsInHour)
    }
    @Test
    fun extractProjectIdPidNull() {
        // Prepare
        val sut = TogglSubsystem()
        val te = TimeEntry()
        te.pid = null

        // Run method under test
        val actRes = sut.extractProjectId(te)

        // Verify
        assertThat(actRes).isEqualTo(-1L)
    }
    @Test
    fun extractProjectIdSunnyDay() {
        // Prepare
        val sut = TogglSubsystem()
        val te = TimeEntry()
        te.pid = 123L

        // Run method under test
        val actRes = sut.extractProjectId(te)

        // Verify
        assertThat(actRes).isEqualTo(123L)
    }
    @Test
    fun calculateEndOfDay() {
        // Prepare
        val sut = TogglSubsystem()
        val day = Date.from(LocalDateTime.of(2017, 4, 26, 11, 30, 39).toInstant(ZoneOffset.UTC))

        // Run method under test
        val actRes = sut.calculateEndOfDay(day)

        // Verify
        val cal = Calendar.getInstance()
        cal.time = actRes
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2017)
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.APRIL)
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(26)
        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(23)
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(59)
        assertThat(cal.get(Calendar.SECOND)).isEqualTo(59)
    }
    @Test
    fun calculateStartOfDay() {
        // Prepare
        val sut = TogglSubsystem()
        val day = Date.from(LocalDateTime.of(2017, 4, 26, 11, 30, 39).toInstant(ZoneOffset.UTC))

        // Run method under test
        val actRes = sut.calculateStartOfDay(day)

        // Verify
        val cal = Calendar.getInstance()
        cal.time = actRes
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2017)
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.APRIL)
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(26)
        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(0)
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0)
        assertThat(cal.get(Calendar.SECOND)).isEqualTo(0)
    }
    @Test
    fun isReadingNonFictionEntry() {
        // Prepare
        val sut = TogglSubsystem()
        val te1 = TimeEntry()
        te1.pid = TogglSubsystem.ReadingNonFiction
        val te2 = TimeEntry()
        te2.pid = TogglSubsystem.Marketing

        // Run method under test
        val actResTe1 = sut.isReadingNonFictionEntry(te1)
        val actResTe2 = sut.isReadingNonFictionEntry(te2)

        // Verify
        assertThat(actResTe1).isTrue()
        assertThat(actResTe2).isFalse()
    }
    @Test
    fun isReadingFictionEntry() {
        // Prepare
        val sut = TogglSubsystem()
        val te1 = TimeEntry()
        te1.pid = TogglSubsystem.ReadingFiction
        val te2 = TimeEntry()
        te2.pid = TogglSubsystem.Marketing

        // Run method under test
        val actResTe1 = sut.isReadingFictionEntry(te1)
        val actResTe2 = sut.isReadingFictionEntry(te2)

        // Verify
        assertThat(actResTe1).isTrue()
        assertThat(actResTe2).isFalse()
    }
    @Test
    fun isMarketingEntry() {
        // Prepare
        val sut = TogglSubsystem()
        val te1 = TimeEntry()
        te1.pid = TogglSubsystem.Marketing
        val te2 = TimeEntry()
        te2.pid = TogglSubsystem.ReadingFiction

        // Run method under test
        val actResTe1 = sut.isMarketingEntry(te1)
        val actResTe2 = sut.isMarketingEntry(te2)

        // Verify
        assertThat(actResTe1).isTrue()
        assertThat(actResTe2).isFalse()
    }
    @Test
    fun isWritingEntry() {
        // Prepare
        val sut = TogglSubsystem()
        val te1 = TimeEntry()
        te1.pid = TogglSubsystem.WritingFiction
        val te2 = TimeEntry()
        te2.pid = TogglSubsystem.ReadingFiction

        // Run method under test
        val actResTe1 = sut.isWritingEntry(te1)
        val actResTe2 = sut.isWritingEntry(te2)

        // Verify
        assertThat(actResTe1).isTrue()
        assertThat(actResTe2).isFalse()
    }
    @Test
    fun isEditingEntry() {
        // Prepare
        val sut = TogglSubsystem()
        val te1 = TimeEntry()
        te1.pid = TogglSubsystem.Editing
        val te2 = TimeEntry()
        te2.pid = TogglSubsystem.ReadingFiction

        // Run method under test
        val actResTe1 = sut.isEditingEntry(te1)
        val actResTe2 = sut.isEditingEntry(te2)

        // Verify
        assertThat(actResTe1).isTrue()
        assertThat(actResTe2).isFalse()
    }

    private fun createTimeEntry(projectId: Long, duration: Long): TimeEntry {
        val te1 = TimeEntry()
        te1.pid = projectId
        te1.duration = duration
        return te1
    }

    private fun toHoursLogic(seconds: Double, hours: Double) {
        // Prepare
        val sut = spy(TogglSubsystem())

        // Run method under test
        val actRes = sut.toHours(seconds)

        // Verify
        assertThat(actRes).isEqualTo(hours)
    }
}