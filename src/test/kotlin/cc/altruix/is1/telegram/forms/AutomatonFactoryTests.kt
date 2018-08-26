package cc.altruix.is1.telegram.forms

import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.rawdata.wordcount.WordCountCommand
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger

/**
 * Created by 1 on 09.04.2017.
 */
class AutomatonFactoryTests {
    @Test
    fun createAutomatonIdsNotUnique() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(
                AutomatonFactory(
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val form = mock<Teleform>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1329L
        doReturn(true).`when`(sut).idsNotUnique(form)

        // Run method under test
        val actRes = sut.createAutomaton(form, bot, chatId)

        // Verify
        verify(sut).idsNotUnique(form)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.result).isNull()
        assertThat(actRes.error).isEqualTo("IDs not unique")
    }
    @Test
    fun createAutomatonSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(
                AutomatonFactory(
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val form = mock<Teleform>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1329L
        doReturn(false).`when`(sut).idsNotUnique(form)
        val initialState = mock<TeleformElementState>()
        doReturn(initialState).`when`(sut).createState(false, true, false)
        val finalState = mock<TeleformElementState>()
        doReturn(finalState).`when`(sut).createState(false, false, true)
        val sunnyDayStates = mock<MutableList<TeleformElementState>>()
        doReturn(sunnyDayStates).`when`(sut).composeSunnyDayStates(form)
        val allowedTransitions = mock<MutableMap<TeleformElementState, List<TeleformElementState>>>()
        doReturn(allowedTransitions).`when`(sut).composeAllowedTransitions(
                initialState,
                finalState,
                sunnyDayStates)

        val inOrder = inOrder(sut, logger, form, bot)
        val res = mock<TeleformAutomaton>()
        doReturn(res).`when`(sut).createTeleformAutomaton(
                allowedTransitions,
                bot,
                chatId,
                finalState,
                form,
                initialState,
                sunnyDayStates
        )

        // Run method under test
        val actRes = sut.createAutomaton(form, bot, chatId)

        // Verify
        inOrder.verify(sut).idsNotUnique(form)
        inOrder.verify(sut).createState(false, true, false)
        inOrder.verify(sut).createState(false, false, true)
        inOrder.verify(sut).composeSunnyDayStates(form)
        inOrder.verify(sut).composeAllowedTransitions(
                initialState,
                finalState,
                sunnyDayStates)
        inOrder.verify(sut).createTeleformAutomaton(
                allowedTransitions,
                bot,
                chatId,
                finalState,
                form,
                initialState,
                sunnyDayStates
        )
        assertThat(actRes.success).isTrue()
        assertThat(actRes.result).isSameAs(res)
        assertThat(actRes.error).isEmpty()
    }
    @Test
    fun idsNotUnique() {
        val form1 = Teleform(listOf(
                StaticText("1", "Please enter the data about the word count "+
                        "in a scene of the work you're currently work at."),
                TextInput("3", "workName", "Work?"),
                TextInput("4", "partName", "Part name (e. g. \"Scene 1\", \"Chapter 2\")?"),
                IntegerInput("5", "wordCount", "Current word count?")
        ), WordCountCommand.MongoCollection)
        val form2 = Teleform(listOf(
                StaticText("1", "Please enter the data about the word count "+
                        "in a scene of the work you're currently work at."),
                TextInput("1", "workName", "Work?"),
                TextInput("4", "partName", "Part name (e. g. \"Scene 1\", \"Chapter 2\")?"),
                IntegerInput("5", "wordCount", "Current word count?")
        ), WordCountCommand.MongoCollection)
        idsNotUniqueTestLogic(form1, false)
        idsNotUniqueTestLogic(form2, true)
    }

    private fun idsNotUniqueTestLogic(form: Teleform, expRes: Boolean) {
        // Prepare
        val logger = mock<Logger>()
        val sut = AutomatonFactory(mock<IMongoSubsystem>(), logger)

        // Run method under test
        val actRes = sut.idsNotUnique(form)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
    @Test
    fun createAllowedTransitions() {
        val nextState = mock<TeleformElementState>()
        createAllowedTransitionsTestLogic(
                true,
                nextState,
                listOf(nextState, CancellingState)
        )
        createAllowedTransitionsTestLogic(
                false,
                nextState,
                listOf(nextState)
        )
    }

    private fun createAllowedTransitionsTestLogic(
            waiting: Boolean,
            nextState: TeleformElementState,
            expRes: List<TeleformElementState>
    ) {
        // Prepare
        val sut = AutomatonFactory(mock<IMongoSubsystem>())

        // Run method under test
        val actRes = sut.createAllowedTransitions(
                waiting,
                nextState)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
    @Test
    fun composeSunnyDayStates() {
        // Prepare
        val sut = spy(AutomatonFactory(mock<IMongoSubsystem>()))
        val static = mock<StaticText>()
        val input = mock<TextInput>()
        val form = Teleform(listOf(static, input), "")
        val staticState = mock<TeleformElementState>()
        val inputState1 = mock<TeleformElementState>()
        val inputState2 = mock<TeleformElementState>()
        val staticStates = listOf(staticState)
        val inputStates = listOf(inputState1, inputState2)
        doReturn(staticStates).`when`(sut).sunnyDayStates(static)
        doReturn(inputStates).`when`(sut).sunnyDayStates(input)

        // Run method under test
        val actRes = sut.composeSunnyDayStates(form)

        // Verify
        verify(sut).sunnyDayStates(static)
        verify(sut).sunnyDayStates(input)
        assertThat(actRes.size).isEqualTo(3)
        assertThat(actRes[0]).isSameAs(staticState)
        assertThat(actRes[1]).isSameAs(inputState1)
        assertThat(actRes[2]).isSameAs(inputState2)
    }
    @Test
    fun sunnyDayStatesStaticText() {
        // Prepare
        val sut = spy(AutomatonFactory(mock<IMongoSubsystem>()))
        val state = mock<TeleformElementState>()
        val elem = mock<StaticText>()
        doReturn(state).`when`(sut).createStaticTextState(elem)

        // Run method under test
        val actRes = sut.sunnyDayStates(elem)

        // Verify
        assertThat(actRes.size).isEqualTo(1)
        assertThat(actRes[0]).isSameAs(state)
    }
    @Test
    fun sunnyDayStatesInputField() {
        // Prepare
        val sut = spy(AutomatonFactory(mock<IMongoSubsystem>()))
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val elem = mock<InputField>()
        doReturn(state1).`when`(sut).createInputMessageState(elem)
        doReturn(state2).`when`(sut).createInputInputState(elem)

        // Run method under test
        val actRes = sut.sunnyDayStates(elem)

        // Verify
        assertThat(actRes.size).isEqualTo(2)
        assertThat(actRes[0]).isSameAs(state1)
        assertThat(actRes[1]).isSameAs(state2)
    }
    @Test
    fun createInputInputState() {
        // Prepare
        val sut = spy(AutomatonFactory(mock<IMongoSubsystem>()))
        val elem = mock<ITeleformElement>()
        `when`(elem.id()).thenReturn("1")

        // Run method under test
        val actRes = sut.createInputInputState(elem)

        // Verify
        assertThat(actRes.id).isEqualTo("1.INPUT")
        assertThat(actRes.waiting).isTrue()
        assertThat(actRes.initial).isFalse()
        assertThat(actRes.terminal).isFalse()
    }
    @Test
    fun createInputMessageState() {
        // Prepare
        val sut = AutomatonFactory(mock<IMongoSubsystem>())
        val elem = mock<ITeleformElement>()
        `when`(elem.id()).thenReturn("1")

        // Run method under test
        val actRes = sut.createInputMessageState(elem)

        // Verify
        assertThat(actRes.id).isEqualTo("1.MSG")
        assertThat(actRes.waiting).isFalse()
        assertThat(actRes.initial).isFalse()
        assertThat(actRes.terminal).isFalse()
    }
    @Test
    fun createStaticTextState() {
        // Prepare
        val sut = AutomatonFactory(mock<IMongoSubsystem>())
        val elem = mock<ITeleformElement>()
        `when`(elem.id()).thenReturn("1")

        // Run method under test
        val actRes = sut.createStaticTextState(elem)

        // Verify
        assertThat(actRes.id).isEqualTo("1")
        assertThat(actRes.waiting).isFalse()
        assertThat(actRes.terminal).isFalse()
        assertThat(actRes.initial).isFalse()
    }
    @Test
    fun composeAllowedTransitions() {
        // Prepare
        val sut = AutomatonFactory(mock<IMongoSubsystem>())
        val initialState = mock<TeleformElementState>()
        val finalState = mock<TeleformElementState>()
        val sunnyDayStates = sut.composeSunnyDayStates(WordCountCommand.Form)

        // Run method under test
        val actRes = sut.composeAllowedTransitions(
                initialState,
                finalState,
                sunnyDayStates
        )

        // Verify
        val state1 = sunnyDayStates[0]
        val state3Msg = sunnyDayStates[1]
        val state3Input = sunnyDayStates[2]
        val state4Msg = sunnyDayStates[3]
        val state4Input = sunnyDayStates[4]
        val state5Msg = sunnyDayStates[5]
        val state6Input = sunnyDayStates[6]

        assertThat(actRes[CancellingState]).isNotNull
        assertThat(actRes[CancellingState]?.size).isEqualTo(1)
        assertThat(actRes[CancellingState]?.get(0)).isSameAs(finalState)

        assertThat(actRes[state1]).isNotNull
        assertThat(actRes[state1]?.size).isEqualTo(1)
        assertThat(actRes[state1]?.get(0)).isSameAs(state3Msg)

        assertThat(actRes[state3Msg]).isNotNull
        assertThat(actRes[state3Msg]?.size).isEqualTo(1)
        assertThat(actRes[state3Msg]?.get(0)).isSameAs(state3Input)

        assertThat(actRes[state3Input]).isNotNull
        assertThat(actRes[state3Input]?.size).isEqualTo(2)
        assertThat(actRes[state3Input]?.get(0)).isSameAs(state4Msg)
        assertThat(actRes[state3Input]?.get(1)).isSameAs(CancellingState)

        assertThat(actRes[state4Msg]).isNotNull
        assertThat(actRes[state4Msg]?.size).isEqualTo(1)
        assertThat(actRes[state4Msg]?.get(0)).isSameAs(state4Input)

        assertThat(actRes[state4Input]).isNotNull
        assertThat(actRes[state4Input]?.size).isEqualTo(2)
        assertThat(actRes[state4Input]?.get(0)).isSameAs(state5Msg)
        assertThat(actRes[state4Input]?.get(1)).isSameAs(CancellingState)

        assertThat(actRes[state5Msg]).isNotNull
        assertThat(actRes[state5Msg]?.size).isEqualTo(1)
        assertThat(actRes[state5Msg]?.get(0)).isSameAs(state6Input)

        assertThat(actRes[state6Input]).isNotNull
        assertThat(actRes[state6Input]?.size).isEqualTo(2)
        assertThat(actRes[state6Input]?.get(0)).isSameAs(SavingState)
        assertThat(actRes[state6Input]?.get(1)).isSameAs(CancellingState)

        assertThat(actRes[SavingState]).isNotNull
        assertThat(actRes[SavingState]?.size).isEqualTo(1)
        assertThat(actRes[SavingState]?.get(0)).isSameAs(finalState)
    }
}