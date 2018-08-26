package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.IParentAutomaton
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.UrlValidator
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 10.02.2017.
 */
open class WAITING_FOR_COMPANY_URL_Handler(
        val parent:IParentBp1AddCmdAutomaton,
        val tu:ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<Bp1AddCmdState>(
        parent
) {
    companion object {
        val CorrectUrlMessage = "Now we need to enter the contact data. Enter E for e-mail, or C for contact form."
        val IncorrectUrlMessage = "URL is incorrect. Please try again or enter /cancel to cancel the conversation."
    }
    override fun handleIncomingMessage(msg: Message) {
        if (tu.cancelCommand(msg)) {
            printMessage(ITelegramUtils.CancelMessage)
            parentAutomaton.goToStateIfPossible(Bp1AddCmdState.CANCELING)
            return
        }
        val url = extractUrl(msg)
        if (urlCorrect(url)) {
            printMessage(CorrectUrlMessage)
            parent.saveMainUrl(url)
            parentAutomaton.goToStateIfPossible(Bp1AddCmdState.WAITING_FOR_CONTACT_DATA_TYPE)
        } else {
            printMessage(IncorrectUrlMessage)
        }
    }

    open fun urlCorrect(url: String?): Boolean = UrlValidator.getInstance().isValid(url)

    open fun extractUrl(msg: Message): String = StringUtils.defaultIfBlank(msg.text, "").trim()
}