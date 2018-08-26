package cc.altruix.caw.telegram

import cc.altruix.is1.App
import cc.altruix.is1.telegram.AbstractBot
import cc.altruix.is1.telegram.Authenticator
import cc.altruix.is1.telegram.IResponsiveBot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.send.SendPhoto
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup
import java.io.File
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow



/**
 * Created by pisarenko on 03.05.2017.
 */
open class EverettBot(
        val auth: Authenticator,
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : AbstractBot(), IResponsiveBot {
    override fun getBotUsername(): String = "EverettBot"
    override fun getBotToken(): String = "342890901:AAEGTmxdn-ZDOlkeOrmyUoHu3uFGhIhbe5s"

    override fun onUpdateReceived(update: Update?) {
        try {
            if (update == null) {
                logger.error("Null update")
                return
            }
            if (!update.hasMessage()) {
                logger.error("No message")
                return
            }
            val msg = update.message
            if (msg.hasText()) {
                handleIncomingMessage(msg)
            }
        }
        catch (e:Throwable) {
            logger.error("EverettBot.onUpdateReceived", e)
            return
        }
    }

    open fun handleIncomingMessage(msg: Message) {
        val sendMsg = defaultMsg(msg)
        sendMessage2(sendMsg)
    }

    open fun sendMessage2(sendMsg: SendMessage) {
        sendMessage(sendMsg)
    }

    open fun defaultMsg(msg: Message): SendMessage {
        val kbd:ReplyKeyboardMarkup = createMainMenuKeyboard()
        val sendMessage = createSendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.setChatId(msg.chatId)
        sendMessage.replyToMessageId = msg.messageId
        sendMessage.replyMarkup = kbd
        sendMessage.text = "Hi there"
        return sendMessage
    }

    open fun createSendMessage() = SendMessage()

    open fun createMainMenuKeyboard(): ReplyKeyboardMarkup {
        val markup = ReplyKeyboardMarkup()
        markup.selective = true
        markup.resizeKeyboard = true
        markup.oneTimeKeyboad = false
        val keyboard = ArrayList<KeyboardRow>()
        val row = KeyboardRow()
        row.add("/test")
        keyboard.add(row)
        markup.keyboard = keyboard
        return markup
    }

    override fun sendTelegramMessage(msg: SendMessage) {
    }

    override fun sendBroadcast(msg: String) {
    }

    override fun readFileContents(fileId: String): File? = null

    override fun sendImage(msg: SendPhoto) {
    }

}