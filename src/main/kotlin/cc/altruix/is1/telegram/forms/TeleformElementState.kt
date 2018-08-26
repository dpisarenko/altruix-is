package cc.altruix.is1.telegram.forms

import cc.altruix.is1.telegram.AutomatonState

/**
 * Created by pisarenko on 06.04.2017.
 */
open class TeleformElementState(
        val waiting:Boolean = true,
        val initial:Boolean = false,
        val terminal: Boolean = false,
        val id:String = ""
):AutomatonState {
    override fun waitingState(): Boolean = waiting
    override fun terminalState(): Boolean = terminal
    override fun initialState(): Boolean = initial
}