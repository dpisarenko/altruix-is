package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.ITelegramUtils

/**
 * Created by pisarenko on 14.02.2017.
 */
open class Bp1ValidatedTextInputHandlerForTesting(
        state: Bp1AddCmdState,
        parent: IParentBp1AddCmdAutomaton,
        errorMsg: String,
        tu: ITelegramUtils) : Bp1ValidatedTextInputHandler(
        parent,
        errorMsg,
        tu) {

    override open fun inputValid(url: String?): Boolean = false

    override open fun saveData(url: String) {
    }
}