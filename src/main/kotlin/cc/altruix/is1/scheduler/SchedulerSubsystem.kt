package cc.altruix.is1.scheduler

import cc.altruix.is1.App
import cc.altruix.is1.validation.ValidationResult
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pisarenko on 24.04.2017.
 */
open class SchedulerSubsystem(
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : ISchedulerSubsystem {
    var scheduler:Scheduler? = null
    override fun init(): ValidationResult {
        scheduler = createScheduler()
        if (scheduler != null) {
            return ValidationResult(true, "")
        }
        return ValidationResult(false, "Internal error")
    }

    open fun createScheduler():Scheduler? = StdSchedulerFactory.getDefaultScheduler()

    override fun close() {
        scheduler?.shutdown(true)
    }
    override fun start():ValidationResult {
        val schd = scheduler
        if (schd == null) {
            return ValidationResult(false, "Internal error")
        }
        schd.start()
        return ValidationResult(true, "")
    }
    override fun schedule(job: JobDetail, trigger: Trigger) : ValidationResult {
        val sch = scheduler
        if (sch == null) {
            return ValidationResult(false, "Internal error")
        }
        try {
            sch.scheduleJob(job, trigger)
            return ValidationResult(true, "")
        } catch (t:Throwable) {
            logger.error("schedule", t)
            return ValidationResult(false, t.message ?: "")
        }
    }
}