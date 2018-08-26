package cc.altruix.is1.tpt

import cc.altruix.is1.App
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.scheduler.ISchedulerSubsystem
import cc.altruix.is1.toggl.ITogglSubsystem
import org.apache.commons.lang3.time.DateUtils
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by pisarenko on 24.04.2017.
 */
open class TotalProductiveTimeJob(
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : Job {
    companion object {
        val TotalProductiveTimeColl = "TotalProductiveTime"
        val StartTimeColumn = "startTime"
        val EndTimeColumn = "endTime"
        val TotalColumn = "total"
        val WritingColumn = "writing"
        val EditingColumn = "editing"
        val MarketingColumn = "marketing"
        val ReadingFictionColumn = "readingFiction"
        val ReadingNonFictionColumn = "readingNonFiction"
        val ScreenWritingMarketing = "screenwritingMarketing"
        val SvWorldBuilding = "svWorldBuilding"
        val JobName = "TotalProductiveTimeJob"
        val TriggerName = "TotalProductiveTimeTrigger"
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
        if(!(jd.containsKey(ISchedulerSubsystem.JobDataToggl) &&
                jd.containsKey(ISchedulerSubsystem.JobDataMongo))) {
            logger.error("Key '${ISchedulerSubsystem.JobDataToggl}' and/or '${ISchedulerSubsystem.JobDataMongo}' is missing in job data map")
            return
        }
        val toggl = jd[ISchedulerSubsystem.JobDataToggl] as ITogglSubsystem?
        val mongo = jd[ISchedulerSubsystem.JobDataMongo] as IMongoSubsystem?
        if ((toggl == null) || (mongo == null)) {
            logger.error("Toggl and/or Mongo subsystem is null in job data map")
            return
        }
        retrieveAndSaveData(toggl, mongo)
    }

    open fun retrieveAndSaveData(toggl: ITogglSubsystem, mongo: IMongoSubsystem) {
        val now = now()
        val yesterday = yesterday(now)
        val tptRes = toggl.totalProductiveTime(yesterday)
        val tpt = tptRes.result
        if (!tptRes.success || (tpt == null)) {
            logger.error("Data could not be retrieved from Toggl ('${tptRes.error}').")
            return
        }
        val data = composeDataToInsert(tpt)
        mongo.insert(data, TotalProductiveTimeColl)
    }

    open fun composeDataToInsert(tpt: TotalProductiveTime): HashMap<String, Any> =
            hashMapOf<String, Any>(
                StartTimeColumn to tpt.startTime,
                EndTimeColumn to tpt.endTime,
                TotalColumn to tpt.total,
                WritingColumn to tpt.writing,
                EditingColumn to tpt.editing,
                MarketingColumn to tpt.marketing,
                ReadingFictionColumn to tpt.readingFiction,
                ReadingNonFictionColumn to tpt.readingNonFiction,
                ScreenWritingMarketing to tpt.screenWritingMarketing,
                SvWorldBuilding to tpt.svWorldBuilding
            )

    open fun yesterday(now: Date) = DateUtils.addDays(now, -1)
    open fun now(): Date = Date()
}