package cc.altruix.is1.telegram

import org.apache.commons.lang3.StringUtils
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.bots.TelegramLongPollingBot

/**
 * Created by pisarenko on 10.02.2017.
 */
abstract class AbstractBot : TelegramLongPollingBot() {
    val subscribers = mutableSetOf<ITelegramCmdAutomaton>()

    open fun extractCmdName(msg: Message): String {
        val cmdTxt = msg.text
        if (StringUtils.isBlank(cmdTxt)) {
            return ""
        }
        val parts = cmdTxt.split(" ")
        if (parts.isEmpty()) {
            return ""
        }
        if (StringUtils.isBlank(parts[0])) {
            return ""
        }
        val cmdName = parts[0].trim().toLowerCase()
        return cmdName
    }
    open fun subscribe(automaton: ITelegramCmdAutomaton) {
        subscribers.add(automaton)
    }
    open fun unsubscribe(automaton: ITelegramCmdAutomaton) {
        subscribers.remove(automaton)
    }

    open fun automatonWaitingForResponse(): ITelegramCmdAutomaton? {
        return subscribers.filter { it.waitingForResponse() }.firstOrNull()
    }
}