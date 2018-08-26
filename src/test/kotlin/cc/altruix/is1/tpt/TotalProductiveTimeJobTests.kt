package cc.altruix.is1.tpt

import cc.altruix.is1.mongo.AltruixIs1MongoSubsystem
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.scheduler.ISchedulerSubsystem
import cc.altruix.is1.toggl.ITogglSubsystem
import cc.altruix.is1.toggl.TogglSubsystem
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.*
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import java.util.*

/**
 * Created by pisarenko on 26.04.2017.
 */
class TotalProductiveTimeJobTests {
    @Test
    fun executeNullCtx() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TotalProductiveTimeJob(logger))

        // Run method under test
        sut.execute(null)

        // Verify
        verify(logger).error("Null context")
    }
    @Test
    fun executeNullJd() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TotalProductiveTimeJob(logger))
        val ctx = mock<JobExecutionContext>()
        `when`(ctx.mergedJobDataMap).thenReturn(null)

        // Run method under test
        sut.execute(ctx)

        // Verify
        verify(logger).error("Null job data map")
    }
    @Test
    fun executeTogglMongoMissing() {
        executeTogglMongoMissingTestLogic(false, false)
        executeTogglMongoMissingTestLogic(false, true)
        executeTogglMongoMissingTestLogic(true, false)
    }
    @Test
    fun executeTogglMongoNull() {
        executeTogglMongoNullTestLogic(null, null)
        executeTogglMongoNullTestLogic(null, mock<IMongoSubsystem>())
        executeTogglMongoNullTestLogic(mock<ITogglSubsystem>(), null)
    }
    @Test
    fun executeSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TotalProductiveTimeJob(logger))
        val jd = mock<JobDataMap>()
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataToggl)).thenReturn(true)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataMongo)).thenReturn(true)
        val mongo = mock<IMongoSubsystem>()
        val toggl = mock<ITogglSubsystem>()
        `when`(jd[ISchedulerSubsystem.JobDataToggl]).thenReturn(toggl)
        `when`(jd[ISchedulerSubsystem.JobDataMongo]).thenReturn(mongo)
        val ctx = mock<JobExecutionContext>()
        `when`(ctx.mergedJobDataMap).thenReturn(jd)
        doNothing().`when`(sut).retrieveAndSaveData(toggl, mongo)

        // Run method under test
        sut.execute(ctx)

        // Verify
        verify(sut).retrieveAndSaveData(toggl, mongo)
    }
    @Test
    fun retrieveAndSaveTimeDataRetrievalFailure() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TotalProductiveTimeJob(logger))
        val toggl = mock<ITogglSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        val now = mock<Date>()
        doReturn(now).`when`(sut).now()
        val yesterday = mock<Date>()
        doReturn(yesterday).`when`(sut).yesterday(now)
        val msg = "msg"
        val tptRes = FailableOperationResult<TotalProductiveTime>(false, msg, null)
        `when`(toggl.totalProductiveTime(yesterday)).thenReturn(tptRes)

        val inOrder = inOrder(logger, sut, toggl, mongo, now)

        // Run method under test
        sut.retrieveAndSaveData(toggl, mongo)

        // Verify
        inOrder.verify(sut).now()
        inOrder.verify(sut).yesterday(now)
        inOrder.verify(toggl).totalProductiveTime(yesterday)
        inOrder.verify(logger).error("Data could not be retrieved from Toggl ('$msg').")
    }
    @Test
    fun retrieveAndSaveTimeSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TotalProductiveTimeJob(logger))
        val toggl = mock<ITogglSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        val now = mock<Date>()
        doReturn(now).`when`(sut).now()
        val yesterday = mock<Date>()
        doReturn(yesterday).`when`(sut).yesterday(now)
        val tpt = TotalProductiveTime(
                Date(), Date(),
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
        )
        val tptRes = FailableOperationResult<TotalProductiveTime>(true, "", tpt)
        `when`(toggl.totalProductiveTime(yesterday)).thenReturn(tptRes)

        val inOrder = inOrder(logger, sut, toggl, mongo, now)
        val data:Map<String,Any> = HashMap()
        doReturn(data).`when`(sut).composeDataToInsert(tpt)

        // Run method under test
        sut.retrieveAndSaveData(toggl, mongo)

        // Verify
        inOrder.verify(sut).now()
        inOrder.verify(sut).yesterday(now)
        inOrder.verify(toggl).totalProductiveTime(yesterday)
        inOrder.verify(sut).composeDataToInsert(tpt)
        inOrder.verify(mongo).insert(data, TotalProductiveTimeJob.TotalProductiveTimeColl)
    }
    @Test
    fun composeDataToInsert() {
        // Prepare
        val sut = TotalProductiveTimeJob()
        val startTime = mock<Date>()
        val total = 1.0
        val writing = 2.0
        val editing = 3.0
        val marketing = 4.0
        val readingFicton = 5.0
        val readingNonFiction = 6.0
        val endTime = mock<Date>()
        val tpt = TotalProductiveTime(
                startTime, endTime, total, writing, editing, marketing, readingFicton, readingNonFiction,
                0.0, 0.0
        )
        // Run method under test
        val actRes = sut.composeDataToInsert(tpt)

        // Verify
        assertThat(actRes[TotalProductiveTimeJob.StartTimeColumn]).isSameAs(startTime)
        assertThat(actRes[TotalProductiveTimeJob.EndTimeColumn]).isSameAs(endTime)
        assertThat(actRes[TotalProductiveTimeJob.TotalColumn]).isEqualTo(total)
        assertThat(actRes[TotalProductiveTimeJob.WritingColumn]).isEqualTo(writing)
        assertThat(actRes[TotalProductiveTimeJob.EditingColumn]).isEqualTo(editing)
        assertThat(actRes[TotalProductiveTimeJob.MarketingColumn]).isEqualTo(marketing)
        assertThat(actRes[TotalProductiveTimeJob.ReadingFictionColumn]).isEqualTo(readingFicton)
        assertThat(actRes[TotalProductiveTimeJob.ReadingNonFictionColumn]).isEqualTo(readingNonFiction)
    }
    @Test
    @Ignore
    fun integrationTestIssue95() {
        val sut = TotalProductiveTimeJob()
        val toggl = TogglSubsystem()
        toggl.init()
        val mongo = AltruixIs1MongoSubsystem()
        mongo.init()

        sut.retrieveAndSaveData(toggl, mongo)
    }

    private fun executeTogglMongoMissingTestLogic(toggl: Boolean, mongo: Boolean) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TotalProductiveTimeJob(logger))
        val jd = mock<JobDataMap>()
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataToggl)).thenReturn(toggl)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataMongo)).thenReturn(mongo)
        val ctx = mock<JobExecutionContext>()
        `when`(ctx.mergedJobDataMap).thenReturn(jd)

        // Run method under test
        sut.execute(ctx)

        // Verify
        verify(logger).error("Key '${ISchedulerSubsystem.JobDataToggl}' and/or '${ISchedulerSubsystem.JobDataMongo}' is missing in job data map")
    }
    private fun executeTogglMongoNullTestLogic(toggl: ITogglSubsystem?, mongo: IMongoSubsystem?) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TotalProductiveTimeJob(logger))
        val jd = mock<JobDataMap>()
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataToggl)).thenReturn(true)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataMongo)).thenReturn(true)
        `when`(jd[ISchedulerSubsystem.JobDataToggl]).thenReturn(toggl)
        `when`(jd[ISchedulerSubsystem.JobDataMongo]).thenReturn(mongo)
        val ctx = mock<JobExecutionContext>()
        `when`(ctx.mergedJobDataMap).thenReturn(jd)

        // Run method under test
        sut.execute(ctx)

        // Verify
        verify(logger).error("Toggl and/or Mongo subsystem is null in job data map")
    }

}