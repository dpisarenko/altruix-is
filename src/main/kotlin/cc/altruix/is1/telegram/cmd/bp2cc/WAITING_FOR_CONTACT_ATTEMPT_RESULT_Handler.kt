package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by 1 on 04.03.2017.
 */
open class WAITING_FOR_CONTACT_ATTEMPT_RESULT_Handler(
        val parent: IParentBp2CcCmdAutomaton,
        val tu: ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<Bp2CcCmdState>(
        parent
) {
    companion object {
        val SuccessMessage = "Now please enter the text you sent to the company " +
                "as well as a note, incl. whether you sent it via e-mail or " +
                "contact form."
    }
    override fun handleIncomingMessage(msg: Message) {
        if (tu.cancelCommand(msg)) {
            printMessage(ITelegramUtils.CancelMessage)
            parentAutomaton.goToStateIfPossible(Bp2CcCmdState.CANCELING)
            return
        }
        val txt = msg.text.toLowerCase()
        if ("y".equals(txt)) {
            printMessage(SuccessMessage)
            parentAutomaton.goToStateIfPossible(Bp2CcCmdState.WAITING_FOR_CONTACT_TEXT_AND_NOTE)
        } else {
            parentAutomaton.goToStateIfPossible(Bp2CcCmdState.CANCELING)
        }
    }
}