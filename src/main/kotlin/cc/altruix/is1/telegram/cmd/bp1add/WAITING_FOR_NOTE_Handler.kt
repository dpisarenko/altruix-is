package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import org.apache.commons.lang3.StringUtils
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 10.02.2017.
 */
open class WAITING_FOR_NOTE_Handler(
        val parent: IParentBp1AddCmdAutomaton,
        val tu: ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<Bp1AddCmdState>(parent) {
    override fun handleIncomingMessage(msg: Message) {
        if (tu.cancelCommand(msg)) {
            printMessage(ITelegramUtils.CancelMessage)
            parentAutomaton.goToStateIfPossible(Bp1AddCmdState.CANCELING)
            return
        }
        val msgText = msg.text
        if (StringUtils.isNotBlank(msgText)) {
            parent.saveNote(msgText)
        }
        parent.saveAgent(msg.from)
        parent.unsubscribe()
        parentAutomaton.goToStateIfPossible(Bp1AddCmdState.SAVING_DATA_IN_CAPSULE)
    }
}