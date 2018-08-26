package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.AbstractCANCELING_Handler
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by pisarenko on 15.02.2017.
 */
open class CANCELING_Handler(
        parent:IParentBp1AddCmdAutomaton
) : AbstractCANCELING_Handler<Bp1AddCmdState>(
        Bp1AddCmdState.END,
        parent
) {

}