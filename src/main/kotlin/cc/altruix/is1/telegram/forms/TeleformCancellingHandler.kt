package cc.altruix.is1.telegram.forms

import cc.altruix.is1.telegram.AbstractCANCELING_Handler
import cc.altruix.is1.telegram.IParentAutomaton
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by pisarenko on 06.04.2017.
 */
open class TeleformCancellingHandler(
        endState: TeleformElementState,
        parent : IParentAutomaton<TeleformElementState>,
        tu: ITelegramUtils = TelegramUtils()) : AbstractCANCELING_Handler<TeleformElementState>(
        endState,
        parent,
        tu) {
}