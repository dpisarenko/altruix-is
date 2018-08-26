package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 02.02.2017.
 */
open class TelegramUtils : ITelegramUtils {
    override fun createErrorResponse(errorMsg: String, chatId: Long) =
            createSendTextMessage("An error occured ('${errorMsg}')", chatId)

    override fun displayError(errorMsg: String, chatId: Long, bot: IResponsiveBot) {
        val sendMsg:SendMessage = createErrorResponse(errorMsg, chatId)
        bot.sendTelegramMessage(sendMsg)
    }
    override fun sendTextMessage(msgTxt: String, chatId: Long, bot: IResponsiveBot) {
        val msg = createSendTextMessage(msgTxt, chatId)
        bot.sendTelegramMessage(msg)
    }

    override fun createSendTextMessage(msgTxt: String, chatId: Long): SendMessage {
        val msg = createSendMessage()
        msg.text = msgTxt
        msg.chatId = chatId.toString()
        msg.enableMarkdown(false)
        return msg
    }

    open fun createSendMessage() = SendMessage()

    override fun extractArgs(text: String, cmdName: String) = text.substring(cmdName.length).trim()

    override fun moreThanOneParameter(args: String) = args.contains(' ')

    override fun cancelCommand(msg: Message): Boolean =
            msg.isCommand &&
                    ((msg.text.toLowerCase() == ITelegramUtils.CancelCommand) ||
                            (msg.text.toLowerCase() == ITelegramUtils.CancelCommandAlias))

}