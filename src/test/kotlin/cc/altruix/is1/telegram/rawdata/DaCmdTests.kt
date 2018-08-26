package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.cmd.DaCmd
import cc.altruix.is1.telegram.forms.IAutomatonFactory
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import cc.altruix.utils.composeWeekText2
import cc.altruix.utils.toDate
import org.apache.commons.io.IOUtils
import org.fest.assertions.Assertions
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.methods.send.SendMessage
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by pisarenko on 12.05.2017.
 */
class DaCmdTests {
    @Test
    fun executeDataRetrievalFailure() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(DaCmd(mongo, tu))
        val msg = "msg"
        val dsRes = FailableOperationResult<List<Date>>(false, msg, null)
        `when`(mongo.distractionStats()).thenReturn(dsRes)


        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1106L
        val userId = 1205

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(mongo).distractionStats()
        verify(tu).displayError("Could not get data from Mongo ('$msg').", chatId, bot)
    }
    @Test
    fun executeDataSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(DaCmd(mongo, tu))
        val msg = "msg"
        val ds = emptyList<Date>()
        val dsRes = FailableOperationResult<List<Date>>(true, "", ds)
        `when`(mongo.distractionStats()).thenReturn(dsRes)


        val text = "text"
        val bot = mock<IResponsiveBot>()
        val chatId = 1106L
        val userId = 1205

        doNothing().`when`(sut).executeLogic(ds, chatId, bot)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        verify(mongo).distractionStats()
        verify(sut).executeLogic(ds, chatId, bot)
    }
    @Test
    fun nameAndHelp() {
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = DaCmd(mongo, tu)
        Assertions.assertThat(sut.name()).isEqualTo(DaCmd.Name)
        Assertions.assertThat(sut.helpText()).isEqualTo(DaCmd.Help)
    }
    @Test
    fun calcMax() {
        calcMaxTestLogic(mapOf(
                0 to AtomicInteger(1),
                1 to AtomicInteger(5),
                2 to AtomicInteger(3)
        ), 5.0)
        calcMaxTestLogic(
                emptyMap(),
                1.0
        )
    }
    @Test
    fun percent() {
        percentTestLogic(100, 100.0, 100.0)
        percentTestLogic(50, 100.0, 50.0)
        percentTestLogic(0, 100.0, 0.0)
    }
    @Test
    fun extractHour() {
        extractHourTestLogic(LocalDateTime.of(2017, 5, 12, 0, 0).toDate(), 0)
        extractHourTestLogic(LocalDateTime.of(2017, 5, 12, 0, 59).toDate(), 0)
        extractHourTestLogic(LocalDateTime.of(2017, 5, 12, 1, 0).toDate(), 1)
        extractHourTestLogic(LocalDateTime.of(2017, 5, 12, 1, 59).toDate(), 1)
        extractHourTestLogic(LocalDateTime.of(2017, 5, 12, 23, 0).toDate(), 23)
        extractHourTestLogic(LocalDateTime.of(2017, 5, 12, 23, 30).toDate(), 23)
        extractHourTestLogic(LocalDateTime.of(2017, 5, 12, 23, 59).toDate(), 23)
    }
    @Test
    fun sameWeek() {
        val day1 = LocalDate.now()
        val week1 = composeWeekText2(day1.toDate())

        val day2 = day1.minusMonths(1)
        val day3 = day1.plusMonths(1)
        sameWeekTestLogic(day1.toDate(), week1, true)
        sameWeekTestLogic(day2.toDate(), week1, false)
        sameWeekTestLogic(day3.toDate(), week1, false)
    }
    @Test
    fun createSendMessage() {
        // Prepare
        val sut = spy(DaCmd(mock<IAltruixIs1MongoSubsystem>()))
        val msg = mock<SendMessage>()
        doReturn(msg).`when`(sut).createSendMessage()
        val chatId = 1453L
        val txt = "txt"

        // Run method under test
        val actRes = sut.createSendMessage(chatId, txt)

        // Verify
        verify(sut).createSendMessage()
        verify(msg).enableMarkdown(true)
        verify(msg).chatId = chatId.toString()
        verify(msg).text = txt
        assertThat(actRes).isSameAs(msg)
    }
    @Test
    fun calculateDistractionsPerHourTotal() {
        // Prepare
        val sut = spy(DaCmd(mock<IAltruixIs1MongoSubsystem>()))
        val date1 = LocalDateTime.of(2017, 5, 12, 15, 16).toDate()
        val date2 = LocalDateTime.of(2017, 5, 12, 16, 16).toDate()
        val date3 = LocalDateTime.of(2017, 5, 12, 16, 30).toDate()

        val ds = listOf(date1, date2, date3)
        val distractionsPerHourTotal = HashMap<Int,AtomicInteger>()
        distractionsPerHourTotal[15] = AtomicInteger(0)
        distractionsPerHourTotal[16] = AtomicInteger(0)

        doReturn(15).`when`(sut).extractHour(date1)
        doReturn(16).`when`(sut).extractHour(date2)
        doReturn(16).`when`(sut).extractHour(date2)

        // Run method under test
        val actRes = sut.calculateDistractionsPerHourTotal(ds, distractionsPerHourTotal)

        // Verify
        assertThat(distractionsPerHourTotal[15]).isNotNull
        assertThat(distractionsPerHourTotal[16]).isNotNull
        assertThat(distractionsPerHourTotal[15]?.get()).isEqualTo(1)
        assertThat(distractionsPerHourTotal[16]?.get()).isEqualTo(2)
    }
    @Test
    fun executeLogic() {
        // Prepare
        val sut = spy(DaCmd(mock<IAltruixIs1MongoSubsystem>()))
        val now = Date()
        doReturn(now).`when`(sut).now()
        val ds = mock<List<Date>>()
        val chatId = 1525L
        val bot = mock<IResponsiveBot>()
        val distractionsPerHourTotal = mock<MutableMap<Int,AtomicInteger>>()
        val distractionsPerHourWeek = mock<MutableMap<Int,AtomicInteger>>()
        doReturn(distractionsPerHourTotal).doReturn(distractionsPerHourWeek)
                .`when`(sut).createHourlyMap()

        doNothing().`when`(sut).calculateDistractionsPerHourWeek(ds, now, distractionsPerHourWeek)
        doNothing().`when`(sut).calculateDistractionsPerHourTotal(ds, distractionsPerHourTotal)

        val maxWeek = 1.0
        val maxTotal = 2.0
        doReturn(maxWeek).`when`(sut).calcMax(distractionsPerHourWeek)
        doReturn(maxTotal).`when`(sut).calcMax(distractionsPerHourTotal)
        val sb = StringBuilder()
        val sbToString = "sbToString"
        sb.append(sbToString)
        doReturn(sb).`when`(sut).composeMsgTxt(distractionsPerHourTotal,
                distractionsPerHourWeek, maxTotal, maxWeek)
        val msg = mock<SendMessage>()
        doReturn(msg).`when`(sut).createSendMessage(chatId, sbToString)

        val inOrder = inOrder(sut, ds, bot)

        // Run method under test
        sut.executeLogic(ds, chatId, bot)

        // Verify
        inOrder.verify(sut).now()
        inOrder.verify(sut, times(2)).createHourlyMap()
        inOrder.verify(sut).calculateDistractionsPerHourWeek(ds, now, distractionsPerHourWeek)
        inOrder.verify(sut).calculateDistractionsPerHourTotal(ds, distractionsPerHourTotal)
        inOrder.verify(sut).calcMax(distractionsPerHourWeek)
        inOrder.verify(sut).calcMax(distractionsPerHourTotal)
        inOrder.verify(sut).composeMsgTxt(distractionsPerHourTotal, distractionsPerHourWeek,
                maxTotal, maxWeek)
        inOrder.verify(sut).createSendMessage(chatId, sbToString)
        inOrder.verify(bot).sendTelegramMessage(msg)
    }
    @Test
    fun createHourlyMap() {
        // Prepare
        val sut = spy(DaCmd(mock<IAltruixIs1MongoSubsystem>()))

        // Run method under test
        val actRes = sut.createHourlyMap()

        // Verify
        assertThat(actRes.entries.size).isEqualTo(24)
        for (hour in 0..23) {
            assertThat(actRes[hour]).isNotNull
            assertThat(actRes[hour]!!.get()).isEqualTo(0)
        }
    }
    @Test
    fun calculateDistractionsPerHourWeek() {
        // Prepare
        val sut = spy(DaCmd(mock<IAltruixIs1MongoSubsystem>()))
        val day1 = LocalDateTime.of(2017, 5, 12, 16, 17).toDate()
        val day2 = LocalDateTime.of(2017, 5, 1, 17, 17).toDate()
        val now = LocalDateTime.of(2017, 5, 12, 15, 17).toDate()
        val distractionsPerHourWeek = HashMap<Int, AtomicInteger>()
        distractionsPerHourWeek[16] = AtomicInteger(0)

        // Run method under test
        sut.calculateDistractionsPerHourWeek(listOf(day1, day2), now, distractionsPerHourWeek)

        // Verify
        assertThat(distractionsPerHourWeek[17]).isNull()
        assertThat(distractionsPerHourWeek[16]).isNotNull
        assertThat(distractionsPerHourWeek[16]!!.get()).isEqualTo(1)
    }
    @Test
    fun composeMsgTxtAndAppendHourlyStats() {
        // Prepare
        val sut = spy(DaCmd(mock<IAltruixIs1MongoSubsystem>()))

        val distractionsPerHourTotal = HashMap<Int, AtomicInteger>()
        val distractionsPerHourWeek = HashMap<Int, AtomicInteger>()
        val maxTotal = 25.0
        val maxWeek = 13.0

        distractionsPerHourWeek[6] = AtomicInteger(5)
        distractionsPerHourWeek[7] = AtomicInteger(13)
        distractionsPerHourWeek[14] = AtomicInteger(2)

        distractionsPerHourTotal[6] = AtomicInteger(6)
        distractionsPerHourTotal[7] = AtomicInteger(23)
        distractionsPerHourTotal[14] = AtomicInteger(25)

        // Run method under test
        val actRes = sut.composeMsgTxt(distractionsPerHourTotal, distractionsPerHourWeek,
                maxTotal, maxWeek).toString()

        // Verify
        val expRes = readFile("DaCmdTests.composeMsgTxtAndAppendHourlyStats.md")
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun readFile(file: String) = IOUtils.toString(javaClass.classLoader.getResourceAsStream("cc/altruix/is1/telegram/cmd/" + file), "UTF-8")

    private fun sameWeekTestLogic(day: Date, curWeek: String, expRes: Boolean) {
        // Prepare
        val sut = DaCmd(mock<IAltruixIs1MongoSubsystem>())

        // Run method under test
        val actRes = sut.sameWeek(day, curWeek)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun extractHourTestLogic(day: Date, expRes: Int) {
        // Prepare
        val sut = DaCmd(mock<IAltruixIs1MongoSubsystem>())

        // Run method under test
        val actRes = sut.extractHour(day)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }


    private fun percentTestLogic(value: Int, max: Double, expRes: Double) {
        // Prepare
        val sut = DaCmd(mock<IAltruixIs1MongoSubsystem>())

        // Run method under test
        val actRes = sut.percent(value, max)

        // Verify
        assertThat(actRes).isEqualTo(expRes)

    }

    private fun calcMaxTestLogic(data: Map<Int, AtomicInteger>, expRes: Double) {
        // Prepare
        val sut = DaCmd(mock<IAltruixIs1MongoSubsystem>())

        // Run method under test
        val actRes = sut.calcMax(data)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}