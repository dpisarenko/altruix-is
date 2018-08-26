package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.send.SendPhoto
import java.io.File

/**
 * Created by pisarenko on 31.01.2017.
 */
interface IResponsiveBot {
    fun sendTelegramMessage(msg: SendMessage)
    fun sendBroadcast(msg:String)
    fun subscribe(automaton:ITelegramCmdAutomaton)
    fun unsubscribe(automaton:ITelegramCmdAutomaton)
    fun readFileContents(fileId: String): File?
    fun sendImage(msg: SendPhoto)
}