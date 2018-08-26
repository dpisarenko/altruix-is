package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 10.02.2017.
 */
open class TelegramCmdAutomatonAdapter : ITelegramCmdAutomaton {
    override fun fire() {
    }

    override fun waitingForResponse(): Boolean = false

    override fun handleIncomingMessage(msg: Message) {
    }

    override fun start() {
    }
}