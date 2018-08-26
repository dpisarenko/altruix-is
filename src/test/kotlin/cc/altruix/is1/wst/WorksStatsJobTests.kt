package cc.altruix.is1.wst

import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.scheduler.ISchedulerSubsystem
import cc.altruix.is1.trello.ITrelloSubsystem
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import org.junit.Test
import org.mockito.Mockito.*
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.slf4j.Logger

/**
 * Created by 1 on 30.04.2017.
 */
class WorksStatsJobTests {
    @Test
    fun executeNullContext() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(WorksStatsJob(logger))

        // Run method under test
        sut.execute(null)

        // Verify
        verify(logger).error("Null context")
    }
    @Test
    fun executeNullJobDataMap() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(WorksStatsJob(logger))
        val ctx = mock<JobExecutionContext>()
        `when`(ctx.mergedJobDataMap).thenReturn(null)

        // Run method under test
        sut.execute(ctx)

        // Verify
        verify(ctx).mergedJobDataMap
        verify(logger).error("Null job data map")
    }
    @Test
    fun executeNoTrelloMongoKeys() {
        executeNoTrelloMongoKeysTestLogic(false, false)
        executeNoTrelloMongoKeysTestLogic(true, false)
        executeNoTrelloMongoKeysTestLogic(false, true)
    }
    @Test
    fun executeNoTrelloMongo() {
        val trello = mock<ITrelloSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        executeNoTrelloMongoTestLogic(null, null)
        executeNoTrelloMongoTestLogic(trello, null)
        executeNoTrelloMongoTestLogic(null, mongo)
    }
    @Test
    fun executeSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(WorksStatsJob(logger))
        val ctx = mock<JobExecutionContext>()
        val jd = mock<JobDataMap>()
        `when`(ctx.mergedJobDataMap).thenReturn(jd)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataTrello)).thenReturn(true)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataMongo)).thenReturn(true)
        val trello = mock<ITrelloSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        `when`(jd[ISchedulerSubsystem.JobDataTrello]).thenReturn(trello)
        `when`(jd[ISchedulerSubsystem.JobDataMongo]).thenReturn(mongo)
        doNothing().`when`(sut).retrieveAndSaveData(trello, mongo)

        // Run method under test
        sut.execute(ctx)

        // Verify
        verify(sut).retrieveAndSaveData(trello, mongo)
    }
    @Test
    fun retrieveAndSaveDataDataReadingFailure() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(WorksStatsJob(logger))
        val trello = mock<ITrelloSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        val msg = "msg"
        val wsRes = FailableOperationResult<Map<String, Any>>(false, msg, null)
        `when`(trello.worksStatistics()).thenReturn(wsRes)

        // Run method under test
        sut.retrieveAndSaveData(trello, mongo)

        // Verify
        verify(trello).worksStatistics()
        verify(logger).error("Can't retrieve Trello data ('$msg').")
    }
    @Test
    fun retrieveAndSaveDataDataSavingFailure() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(WorksStatsJob(logger))
        val trello = mock<ITrelloSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        val ws = emptyMap<String, Any>()
        val wsRes = FailableOperationResult<Map<String, Any>>(true, "", ws)
        `when`(trello.worksStatistics()).thenReturn(wsRes)
        val msg = "msg"
        val insRes = ValidationResult(false, msg)
        `when`(mongo.insert(ws, WorksStatsJob.WorksStatsColl)).thenReturn(insRes)

        // Run method under test
        sut.retrieveAndSaveData(trello, mongo)

        // Verify
        verify(trello).worksStatistics()
        verify(mongo).insert(ws, WorksStatsJob.WorksStatsColl)
        verify(logger).error("Could not write Trello data into Mongo ('$msg').")
    }
    @Test
    fun retrieveAndSaveDataSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(WorksStatsJob(logger))
        val trello = mock<ITrelloSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        val ws = emptyMap<String, Any>()
        val wsRes = FailableOperationResult<Map<String, Any>>(true, "", ws)
        `when`(trello.worksStatistics()).thenReturn(wsRes)
        val msg = "msg"
        val insRes = ValidationResult(true, msg)
        `when`(mongo.insert(ws, WorksStatsJob.WorksStatsColl)).thenReturn(insRes)

        // Run method under test
        sut.retrieveAndSaveData(trello, mongo)

        // Verify
        verify(trello).worksStatistics()
        verify(mongo).insert(ws, WorksStatsJob.WorksStatsColl)
        verify(logger, never()).error("Could not write Trello data into Mongo ('$msg').")
    }

    private fun executeNoTrelloMongoTestLogic(
            trello: ITrelloSubsystem?,
            mongo: IMongoSubsystem?
    ) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(WorksStatsJob(logger))
        val ctx = mock<JobExecutionContext>()
        val jd = mock<JobDataMap>()
        `when`(ctx.mergedJobDataMap).thenReturn(jd)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataTrello)).thenReturn(true)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataMongo)).thenReturn(true)
        `when`(jd[ISchedulerSubsystem.JobDataTrello]).thenReturn(trello)
        `when`(jd[ISchedulerSubsystem.JobDataMongo]).thenReturn(mongo)

        // Run method under test
        sut.execute(ctx)

        // Verify
        verify(logger).error("Trello and/or Mongo subsystem is null in job data map")
    }

    private fun executeNoTrelloMongoKeysTestLogic(
            trello: Boolean,
            mongo: Boolean
    ) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(WorksStatsJob(logger))
        val ctx = mock<JobExecutionContext>()
        val jd = mock<JobDataMap>()
        `when`(ctx.mergedJobDataMap).thenReturn(jd)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataTrello)).thenReturn(trello)
        `when`(jd.containsKey(ISchedulerSubsystem.JobDataMongo)).thenReturn(mongo)

        // Run method under test
        sut.execute(ctx)

        // Verify
        verify(ctx).mergedJobDataMap
        verify(logger).error("Key '${ISchedulerSubsystem.JobDataTrello}' and/or '${ISchedulerSubsystem.JobDataMongo}' is missing in job data map")
    }

}