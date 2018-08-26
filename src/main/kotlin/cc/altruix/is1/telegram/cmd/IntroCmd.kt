package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.EmailValidator
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 02.02.2017.
 */
open class IntroCmd(
        val jena: IJenaSubsystem,
        val boss: IResponsiveBot,
        val telegramUtils: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/intro"
        val Help = "Command with which the person introduces himself at Fr. Knackal"
        val OneParameterOnlyAllowed = "2 parameters required: <Nick name> <e-mail> (separated by a space)"
        val NickNameIncorrect = "Nick name incorrect. Only alphanumeric characters are allowed"
        val EmailIncorrect = "E-mail is incorrect"
    }

    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val args = text.substring(Name.length).trim()
        val parts = args.split(" ")
        if (!argCountCorrect(parts)) {
            telegramUtils.sendTextMessage(OneParameterOnlyAllowed, chatId, bot)
            return
        }
        val nick = parts[0]

        if (!isAlphanumeric(nick)) {
            telegramUtils.sendTextMessage(NickNameIncorrect, chatId, bot)
            return
        }

        val email = parts[1]
        if (!isValidEmail(email)) {
            telegramUtils.sendTextMessage(EmailIncorrect, chatId, bot)
            return
        }
        val res = jena.createNewUser(nick, email, userId, chatId)
        if (res.success) {
            telegramUtils.sendTextMessage(
                    "User created. My boss will contact you via e-mail within 72 hours.",
                    chatId,
                    bot
            )
            boss.sendBroadcast("Knackal: Mir haben an Neuen: ${nick} ('${email}') hinzugefügt.")
        } else {
            telegramUtils.displayError(res.error, chatId, bot)
        }
    }

    open fun isValidEmail(email: String) = EmailValidator.getInstance().isValid(email)

    open fun isAlphanumeric(nick: String) = StringUtils.isAlphanumeric(nick)

    open fun argCountCorrect(parts: List<String>) = (parts.size == 2)

    override fun name(): String = Name

    override fun helpText(): String = Help
}