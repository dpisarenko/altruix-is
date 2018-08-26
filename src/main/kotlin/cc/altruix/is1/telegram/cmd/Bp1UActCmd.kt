package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by pisarenko on 07.02.2017.
 */
open class Bp1UActCmd(
        val jena: IJenaSubsystem,
        val telegramUtils: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/bp1uact"
        val Help = "BP 1 and 2: Activate a new user (who previously introduced himself at Knackal, obsolete)"
    }
    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val args = telegramUtils.extractArgs(text, Bp1UActCmd.Name)
        if (telegramUtils.moreThanOneParameter(args)) {
            telegramUtils.sendTextMessage(ITelegramUtils.OneParameterOnlyAllowed, chatId, bot)
            return
        }
        val email = args.trim()
        val res = jena.activateUser(email)
        if (res.success) {
            telegramUtils.sendTextMessage(
                    "User activated. Now notify him via e-mail '${email}'",
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