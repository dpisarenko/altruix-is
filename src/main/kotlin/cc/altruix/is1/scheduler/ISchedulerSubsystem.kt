package cc.altruix.is1.scheduler

import cc.altruix.is1.validation.ValidationResult
import org.quartz.JobDetail
import org.quartz.Trigger

/**
 * Created by pisarenko on 24.04.2017.
 */
interface ISchedulerSubsystem {
    companion object {
        val JobDataMongo = "Mongo"
        val JobDataToggl = "Toggl"
        val JobDataTrello = "Trello"
    }
    fun init():ValidationResult
    fun schedule(job: JobDetail, trigger: Trigger): ValidationResult
    fun start():ValidationResult
    fun close()
}