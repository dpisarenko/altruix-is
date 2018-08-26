package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.telegram.AbstractAutomaton
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmdState
import org.slf4j.Logger
import cc.altruix.mock

/**
 * Created by pisarenko on 13.02.2017.
 */
open class AbstractAutomatonForTesting(logger: Logger = mock<Logger>()) :
        AbstractAutomaton<Bp1AddCmdState>(
                emptyMap(),
                Bp1AddCmdState.NEW,
                logger
        ) {
    override fun createHandlers(): Map<Bp1AddCmdState, AutomatonMessageHandler<Bp1AddCmdState>> {
        return emptyMap()
    }
}