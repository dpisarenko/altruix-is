package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.telegram.AbstractCANCELING_Handler

/**
 * Created by pisarenko on 15.03.2017.
 */
open class CANCELING_Handler(parent: IParentBp2CcCmdAutomaton) :
        AbstractCANCELING_Handler<Bp2CcCmdState>(
                Bp2CcCmdState.END,
                parent
) {

}