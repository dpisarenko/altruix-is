package cc.altruix.is1.trello

import cc.altruix.is1.validation.FailableOperationResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.trello4j.Trello
import org.trello4j.TrelloImpl
import cc.altruix.is1.App
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.utils.timestamp
import java.time.ZonedDateTime

/**
 * Created by 1 on 30.04.2017.
 */
open class TrelloSubsystem(
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : ITrelloSubsystem {
    companion object {
        val ApiKey = "7786ec055783008d7f993655ba6ff793"
        val Token = "346527f25daab44a1ac34a1d76e03e999663c800e6e624176131b82c9bed34b0"
        val FirstDraftList = "58f459921dd62c44ab266f2c"
        val ToDoList = "58f4598fa5b3238cb5bc7dad"
        val EditedList = "58f459943382f0d7c0914b60"
        val PublishedList = "58f459ad40ac9e6a0cc277b2"
        val Error = -1
        val TrelloLists = arrayOf(
                Pair<String, String>(
                        ITrelloSubsystem.ToDo,
                        ToDoList
                ),
                Pair<String, String>(
                        ITrelloSubsystem.FirstDraft,
                        FirstDraftList
                ),
                Pair<String, String>(
                        ITrelloSubsystem.Edited,
                        EditedList
                ),
                Pair<String, String>(ITrelloSubsystem.Published,
                        PublishedList
                )
        )
    }
    var trello:Trello? = null
    override fun worksStatistics(): FailableOperationResult<Map<String, Any>> {
        val trl = trello
        if (trl == null) {
            return FailableOperationResult<Map<String, Any>>(false, "Internal error", null)
        }
        return worksStatisticsLogic(trl)
    }

    open fun worksStatisticsLogic(trl: Trello): FailableOperationResult<Map<String, Any>> {
        val res = retrieveWorksStats(trl)
        if (invalidData(res)) {
            return FailableOperationResult<Map<String, Any>>(
                    false,
                    "Data reading error",
                    null
            )
        }
        return FailableOperationResult<Map<String, Any>>(
                true,
                "",
                res
        )
    }

    open fun retrieveWorksStats(trl: Trello): Map<String, Any> {
        val res = HashMap<String, Any>()
        TrelloLists.map { (property, listId) ->
            val x = getCardsInList(listId, trl)
            Pair<String, Int>(property, x)
        }.forEach { (x, y) ->
            res[x] = y
        }
        res[IMongoSubsystem.TimeStampField] = timestamp2()
        return res
    }

    open fun timestamp2(): ZonedDateTime = timestamp()

    open fun invalidData(res: Map<String, Any>): Boolean {
        if (res.isEmpty()) {
            return true
        }
        val invalidEntries = res.values.filter { it == Error }.count()
        return invalidEntries > 0
    }

    open fun getCardsInList(listId: String, trl: Trello): Int {
        try {
            val cards = trl.getCardsByList(listId)
            return cards.size
        } catch (t:Throwable) {
            logger.error("getCardsInList", t)
            return Error
        }
    }

    override fun init() {
        trello = createTrello()
    }

    open fun createTrello():Trello = TrelloImpl(ApiKey, Token)
}