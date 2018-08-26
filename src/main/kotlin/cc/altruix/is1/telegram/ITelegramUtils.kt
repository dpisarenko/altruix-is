package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 02.02.2017.
 */
interface ITelegramUtils {
    companion object {
        val OneParameterOnlyAllowed = "1 parameter only allowed"
        val NoParametersAllowed = "No parameters allowed"
        val LineSeparator = "\n"
        val CancelMessage = "Cancelling conversation..."
        val CancelCommand = "/cancel"
        val CancelCommandAlias = "/kusch"
        val CanceledMessage = "Canceled."
    }
    fun createErrorResponse(errorMsg: String, chatId: Long): SendMessage
    fun displayError(errorMsg: String, chatId: Long, bot: IResponsiveBot)
    fun sendTextMessage(msgTxt: String, chatId: Long, bot: IResponsiveBot)
    fun createSendTextMessage(msgTxt: String, chatId: Long): SendMessage
    fun extractArgs(text: String, cmdName: String):String
    fun moreThanOneParameter(args: String): Boolean
    fun cancelCommand(msg: Message): Boolean;
}