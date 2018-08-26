package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.capsulecrm.Party
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message


/**
 * Created by pisarenko on 31.01.2017.
 */
open class FpwCmd(
        val capsule: ICapsuleCrmSubsystem,
        val telegramUtils: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/fpw"
        val Help = "Find party in Capsule CRM (obsolete)"
        val InvalidCharactersInUrl = "Invalid characters in URL fragment"
    }
    val hostName = Regex("([a-z0-9]|[a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])(\\.[a-z0-9]|[a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])*\\.?")
    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val args = telegramUtils.extractArgs(text, Name)
        if (telegramUtils.moreThanOneParameter(args)) {
            telegramUtils.sendTextMessage(ITelegramUtils.OneParameterOnlyAllowed, chatId, bot)
            return
        }
        if (!validUrlFragment(args)) {
            telegramUtils.sendTextMessage(InvalidCharactersInUrl, chatId, bot)
            return
        }
        val searchResult = capsule.findPartiesByUrlFragment(args)
        if (searchResult.success) {
            displayFoundParties(bot, chatId, searchResult.parties)
        } else {
            telegramUtils.displayError(searchResult.errorMsg, chatId, bot)
        }
    }

    open fun displayFoundParties(bot: IResponsiveBot, chatId: Long, parties: List<Party>) {
        if (parties.isEmpty()) {
            displayNoPartiesFoundMessage(bot, chatId)
            return
        }
        displayFoundPartiesProper(bot, chatId, parties)
    }

    open fun displayFoundPartiesProper(bot: IResponsiveBot, chatId: Long, parties: List<Party>) {
        val sendMsg: SendMessage = telegramUtils.createSendTextMessage(composeFoundPartiesText(parties), chatId)
        bot.sendTelegramMessage(sendMsg)
    }

    open fun composeFoundPartiesText(parties: List<Party>): String {
        val builder = StringBuilder()
        builder.append("Found ${parties.size} parties (ID, list of web sites):")
        builder.append(ITelegramUtils.LineSeparator)
        parties.forEach { party ->
            builder.append("${party.id}:  ")
            var firstSite = true
            for (webSite in party.webSites) {
                if (!firstSite) {
                    builder.append(", ")
                } else {
                    firstSite = false
                }
                builder.append(webSite)
            }
            builder.append(ITelegramUtils.LineSeparator)
        }
        return builder.toString()
    }

    open fun displayNoPartiesFoundMessage(bot: IResponsiveBot, chatId: Long) {
        val sendMsg: SendMessage = telegramUtils.createSendTextMessage("No parties found", chatId)
        bot.sendTelegramMessage(sendMsg)
    }

    open fun validUrlFragment(urlFragment: String):Boolean = hostName.matches(urlFragment.toLowerCase())

    override fun name(): String = Name

    override fun helpText(): String = Help
}