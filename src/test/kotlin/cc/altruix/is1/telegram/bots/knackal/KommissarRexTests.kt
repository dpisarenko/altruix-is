package cc.altruix.is1.telegram.bots.knackal

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.bots.herrKarl.Wolf
import cc.altruix.is1.telegram.bots.knackal.KommissarRex
import cc.altruix.is1.telegram.cmd.Bp1SuCmd
import cc.altruix.is1.telegram.cmd.Bp1UActCmd
import cc.altruix.is1.telegram.cmd.IntroCmd
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User

/**
 * Created by pisarenko on 31.01.2017.
 */
class KommissarRexTests {
    @Test
    fun rightUserReturnsTrueOnIntroCommand() {
        // Prepare
        val sut = spy(KommissarRex(mock<IJenaSubsystem>()))
        val update = mock<Update>()
        doReturn(true).`when`(sut).isIntroCommand(update)
        val inOrder = inOrder(sut, update)

        // Run method under test
        val actRes = sut.rightUser(update)

        // Verify
        inOrder.verify(sut).isIntroCommand(update)
        inOrder.verify(sut, never()).userActivated(update)
        assertThat(actRes).isTrue()
    }
    @Test
    fun rightUserReturnsIsActivatedResOnNonIntroCommand() {
        rightUserReturnsIsActivatedResOnNonIntroCommandTestLogic(false)
        rightUserReturnsIsActivatedResOnNonIntroCommandTestLogic(true)
    }
    @Test
    fun isIntroCommand() {
        isIntroCommandTestLogic(IntroCmd.Name, true)
        isIntroCommandTestLogic("${IntroCmd.Name} mw d.a.pisarenko@bk.ru", true)
        isIntroCommandTestLogic("${IntroCmd.Name}o mw d.a.pisarenko@bk.ru", false)
        isIntroCommandTestLogic("/intr", false)
        isIntroCommandTestLogic("/introo", false)
        isIntroCommandTestLogic(Bp1UActCmd.Name, false)
        isIntroCommandTestLogic(Bp1SuCmd.Name, false)
    }
    @Test
    fun extractCommand() {
        extractCommandTestLogic("/cmd", "/cmd")
        extractCommandTestLogic("/cmd a", "/cmd")
        extractCommandTestLogic("/cmd a b c", "/cmd")
    }
    @Test
    fun userActivatedSunnyDay() {
        userActivatedSunnyDayTestLogic(true)
        userActivatedSunnyDayTestLogic(false)
    }
    @Test
    fun userActivatedRainyDay() {
        userActivatedRainyDayTestLogic(false, null)
        userActivatedRainyDayTestLogic(false, true)
        userActivatedRainyDayTestLogic(false, false)
        userActivatedRainyDayTestLogic(true, null)
    }
    @Test
    fun rightUserReturnsTrueForDp() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val wolf = mock<Wolf>()
        val sut = spy(
                KommissarRex(
                        jena,
                        wolf
                )
        )
        val update = mock<Update>()
        doReturn(false).`when`(sut).isIntroCommand(update)
        `when`(wolf.rightUser(update)).thenReturn(true)
        val inOrder = inOrder(jena, wolf, sut, update)

        // Run method under test
        val actRes = sut.rightUser(update)

        // Verify
        inOrder.verify(sut).isIntroCommand(update)
        inOrder.verify(wolf).rightUser(update)
        assertThat(actRes).isTrue()
    }

    private fun userActivatedRainyDayTestLogic(success: Boolean, result: Boolean?) {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val sut = spy(KommissarRex(jena))
        val userId = 755
        val update = mockUpdateFromUser(userId)
        val cmd = "cmd"
        doReturn(cmd).`when`(sut).extractCommand(update)
        val res = FailableOperationResult<Boolean>(success, "", result)
        `when`(jena.hasPermission(userId, cmd)).thenReturn(res)

        // Run method under test
        val actRes = sut.userActivated(update)

        // Verify
        verify(sut).extractCommand(update)
        verify(jena).hasPermission(userId, cmd)
        assertThat(actRes).isFalse()
    }

    private fun userActivatedSunnyDayTestLogic(resResult: Boolean) {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val sut = spy(KommissarRex(jena))
        val userId = 755
        val update = mockUpdateFromUser(userId)
        val cmd = "cmd"
        doReturn(cmd).`when`(sut).extractCommand(update)
        val res = FailableOperationResult<Boolean>(true, "", resResult)
        `when`(jena.hasPermission(userId, cmd)).thenReturn(res)

        // Run method under test
        val actRes = sut.userActivated(update)

        // Verify
        verify(sut).extractCommand(update)
        verify(jena).hasPermission(userId, cmd)
        assertThat(actRes).isEqualTo(resResult)
    }

    private fun mockUpdateFromUser(userId: Int): Update {
        val update = mock<Update>()
        val message = mock<Message>()
        val user = mock<User>()
        `when`(user.id).thenReturn(userId)
        `when`(message.from).thenReturn(user)
        `when`(update.message).thenReturn(message)
        return update
    }

    private fun extractCommandTestLogic(input: String, expRes: String) {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val sut = KommissarRex(jena)
        val update = mockMessage(input)

        // Run method under test
        val actRes = sut.extractCommand(update)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun isIntroCommandTestLogic(cmdName: String, expRes: Boolean) {
        // Prepare
        val sut = spy(KommissarRex(mock<IJenaSubsystem>()))
        val update = mockMessage(cmdName)

        // Run method under test
        val actRes = sut.isIntroCommand(update)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun mockMessage(cmdName: String): Update {
        val update = mock<Update>()
        val message = mock<Message>()
        `when`(update.message).thenReturn(message)
        `when`(message.text).thenReturn(cmdName)
        return update
    }

    private fun rightUserReturnsIsActivatedResOnNonIntroCommandTestLogic(expRes: Boolean) {
        // Prepare
        val wolf = mock<Wolf>()
        val sut = spy(KommissarRex(mock<IJenaSubsystem>(), wolf))
        val update = mock<Update>()
        doReturn(false).`when`(sut).isIntroCommand(update)
        doReturn(expRes).`when`(sut).userActivated(update)
        `when`(wolf.rightUser(update)).thenReturn(false)
        val inOrder = inOrder(sut, update, wolf)

        // Run method under test
        val actRes = sut.rightUser(update)

        // Verify
        inOrder.verify(sut).isIntroCommand(update)
        inOrder.verify(wolf).rightUser(update)
        inOrder.verify(sut).userActivated(update)
        assertThat(actRes).isEqualTo(expRes)
    }
}