package cc.altruix.is1.telegram.forms

import cc.altruix.is1.App
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.validation.FailableOperationResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.objects.Message

/**
 * Created by pisarenko on 06.04.2017.
 */
open class TeleformElementHandler(
        val parent: ITeleformParentAutomaton,
        val form: Teleform,
        val sunnyDayStates: List<TeleformElementState>,
        val finalState: TeleformElementState,
        val mongo: IMongoSubsystem,
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName),
        val tu:ITelegramUtils = TelegramUtils()
) : AutomatonMessageHandler<TeleformElementState>(parent) {
    companion object {
        val InvalidCommandError =
                "You can't enter commands, while I'm waiting for input. If you want to cancel, enter /kusch."
    }
    override fun fire() {
        val state = parentAutomaton.state()
        if (state.waitingState() || state.terminalState() || state.initialState()) {
            logger.error("Wrong state type")
            return
        }
        if (state == SavingState) {
            saveData()
            return
        }
        val id = extractId(state.id)
        val element = form.elements.find { it.id() == id }
        if (element == null) {
            logger.error("Null element")
            return
        }
        when {
            (element is StaticText) -> displayStaticText(element, state)
            ((element is InputField) && messageState(state.id)) -> displayInputMessage(element, state)
        }
    }

    open fun saveData() {
        val stuffToSave = parent.stuffToSave()
        val res = mongo.insert(stuffToSave, form.collection)
        if (res.success) {
            parent.printMessage("Fertig")
        } else {
            parent.printMessage("Database error ('${res.error}'). Your data weren't saved. Please contact the admin.")
        }
        parentAutomaton.goToStateIfPossible(finalState)
    }

    open fun displayInputMessage(
            element: InputField,
            state: TeleformElementState
    ) {
        printMessage(element.msg)
        goToNextSunnyDayState(state)
    }

    open fun messageState(id: String): Boolean = id.endsWith(".MSG")

    open fun inputState(id: String): Boolean = id.endsWith(".INPUT")

    open fun displayStaticText(
            staticText: StaticText,
            state: TeleformElementState
    ) {
        printMessage(staticText.text)
        goToNextSunnyDayState(state)
    }

    open fun goToNextSunnyDayState(state: TeleformElementState) {
        val next = findNextSunnyDayState(state)
        parentAutomaton.goToStateIfPossible(next)
    }

    open fun findNextSunnyDayState(
            state: TeleformElementState
    ): TeleformElementState {
        val idx = sunnyDayStates.indexOf(state)
        if (idx < 0) {
            return CancellingState
        }
        val next = idx + 1
        if (next > (sunnyDayStates.size - 1)) {
            return SavingState
        }
        return sunnyDayStates[next]
    }

    open fun extractId(id: String): String {
        if (id.contains(".")) {
            val dot = id.indexOf(".")
            return id.substring(0, dot)
        }
        return id
    }

    override fun handleIncomingMessage(msg: Message) {
        val state = parentAutomaton.state()
        if (!state.waitingState() || state.initialState() ||state.terminalState()) {
            logger.error("Wrong state type")
            return
        }
        if (tu.cancelCommand(msg)) {
            parentAutomaton.goToStateIfPossible(CancellingState)
            return
        }
        if (msg.isCommand) {
            printMessage(InvalidCommandError)
            return
        }
        val id = extractId(state.id)
        val element = form.elements.find { it.id() == id }
        if (element == null) {
            logger.error("Null element")
            return
        }
        when {
            ((element is InputField) && inputState(state.id)) ->
                processInput(element, state, msg.text)
        }
    }
    open fun processInput(
            element: InputField,
            state: TeleformElementState,
            text: String
    ) {
        val parseRes: FailableOperationResult<Any> =
                element.parse(text)
        if (parseRes.success && (parseRes.result != null)) {
            parent.saveInMemory(element.targetProperty, parseRes.result)
            goToNextSunnyDayState(state)
        } else {
            printMessage(parseRes.error)
        }
    }
}