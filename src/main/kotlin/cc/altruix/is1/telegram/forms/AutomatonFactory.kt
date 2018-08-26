package cc.altruix.is1.telegram.forms

import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCmdAutomaton
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.utils.allIdsUnique
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pisarenko on 06.04.2017.
 */
open class AutomatonFactory(
        val mongo: IMongoSubsystem,
        val logger: Logger = LoggerFactory.getLogger("cc.altruix.is1.telegram.AbstractAutomaton")
) : IAutomatonFactory {
    override fun createAutomaton(
            form:Teleform,
            bot: IResponsiveBot,
            chatId: Long
    ): FailableOperationResult<ITelegramCmdAutomaton> {
        if (idsNotUnique(form)) {
            return FailableOperationResult(false, "IDs not unique", null)
        }
        val initialState = createState(false, true, false)
        val finalState = createState(false, false, true)
        val sunnyDayStates = composeSunnyDayStates(form)
        val allowedTransitions: MutableMap<TeleformElementState, List<TeleformElementState>> =
                composeAllowedTransitions(
                        initialState,
                        finalState,
                        sunnyDayStates)
        val res = createTeleformAutomaton(
                allowedTransitions,
                bot, chatId, finalState, form, initialState, sunnyDayStates)
        return FailableOperationResult<ITelegramCmdAutomaton>(true, "", res)
    }

    open fun createTeleformAutomaton(
            allowedTransitions: MutableMap<TeleformElementState, List<TeleformElementState>>,
            bot: IResponsiveBot,
            chatId: Long,
            finalState: TeleformElementState,
            form: Teleform,
            initialState: TeleformElementState,
            sunnyDayStates: MutableList<TeleformElementState>
    ): TeleformAutomaton = TeleformAutomaton(
            bot,
            chatId,
            allowedTransitions as Map<TeleformElementState, List<TeleformElementState>>,
            initialState,
            finalState,
            sunnyDayStates,
            form,
            mongo,
            TelegramUtils(), logger
    )

    open fun createState(waiting:Boolean,
                         initial:Boolean,
                         terminal: Boolean) =
            TeleformElementState(false, true, false)

    open fun idsNotUnique(form: Teleform): Boolean =
            !form.allIdsUnique()

    open fun composeAllowedTransitions(
            initialState: TeleformElementState,
            finalState: TeleformElementState,
            sunnyDayStates: MutableList<TeleformElementState>
    ): MutableMap<TeleformElementState, List<TeleformElementState>> {
        val allowedTransitions: MutableMap<TeleformElementState, List<TeleformElementState>> =
                hashMapOf(CancellingState to listOf(finalState)
                )
        for (i in 0..(sunnyDayStates.size - 2)) {
            val state = sunnyDayStates[i]
            val nextState = sunnyDayStates[i + 1]
            allowedTransitions[state] = createAllowedTransitions(state.waiting, nextState)
        }
        allowedTransitions[initialState] = listOf(sunnyDayStates[0])
        val lastSunnyDayState = sunnyDayStates[sunnyDayStates.size - 1]
        allowedTransitions[lastSunnyDayState] =
                createAllowedTransitions(lastSunnyDayState.waiting, SavingState)
        allowedTransitions[SavingState] = listOf(finalState)
        return allowedTransitions
    }

    open fun createAllowedTransitions(
            waiting: Boolean,
            nextState: TeleformElementState
    ): List<TeleformElementState> = when {
        waiting -> {listOf(nextState, CancellingState)}
        else -> listOf(nextState)
    }

    open fun composeSunnyDayStates(form: Teleform): MutableList<TeleformElementState> {
        val sunnyDayStates = mutableListOf<TeleformElementState>()
        form.elements.map { sunnyDayStates(it) }.forEach{ sunnyDayStates.addAll(it) }
        return sunnyDayStates
    }

    open fun sunnyDayStates(elem:ITeleformElement):List<TeleformElementState> =
        when {
            (elem is StaticText) -> {
                listOf(createStaticTextState(elem))
            }
            (elem is InputField) -> {
                listOf(
                        createInputMessageState(elem),
                        createInputInputState(elem)
                )
            }
            else -> emptyList()
        }

    open fun createInputInputState(elem: ITeleformElement): TeleformElementState {
        return TeleformElementState(
                true,
                false,
                false,
                "${elem.id()}.INPUT"
        )
    }

    open fun createInputMessageState(elem: ITeleformElement): TeleformElementState {
        return TeleformElementState(
                false,
                false,
                false,
                "${elem.id()}.MSG"
        )
    }

    open fun createStaticTextState(elem: ITeleformElement): TeleformElementState {
        return TeleformElementState(
                false,
                false,
                false,
                elem.id()
        )
    }
}