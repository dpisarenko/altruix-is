package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.capsulecrm.PartiesSearchResult
import cc.altruix.is1.capsulecrm.Party
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.mock
import org.apache.commons.io.IOUtils
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.*
import org.telegram.telegrambots.api.methods.send.SendMessage

/**
 * Created by pisarenko on 31.01.2017.
 */
class FpwCmdTests {
    @Test
    fun executeReactsCorrectlyTo1ParameterProblem() {
        executeErrorHandlingTestLogic(
                "/fpw a b c",
                ITelegramUtils.OneParameterOnlyAllowed
        )
    }

    @Test
    fun executeReactsCorrectlyToWrongCharactersProblem() {
        val invalidUrlFragment = arrayListOf(
                "/fpw a/c",
                "/fpw aüb",
                "/fpw a\\a"
        )

        invalidUrlFragment.forEach { invalidUrlFragment ->
            executeErrorHandlingTestLogic(
                    invalidUrlFragment,
                    FpwCmd.InvalidCharactersInUrl
            )
        }
    }

    @Test
    fun executeSearchesForPartiesInCapsuleIfArgsAreCorrect() {
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(FpwCmd(capsule))
        val bot = mock<IResponsiveBot>()
        val chatId = 2011L
        val cmd = "/fpw"
        val args = "someInterestingWebSite"
        val text = "${cmd} ${args}"
        val parties:List<Party> = emptyList()
        val searchResult = PartiesSearchResult(true, "", parties)
        `when`(capsule.findPartiesByUrlFragment(args)).thenReturn(searchResult)
        doNothing().`when`(sut).displayFoundParties(bot, chatId, parties)

        // Run method under test
        sut.execute(text, bot, chatId, -1)

        // Verify
        verify(capsule).findPartiesByUrlFragment(args)
    }

    @Test
    fun validUrlFragment() {
        validUrlFragmentTestLogic("abc", true)
        validUrlFragmentTestLogic("someInterestingWebSite", true)
        validUrlFragmentTestLogic("a*c", false)
        validUrlFragmentTestLogic("a c", false)
    }

    @Test
    fun executeBehavesCorrectlyIfQuerySuccessful() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(FpwCmd(capsule))
        val urlFragment = "bernadette"
        val text = "/fpw ${urlFragment}"
        val bot = mock<IResponsiveBot>()
        val chatId = 2022L
        val foundParties = mock<List<Party>>()
        val searchResult = PartiesSearchResult(true, "", foundParties)
        `when`(capsule.findPartiesByUrlFragment(urlFragment)).thenReturn(searchResult)
        doNothing().`when`(sut).displayFoundParties(bot, chatId, foundParties)

        // Run method under test
        sut.execute(text, bot, chatId, -1)

        // Verify
        verify(capsule).findPartiesByUrlFragment(urlFragment)
        verify(sut).displayFoundParties(bot, chatId, foundParties)
    }

    @Test
    fun executeBehavesCorrectlyIfQueryFails() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val tu = spy(TelegramUtils())
        val sut = spy(FpwCmd(capsule, tu))
        val urlFragment = "bernadette"
        val text = "/fpw ${urlFragment}"
        val bot = mock<IResponsiveBot>()
        val chatId = 2022L
        val errorMsg = "boner"
        val searchResult = PartiesSearchResult(false, errorMsg, emptyList())
        `when`(capsule.findPartiesByUrlFragment(urlFragment)).thenReturn(searchResult)

        // Run method under test
        sut.execute(text, bot, chatId, -1)

        // Verify
        verify(capsule).findPartiesByUrlFragment(urlFragment)
        verify(tu).displayError(errorMsg, chatId, bot)
    }

    @Test
    fun displayFoundPartiesDisplaysNoPartiesFoundMessage() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(FpwCmd(capsule))
        val bot = mock<IResponsiveBot>()
        val parties = emptyList<Party>()
        val chatId = 2022L
        doNothing().`when`(sut).displayNoPartiesFoundMessage(bot, chatId)

        // Run method under test
        sut.displayFoundParties(bot, chatId, parties)

        // Verify
        verify(sut).displayNoPartiesFoundMessage(bot, chatId)
        verify(sut, never()).displayFoundPartiesProper(bot, chatId, parties)
    }
    @Test
    fun displayFoundPartiesDisplaysParties() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(FpwCmd(capsule))
        val bot = mock<IResponsiveBot>()
        val parties = listOf(Party(1L, emptyList()))
        val chatId = 2022L
        doNothing().`when`(sut).displayNoPartiesFoundMessage(bot, chatId)

        // Run method under test
        sut.displayFoundParties(bot, chatId, parties)

        // Verify
        verify(sut, never()).displayNoPartiesFoundMessage(bot, chatId)
        verify(sut).displayFoundPartiesProper(bot, chatId, parties)
    }
    @Test
    fun displayFoundPartiesProper() {
        // Prepare
        val tu = mock<ITelegramUtils>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(FpwCmd(capsule, tu))
        val bot = mock<IResponsiveBot>()
        val parties = emptyList<Party>()
        val chatId = 2022L
        val msgTxt = "msgText"
        doReturn(msgTxt).`when`(sut).composeFoundPartiesText(parties)
        val sendMsg = mock<SendMessage>()
        `when`(tu.createSendTextMessage(msgTxt, chatId)).thenReturn(sendMsg)

        // Run method under test
        sut.displayFoundPartiesProper(bot, chatId, parties)

        // Verify
        verify(sut).composeFoundPartiesText(parties)
        verify(tu).createSendTextMessage(msgTxt, chatId)
        verify(bot).sendTelegramMessage(sendMsg)
    }

    @Test
    fun composeFoundPartiesText() {
        composeFoundPartiesTextTestLogic(
                listOf(createParty(1, listOf("a"))),
                readFile("FpwCmdTests.01.txt")
        )
        composeFoundPartiesTextTestLogic(
                listOf(createParty(1, listOf("a")), createParty(2, listOf("b"))),
                readFile("FpwCmdTests.02.txt")
        )
        composeFoundPartiesTextTestLogic(
                listOf(createParty(1, listOf("a")), createParty(2, listOf("b", "c"))),
                readFile("FpwCmdTests.03.txt")
        )
    }

    @Test
    fun displayNoPartiesFoundMessage() {
        // Prepare
        val tu = mock<ITelegramUtils>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(FpwCmd(capsule, tu))
        val chatId = 2022L
        val bot = mock<IResponsiveBot>()
        val sendMsg = mock<SendMessage>()
        `when`(tu.createSendTextMessage("No parties found", chatId)).thenReturn(sendMsg)

        // Run method under test
        sut.displayNoPartiesFoundMessage(bot, chatId)

        // Verify
        verify(tu).createSendTextMessage("No parties found", chatId)
        verify(bot).sendTelegramMessage(sendMsg)
    }
    @Test
    fun nameAndHelp() {
        val tu = mock<ITelegramUtils>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = FpwCmd(capsule, tu)
        assertThat(sut.name()).isEqualTo(FpwCmd.Name)
        assertThat(sut.helpText()).isEqualTo(FpwCmd.Help)
    }
    private fun readFile(file: String) = IOUtils.toString(javaClass.classLoader.getResourceAsStream("cc/altruix/is1/telegram/" + file), "UTF-8")

    private fun createParty(id: Long, webSites: List<String>): Party = Party(id, webSites)

    private fun composeFoundPartiesTextTestLogic(parties: List<Party>, expectedResult: String) {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(FpwCmd(capsule))

        // Run method under test
        val actRes = sut.composeFoundPartiesText(parties)

        // Verify
        assertThat(actRes).isEqualTo(expectedResult)
    }

    private fun validUrlFragmentTestLogic(input: String, expectedResult: Boolean) {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = FpwCmd(capsule)

        // Run method under test
        val actRes = sut.validUrlFragment(input)

        // Verify
        Assert.assertEquals(expectedResult, actRes)
    }

    private fun executeErrorHandlingTestLogic(
            args: String,
            expectedError: String
    ) {
        // Prepare
        val tu = spy(TelegramUtils())
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(FpwCmd(capsule, tu))
        val bot = mock<IResponsiveBot>()
        val chatId = 2011L

        // Run method under test
        sut.execute(args, bot, chatId, -1)

        // Verify
        verify(tu).sendTextMessage(expectedError, chatId, bot)
    }
}