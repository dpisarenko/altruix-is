package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils

/**
 * Created by 1 on 04.03.2017.
 */
open class GETTING_NEXT_COMPANY_DATA_Handler(
        val parent: IParentBp2CcCmdAutomaton,
        val jena: IJenaSubsystem,
        val capsule: ICapsuleCrmSubsystem
) : AutomatonMessageHandler<Bp2CcCmdState>(
                parent
) {
    override fun fire() {
        val batchId = parent.batchId()
        val companyIdRes = jena.fetchNextCompanyIdToContact(batchId)
        val compId = companyIdRes.result
        if (!companyIdRes.success || (compId == null)) {
            printMessage("Couldn't find next company to process ('${companyIdRes.error}')")
            parent.goToStateIfPossible(Bp2CcCmdState.CANCELING)
            return
        }
        val compDataRes = capsule.fetchBp2CompanyData(compId)
        val compData = compDataRes.result
        if (!compDataRes.success || (compData == null)) {
            printMessage("CRM interaction fault ('${compDataRes.error}')")
            parent.goToStateIfPossible(Bp2CcCmdState.CANCELING)
            return
        }
        parent.setCompanyData(compData)
        printCompanyData(compData)
        parent.goToStateIfPossible(Bp2CcCmdState.WAITING_FOR_CONTACT_ATTEMPT_RESULT)
    }

    open fun printCompanyData(companyData: Bp2CompanyData) =
            printMessage(composeCompanyDataMessage(companyData))

    open fun composeCompanyDataMessage(companyData: Bp2CompanyData): String {
        val sb = StringBuilder()
        sb.append("Now please contact the company via one of the following venues.")
        sb.append(ITelegramUtils.LineSeparator)
        if (companyData.emails.isNotEmpty()) {
            sb.append(ITelegramUtils.LineSeparator)
            sb.append("Emails:")
            sb.append(ITelegramUtils.LineSeparator)
            sb.append(ITelegramUtils.LineSeparator)
            var i = 1
            for (email in companyData.emails) {
                sb.append(i)
                sb.append(") $email")
                sb.append(ITelegramUtils.LineSeparator)
                i++
            }
        }
        if (companyData.webSites.isNotEmpty()) {
            sb.append(ITelegramUtils.LineSeparator)
            sb.append("Web pages:")
            sb.append(ITelegramUtils.LineSeparator)
            sb.append(ITelegramUtils.LineSeparator)
            var i = 1
            for (site in companyData.webSites) {
                sb.append(i)
                sb.append(") $site")
                sb.append(ITelegramUtils.LineSeparator)
                i++
            }
        }
        sb.append(ITelegramUtils.LineSeparator)
        sb.append("Please contact the company now. ")
        sb.append("Enter 'y' if the attempt was successful. ")
        sb.append("If you couldn't contact the company, enter 'n'.")

        return sb.toString()
    }
}