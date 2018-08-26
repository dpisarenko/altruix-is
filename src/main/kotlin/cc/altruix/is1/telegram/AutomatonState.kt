package cc.altruix.is1.telegram

/**
 * Created by pisarenko on 13.02.2017.
 */
interface AutomatonState {
    fun waitingState():Boolean
    fun terminalState():Boolean
    fun initialState():Boolean
}