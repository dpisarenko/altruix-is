package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.mock
import org.fest.assertions.Assertions
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger

/**
 * Created by pisarenko on 15.03.2017.
 */
class Bp2CcCmdAutomatonTests {
    @Test
    fun createHandlers() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1435L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = Bp2CcCmdAutomaton(
                bot,
                chatId,
                jena,
                -1,
                mock<ICapsuleCrmSubsystem>(),
                tu,
                logger
        )

        // Run method under test
        val actRes = sut.createHandlers()

        // Verify
        Bp2CcCmdState.values()
                .filter { !it.terminalState && !it.initialState }
                .forEach { state ->
                    Assertions.assertThat(actRes[state]).isNotNull
                }
    }
    @Test
    fun unsubscribe() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1435L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = Bp2CcCmdAutomaton(
                bot,
                chatId,
                jena,
                -1,
                mock<ICapsuleCrmSubsystem>(),
                tu, logger
        )

        // Run method under test
        sut.unsubscribe()

        // Verify
        verify(bot).unsubscribe(sut)
    }
    @Test
    fun sendTextMessage() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1435L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = Bp2CcCmdAutomaton(
                bot,
                chatId,
                jena,
                -1,
                mock<ICapsuleCrmSubsystem>(),
                tu, logger
        )
        val msg = "msg"

        // Run method under test
        sut.printMessage(msg)

        // Verify
        verify(tu).sendTextMessage(msg, chatId, bot)
    }
    @Test
    fun startSunnyDay() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1435L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(Bp2CcCmdAutomaton(
                bot,
                chatId,
                jena,
                -1,
                mock<ICapsuleCrmSubsystem>(),
                tu, logger
        ))
        doNothing().`when`(sut).changeState(Bp2CcCmdState.GETTING_NEXT_COMPANY_DATA)
        val inOrder = inOrder(sut)

        // Run method under test
        sut.start()

        // Verify
        inOrder.verify(sut).createHandlers()
        inOrder.verify(sut).canChangeState(Bp2CcCmdState.GETTING_NEXT_COMPANY_DATA)
        inOrder.verify(sut).changeState(Bp2CcCmdState.GETTING_NEXT_COMPANY_DATA)
    }
    @Test
    fun batchId() {
        // Prepare
        val batchId = 2120
        val bot = mock<IResponsiveBot>()
        val chatId = 1435L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(Bp2CcCmdAutomaton(
                bot,
                chatId,
                jena,
                batchId,
                mock<ICapsuleCrmSubsystem>(),
                tu, logger
        ))

        // Run method under test
        val actRes = sut.batchId()

        // Verify
        assertThat(actRes).isEqualTo(batchId)
    }
    @Test
    fun setCompanyData() {
        // Prepare
        val batchId = 2120
        val bot = mock<IResponsiveBot>()
        val chatId = 1435L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(Bp2CcCmdAutomaton(
                bot,
                chatId,
                jena,
                batchId,
                mock<ICapsuleCrmSubsystem>(),
                tu, logger
        ))
        val comp = Bp2CompanyData("2122", emptyList(), emptyList())

        // Run method under test
        sut.setCompanyData(comp)

        // Verify
        assertThat(sut.companyToContact).isSameAs(comp)
    }
    @Test
    fun contactTextAndNoteSavingAndRetrieval() {
        // Prepare
        val batchId = 2120
        val bot = mock<IResponsiveBot>()
        val chatId = 1435L
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val sut = spy(Bp2CcCmdAutomaton(
                bot,
                chatId,
                jena,
                batchId,
                mock<ICapsuleCrmSubsystem>(),
                tu, logger
        ))
        val contactTextAndNote = "contactTextAndNote"

        // Test #1
        assertThat(sut.getContactTextAndNote()).isNull()

        // Run method under test 1
        sut.setContactTextAndNote(contactTextAndNote)

        // Test #2
        assertThat(sut.getContactTextAndNote()).isEqualTo(contactTextAndNote)
    }
}