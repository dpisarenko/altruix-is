package cc.altruix.is1.telegram.bots.knackal

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.AbstractCommandRegistry
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.cmd.FpwCmd
import cc.altruix.is1.telegram.cmd.IntroCmd
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmd
import cc.altruix.is1.telegram.cmd.bp2cc.Bp2CcCmd

/**
 * Created by pisarenko on 31.01.2017.
 */
open class KnackalCommandRegistry(
        val capsule: ICapsuleCrmSubsystem,
        val jena: IJenaSubsystem,
        val boss: IResponsiveBot
) : AbstractCommandRegistry() {
    override fun init() {
        commandsByName[FpwCmd.Name] = createFpwsCommand()
        commandsByName[IntroCmd.Name] = createIntroCommand(jena, boss)
        commandsByName[Bp1AddCmd.Name] = createBp1AddCmd(capsule)
    }
    open fun createBp2CcCmd(): ITelegramCommand = Bp2CcCmd(jena, capsule)

    open fun createBp1AddCmd(capsule: ICapsuleCrmSubsystem) = Bp1AddCmd(capsule)

    open fun createIntroCommand(jss: IJenaSubsystem, karl: IResponsiveBot): ITelegramCommand =
            IntroCmd(jss, karl)

    open fun createFpwsCommand(): ITelegramCommand = FpwCmd(capsule)
}