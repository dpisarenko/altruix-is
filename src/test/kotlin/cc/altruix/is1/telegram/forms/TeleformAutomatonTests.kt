package cc.altruix.is1.telegram.forms

import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.rawdata.wordcount.WordCountCommand
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Created by 1 on 09.04.2017.
 */
class TeleformAutomatonTests {
    @Test
    fun startNoTransitions() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val allowedTransitions =
                emptyMap<TeleformElementState, List<TeleformElementState>>()
        val sut = spy(TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        ))

        // Run method under test
        sut.start()

        // Verify
        verify(logger).error("Target state null")
    }
    @Test
    fun startAmbiguousTransitions() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                mock<TeleformElementState>(),
                                mock<TeleformElementState>()
                        )
                )
        val sut = spy(TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        ))

        // Run method under test
        sut.start()

        // Verify
        verify(logger).error("Ambiguous target state")
    }
    @Test
    fun startInvalidTransition() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val targetState = mock<TeleformElementState>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                targetState
                        )
                )
        val sut = spy(TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        ))
        doReturn(false).`when`(sut).canChangeState(targetState)

        // Run method under test
        sut.start()

        // Verify
        verify(sut).canChangeState(targetState)
        verify(tu).displayError(
                "Invalid transition attempt (${initState} -> $targetState)", chatId, bot)
    }
    @Test
    fun startSunnyDay() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val targetState = mock<TeleformElementState>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                targetState
                        )
                )
        val sut = spy(TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        ))
        doReturn(true).`when`(sut).canChangeState(targetState)
        doNothing().`when`(sut).changeState(targetState)

        // Run method under test
        sut.start()

        // Verify
        verify(sut).canChangeState(targetState)
        verify(sut).changeState(targetState)
    }
    @Test
    fun saveInMemory() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val targetState = mock<TeleformElementState>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                targetState
                        )
                )
        val sut = spy(TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        ))
        val key = "key"
        val value = LocalDateTime.of(2017, 4, 9, 17, 25)

        // Run method under test
        sut.saveInMemory(key, value)

        // Verify
        assertThat(sut.formData[key]).isEqualTo(value)
    }
    @Test
    fun unsubscribe() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val targetState = mock<TeleformElementState>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                targetState
                        )
                )
        val sut = spy(TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        ))

        // Run method under test
        sut.unsubscribe()

        // Verify
        verify(bot).unsubscribe(sut)
    }
    @Test
    fun printMessage() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val targetState = mock<TeleformElementState>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                targetState
                        )
                )
        val sut = TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        )
        val msg = "test"

        // Run method under test
        sut.printMessage(msg)

        // Verify
        verify(tu).sendTextMessage(msg, chatId, bot)
    }
    @Test
    fun state() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val targetState = mock<TeleformElementState>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                targetState
                        )
                )
        val sut = TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        )
        val curState = mock<TeleformElementState>()
        sut.state = curState

        // Run method under test
        val actRes = sut.state()

        // Verify
        assertThat(actRes).isSameAs(curState)
    }
    @Test
    fun createHandlers() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()

        val sunnyDayState1 = mock<TeleformElementState>()
        val sunnyDayState2 = mock<TeleformElementState>()

        val sunnyDayStates = listOf<TeleformElementState>(
                sunnyDayState1,
                sunnyDayState2
        )
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val targetState = mock<TeleformElementState>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                targetState
                        )
                )
        val sut = spy(
                TeleformAutomaton(
                        bot, chatId, allowedTransitions,
                        initState, finalState, sunnyDayStates,
                        form, mock<IMongoSubsystem>(), tu, logger
                )
        )
        val universalHandler = mock<TeleformElementHandler>()
        doReturn(universalHandler).`when`(sut).createTeleformElementHandler(
                sut, form, sunnyDayStates, finalState
        )
        val cancellingHandler = mock<TeleformCancellingHandler>()
        doReturn(cancellingHandler).`when`(sut).createTeleformCancellingHandler(
                finalState,
                sut
        )

        // Run method under test
        val actRes = sut.createHandlers()

        // Verify
        verify(sut).createTeleformElementHandler(
                sut, form, sunnyDayStates, finalState
        )
        verify(sut).createTeleformCancellingHandler(
                finalState,
                sut
        )
        assertThat(actRes[sunnyDayState1]).isNotNull
        assertThat(actRes[sunnyDayState1]).isSameAs(universalHandler)

        assertThat(actRes[sunnyDayState2]).isNotNull
        assertThat(actRes[sunnyDayState2]).isSameAs(universalHandler)

        assertThat(actRes[SavingState]).isNotNull
        assertThat(actRes[SavingState]).isSameAs(universalHandler)

        assertThat(actRes[CancellingState]).isNotNull
        assertThat(actRes[CancellingState]).isSameAs(cancellingHandler)
    }
    @Test
    fun stuffToSave() {
        // Prepare
        val bot = mock<IResponsiveBot>()
        val chatId = 1710L
        val initState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = emptyList<TeleformElementState>()
        val form = WordCountCommand.Form
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val targetState = mock<TeleformElementState>()
        val allowedTransitions =
                mapOf<TeleformElementState, List<TeleformElementState>>(
                        initState to listOf(
                                targetState
                        )
                )
        val sut = spy(TeleformAutomaton(
                bot, chatId, allowedTransitions,
                initState, finalState, sunnyDayStates,
                form, mock<IMongoSubsystem>(), tu, logger
        ))
        val key1 = "key1"
        val value1 = "value1"
        val key2 = "key2"
        val value2 = 1405
        sut.saveInMemory(key1, value1)
        sut.saveInMemory(key2, value2)
        val zdt = ZonedDateTime.of(2017, 4, 12, 14, 5, 0, 0, ZoneId.of("Europe/Moscow"))
        doReturn(zdt).`when`(sut).timestamp2()

        // Run method under test
        val actRes = sut.stuffToSave()

        // Verify
        verify(sut).timestamp2()
        assertThat(actRes[key1]).isEqualTo(value1)
        assertThat(actRes[key2]).isEqualTo(value2)
        assertThat(actRes[IMongoSubsystem.TimeStampField]).isEqualTo(zdt)
    }
}