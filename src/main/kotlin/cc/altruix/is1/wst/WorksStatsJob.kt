package cc.altruix.is1.wst

import cc.altruix.is1.App
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.scheduler.ISchedulerSubsystem
import cc.altruix.is1.trello.ITrelloSubsystem
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by 1 on 30.04.2017.
 */
open class WorksStatsJob(
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : Job {
    companion object {
        val WorksStatsColl = "WorksStats"
        val JobName = "WorksStatsJob"
        val TriggerName = "WorksStatsTrigger"
    }
    override fun execute(ctx: JobExecutionContext?) {
        if (ctx == null) {
            logger.error("Null context")
            return
        }
        val jd = ctx.mergedJobDataMap
        if (jd == null) {
            logger.error("Null job data map")
            return
        }
        if(!(jd.containsKey(ISchedulerSubsystem.JobDataTrello) &&
                jd.containsKey(ISchedulerSubsystem.JobDataMongo))) {
            logger.error("Key '${ISchedulerSubsystem.JobDataTrello}' and/or '${ISchedulerSubsystem.JobDataMongo}' is missing in job data map")
            return
        }
        val trello = jd[ISchedulerSubsystem.JobDataTrello] as ITrelloSubsystem?
        val mongo = jd[ISchedulerSubsystem.JobDataMongo] as IMongoSubsystem?
        if ((trello == null) || (mongo == null)) {
            logger.error("Trello and/or Mongo subsystem is null in job data map")
            return
        }
        retrieveAndSaveData(trello, mongo)
    }

    open fun retrieveAndSaveData(
            trello: ITrelloSubsystem,
            mongo: IMongoSubsystem
    ) {
        val wsRes = trello.worksStatistics()
        val ws = wsRes.result
        if (!wsRes.success || (ws == null)) {
            logger.error("Can't retrieve Trello data ('${wsRes.error}').")
            return
        }
        val insRes = mongo.insert(ws, WorksStatsColl)
        if (!insRes.success) {
            logger.error("Could not write Trello data into Mongo ('${insRes.error}').")
        }
    }
}