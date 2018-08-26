package cc.altruix.is1.telegram.forms

import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCmdAutomaton
import cc.altruix.is1.validation.FailableOperationResult

/**
 * Created by pisarenko on 06.04.2017.
 */
interface IAutomatonFactory {
    fun createAutomaton(form:Teleform, bot: IResponsiveBot, chatId: Long):
            FailableOperationResult<ITelegramCmdAutomaton>
}