package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.User
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by pisarenko on 13.02.2017.
 */
class Bp1AddCmdAutomatonTests {
    @Test
    fun startSunnyDay() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()

        val sut = spy(Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu))
        doReturn(true).`when`(sut).canChangeState(Bp1AddCmdState.WAITING_FOR_COMPANY_URL)

        // Run method under test
        sut.start()

        // Verify
        verify(sut).canChangeState(Bp1AddCmdState.WAITING_FOR_COMPANY_URL)
        verify(sut).changeState(Bp1AddCmdState.WAITING_FOR_COMPANY_URL)
    }
    @Test
    fun startCantChangeState() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = spy(Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu))
        doReturn(false).`when`(sut).canChangeState(Bp1AddCmdState.WAITING_FOR_COMPANY_URL)

        // Run method under test
        sut.start()

        // Verify
        verify(sut).canChangeState(Bp1AddCmdState.WAITING_FOR_COMPANY_URL)
        verify(tu).displayError("Invalid transition attempt (NEW -> WAITING_FOR_COMPANY_URL)", chatId, bot)
        verify(sut, never()).changeState(Bp1AddCmdState.WAITING_FOR_COMPANY_URL)
    }
    @Test
    fun ctorSetsContactDataType() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)

        // Run method under test
        val actRes = sut.contactDataType

        // Verify
        assertThat(actRes).isEqualTo(ContactDataType.UNKNOWN)
    }
    @Test
    fun saveContactDataType() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)
        assertThat(sut.contactDataType).isEqualTo(ContactDataType.UNKNOWN)

        // Run method under test
        sut.saveContactDataType(ContactDataType.EMAIL)

        // Verify
        assertThat(sut.contactDataType).isEqualTo(ContactDataType.EMAIL)
    }
    @Test
    fun saveEmail() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)

        assertThat(sut.email).isEmpty()

        // Run method under test
        sut.saveEmail("dp@altruix.co")

        // Verify
        assertThat(sut.email).isEqualTo("dp@altruix.co")
    }
    @Test
    fun saveContactFormUrl() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)

        assertThat(sut.contactFormUrl).isEmpty()

        // Run method under test
        sut.saveContactFormUrl("http://altruix.cc")

        // Verify
        assertThat(sut.contactFormUrl).isEqualTo("http://altruix.cc")
    }
    @Test
    fun saveNote() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)
        val note =
"""Yes, there were times, I'm sure you knew
When I bit off more than I could chew
But through it all, when there was doubt
I ate it up and spit it out
I faced it all and I stood tall
And did it my way"""
        assertThat(sut.note).isEmpty()

        // Run method under test
        sut.saveNote(note)

        // Verify
        assertThat(sut.note).isEqualTo(note)
    }
    @Test
    fun startCreatesHandlerForEveryState() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)

        // Run method under test
        sut.start()

        // Verify
        Bp1AddCmdState.values()
                .filter { !it.terminalState && !it.initialState }
                .forEach { state ->
                    assertThat(sut.handlers[state]).isNotNull
                }
    }
    @Test
    fun unsubscribe() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)

        // Run method under test
        sut.unsubscribe()

        // Verify
        verify(bot).unsubscribe(sut)
    }
    @Test
    fun saveMainUrl() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)
        assertThat(sut.mainUrl).isEmpty()
        val url = "http://altruix.cc"

        // Run method under test
        sut.saveMainUrl(url)

        // Verify
        assertThat(sut.mainUrl).isEqualTo(url)
    }
    @Test
    fun companyData() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)
        sut.mainUrl = "url"
        sut.contactDataType = ContactDataType.CONTACT_FORM
        sut.email = "email"
        sut.contactFormUrl = "contactFormUrl"
        sut.note = "note"

        // Run method under test
        val actRes = sut.companyData()

        // Verify
        assertThat(actRes.url).isEqualTo(sut.mainUrl)
        assertThat(actRes.ctype).isEqualTo(sut.contactDataType)
        assertThat(actRes.email).isEqualTo(sut.email)
        assertThat(actRes.contactFormUrl).isEqualTo(sut.contactFormUrl)
        assertThat(actRes.note).isEqualToIgnoringCase(sut.note)
    }
    @Test
    fun printMessage() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)
        val msg = "msg"

        // Run method under test
        sut.printMessage(msg)

        // Verify
        verify(tu).sendTextMessage(msg, chatId, bot)
    }
    @Test
    fun saveAgent() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1908L
        val tu = mock<ITelegramUtils>()
        val sut = Bp1AddCmdAutomaton(bot, chatId, mock<ICapsuleCrmSubsystem>(), tu)
        val agent = mock<org.telegram.telegrambots.api.objects.User>()
        `when`(agent.firstName).thenReturn("First name")
        `when`(agent.id).thenReturn(1607)
        `when`(agent.lastName).thenReturn("Last name")
        `when`(agent.userName).thenReturn("User name")
        assertThat(sut.agent).isEmpty()

        // Run method under test
        sut.saveAgent(agent)

        // Verify
        assertThat(sut.agent).isEqualTo("First name = 'First name', last name = 'Last name', user name = 'User name', id = 1607")
    }
}