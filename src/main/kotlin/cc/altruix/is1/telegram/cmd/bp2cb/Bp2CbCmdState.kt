package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.telegram.AutomatonState

/**
 * Created by 1 on 25.02.2017.
 */
enum class Bp2CbCmdState(
        val waitingState:Boolean,
        val terminalState:Boolean = false,
        val initialState:Boolean = false
) : AutomatonState {
    NEW(false, false, true),
    WAITING_FOR_FILE_UPLOAD(true),
    CANCELING(false),
    END(false, true, false);

    override fun waitingState():Boolean = waitingState
    override fun terminalState():Boolean = terminalState
    override fun initialState(): Boolean = initialState

}