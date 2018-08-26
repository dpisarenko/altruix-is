package cc.altruix.is1.telegram.forms

import cc.altruix.is1.telegram.*
import cc.altruix.is1.validation.FailableOperationResult

/**
 * Created by pisarenko on 19.04.2017.
 */
open abstract class AbstractFormCommand(
        val af: IAutomatonFactory,
        val form:Teleform,
        val tu: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    override fun execute(
            text: String,
            bot: IResponsiveBot,
            chatId: Long,
            userId: Int
    ) {
        val res: FailableOperationResult<ITelegramCmdAutomaton> =
                af.createAutomaton(form, bot, chatId)
        if (res.success && (res.result != null)) {
            val automaton =res.result
            bot.subscribe(automaton)
            automaton.start()
        } else {
            tu.displayError("Internal error", chatId, bot)
        }
    }
}