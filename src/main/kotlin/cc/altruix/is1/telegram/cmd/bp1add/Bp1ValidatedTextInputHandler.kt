package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 14.02.2017.
 */
abstract class Bp1ValidatedTextInputHandler(
        parent: IParentBp1AddCmdAutomaton,
        val errorMsg: String,
        val tu: ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<Bp1AddCmdState>(
        parent
) {
    override fun handleIncomingMessage(msg: Message) {
        if (tu.cancelCommand(msg)) {
            printMessage(ITelegramUtils.CancelMessage)
            parentAutomaton.goToStateIfPossible(Bp1AddCmdState.CANCELING)
            return
        }
        val msgTxt = msg.text
        if (!inputValid(msgTxt)) {
            printMessage(errorMsg)
            return
        }
        saveData(msgTxt)
        parentAutomaton.goToStateIfPossible(Bp1AddCmdState.WAITING_FOR_NOTE)
    }
    abstract fun inputValid(txt:String?):Boolean
    abstract fun saveData(txt:String)
}