package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 10.02.2017.
 */
interface ITelegramCmdAutomaton {
    fun start()
    fun waitingForResponse():Boolean
    fun handleIncomingMessage(msg:Message)
    fun fire()
}