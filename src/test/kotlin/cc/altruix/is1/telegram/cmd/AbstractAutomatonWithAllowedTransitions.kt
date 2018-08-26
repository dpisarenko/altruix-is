package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.telegram.AbstractAutomaton
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmdState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pisarenko on 13.02.2017.
 */
open class AbstractAutomatonWithAllowedTransitions(
        allowedTransitions:Map<Bp1AddCmdState, List<Bp1AddCmdState>>,
        logger: Logger = LoggerFactory.getLogger("test")
) : AbstractAutomaton<Bp1AddCmdState>(
        allowedTransitions,
        Bp1AddCmdState.NEW,
        logger
) {
    override fun createHandlers(): Map<Bp1AddCmdState, AutomatonMessageHandler<Bp1AddCmdState>> = emptyMap()
}