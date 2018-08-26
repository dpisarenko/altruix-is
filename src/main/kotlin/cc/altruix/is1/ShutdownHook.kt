package cc.altruix.is1

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.scheduler.ISchedulerSubsystem
import cc.altruix.is1.toggl.ITogglSubsystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.PrintStream

/**
 * Created by pisarenko on 02.02.2017.
 */
open class ShutdownHook(
        val jena: IJenaSubsystem,
        val capsule: ICapsuleCrmSubsystem,
        val mongo:IMongoSubsystem,
        val toggl: ITogglSubsystem,
        val scheduler: ISchedulerSubsystem,
        val logger: Logger = LoggerFactory.getLogger("cc.altruix.is1")
) : Thread() {
    override fun run() {
        logger.info("Preparing to shut down Altruix IS")
        jena.close()
        capsule.close()
        mongo.close()
        toggl.close()
        scheduler.close()
        logger.info("Preparation to shut down completed")
    }
}