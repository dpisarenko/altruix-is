package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.App
import cc.altruix.is1.telegram.AbstractCommandRegistry
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import org.telegram.telegrambots.api.methods.send.SendMessage

/**
 * Created by pisarenko on 10.05.2017.
 */
open class HCmd(
        val cmdReg: AbstractCommandRegistry
) : ITelegramCommand {
    companion object {
        val Name = "/h"
        val Help = "Show this help text"
        val HelpTextPrefix = "*Altruix IS ${App.Version}*\n\nFollowing commands are supported:\n\n"
    }
    override fun execute(
            text: String,
            bot: IResponsiveBot,
            chatId: Long,
            userId: Int
    ) {
        val txt = composeHelpText()
        val msg = createSendMessage(chatId, txt)
        bot.sendTelegramMessage(msg)
    }

    open fun createSendMessage(chatId: Long, txt: String): SendMessage {
        val msg = createSendMessage()
        msg.enableMarkdown(true)
        msg.chatId = chatId.toString()
        msg.text = txt
        return msg
    }

    open fun createSendMessage() = SendMessage()

    open fun composeHelpText(): String =
            HelpTextPrefix + cmdReg.commandsByName.entries
            .sortedBy { it.key }
            .map { composeCmdText(it) }
            .joinToString("\n")

    open fun composeCmdText(cmdEntry: MutableMap.MutableEntry<String, ITelegramCommand>): String {
        val cmd = cmdEntry.value
        val name = cmd.name()
        val help = cmd.helpText()
        return "$name: $help"
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}