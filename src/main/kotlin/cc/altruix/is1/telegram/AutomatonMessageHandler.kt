package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 10.02.2017.
 */
open class AutomatonMessageHandler<C>(
        val parentAutomaton: IParentAutomaton<C>
) : TelegramCmdAutomatonAdapter() {
    override fun handleIncomingMessage(msg: Message) {

    }

    open fun printMessage(msg: String) {
        parentAutomaton.printMessage(msg)
    }
}