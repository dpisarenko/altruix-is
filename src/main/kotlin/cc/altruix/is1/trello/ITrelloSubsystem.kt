package cc.altruix.is1.trello

import cc.altruix.is1.validation.FailableOperationResult

/**
 * Created by 1 on 30.04.2017.
 */
interface ITrelloSubsystem {
    companion object {
        val ToDo = "todo"
        val FirstDraft = "firstDraftCompleted"
        val Edited = "edited"
        val Published = "published"
    }
    fun init()
    fun worksStatistics(): FailableOperationResult<Map<String, Any>>
}