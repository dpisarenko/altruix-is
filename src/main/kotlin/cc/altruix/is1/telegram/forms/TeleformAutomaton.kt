package cc.altruix.is1.telegram.forms

import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.telegram.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import cc.altruix.is1.App
import cc.altruix.utils.timestamp

/**
 * Created by pisarenko on 06.04.2017.
 */
open class TeleformAutomaton(
        val bot: IResponsiveBot,
        val chatId: Long,
        allowedTransitions: Map<TeleformElementState, List<TeleformElementState>>,
        val initState: TeleformElementState,
        val finalState: TeleformElementState,
        val sunnyDayStates: List<TeleformElementState>,
        val form: Teleform,
        val mongo: IMongoSubsystem,
        val tu: ITelegramUtils = TelegramUtils(),
        logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : AbstractAutomaton<TeleformElementState>(allowedTransitions, initState, logger),
        ITelegramCmdAutomaton,
        ITeleformParentAutomaton {
    val formData:MutableMap<String,Any> =
            HashMap<String,Any>()
    override fun start() {
        super.start()
        val targetStates = allowedTransitions[initState]
        if (targetStates == null) {
            logger.error("Target state null")
            return
        }
        if (targetStates.size != 1) {
            logger.error("Ambiguous target state")
            return
        }
        val targetState = targetStates[0]
        if (canChangeState(targetState)) {
            changeState(targetState)
        } else {
            tu.displayError("Invalid transition attempt ($state -> $targetState)", chatId, bot)
        }
    }

    override fun fire() {
    }

    override fun createHandlers():
            Map<TeleformElementState, AutomatonMessageHandler<TeleformElementState>> {
        val universalHandler = createTeleformElementHandler(this, form, sunnyDayStates, finalState)
        val handlers = mutableMapOf<TeleformElementState, AutomatonMessageHandler<TeleformElementState>>()
        sunnyDayStates.forEach {
            handlers[it] = universalHandler
        }
        handlers[SavingState] = universalHandler
        handlers[CancellingState] = createTeleformCancellingHandler(finalState, this)
        return handlers
    }

    open fun createTeleformCancellingHandler(
            termState: TeleformElementState,
            parent: TeleformAutomaton
    ) = TeleformCancellingHandler(termState, parent)

    open fun createTeleformElementHandler(
            parent: TeleformAutomaton,
            form: Teleform,
            sunnyDayStates: List<TeleformElementState>,
            finalState: TeleformElementState
    ) = TeleformElementHandler(
            parent,
            form,
            sunnyDayStates,
            finalState,
            mongo
    )

    override fun unsubscribe() {
        this.bot.unsubscribe(this)
    }

    override fun printMessage(msg: String) {
        tu.sendTextMessage(msg, chatId, bot)
    }
    override fun state(): TeleformElementState = this.state

    override fun saveInMemory(key: String, value: Any) {
        formData[key] = value
    }
    override fun stuffToSave(): Map<String, Any> {
        val stuffToSave = HashMap<String, Any>()
        formData.entries.forEach { entry ->
             stuffToSave[entry.key] = entry.value
        }
        stuffToSave[IMongoSubsystem.TimeStampField] =
                timestamp2()
        return stuffToSave
    }

    open fun timestamp2(): ZonedDateTime = timestamp()
}