package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.telegram.AbstractCANCELING_Handler

/**
 * Created by 1 on 25.02.2017.
 */
open class CANCELING_Handler(
        parent:IParentBp2CbCmdAutomaton
) : AbstractCANCELING_Handler<Bp2CbCmdState>(
        Bp2CbCmdState.END,
        parent
)  {

}