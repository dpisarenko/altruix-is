package cc.altruix.is1.telegram.cmd

import cc.altruix.mock
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.User
import cc.altruix.is1.validation.FailableOperationResult
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.methods.send.SendMessage

/**
 * Created by pisarenko on 09.02.2017.
 */
class Bp1SuCmdTests {
    @Test
    fun executeSunnyDay() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp1SuCmd(jena, tu))
        val args = ""
        val text = "/bp1su ${args}"
        val bot = mock<IResponsiveBot>()
        val chatId = 1714L
        val userId = 902
        `when`(tu.extractArgs(text, Bp1SuCmd.Name)).thenReturn(args)
        val users = mock<List<User>>()
        val res = FailableOperationResult<List<User>>(true, "", users)
        `when`(jena.fetchAllUsers()).thenReturn(res)
        doNothing().`when`(sut).displayUsers(users, chatId, bot)

        val inOrder = inOrder(sut, jena, tu)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp1SuCmd.Name)
        inOrder.verify(jena).fetchAllUsers()
        inOrder.verify(sut).displayUsers(users, chatId, bot)
    }
    @Test
    fun executeDatabaseError() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp1SuCmd(jena, tu))
        val args = ""
        val text = "/bp1su ${args}"
        val bot = mock<IResponsiveBot>()
        val chatId = 1714L
        val userId = 902
        `when`(tu.extractArgs(text, Bp1SuCmd.Name)).thenReturn(args)
        val users = null
        val errorMsg = "DB-related fuck-up"
        val res = FailableOperationResult<List<User>>(false, errorMsg, users)
        `when`(jena.fetchAllUsers()).thenReturn(res)

        val inOrder = inOrder(sut, jena, tu)

        // Run method under test
        sut.execute(text, bot, chatId, userId)

        // Verify
        inOrder.verify(tu).extractArgs(text, Bp1SuCmd.Name)
        inOrder.verify(jena).fetchAllUsers()
        inOrder.verify(tu).displayError(errorMsg, chatId, bot)
    }
    @Test
    fun displayUsers() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp1SuCmd(jena, tu))

        val users = mock<List<User>>()
        val chatId = 1730L
        val bot = mock<IResponsiveBot>()
        val usersTxt = "usersTxt"
        doReturn(usersTxt).`when`(sut).composeUsersText(users)
        val sendMsg = mock<SendMessage>()
        `when`(tu.createSendTextMessage(usersTxt, chatId)).thenReturn(sendMsg)

        val inOrder = inOrder(jena, tu, sut, users, bot)

        // Run method under test
        sut.displayUsers(users, chatId, bot)

        // Verify
        inOrder.verify(sut).composeUsersText(users)
        inOrder.verify(tu).createSendTextMessage(usersTxt, chatId)
        inOrder.verify(bot).sendTelegramMessage(sendMsg)
    }
    @Test
    fun composeUsersText() {
        // Prepare
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = Bp1SuCmd(jena, tu)
        val users = listOf<User>(
                User("dp118m", "dp@altruix.co"),
                User("PrinceOfSilence", "dp@altruix.cc")
        )

        // Run method under test
        val actRes = sut.composeUsersText(users)

        // Verify
        assertThat(actRes).isEqualTo(
            "Found 2 users:${ITelegramUtils.LineSeparator}" +
            "1) dp118m: dp@altruix.co${ITelegramUtils.LineSeparator}" +
            "2) PrinceOfSilence: dp@altruix.cc${ITelegramUtils.LineSeparator}"
        )
    }
    @Test
    fun nameAndHelp() {
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val sut = Bp1SuCmd(jena, tu)
        assertThat(sut.name()).isEqualTo(Bp1SuCmd.Name)
        assertThat(sut.helpText()).isEqualTo(Bp1SuCmd.Help)
    }
}