package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.*
import cc.altruix.utils.isNumeric

/**
 * Created by 1 on 25.02.2017.
 *
 * /bp2cc: Contact a company. Parameter: Batch ID. Shows the
 * data of the next unprocessed company. When the worker
 * has contacted them, saves the results (OK, not OK), and
 * if it’s not OK — a comment. When the company has been
 * contacted, adds a note about it in Capsule CRM
 * (with the text sent to them). Accepts a file with the
 * actual message sent to them.
 */
open class Bp2CcCmd(
        val jena: IJenaSubsystem,
        val capsule: ICapsuleCrmSubsystem,
        val tu: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/bp2cc"
        val Help = "Use this command after you contacted a SEO company (obsolete)"
        val NonNumericBatchNumber = "Non-numeric batch number"
    }
    override fun execute(
            text: String,
            bot: IResponsiveBot,
            chatId: Long,
            userId: Int
    ) {
        val args = tu.extractArgs(text, Name)
        if (args.isBlank()) {
            tu.sendTextMessage(ITelegramUtils.OneParameterOnlyAllowed, chatId, bot)
            return
        }
        if (!args.isNumeric()) {
            tu.sendTextMessage(NonNumericBatchNumber, chatId, bot)
            return

        }
        val batchId = args.toInt()
        val automaton = createAutomaton(bot, chatId, batchId)
        bot.subscribe(automaton)
        automaton.start()
    }

    open fun createAutomaton(bot: IResponsiveBot, chatId: Long, batchId: Int): Bp2CcCmdAutomaton =
            Bp2CcCmdAutomaton(bot, chatId, jena, batchId, capsule)

    override fun name(): String = Name

    override fun helpText(): String = Help
}