package cc.altruix.is1.telegram.forms

import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.rawdata.wordcount.WordCountCommand
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by 1 on 09.04.2017.
 */
class TeleformElementHandlerTests {
    @Test
    fun fireWrongStateType() {
        fireWrongStateTypeTestLogic(TeleformElementState(true, false, false))
        fireWrongStateTypeTestLogic(TeleformElementState(false, true, false))
        fireWrongStateTypeTestLogic(TeleformElementState(false, false, true))
    }

    private fun fireWrongStateTypeTestLogic(state: TeleformElementState) {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = mock<Teleform>()
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        `when`(parent.state()).thenReturn(state)

        // Run method under test
        sut.fire()

        // Verify
        verify(logger).error("Wrong state type")
        verify(sut, never()).saveData()
    }
    @Test
    fun fireNullElement() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val id = "1809"
        val state = TeleformElementState(false, false, false, "1809.MSG")
        `when`(parent.state()).thenReturn(state)
        doReturn(id).`when`(sut).extractId("1809.MSG")

        // Run method under test
        sut.fire()

        // Verify
        verify(sut).extractId("1809.MSG")
        verify(logger).error("Null element")
        verify(sut, never()).saveData()
    }
    @Test
    fun fireSunnyDayStaticText() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val state = TeleformElementState(false, false, false, "1")
        `when`(parent.state()).thenReturn(state)
        val staticText = WordCountCommand.Form.elements[0] as StaticText
        doNothing().`when`(sut).displayStaticText(staticText, state)

        // Run method under test
        sut.fire()

        // Verify
        verify(sut).extractId("1")
        verify(sut).displayStaticText(staticText, state)
        verify(sut, never()).saveData()
    }
    @Test
    fun fireSunnyDayInputNoMessageStateField() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val state = TeleformElementState(false, false, false, "3.INPUT")
        `when`(parent.state()).thenReturn(state)
        val inputText = WordCountCommand.Form.elements[1] as InputField
        doNothing().`when`(sut).displayInputMessage(inputText, state)

        // Run method under test
        sut.fire()

        // Verify
        verify(sut).extractId(state.id)
        verify(sut).messageState(state.id)
        verify(sut, never()).displayInputMessage(inputText, state)
        verify(sut, never()).saveData()
    }
    @Test
    fun fireSunnyDayInputMessageStateField() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val state = TeleformElementState(false, false, false, "3.MSG")
        `when`(parent.state()).thenReturn(state)
        val inputText = WordCountCommand.Form.elements[1] as InputField
        doNothing().`when`(sut).displayInputMessage(inputText, state)

        // Run method under test
        sut.fire()

        // Verify
        verify(sut).extractId(state.id)
        verify(sut).messageState(state.id)
        verify(sut).displayInputMessage(inputText, state)
        verify(sut, never()).saveData()
    }
    @Test
    fun displayInputMessage() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val state = TeleformElementState(false, false, false, "3.MSG")
        val msg = "msg"
        val element = object : InputField("", "", msg) {
            override fun parse(text: String): FailableOperationResult<Any> =
                    FailableOperationResult(false, "not implemented", null)
        }
        doNothing().`when`(sut).printMessage(msg)
        doNothing().`when`(sut).goToNextSunnyDayState(state)

        // Run method under test
        sut.displayInputMessage(element, state)

        // Verify
        verify(sut).printMessage(msg)
        verify(sut).goToNextSunnyDayState(state)
    }
    @Test
    fun messageState() {
        messageStateTestLogic("3.MSG", true)
        messageStateTestLogic("3.INPUT", false)
    }
    @Test
    fun inputState() {
        inputStateTestLogic("3.MSG", false)
        inputStateTestLogic("3.INPUT", true)
    }
    @Test
    fun displayStaticText() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val staticText = StaticText("3", "Hello")
        val state = mock<TeleformElementState>()
        doNothing().`when`(sut).printMessage(staticText.text)
        doNothing().`when`(sut).goToNextSunnyDayState(state)

        // Run method under test
        sut.displayStaticText(staticText, state)

        // Verify
        verify(sut).printMessage(staticText.text)
        verify(sut).goToNextSunnyDayState(state)
    }
    @Test
    fun goToNextSunnyDayState() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val state = mock<TeleformElementState>()
        val next = mock<TeleformElementState>()
        doReturn(next).`when`(sut).findNextSunnyDayState(state)

        // Run method under test
        sut.goToNextSunnyDayState(state)

        // Verify
        verify(sut).findNextSunnyDayState(state)
        verify(parent).goToStateIfPossible(next)
    }
    @Test
    fun findNextSunnyDayStateInvalidState() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )

        // Run method under test
        val actRes = sut.findNextSunnyDayState(mock<TeleformElementState>())

        // Verify
        assertThat(actRes).isSameAs(CancellingState)
    }
    @Test
    fun findNextSunnyDayStateMiddle() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )

        // Run method under test
        val actRes = sut.findNextSunnyDayState(state1)

        // Verify
        assertThat(actRes).isSameAs(state2)
    }
    @Test
    fun findNextSunnyDayStateUltimo() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )

        // Run method under test
        val actRes = sut.findNextSunnyDayState(state3)

        // Verify
        assertThat(actRes).isSameAs(SavingState)
    }
    
    @Test
    fun extractId() {
        extractIdTestLogic("1", "1")
        extractIdTestLogic("3.MSG", "3")
        extractIdTestLogic("4.INPUT", "4")
    }

    @Test
    fun handleIncomingMessageWrongStateType() {
        handleIncomingMessageWrongStateTypeTestLogic(TeleformElementState(false, false, false))
        handleIncomingMessageWrongStateTypeTestLogic(TeleformElementState(true, true, false))
        handleIncomingMessageWrongStateTypeTestLogic(TeleformElementState(true, false, true))
    }
    @Test
    fun handleIncomingMessageNullElement() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val state = TeleformElementState(true, false, false, "100.MSG")
        `when`(parent.state()).thenReturn(state)
        val msg = mock<Message>()

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(parent).state()
        verify(sut).extractId(state.id)
        verify(logger).error("Null element")
    }
    @Test
    fun handleIncomingMessageInputFieldNoInputState() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val state = TeleformElementState(true, false, false, "3.MSG")
        val element = WordCountCommand.Form.elements[1] as InputField
        `when`(parent.state()).thenReturn(state)
        val msg = mock<Message>()
        val msgTxt = "msgTxt"
        `when`(msg.text).thenReturn(msgTxt)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(parent).state()
        verify(sut).extractId(state.id)
        verify(sut, never()).processInput(element, state, msgTxt)
    }
    @Test
    fun handleIncomingMessageInputFieldInputState() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val state = TeleformElementState(true, false, false, "3.INPUT")
        val element = WordCountCommand.Form.elements[1] as InputField
        `when`(parent.state()).thenReturn(state)
        val msg = mock<Message>()
        val msgTxt = "msgTxt"
        `when`(msg.text).thenReturn(msgTxt)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(parent).state()
        verify(sut).extractId(state.id)
        verify(sut).processInput(element, state, msgTxt)
    }
    @Test
    fun processInputSunnyDay() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = emptyList<TeleformElementState>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val targetProperty = "targetProperty"
        val element = spy(InputFieldForTesting2("", targetProperty, ""))
        val state = mock<TeleformElementState>()
        val text = "text"
        val text2 = "text2"
        val parseRes = FailableOperationResult<Any>(true, "", text2)
        `when`(element.parse(text)).thenReturn(parseRes)
        doNothing().`when`(sut).goToNextSunnyDayState(state)
        val inOrder = inOrder(parent, logger, sut, element, state)

        // Run method under test
        sut.processInput(element, state, text)

        // Verify
        inOrder.verify(element).parse(text)
        inOrder.verify(parent).saveInMemory(targetProperty, text2)
        inOrder.verify(sut).goToNextSunnyDayState(state)
    }
    @Test
    fun processInputRainyDay() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = emptyList<TeleformElementState>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        val targetProperty = "targetProperty"
        val element = spy(InputFieldForTesting2("", targetProperty, ""))
        val state = mock<TeleformElementState>()
        val text = "text"
        val errMsg = "errMsg"
        val parseRes = FailableOperationResult<Any>(false, errMsg, null)
        doReturn(parseRes).`when`(element).parse(text)
        doNothing().`when`(sut).printMessage(errMsg)
        val inOrder = inOrder(parent, logger, sut, element, state)

        // Run method under test
        sut.processInput(element, state, text)

        // Verify
        inOrder.verify(element).parse(text)
        inOrder.verify(sut).printMessage(errMsg)
    }

    @Test
    fun fireCallsSaveDataInSavingState() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = emptyList<TeleformElementState>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        `when`(parent.state()).thenReturn(SavingState)
        doNothing().`when`(sut).saveData()

        // Run method under test
        sut.fire()

        // Verify
        verify(parent).state()
        verify(sut).saveData()
    }

    @Test
    fun saveDataSunnyDay() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = emptyList<TeleformElementState>()
        val logger = mock<Logger>()
        val mongo = mock<IMongoSubsystem>()
        val finalState = mock<TeleformElementState>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        finalState,
                        mongo,
                        logger
                )
        )
        val stuffToSave = mock<Map<String,Any>>()
        `when`(parent.stuffToSave()).thenReturn(stuffToSave)
        val res = ValidationResult(true, "")
        `when`(mongo.insert(stuffToSave, form.collection)).thenReturn(res)

        val inOrder = inOrder(parent, logger, sut, stuffToSave, mongo)

        // Run method under test
        sut.saveData()

        // Verify
        inOrder.verify(parent).stuffToSave()
        inOrder.verify(mongo).insert(stuffToSave, form.collection)
        inOrder.verify(parent).printMessage("Fertig")
        inOrder.verify(parent).goToStateIfPossible(finalState)
    }
    @Test
    fun saveDataRainyDay() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = emptyList<TeleformElementState>()
        val logger = mock<Logger>()
        val mongo = mock<IMongoSubsystem>()
        val finalState = mock<TeleformElementState>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        finalState,
                        mongo,
                        logger
                )
        )
        val stuffToSave = mock<Map<String,Any>>()
        `when`(parent.stuffToSave()).thenReturn(stuffToSave)
        val msg = "msg"
        val res = ValidationResult(false, msg)
        `when`(mongo.insert(stuffToSave, form.collection)).thenReturn(res)


        val inOrder = inOrder(parent, logger, sut, stuffToSave, mongo)

        // Run method under test
        sut.saveData()

        // Verify
        inOrder.verify(parent).stuffToSave()
        inOrder.verify(mongo).insert(stuffToSave, form.collection)
        inOrder.verify(parent).printMessage("Database error ('$msg'). Your data weren't saved. Please contact the admin.")
        inOrder.verify(parent).goToStateIfPossible(finalState)
    }
    @Test
    fun handleIncomingMessageReactsToCancel() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger,
                        tu
                )
        )
        val state = mock<TeleformElementState>()
        `when`(state.waitingState()).thenReturn(true)
        `when`(parent.state()).thenReturn(state)
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(true)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(parent).state()
        verify(tu).cancelCommand(msg)
        verify(parent).goToStateIfPossible(CancellingState)
    }
    @Test
    fun handleIncomingMessageIgnoresCommandsInWaitingState() {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger,
                        tu
                )
        )
        val state = mock<TeleformElementState>()
        `when`(state.waitingState()).thenReturn(true)
        `when`(parent.state()).thenReturn(state)
        val msg = mock<Message>()
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        `when`(msg.isCommand()).thenReturn(true)
        doNothing().`when`(sut).printMessage(TeleformElementHandler.InvalidCommandError)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(parent).state()
        verify(tu).cancelCommand(msg)
        verify(msg).isCommand()
        verify(sut).printMessage(TeleformElementHandler.InvalidCommandError)
    }

    fun handleIncomingMessageWrongStateTypeTestLogic(state:TeleformElementState) {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val state1 = mock<TeleformElementState>()
        val state2 = mock<TeleformElementState>()
        val state3 = mock<TeleformElementState>()
        val sunnyDayStates = listOf(state1, state2, state3)
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )
        `when`(parent.state()).thenReturn(state)
        val msg = mock<Message>()

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(parent).state()
        verify(logger).error("Wrong state type")
    }

    private fun extractIdTestLogic(input: String, expRes: String) {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = emptyList<TeleformElementState>()
        val logger = mock<Logger>()
        val sut = spy(
                TeleformElementHandler(
                        parent,
                        form,
                        sunnyDayStates,
                        mock<TeleformElementState>(),
                        mock<IMongoSubsystem>(),
                        logger
                )
        )

        // Run method under test
        val actRes = sut.extractId(input)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun inputStateTestLogic(input: String, expRes: Boolean) {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = TeleformElementHandler(
                parent,
                form,
                sunnyDayStates,
                mock<TeleformElementState>(),
                mock<IMongoSubsystem>(),
                logger
        )

        // Run method under test
        val actRes = sut.inputState(input)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun messageStateTestLogic(input: String, expRes: Boolean) {
        // Prepare
        val parent = mock<ITeleformParentAutomaton>()
        val form = WordCountCommand.Form
        val sunnyDayStates = mock<List<TeleformElementState>>()
        val logger = mock<Logger>()
        val sut = TeleformElementHandler(
                parent,
                form,
                sunnyDayStates,
                mock<TeleformElementState>(),
                mock<IMongoSubsystem>(),
                logger
        )

        // Run method under test
        val actRes = sut.messageState(input)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}