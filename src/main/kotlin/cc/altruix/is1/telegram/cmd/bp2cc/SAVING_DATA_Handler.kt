package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.validation.ValidationResult

/**
 * Created by 1 on 04.03.2017.
 */
open class SAVING_DATA_Handler(
        val parent: IParentBp2CcCmdAutomaton,
        val jena: IJenaSubsystem,
        val capsule: ICapsuleCrmSubsystem
) : AutomatonMessageHandler<Bp2CcCmdState>(
        parent
) {
    companion object {
        val NoCompanyDataMessage = "Internal error."
        val CrmInteractionFaultMessage = "The note you entered could not be sent to the CRM system. Please" +
                ITelegramUtils.LineSeparator +
                "1) Save the note (incl. the web site of the company) somehwere. " +
                ITelegramUtils.LineSeparator +
                "2) Send those notes to him." +
                ITelegramUtils.LineSeparator +
                "3) Ask him to attach them to the company page in the CRM system manually."
        val PersonaNotFound = "Persona not found. Please tell it to Dmitri Pisarenko."
        val UnknownPersona = "?"
    }
    override fun fire() {
        val batchId = parent.batchId()
        val compData = parent.getCompanyData()
        if (compData == null) {
            printMessage(NoCompanyDataMessage)
            parent.goToStateIfPossible(Bp2CcCmdState.CANCELING)
            return
        }
        val compId = compData.companyId
        val contactTextNote = parent.getContactTextAndNote() ?: ""
        val personaRes = jena.fetchPersona(batchId)
        var persona = personaRes.result
        if (!personaRes.success || (persona == null)) {
            persona = UnknownPersona
            printMessage(PersonaNotFound)
        }
        val crmRes: ValidationResult = capsule.attachContactResult(
                compId,
                persona,
                contactTextNote
        )
        if (!crmRes.success) {
            printMessage(CrmInteractionFaultMessage)
        }
        val compRemRes = jena.removeCompanyFromBatch(batchId, compId)
        if (!compRemRes.success) {
            printMessage("Batch update problem ('${compRemRes.error}'). Please contact Dmitri Pisarenko.")
        }
        printMessage(composeOperationCompletedMessage(compId))
        parent.goToStateIfPossible(Bp2CcCmdState.END)
    }

    open fun composeOperationCompletedMessage(compId: String): String =
            "Die Sache mit Firma '$compId' hob' I jetzt umebogn."

}