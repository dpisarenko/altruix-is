package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.*
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 07.02.2017.
 */
open class Bp1SuCmd(
        val jena:IJenaSubsystem,
        val tu: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/bp1su"
        val Help = "Show all external users, who can work in business processes 1 and 2 (collecting contact data and contacting SEO companies, obsolete)"
    }

    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val args = tu.extractArgs(text, Bp1SuCmd.Name)
        if (!args.isBlank()) {
            tu.sendTextMessage(ITelegramUtils.NoParametersAllowed, chatId, bot)
            return
        }
        val res = jena.fetchAllUsers()
        if (res.success && (res.result != null)) {
            displayUsers(res.result, chatId, bot)
        } else {
            tu.displayError(res.error, chatId, bot)
        }
    }

    open fun displayUsers(users: List<User>, chatId: Long, bot: IResponsiveBot) {
        val sendMsg = tu.createSendTextMessage(composeUsersText(users), chatId)
        bot.sendTelegramMessage(sendMsg)
    }

    open fun composeUsersText(users: List<User>): String {
        val builder = StringBuilder()
        builder.append("Found ${users.size} users:")
        builder.append(ITelegramUtils.LineSeparator)
        var i = 1
        users.forEach { user ->
            builder.append("${i}) ${user.nick}: ${user.email}")
            builder.append(ITelegramUtils.LineSeparator)
            i++
        }
        return builder.toString()
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}