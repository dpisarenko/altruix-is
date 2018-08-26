package cc.altruix.is1

import cc.altruix.is1.capsulecrm.CapsuleCrmSubsystem
import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.jena.JenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.mongo.AltruixIs1MongoSubsystem
import cc.altruix.is1.scheduler.ISchedulerSubsystem
import cc.altruix.is1.scheduler.SchedulerSubsystem
import cc.altruix.is1.telegram.TelegramSubsystem
import cc.altruix.is1.toggl.ITogglSubsystem
import cc.altruix.is1.toggl.TogglSubsystem
import cc.altruix.is1.tpt.TotalProductiveTimeJob
import cc.altruix.is1.trello.ITrelloSubsystem
import cc.altruix.is1.trello.TrelloSubsystem
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.is1.wst.WorksStatsJob
import org.quartz.CronScheduleBuilder.dailyAtHourAndMinute
import org.quartz.JobBuilder.newJob
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Trigger
import org.quartz.TriggerBuilder.newTrigger
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.telegram.telegrambots.TelegramBotsApi

open class App(val logger:Logger = LoggerFactory.getLogger(LoggerName)) {
	companion object {
		val Version = "1.43"
        val LoggerName = "cc.altruix.is1"
	}
	open fun run() {
		logger.info("Starting Altruix IS v. ${Version}")
		val mongo = createMongo()
		val mongoStatus = mongo.init()
		if (!mongoStatus.success) {
			logger.error("Mongo hat an Patsch'n ('${mongoStatus.error}').")
			logger.error("Shutting down after a failed start.")
			return
		}
		val trello = createTrello()
		trello.init()
		val toggl = createToggl()
		toggl.init()
		val scheduler = createSchedulerSubsystem()
		val schInitRes = scheduler.init()
		if (!schInitRes.success) {
			logger.error("Scheduler can't be initialized ('${schInitRes.error}').")
			logger.error("Shutting down after a failed start.")
			return
		}
		val tptSchRes = scheduleDailyTotalProductiveTimeGenerator(scheduler, toggl, mongo)
		if (!tptSchRes.success) {
			logger.error("Daily total productive time task can't be scheduled ('${tptSchRes.error}').")
			logger.error("Shutting down after a failed start.")
			return
		}
		val wstSchRes = scheduleWorksStatsJob(scheduler, trello, mongo)
		if (!wstSchRes.success) {
			logger.error("Works stats task can't be scheduled ('${wstSchRes.error}').")
			logger.error("Shutting down after a failed start.")
			return
		}
		val schStartRes = scheduler.start()
		if (!schStartRes.success) {
			logger.error("Scheduler can't be started ('${schStartRes.error}').")
			logger.error("Shutting down after a failed start.")
			return
		}
		val jena = createJena()
		//jena.init()
		val botApi = createTelegramBotsApi()
		val capsule = createCapsuleCrmSubsystem()
		getRuntime().addShutdownHook(createShutdownHook(jena, capsule, mongo, toggl, scheduler))
		// capsule.init()
		val telegram = createTelegramSubsystem(botApi, capsule, jena, mongo)
		telegram.init()
	}

	open fun scheduleWorksStatsJob(
			scheduler: ISchedulerSubsystem,
			trello: ITrelloSubsystem,
			mongo: IMongoSubsystem
	): ValidationResult {
		val jobDetail = createWorksStatsJobDetail(trello, mongo)
		val trigger = createWorksStatsTrigger()
		return scheduler.schedule(jobDetail, trigger)
	}
	open fun createWorksStatsJobDetail(
			trello: ITrelloSubsystem,
			mongo: IMongoSubsystem
	):JobDetail {
		val jd = createJobDataMap()
		jd[ISchedulerSubsystem.JobDataMongo] = mongo
		jd[ISchedulerSubsystem.JobDataTrello] = trello
		val res = createWorksStatsJob()
				.withIdentity(WorksStatsJob.JobName)
				.usingJobData(jd)
				.build()
		return res
	}

	open fun createWorksStatsTrigger(): Trigger =
			createNewTrigger()
					.withIdentity(WorksStatsJob.TriggerName)
					.withSchedule(createWorksStatsSchedule())
					.build()

	open fun createWorksStatsSchedule() = dailyAtHourAndMinute(0, 10)

	open fun createWorksStatsJob() = newJob(WorksStatsJob::class.java)

	open fun createTrello(): ITrelloSubsystem = TrelloSubsystem()

	open fun createSchedulerSubsystem():ISchedulerSubsystem = SchedulerSubsystem()

	open fun createToggl(): ITogglSubsystem = TogglSubsystem()

	open fun scheduleDailyTotalProductiveTimeGenerator(
			scheduler:ISchedulerSubsystem,
			toggl: ITogglSubsystem,
			mongo: IMongoSubsystem): ValidationResult {
		val jobDetail = createTotalProductiveTimeJobDetail(toggl, mongo)
		val trigger = createDailyProductiveTimeTrigger()
		return scheduler.schedule(jobDetail, trigger)
	}

	open fun createTotalProductiveTimeJobDetail(
			toggl: ITogglSubsystem,
			mongo: IMongoSubsystem
	):JobDetail {
		val jd = createJobDataMap()
		jd[ISchedulerSubsystem.JobDataMongo] = mongo
		jd[ISchedulerSubsystem.JobDataToggl] = toggl
		val res = createTotalProductiveTimeJob()
				.withIdentity(TotalProductiveTimeJob.JobName)
				.usingJobData(jd)
				.build()
		return res
	}

	open fun createJobDataMap() = JobDataMap()

	open fun createTotalProductiveTimeJob() = newJob(TotalProductiveTimeJob::class.java)

	open fun createDailyProductiveTimeTrigger(): Trigger =
			newTrigger()
					.withIdentity(TotalProductiveTimeJob.TriggerName)
					.withSchedule(dailyAtHourAndMinute(0, 5))
					.build()

	open fun createNewTrigger() = newTrigger()

	open fun createMongo(): IAltruixIs1MongoSubsystem {
		return AltruixIs1MongoSubsystem()
	}

	open fun createJena(): IJenaSubsystem = JenaSubsystem()

	open fun createShutdownHook(
			jena: IJenaSubsystem,
			capsule: ICapsuleCrmSubsystem,
			mongo:IMongoSubsystem,
			toggl: ITogglSubsystem,
			scheduler: ISchedulerSubsystem):Thread =
			ShutdownHook(jena, capsule, mongo, toggl, scheduler)

	open fun getRuntime() = Runtime.getRuntime()

	open fun createTelegramBotsApi() = TelegramBotsApi()

	open fun createCapsuleCrmSubsystem():ICapsuleCrmSubsystem = CapsuleCrmSubsystem()

	open fun createTelegramSubsystem(
			botApi:TelegramBotsApi,
			capsule: ICapsuleCrmSubsystem,
			jena:IJenaSubsystem,
			mongo: IAltruixIs1MongoSubsystem
	) = TelegramSubsystem(botApi, capsule, jena, mongo)
}

fun main(args : Array<String>) {
	val app = App()
	app.run()
}