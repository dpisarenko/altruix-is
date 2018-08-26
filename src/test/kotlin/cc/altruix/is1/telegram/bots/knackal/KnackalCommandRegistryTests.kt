package cc.altruix.is1.telegram.bots.knackal

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.bots.knackal.KnackalCommandRegistry
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmd
import cc.altruix.is1.telegram.cmd.FpwCmd
import cc.altruix.is1.telegram.cmd.IntroCmd
import cc.altruix.is1.telegram.cmd.bp2cc.Bp2CcCmd
import org.junit.Test
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert
import org.mockito.Mockito.*

/**
 * Created by pisarenko on 31.01.2017.
 */
class KnackalCommandRegistryTests {
    @Test
    fun initCreatesFwpsCommand() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(KnackalCommandRegistry(capsule, mock<IJenaSubsystem>(), mock<IResponsiveBot>()))
        val fpwsCmd = mock<ITelegramCommand>()
        doReturn(fpwsCmd).`when`(sut).createFpwsCommand()

        // Run method under test
        sut.init()
        val actRes = sut.find(FpwCmd.Name)

        // Verify
        verify(sut).createFpwsCommand()
        Assert.assertSame(fpwsCmd, actRes)
    }
    @Test
    fun initCreatesIntroCommand() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val jena = mock<IJenaSubsystem>()
        val boss = mock<IResponsiveBot>()
        val sut = spy(KnackalCommandRegistry(capsule, jena, boss))
        val introCmd = mock<IntroCmd>()
        doReturn(introCmd).`when`(sut).createIntroCommand(jena, boss)

        // Run method under test
        sut.init()
        val actRes = sut.find(IntroCmd.Name)

        // Verify
        verify(sut).createIntroCommand(jena, boss)
        Assert.assertSame(introCmd, actRes)
    }
    @Test
    fun initCreatesBp1AddCmdCommand() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val jena = mock<IJenaSubsystem>()
        val boss = mock<IResponsiveBot>()
        val sut = spy(KnackalCommandRegistry(capsule, jena, boss))
        val bp1AddCmd = mock<Bp1AddCmd>()
        doReturn(bp1AddCmd).`when`(sut).createBp1AddCmd(capsule)

        // Run method under test
        sut.init()
        val actRes = sut.find(Bp1AddCmd.Name)

        // Verify
        verify(sut).createBp1AddCmd(capsule)
        Assert.assertSame(bp1AddCmd, actRes)
    }
    @Test
    fun initRegistersBp2Commands() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val jena = mock<IJenaSubsystem>()
        val boss = mock<IResponsiveBot>()
        val sut = spy(KnackalCommandRegistry(capsule, jena, boss))
        val bp2CcCmd = mock<ITelegramCommand>()
        doReturn(bp2CcCmd).`when`(sut).createBp2CcCmd()

        // Run method under test
        sut.init()

        // Verify
        verify(sut, never()).createBp2CcCmd()
        assertThat(sut.find(Bp2CcCmd.Name)).isNull()
    }
}