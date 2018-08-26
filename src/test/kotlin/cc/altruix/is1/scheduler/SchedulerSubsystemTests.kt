package cc.altruix.is1.scheduler

import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger
import org.slf4j.Logger

/**
 * Created by pisarenko on 25.04.2017.
 */
class SchedulerSubsystemTests {
    @Test
    fun initRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(SchedulerSubsystem(logger))
        doReturn(null).`when`(sut).createScheduler()

        // Run method under test
        val actRes = sut.init()

        // Verify
        verify(sut).createScheduler()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
        assertThat(sut.scheduler).isNull()
    }
    @Test
    fun initSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(SchedulerSubsystem(logger))
        val scheduler = mock<Scheduler>()
        doReturn(scheduler).`when`(sut).createScheduler()

        // Run method under test
        val actRes = sut.init()

        // Verify
        verify(sut).createScheduler()
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEqualTo("")
        assertThat(actRes.result).isNull()
        assertThat(sut.scheduler).isSameAs(scheduler)
    }
    @Test
    fun close() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(SchedulerSubsystem(logger))
        val scheduler = mock<Scheduler>()
        doReturn(scheduler).`when`(sut).createScheduler()

        sut.init()
        // Run method under test
        sut.close()

        // Verify
        verify(sut).createScheduler()
        verify(scheduler).shutdown(true)
    }
    @Test
    fun startSchedulerNotInitialized() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(SchedulerSubsystem(logger))

        // Run method under test
        val actRes = sut.start()

        // Verify
        verify(sut, never()).init()
        verify(sut, never()).createScheduler()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun startSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(SchedulerSubsystem(logger))
        val scheduler = mock<Scheduler>()
        doReturn(scheduler).`when`(sut).createScheduler()
        sut.init()

        // Run method under test
        val actRes = sut.start()

        // Verify
        verify(sut).createScheduler()
        verify(scheduler).start()
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNull()
    }
    @Test
    fun scheduleSchedulerNotInitialized() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(SchedulerSubsystem(logger))
        val jd = mock<JobDetail>()
        val trigger = mock<Trigger>()

        // Run method under test
        val actRes = sut.schedule(jd, trigger)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun scheduleSchedulingError() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(SchedulerSubsystem(logger))
        val scheduler = mock<Scheduler>()
        doReturn(scheduler).`when`(sut).createScheduler()
        val jd = mock<JobDetail>()
        val trigger = mock<Trigger>()
        val msg = "msg"
        val t = RuntimeException(msg)
        `when`(scheduler.scheduleJob(jd, trigger)).thenThrow(t)

        sut.init()

        // Run method under test
        val actRes = sut.schedule(jd, trigger)

        // Verify
        verify(scheduler).scheduleJob(jd, trigger)
        verify(logger).error("schedule", t)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(msg)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun scheduleSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(SchedulerSubsystem(logger))
        val scheduler = mock<Scheduler>()
        doReturn(scheduler).`when`(sut).createScheduler()
        val jd = mock<JobDetail>()
        val trigger = mock<Trigger>()

        sut.init()

        // Run method under test
        val actRes = sut.schedule(jd, trigger)

        // Verify
        verify(scheduler).scheduleJob(jd, trigger)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNull()
    }
}