package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.AutomatonState

/**
 * Created by pisarenko on 10.02.2017.
 */
enum class Bp1AddCmdState(
        val waitingState:Boolean,
        val terminalState:Boolean = false,
        val initialState:Boolean = false) : AutomatonState {

    NEW(false, false, true),
    WAITING_FOR_COMPANY_URL(true),
    WAITING_FOR_CONTACT_DATA_TYPE(true),
    WAITING_FOR_EMAIL(true),
    WAITING_FOR_CONTACT_FORM_URL(true),
    WAITING_FOR_NOTE(true),
    SAVING_DATA_IN_CAPSULE(false),
    CANCELING(false),
    END(false, true, false);

    override fun waitingState():Boolean = waitingState
    override fun terminalState():Boolean = terminalState
    override fun initialState(): Boolean = initialState
}