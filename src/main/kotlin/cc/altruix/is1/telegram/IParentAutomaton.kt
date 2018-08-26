package cc.altruix.is1.telegram

/**
 * Created by pisarenko on 10.02.2017.
 */
interface IParentAutomaton<C> {
    fun goToStateIfPossible(target:C)
    fun unsubscribe()
    fun printMessage(msg: String)
    fun state():C
}