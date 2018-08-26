package cc.altruix.is1

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.scheduler.ISchedulerSubsystem
import cc.altruix.is1.toggl.ITogglSubsystem
import org.junit.Test
import cc.altruix.mock
import org.mockito.Mockito.*
import org.slf4j.Logger
import java.io.PrintStream

/**
 * Created by pisarenko on 02.02.2017.
 */
class ShutdownHookTests {
    @Test
    fun run() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val logger = mock<Logger>()
        val mongo = mock<IMongoSubsystem>()
        val toggl = mock<ITogglSubsystem>()
        val scheduler = mock<ISchedulerSubsystem>()
        val sut = spy(ShutdownHook(jena, capsule, mongo,
                toggl,
                scheduler, logger))
        val inOrder = inOrder(logger, jena, capsule, mongo, toggl, scheduler)

        // Run method under test
        sut.run()

        // Verify
        inOrder.verify(logger).info("Preparing to shut down Altruix IS")
        inOrder.verify(jena).close()
        inOrder.verify(capsule).close()
        inOrder.verify(mongo).close()
        inOrder.verify(toggl).close()
        inOrder.verify(scheduler).close()
        inOrder.verify(logger).info("Preparation to shut down completed")
    }
}