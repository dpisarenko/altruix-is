package cc.altruix.is1

import cc.altruix.is1.capsulecrm.CapsuleCrmSubsystem
import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.scheduler.ISchedulerSubsystem
import cc.altruix.is1.telegram.TelegramSubsystem
import cc.altruix.is1.toggl.ITogglSubsystem
import cc.altruix.is1.tpt.TotalProductiveTimeJob
import cc.altruix.is1.trello.ITrelloSubsystem
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.is1.wst.WorksStatsJob
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.quartz.*
import org.slf4j.Logger
import org.telegram.telegrambots.TelegramBotsApi

/**
 * Created by pisarenko on 01.02.2017.
 */
class AppTests {
    @Test
    fun initCreatesCapsuleCrmSubsystem() {
        // Prepare
        val logger = mock<Logger>()
        val botApi = mock<TelegramBotsApi>()
        val sut = Mockito.spy(App(logger))
        doReturn(botApi).`when`(sut).createTelegramBotsApi()
        val capsule = mock< ICapsuleCrmSubsystem>()
        doReturn(capsule).`when`(sut).createCapsuleCrmSubsystem()
        val telegram = mock<TelegramSubsystem>()
        val jena = mock<IJenaSubsystem>()
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        doReturn(mongo).`when`(sut).createMongo()
        val mongoInit = ValidationResult(true, "")
        `when`(mongo.init()).thenReturn(mongoInit)
        doReturn(jena).`when`(sut).createJena()
        doReturn(telegram).`when`(sut).createTelegramSubsystem(
                botApi,
                capsule,
                jena,
                mongo
        )
        val toggl = mock<ITogglSubsystem>()
        doReturn(toggl).`when`(sut).createToggl()
        val scheduler = mock<ISchedulerSubsystem>()
        doReturn(scheduler).`when`(sut).createSchedulerSubsystem()
        val schInitRes = ValidationResult(true, "")
        `when`(scheduler.init()).thenReturn(schInitRes)
        val schRes = ValidationResult(true, "")
        doReturn(schRes).`when`(sut).scheduleDailyTotalProductiveTimeGenerator(
                scheduler,
                toggl,
                mongo
        )
        val schStartRes = ValidationResult(true, "")
        `when`(scheduler.start()).thenReturn(schStartRes)
        val trello = mock<ITrelloSubsystem>()
        doReturn(trello).`when`(sut).createTrello()
        doReturn(ValidationResult(true, "")).`when`(sut)
                .scheduleWorksStatsJob(scheduler, trello, mongo)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createTelegramBotsApi()
        verify(sut).createCapsuleCrmSubsystem()
        verify(capsule, never()).init()
        verify(sut).createTelegramSubsystem(
                botApi,
                capsule,
                jena,
                mongo
        )
        verify(telegram).init()
    }
    @Test
    fun runAddsShutdownHook() {
        // Prepare
        val logger = mock<Logger>()
        val botApi = mock<TelegramBotsApi>()
        val sut = Mockito.spy(App(logger))
        val runtime = mock<Runtime>()
        doReturn(runtime).`when`(sut).getRuntime()
        val shutdownHook = mock<Thread>()
        val jena = mock<IJenaSubsystem>()
        doReturn(jena).`when`(sut).createJena()
        val capsule = mock<CapsuleCrmSubsystem>()
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        doReturn(mongo).`when`(sut).createMongo()
        val mongoRes = ValidationResult(true, "")
        `when`(mongo.init()).thenReturn(mongoRes)
        val scheduler = mock<ISchedulerSubsystem>()
        doReturn(scheduler).`when`(sut).createSchedulerSubsystem()
        `when`(scheduler.init()).thenReturn(ValidationResult(true, ""))
        val toggl = mock<ITogglSubsystem>()
        doReturn(toggl).`when`(sut).createToggl()
        doReturn(ValidationResult(true, "")).`when`(sut).scheduleDailyTotalProductiveTimeGenerator(scheduler,
                toggl, mongo)
        `when`(scheduler.start()).thenReturn(ValidationResult(true, ""))

        doReturn(shutdownHook).`when`(sut).createShutdownHook(
                jena,
                capsule,
                mongo,
                toggl,
                scheduler)
        doReturn(botApi).`when`(sut).createTelegramBotsApi()
        doReturn(capsule).`when`(sut).createCapsuleCrmSubsystem()
        val telegramSubsystem = mock<TelegramSubsystem>()
        doReturn(telegramSubsystem).`when`(sut).createTelegramSubsystem(botApi, capsule, jena,
                mongo)
        val trello = mock<ITrelloSubsystem>()
        doReturn(trello).`when`(sut).createTrello()
        doReturn(ValidationResult(true, "")).`when`(sut)
                .scheduleWorksStatsJob(scheduler, trello, mongo)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).getRuntime()
        verify(sut).createShutdownHook(jena, capsule, mongo,
                toggl,
                scheduler)
        verify(runtime).addShutdownHook(shutdownHook)
    }
    @Test
    fun runDoesNotInitializeJena() {
        // Prepare
        val logger = mock<Logger>()
        val sut = Mockito.spy(App(logger))
        val jena = mock<IJenaSubsystem>()
        doReturn(jena).`when`(sut).createJena()
        val botApi = mock<TelegramBotsApi>()
        doReturn(botApi).`when`(sut).createTelegramBotsApi()

        // Run method under test
        sut.run()

        // Verify
        verify(jena, never()).init()
    }
    @Test
    fun runInitializesMongo() {
        // Prepare
        val logger = mock<Logger>()
        val botApi = mock<TelegramBotsApi>()
        val sut = Mockito.spy(App(logger))
        val runtime = mock<Runtime>()
        doReturn(runtime).`when`(sut).getRuntime()
        val shutdownHook = mock<Thread>()
        val jena = mock<IJenaSubsystem>()
        doReturn(jena).`when`(sut).createJena()
        val capsule = mock<CapsuleCrmSubsystem>()
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val mongoRes = ValidationResult(true, "")
        `when`(mongo.init()).thenReturn(mongoRes)

        doReturn(mongo).`when`(sut).createMongo()
        doReturn(shutdownHook).`when`(sut).createShutdownHook(jena, capsule, mongo,
                mock<ITogglSubsystem>(),
                mock<ISchedulerSubsystem>())
        doReturn(botApi).`when`(sut).createTelegramBotsApi()
        doReturn(capsule).`when`(sut).createCapsuleCrmSubsystem()
        doReturn(mock<TelegramSubsystem>()).`when`(sut).createTelegramSubsystem(botApi, capsule, jena,
                mongo)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createMongo()
        verify(mongo).init()
    }
    @Test
    fun runFailsToInitializeMongo() {
        // Prepare
        val logger = mock<Logger>()
        val botApi = mock<TelegramBotsApi>()
        val sut = Mockito.spy(App(logger))
        val runtime = mock<Runtime>()
        doReturn(runtime).`when`(sut).getRuntime()
        val shutdownHook = mock<Thread>()
        val jena = mock<IJenaSubsystem>()
        doReturn(jena).`when`(sut).createJena()
        val capsule = mock<CapsuleCrmSubsystem>()
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val msg = "msg"
        val mongoStatus = ValidationResult(false, msg)

        `when`(mongo.init()).thenReturn(mongoStatus)
        doReturn(mongo).`when`(sut).createMongo()
        doReturn(shutdownHook).`when`(sut).createShutdownHook(jena, capsule, mock<IMongoSubsystem>(),
                mock<ITogglSubsystem>(),
                mock<ISchedulerSubsystem>())
        doReturn(botApi).`when`(sut).createTelegramBotsApi()
        doReturn(capsule).`when`(sut).createCapsuleCrmSubsystem()
        doReturn(mock<TelegramSubsystem>()).`when`(sut).createTelegramSubsystem(botApi, capsule, jena,
                mock<IAltruixIs1MongoSubsystem>())

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createMongo()
        verify(mongo).init()
        verify(logger).error("Mongo hat an Patsch'n ('$msg').")
        verify(logger).error("Shutting down after a failed start.")
    }
    @Test
    fun runScheduleInitializationFault() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(App(logger))
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        doReturn(mongo).`when`(sut).createMongo()
        val mongoStatus = ValidationResult(true, "")
        `when`(mongo.init()).thenReturn(mongoStatus)
        val toggl = mock<ITogglSubsystem>()
        doReturn(toggl).`when`(sut).createToggl()
        val scheduler = mock<ISchedulerSubsystem>()
        doReturn(scheduler).`when`(sut).createSchedulerSubsystem()
        val msg = "msg"
        val schedulerRes = ValidationResult(false, msg)
        `when`(scheduler.init()).thenReturn(schedulerRes)

        val inOrder = inOrder(logger, sut, mongo, toggl, scheduler)

        // Run method under test
        sut.run()

        // Verify
        inOrder.verify(sut).createToggl()
        inOrder.verify(toggl).init()
        inOrder.verify(sut).createSchedulerSubsystem()
        inOrder.verify(scheduler).init()
        inOrder.verify(logger).error("Scheduler can't be initialized ('$msg').")
        inOrder.verify(logger).error("Shutting down after a failed start.")
    }
    @Test
    fun runSchedulerInitializationFault() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(App(logger))
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        doReturn(mongo).`when`(sut).createMongo()
        val mongoStatus = ValidationResult(true, "")
        `when`(mongo.init()).thenReturn(mongoStatus)
        val toggl = mock<ITogglSubsystem>()
        doReturn(toggl).`when`(sut).createToggl()
        val scheduler = mock<ISchedulerSubsystem>()
        doReturn(scheduler).`when`(sut).createSchedulerSubsystem()
        val msg = "msg"
        val schedulerRes = ValidationResult(true, "")
        `when`(scheduler.init()).thenReturn(schedulerRes)
        val schRes = ValidationResult(false, msg)
        doReturn(schRes).`when`(sut).scheduleDailyTotalProductiveTimeGenerator(scheduler,
                toggl, mongo)

        val inOrder = inOrder(logger, sut, mongo, toggl, scheduler)

        // Run method under test
        sut.run()

        // Verify
        inOrder.verify(sut).createToggl()
        inOrder.verify(toggl).init()
        inOrder.verify(sut).createSchedulerSubsystem()
        inOrder.verify(scheduler).init()
        inOrder.verify(sut).scheduleDailyTotalProductiveTimeGenerator(scheduler, toggl, mongo)
        inOrder.verify(logger).error("Daily total productive time task can't be scheduled ('$msg').")
        inOrder.verify(logger).error("Shutting down after a failed start.")
    }
    @Test
    fun runSchedulerStatingFault() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(App(logger))
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        doReturn(mongo).`when`(sut).createMongo()
        val mongoStatus = ValidationResult(true, "")
        `when`(mongo.init()).thenReturn(mongoStatus)
        val toggl = mock<ITogglSubsystem>()
        doReturn(toggl).`when`(sut).createToggl()
        val scheduler = mock<ISchedulerSubsystem>()
        doReturn(scheduler).`when`(sut).createSchedulerSubsystem()
        val msg = "msg"
        val schedulerRes = ValidationResult(true, "")
        `when`(scheduler.init()).thenReturn(schedulerRes)
        val schRes = ValidationResult(true, "")
        doReturn(schRes).`when`(sut).scheduleDailyTotalProductiveTimeGenerator(scheduler,
                toggl, mongo)
        val schStartRes = ValidationResult(false, msg)
        `when`(scheduler.start()).thenReturn(schStartRes)
        val trello = mock<ITrelloSubsystem>()
        doReturn(trello).`when`(sut).createTrello()
        doReturn(ValidationResult(true, "")).`when`(sut)
                .scheduleWorksStatsJob(scheduler, trello, mongo)

        val inOrder = inOrder(logger, sut, mongo, toggl, scheduler)

        // Run method under test
        sut.run()

        // Verify
        inOrder.verify(sut).createToggl()
        inOrder.verify(toggl).init()
        inOrder.verify(sut).createSchedulerSubsystem()
        inOrder.verify(scheduler).init()
        inOrder.verify(sut).scheduleDailyTotalProductiveTimeGenerator(scheduler, toggl, mongo)
        inOrder.verify(scheduler).start()
        inOrder.verify(logger).error("Scheduler can't be started ('$msg').")
        inOrder.verify(logger).error("Shutting down after a failed start.")
    }
    @Test
    fun scheduleDailyTotalProductiveTimeGenerator() {
        // Prepare
        val sut = spy(App())
        val scheduler = mock<ISchedulerSubsystem>()
        val toggl = mock<ITogglSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        val jobDetail = mock<JobDetail>()
        doReturn(jobDetail).`when`(sut).createTotalProductiveTimeJobDetail(toggl, mongo)
        val trigger = mock<Trigger>()
        doReturn(trigger).`when`(sut).createDailyProductiveTimeTrigger()
        val res = ValidationResult(true, "")
        `when`(scheduler.schedule(jobDetail, trigger)).thenReturn(res)

        // Run method under test
        val actRes = sut.scheduleDailyTotalProductiveTimeGenerator(scheduler,
                toggl, mongo)

        // Verify
        verify(sut).createTotalProductiveTimeJobDetail(toggl, mongo)
        verify(sut).createDailyProductiveTimeTrigger()
        verify(scheduler).schedule(jobDetail, trigger)
        assertThat(actRes).isSameAs(res)
    }
    @Test
    fun createTotalProductiveTimeJobDetail() {
        // Prepare
        val sut = spy(App())
        val jd = mock<JobDataMap>()
        doReturn(jd).`when`(sut).createJobDataMap()
        val mongo = mock<IMongoSubsystem>()
        val toggl = mock<ITogglSubsystem>()
        val builder1 = mock<JobBuilder>()
        doReturn(builder1).`when`(sut).createTotalProductiveTimeJob()
        val jobDetail = mock<JobDetail>()
        `when`(builder1.build()).thenReturn(jobDetail)
        `when`(builder1.withIdentity(TotalProductiveTimeJob.JobName)).thenReturn(builder1)
        `when`(builder1.usingJobData(jd)).thenReturn(builder1)

        // Run method under test
        val actRes = sut.createTotalProductiveTimeJobDetail(toggl, mongo)

        // Verify
        verify(sut).createJobDataMap()
        verify(jd).put(ISchedulerSubsystem.JobDataMongo, mongo)
        verify(jd).put(ISchedulerSubsystem.JobDataToggl, toggl)
        verify(sut).createTotalProductiveTimeJob()
        verify(builder1).withIdentity(TotalProductiveTimeJob.JobName)
        verify(builder1).usingJobData(jd)
        verify(builder1).build()
        assertThat(actRes).isSameAs(jobDetail)
    }
    @Test
    fun createWorksStatsTrigger() {
        // Prepare
        val sut = spy(App())
        val builder = mock<TriggerBuilder<CronTrigger>>()
        doReturn(builder).`when`(sut).createNewTrigger()
        `when`(builder.withIdentity(WorksStatsJob.TriggerName))
                .thenReturn(builder)
        val schedule = mock<CronScheduleBuilder>()
        doReturn(schedule).`when`(sut).createWorksStatsSchedule()
        `when`(builder.withSchedule(schedule)).thenReturn(builder)
        val trigger = mock<CronTrigger>()
        `when`(builder.build()).thenReturn(trigger)

        val inOrder = inOrder(sut, builder)

        // Run method under test
        val actRes = sut.createWorksStatsTrigger()

        // Verify
        inOrder.verify(sut).createNewTrigger()
        inOrder.verify(builder).withIdentity(WorksStatsJob.TriggerName)
        inOrder.verify(sut).createWorksStatsSchedule()
        inOrder.verify(builder).withSchedule(schedule)
        inOrder.verify(builder).build()
        assertThat(actRes).isSameAs(trigger)
    }
    @Test
    fun createWorksStatsJobDetail() {
        // Prepare
        val sut = spy(App())
        val trello = mock<ITrelloSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        val jd = mock<JobDataMap>()
        doReturn(jd).`when`(sut).createJobDataMap()
        val builder = mock<JobBuilder>()
        doReturn(builder).`when`(sut).createWorksStatsJob()
        `when`(builder.withIdentity(WorksStatsJob.JobName)).thenReturn(builder)
        `when`(builder.usingJobData(jd)).thenReturn(builder)
        val res = mock<JobDetail>()
        `when`(builder.build()).thenReturn(res)

        val inOrder = inOrder(sut, trello, mongo, jd, builder)

        // Run method under test
        val actRes = sut.createWorksStatsJobDetail(trello, mongo)

        // Verify
        inOrder.verify(sut).createJobDataMap()
        inOrder.verify(jd).put(ISchedulerSubsystem.JobDataMongo, mongo)
        inOrder.verify(jd).put(ISchedulerSubsystem.JobDataTrello, trello)
        inOrder.verify(sut).createWorksStatsJob()
        inOrder.verify(builder).withIdentity(WorksStatsJob.JobName)
        inOrder.verify(builder).usingJobData(jd)
        inOrder.verify(builder).build()
        assertThat(actRes).isSameAs(res)
    }
    @Test
    fun scheduleWorksStatsJob() {
        // Prepare
        val sut = spy(App())
        val trello = mock<ITrelloSubsystem>()
        val mongo = mock<IMongoSubsystem>()
        val scheduler = mock<ISchedulerSubsystem>()
        val jobDetail = mock<JobDetail>()
        doReturn(jobDetail).`when`(sut)
                .createWorksStatsJobDetail(trello, mongo)
        val trigger = mock<Trigger>()
        doReturn(trigger).`when`(sut).createWorksStatsTrigger()
        val res = ValidationResult(true, "")
        `when`(scheduler.schedule(jobDetail, trigger)).thenReturn(res)

        // Run method under test
        val actRes = sut.scheduleWorksStatsJob(scheduler, trello, mongo)

        // Verify
        verify(sut).createWorksStatsJobDetail(trello, mongo)
        verify(sut).createWorksStatsTrigger()
        verify(scheduler).schedule(jobDetail, trigger)
        assertThat(actRes).isSameAs(res)
    }
    @Test
    fun runWstSchedulingError() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(App(logger))
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        doReturn(mongo).`when`(sut).createMongo()
        `when`(mongo.init()).thenReturn(ValidationResult(true, ""))
        val trello = mock<ITrelloSubsystem>()
        doReturn(trello).`when`(sut).createTrello()
        val toggl = mock<ITogglSubsystem>()
        doReturn(toggl).`when`(sut).createToggl()
        val scheduler = mock<ISchedulerSubsystem>()
        doReturn(scheduler).`when`(sut).createSchedulerSubsystem()
        `when`(scheduler.init()).thenReturn(ValidationResult(true, ""))
        doReturn(ValidationResult(true, "")).`when`(sut)
                .scheduleDailyTotalProductiveTimeGenerator(scheduler, toggl, mongo)
        val msg = "msg"
        val wstSchRes = ValidationResult(false, msg)
        doReturn(wstSchRes).`when`(sut).scheduleWorksStatsJob(scheduler, trello, mongo)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createTrello()
        verify(trello).init()
        verify(sut).scheduleWorksStatsJob(scheduler, trello, mongo)
        verify(logger).error("Works stats task can't be scheduled ('$msg').")
        verify(logger).error("Shutting down after a failed start.")
    }
}