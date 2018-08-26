package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils

/**
 * Created by 1 on 25.02.2017.
 *
 * /bp2cb: Creates a batch. Takes no parameters and returns
 * an integer batch number. Accepts a file with IDs of companies
 * to contact in scope of this batch. Like “BP2-B-1”. Target user: DP
 */
open class Bp2CbCmd(
        val jena:IJenaSubsystem,
        val tu: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/bp2cb"
        val Help = "Creates a batch for contacting SEO companies (obsolete)"
        val WrongPersona = "Wrong persona"
    }
    override fun execute(
            text: String,
            bot: IResponsiveBot,
            chatId: Long,
            userId: Int
    ) {
        val args = tu.extractArgs(text, Bp2CbCmd.Name)
        if (args.isNullOrBlank()) {
            tu.sendTextMessage(ITelegramUtils.OneParameterOnlyAllowed, chatId, bot)
            return
        }
        val persona = args.trim().toUpperCase()
        if (!personaValid(persona)) {
            tu.sendTextMessage(WrongPersona, chatId, bot)
            return
        }
        val automaton = createAutomaton(bot, chatId, jena, persona)
        bot.subscribe(automaton)
        automaton.start()
    }

    open fun personaValid(persona: String): Boolean = (persona == "DP") || (persona == "FD")

    open fun createAutomaton(
            bot: IResponsiveBot,
            chatId: Long,
            jena: IJenaSubsystem,
            persona:String) =
            Bp2CbCmdAutomaton(bot, chatId, jena, persona)

    override fun name(): String = Name

    override fun helpText(): String = Help
}