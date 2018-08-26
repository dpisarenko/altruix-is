package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Created by pisarenko on 17.02.2017.
 */
class SAVING_DATA_IN_CAPSULE_HandlerTests {
    @Test
    fun fireSunnyDay() {
        fireTestLogic(true, "Company successfully created. Total today: 0", "")
    }
    @Test
    fun fireRainyDay() {
        val error = "error"
        fireTestLogic(false, "An error occured ('$error'). Please tell to Dmitri Pisarenko.", error)
    }
    @Test
    fun ctorInitializesDailyStats() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val tu = mock<ITelegramUtils>()

        // Run method under test
        val sut = spy(SAVING_DATA_IN_CAPSULE_Handler(parent, capsule, tu))

        // Verify
        assertThat(sut.companiesEnteredToday).isZero
    }
    @Test
    fun updateDailyStatisticsSameDay() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(SAVING_DATA_IN_CAPSULE_Handler(parent, capsule, tu))
        val now1 = mock<Date>()
        val now2 = ZonedDateTime.ofInstant(Date().toInstant(), ZoneId.of("Europe/Moscow"))
        doReturn(now1).`when`(sut).now()
        doReturn(now2).`when`(sut).createZonedDateTime(now1)
        val today = sut.today
        doReturn(true).`when`(sut).sameDay(now2, today)

        // Run method under test
        assertThat(sut.companiesEnteredToday).isZero
        assertThat(sut.today).isEqualTo(today)

        sut.updateDailyStatistics()

        assertThat(sut.companiesEnteredToday).isEqualTo(1)
        assertThat(sut.today).isEqualTo(today)

        // Verify
        verify(sut).createZonedDateTime(now1)
        verify(sut).sameDay(now2, sut.today)
    }
    @Test
    fun updateDailyStatisticsNextDay() {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(SAVING_DATA_IN_CAPSULE_Handler(parent, capsule, tu))
        val now1 = mock<Date>()
        val now2 = ZonedDateTime.ofInstant(Date().toInstant(), ZoneId.of("Europe/Moscow"))
        doReturn(now1).`when`(sut).now()
        doReturn(now2).`when`(sut).createZonedDateTime(now1)
        val today = sut.today
        doReturn(false).`when`(sut).sameDay(now2, today)

        // Run method under test
        sut.companiesEnteredToday = 10
        assertThat(sut.companiesEnteredToday).isEqualTo(10)
        assertThat(sut.today).isEqualTo(today)

        sut.updateDailyStatistics()

        assertThat(sut.companiesEnteredToday).isEqualTo(1)
        assertThat(sut.today).isEqualTo(now2)

        // Verify
        verify(sut).createZonedDateTime(now1)
        verify(sut).sameDay(now2, today)
    }
    @Test
    fun sameDay() {
        sameDayTestLogic(
                ZonedDateTime.of(
                        2017, 3, 6, 15, 55, 39, 0, ZoneId.of("Europe/Moscow")
                ),
                ZonedDateTime.of(
                        2017, 3, 6, 15, 55, 40, 0, ZoneId.of("Europe/Moscow")
                ),
                true
        )

    }

    private fun sameDayTestLogic(date1: ZonedDateTime, date2: ZonedDateTime, expRes: Boolean) {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(SAVING_DATA_IN_CAPSULE_Handler(parent, capsule, tu))

        // Run method under test
        val actRes = sut.sameDay(date1, date2)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }


    private fun fireTestLogic(success: Boolean, expMessage: String, error: String) {
        // Prepare
        val parent = mock<IParentBp1AddCmdAutomaton>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(SAVING_DATA_IN_CAPSULE_Handler(parent, capsule, tu))
        val data = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.CONTACT_FORM,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        `when`(parent.companyData()).thenReturn(data)
        val res = ValidationResult(success, error)
        `when`(capsule.createCompany(data)).thenReturn(res)
        doNothing().`when`(sut).printMessage(expMessage)
        doNothing().`when`(sut).updateDailyStatistics()
        val inOrder = inOrder(parent, capsule, tu, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).companyData()
        inOrder.verify(capsule).createCompany(data)
        if (success) {
            inOrder.verify(sut).updateDailyStatistics()
        }
        inOrder.verify(sut).printMessage(expMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp1AddCmdState.END)
    }
}