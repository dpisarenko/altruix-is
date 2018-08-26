package cc.altruix.is1.telegram

/**
 * Created by 1 on 01.03.2017.
 */
abstract class AbstractCANCELING_Handler<S>(
        val endState:S,
        val parent : IParentAutomaton<S>,
        val tu: ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<S>(
        parent
) {
    override fun fire() {
        parent.unsubscribe()
        printMessage(ITelegramUtils.CanceledMessage)
        parent.goToStateIfPossible(endState)
    }
}