package cc.altruix.is1.telegram

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 13.02.2017.
 */
abstract class AbstractAutomaton<S : AutomatonState>(
        val allowedTransitions:Map<S, List<S>>,
        initState:S,
        val logger:Logger = LoggerFactory.getLogger("cc.altruix.is1.telegram.AbstractAutomaton")
) {
    var handlers: Map<S, AutomatonMessageHandler<S>> = emptyMap()
    var state: S = initState
    open fun changeState(newState: S) {
        val hdlrs = this.handlers
        if (hdlrs == null) {
            logger.error("No handlers")
            return
        }
        val handler = hdlrs[newState]
        if (handler == null) {
            logger.error("There is no handler for state '$newState'")
            return
        }
        this.state = newState

        if (!this.state.waitingState()) {
            handler.fire()
        }
    }

    open fun canChangeState(newState: S): Boolean {
        val allowedTargetStates = allowedTransitions[state] ?: return false
        return allowedTargetStates.contains(newState)
    }

    fun goToStateIfPossible(target: S) {
        if (canChangeState(target)) {
            changeState(target)
        }
    }
    fun waitingForResponse(): Boolean = this.state.waitingState()
    open fun handleIncomingMessage(msg: Message) {
        val handler = this.handlers[this.state]
        if (handler == null) {
            logger.error("No handler for state '${this.state}'")
            return
        }
        handler.handleIncomingMessage(msg)
    }

    open fun start() {
        handlers = createHandlers()
    }
    abstract fun createHandlers():Map<S, AutomatonMessageHandler<S>>
}

