package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import org.apache.commons.lang3.StringUtils
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by 1 on 04.03.2017.
 */
open class WAITING_FOR_CONTACT_TEXT_AND_NOTE_Handler(
        val parent: IParentBp2CcCmdAutomaton,
        val tu: ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<Bp2CcCmdState>(
        parent
) {
    companion object {
        val EmptyMessageErrorText = "Enter what text you sent to the company and how (web site, e-mail)!"
    }
    override fun handleIncomingMessage(msg: Message) {
        if (tu.cancelCommand(msg)) {
            printMessage(ITelegramUtils.CancelMessage)
            parentAutomaton.goToStateIfPossible(Bp2CcCmdState.CANCELING)
            return
        }
        val txt = msg.text.trim()
        if (StringUtils.isEmpty(txt)) {
            printMessage(EmptyMessageErrorText)
            return
        }
        parent.setContactTextAndNote(txt)
        parentAutomaton.goToStateIfPossible(Bp2CcCmdState.SAVING_DATA)
    }
}