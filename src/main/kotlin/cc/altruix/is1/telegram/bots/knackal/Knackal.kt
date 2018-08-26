package cc.altruix.is1.telegram.bots.knackal

import cc.altruix.is1.telegram.*
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.send.SendPhoto
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import java.io.File

/**
 * Created by pisarenko on 31.01.2017.
 */
open class Knackal(
        val protocol: Logger,
        val logger: Logger,
        val auth: Authenticator,
        val cmdRegistry: ITelegramCommandRegistry,
        val invalidUpdateFormatter: IInvalidUpdateFormatter = InvalidUpdateFormatter()
) : AbstractBot(), IResponsiveBot {
    override fun getBotUsername() = "KnackalBot"

    override fun getBotToken() = "CENSORED"

    override fun onUpdateReceived(update: Update?) {
        if (update == null) {
            return
        }
        if (!update.hasMessage()) {
            return
        }
        val msg = update.message
        if (msg == null) {
            return
        }

        val automatonWaitingForResponse = automatonWaitingForResponse()

        if (automatonWaitingForResponse != null) {
            automatonWaitingForResponse.handleIncomingMessage(msg)
            return
        }

        if (!msg.isCommand) {
            logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.NoCommand)
            return
        }
        if (!auth.rightUser(update)) {
            logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.WrongUser)
            return
        }
        handleIncomingMessage(msg)
    }

    open fun logInvalidAccessAttempt(
            update: Update,
            msg: Message,
            type: InvalidAccessAttemptType
    ) {
        val logMsg = invalidUpdateFormatter.format(
                update,
                msg,
                composeInvalidAccessComment(type)
        )
        logger.error(logMsg)
    }

    open fun composeInvalidAccessComment(
            type: InvalidAccessAttemptType) =
            String.format(
                    "Jössas, irgendwer will was von mir! Invalid access type: '%s'.", type.toString()
            )

    open fun handleIncomingMessage(msg: Message) {
        val cmdName = extractCmdName(msg)
        if (cmdName == "") {
            return
        }
        val cmd = cmdRegistry.find(cmdName)
        if (cmd == null) {
            processUnrecognizedCommandError(cmdName, msg)
            return
        }
        cmd.execute(msg.text, this, msg.chatId, msg.from.id)
    }

    open fun processUnrecognizedCommandError(cmdName: String, msg: Message) {
        sendErrorMessageToUser(cmdName, msg)
        logUnrecognizedCommand(cmdName)
    }

    open fun logUnrecognizedCommand(cmdName: String) {
        logger.error(String.format("Was soll der Schas - '%s' ?", cmdName))
    }

    open fun sendErrorMessageToUser(cmdName: String, msg: Message) {
        val reply = createUnrecognizedCommandReply(cmdName, msg)
        sendTelegramMessage(reply)
    }
    override fun sendTelegramMessage(msg: SendMessage) {
        sendMessage(msg)
    }

    open fun createUnrecognizedCommandReply(cmdName: String, req: Message): SendMessage {
        val msg = SendMessage()
        msg.text = "Unrecognized command '${cmdName}'"
        msg.chatId = req.chatId.toString()
        msg.enableMarkdown(false)
        return msg
    }
    override fun sendBroadcast(msg: String) {
    }
    override fun readFileContents(fileId: String): File? = null

    override fun sendImage(msg: SendPhoto) {
    }
}