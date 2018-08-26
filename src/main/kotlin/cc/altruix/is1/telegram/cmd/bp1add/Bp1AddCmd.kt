package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by pisarenko on 07.02.2017.
 */
open class Bp1AddCmd(
        val capsule: ICapsuleCrmSubsystem,
        val tu: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/bp1add"
        val Help = "Adds a SEO company to Capsule CRM (obsolete)"
    }

    override fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int) {
        val args = tu.extractArgs(text, Name)
        if (!args.isBlank()) {
            tu.sendTextMessage(ITelegramUtils.NoParametersAllowed, chatId, bot)
            return
        }
        val automaton = createAutomaton(bot, chatId, capsule)
        bot.subscribe(automaton)
        automaton.start()
    }

    open fun createAutomaton(
            bot: IResponsiveBot,
            chatId: Long,
            capsule: ICapsuleCrmSubsystem
    ) = Bp1AddCmdAutomaton(bot, chatId, capsule, tu)

    override fun name(): String = Name

    override fun helpText(): String = Help
}