package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.telegram.AutomatonState

/**
 * Created by 1 on 04.03.2017.
 */
enum class Bp2CcCmdState(
        val waitingState:Boolean,
        val terminalState:Boolean = false,
        val initialState:Boolean = false
) : AutomatonState {
    NEW(false, false, true),
    GETTING_NEXT_COMPANY_DATA(false),
    WAITING_FOR_CONTACT_ATTEMPT_RESULT(true),
    WAITING_FOR_CONTACT_TEXT_AND_NOTE(true),
    SAVING_DATA(false),
    CANCELING(false),
    END(false, true, false);

    override fun waitingState():Boolean = waitingState
    override fun terminalState():Boolean = terminalState
    override fun initialState(): Boolean = initialState

}