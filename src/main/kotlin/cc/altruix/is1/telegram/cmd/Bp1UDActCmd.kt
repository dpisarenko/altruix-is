package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by pisarenko on 06.03.2017.
 */
open class Bp1UDActCmd (
        val jena: IJenaSubsystem,
        val telegramUtils: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/bp1udact"
        val Help = "Deactivate user (obsolete)"
    }
    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val args = telegramUtils.extractArgs(text, Bp1UDActCmd.Name)
        if (telegramUtils.moreThanOneParameter(args)) {
            telegramUtils.sendTextMessage(ITelegramUtils.OneParameterOnlyAllowed, chatId, bot)
            return
        }
        val email = args.trim()
        val res = jena.deActivateUser(email)
        if (res.success) {
            telegramUtils.sendTextMessage(
                    "User '${email}' de-activated.",
                    chatId,
                    bot
            )
        } else {
            telegramUtils.displayError(res.error, chatId, bot)
        }
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}