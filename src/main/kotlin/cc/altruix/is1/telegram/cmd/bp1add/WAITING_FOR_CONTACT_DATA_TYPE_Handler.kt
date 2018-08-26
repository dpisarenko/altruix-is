package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 10.02.2017.
 */
open class WAITING_FOR_CONTACT_DATA_TYPE_Handler(
        val customParent: IParentBp1AddCmdAutomaton,
        val tu:ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<Bp1AddCmdState>(
        customParent
) {
    companion object {
        val WrongMessage = "Wrong message. Enter E for e-mail, or C for contact form. Please try again or enter /cancel"
    }
    override fun handleIncomingMessage(msg: Message) {
        if (tu.cancelCommand(msg)) {
            printMessage(ITelegramUtils.CancelMessage)
            parentAutomaton.goToStateIfPossible(Bp1AddCmdState.CANCELING)
            return
        }
        val msgText = msg.text
        val type = extractContactType(msgText)
        if (type == ContactDataType.UNKNOWN) {
            printMessage(WrongMessage)
            return
        }
        customParent.saveContactDataType(type)
        val (contactMechanism, targetState) = determineContactTextAndTargetState(type)
        printMessage("Now please enter ${contactMechanism} of the company.")
        parentAutomaton.goToStateIfPossible(targetState)
    }

    open fun determineContactTextAndTargetState(type:ContactDataType):Pair<String,Bp1AddCmdState> {
        val contactMechanism:String
        val targetState:Bp1AddCmdState
        when (type) {
            ContactDataType.EMAIL -> {
                contactMechanism = "e-mail"
                targetState = Bp1AddCmdState.WAITING_FOR_EMAIL
            }
            ContactDataType.CONTACT_FORM ->
            {
                contactMechanism = "URL of the contact form"
                targetState = Bp1AddCmdState.WAITING_FOR_CONTACT_FORM_URL
            }
            ContactDataType.UNKNOWN ->
            {
                contactMechanism = "?"
                targetState = Bp1AddCmdState.CANCELING
            }
        }
        return Pair(contactMechanism, targetState)
    }

    open fun extractContactType(msgText: String?): ContactDataType {
        if (msgText == null) {
            return ContactDataType.UNKNOWN
        }
        val res:ContactDataType
        when (msgText.trim().toLowerCase()) {
            "e" -> res = ContactDataType.EMAIL
            "c" -> res = ContactDataType.CONTACT_FORM
            else -> res = ContactDataType.UNKNOWN
        }
        return res
    }
}