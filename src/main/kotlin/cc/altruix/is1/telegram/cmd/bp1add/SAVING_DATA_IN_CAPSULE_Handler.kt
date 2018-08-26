package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Created by pisarenko on 15.02.2017.
 */
open class SAVING_DATA_IN_CAPSULE_Handler(
        val parent: IParentBp1AddCmdAutomaton,
        val capsule: ICapsuleCrmSubsystem,
        val tu: ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<Bp1AddCmdState>(
        parent
) {
    var today = createZonedDateTime(now())
    var companiesEnteredToday = 0

    override fun fire() {
        val data = parent.companyData()
        val res = capsule.createCompany(data)
        if (res.success) {
            updateDailyStatistics()
            printMessage("Company successfully created. Total today: $companiesEnteredToday")
        } else {
            printMessage("An error occured ('${res.error}'). Please tell to Dmitri Pisarenko.")
        }
        parent.goToStateIfPossible(Bp1AddCmdState.END)
    }

    open fun updateDailyStatistics() {
        val now = createZonedDateTime(now())
        if (sameDay(now, today)) {
            companiesEnteredToday += 1
        } else {
            today = now
            companiesEnteredToday = 1
        }
    }

    open fun now() = Date()

    open fun sameDay(now: ZonedDateTime, today: ZonedDateTime): Boolean =
            (now.dayOfMonth == today.dayOfMonth) &&
                    (now.month == today.month) &&
                    (now.year == today.year)

    open fun createZonedDateTime(date: Date) =
            ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Moscow"))
}