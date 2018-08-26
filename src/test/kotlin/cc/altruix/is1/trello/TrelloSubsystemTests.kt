package cc.altruix.is1.trello

import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.trello4j.Trello
import org.trello4j.model.Card
import java.time.ZonedDateTime
import java.util.List

/**
 * Created by 1 on 30.04.2017.
 */
class TrelloSubsystemTests {
    @Test
    fun worksStatisticsTrelloNotInitialized() {
        // Prepare
        val sut = spy(TrelloSubsystem())

        // Run method under test
        val actRes = sut.worksStatistics()

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun worksStatisticsSunnyDay() {
        // Prepare
        val sut = spy(TrelloSubsystem())
        val trello = mock<Trello>()
        doReturn(trello).`when`(sut).createTrello()
        val wsRes = FailableOperationResult<Map<String,Any>>(
                true,
                "",
                emptyMap()
        )
        doReturn(wsRes).`when`(sut).worksStatisticsLogic(trello)

        sut.init()

        // Run method under test
        val actRes = sut.worksStatistics()

        // Verify
        verify(sut).createTrello()
        verify(sut).worksStatisticsLogic(trello)
        assertThat(actRes).isSameAs(wsRes)
    }
    @Test
    fun worksStatisticsLogicInvalidData() {
        // Prepare
        val sut = spy(TrelloSubsystem())
        val trl = mock<Trello>()
        val res:Map<String,Any> = emptyMap()
        doReturn(res).`when`(sut).retrieveWorksStats(trl)
        doReturn(true).`when`(sut).invalidData(res)

        // Run method under test
        val actRes = sut.worksStatisticsLogic(trl)

        // Verify
        verify(sut).retrieveWorksStats(trl)
        verify(sut).invalidData(res)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Data reading error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun worksStatisticsLogicSunnyDay() {
        // Prepare
        val sut = spy(TrelloSubsystem())
        val trl = mock<Trello>()
        val res:Map<String,Any> = emptyMap()
        doReturn(res).`when`(sut).retrieveWorksStats(trl)
        doReturn(false).`when`(sut).invalidData(res)

        // Run method under test
        val actRes = sut.worksStatisticsLogic(trl)

        // Verify
        verify(sut).retrieveWorksStats(trl)
        verify(sut).invalidData(res)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isSameAs(res)
    }
    @Test
    fun retrieveWorksStats() {
        // Prepare
        val sut = spy(TrelloSubsystem())
        val trl = mock<Trello>()
        val todoCards = 10
        val firstDraftCards = 20
        val editedCards = 30
        val publishedCards = 40
        doReturn(todoCards).`when`(sut).getCardsInList(TrelloSubsystem.ToDoList, trl)
        doReturn(firstDraftCards).`when`(sut).getCardsInList(TrelloSubsystem.FirstDraftList, trl)
        doReturn(editedCards).`when`(sut).getCardsInList(TrelloSubsystem.EditedList, trl)
        doReturn(publishedCards).`when`(sut).getCardsInList(TrelloSubsystem.PublishedList, trl)
        val timestamp = ZonedDateTime.now()
        doReturn(timestamp).`when`(sut).timestamp2()

        // Run method under test
        val actRes = sut.retrieveWorksStats(trl)

        // Verify
        assertThat(actRes.keys.size).isEqualTo(TrelloSubsystem.TrelloLists.size + 1)
        assertThat(actRes[ITrelloSubsystem.ToDo]).isEqualTo(todoCards)
        assertThat(actRes[ITrelloSubsystem.FirstDraft]).isEqualTo(firstDraftCards)
        assertThat(actRes[ITrelloSubsystem.Edited]).isEqualTo(editedCards)
        assertThat(actRes[ITrelloSubsystem.Published]).isEqualTo(publishedCards)
        assertThat(actRes[IMongoSubsystem.TimeStampField]).isSameAs(timestamp)
    }
    @Test
    fun invalidData() {
        val input1 = emptyMap<String,Any>()
        val input2 = hashMapOf<String,Any>(
                ITrelloSubsystem.ToDo to 1,
                ITrelloSubsystem.FirstDraft to 2,
                ITrelloSubsystem.Edited to 3,
                ITrelloSubsystem.Published to 4
        )
        val input3 = hashMapOf<String,Any>(
                ITrelloSubsystem.ToDo to TrelloSubsystem.Error,
                ITrelloSubsystem.FirstDraft to 2,
                ITrelloSubsystem.Edited to 3,
                ITrelloSubsystem.Published to 4
        )
        invalidDataTestLogic(input1, true)
        invalidDataTestLogic(input2, false)
        invalidDataTestLogic(input3, true)
    }
    @Test
    fun getCardsInListException() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TrelloSubsystem(logger))
        val listId = TrelloSubsystem.EditedList
        val trl = mock<Trello>()
        val msg = "msg"
        val throwable = RuntimeException(msg)
        `when`(trl.getCardsByList(listId)).thenThrow(throwable)

        // Run method under test
        val actRes = sut.getCardsInList(listId, trl)

        // Verify
        verify(logger).error("getCardsInList", throwable)
        assertThat(actRes).isEqualTo(TrelloSubsystem.Error)
    }
    @Test
    fun getCardsInListSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(TrelloSubsystem(logger))
        val listId = TrelloSubsystem.EditedList
        val trl = mock<Trello>()
        val cards = mock<MutableList<Card>>()
        val expRes = 1642
        `when`(cards.size).thenReturn(expRes)
        `when`(trl.getCardsByList(listId)).thenReturn(cards)

        // Run method under test
        val actRes = sut.getCardsInList(listId, trl)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
    @Test
    fun init() {
        // Prepare
        val sut = spy(TrelloSubsystem())
        val trello = mock<Trello>()
        doReturn(trello).`when`(sut).createTrello()

        // Run method under test
        sut.init()

        // Verify
        verify(sut).createTrello()
        assertThat(sut.trello).isSameAs(trello)
    }

    private fun invalidDataTestLogic(res: Map<String, Any>, expRes: Boolean) {
        // Prepare
        val sut = TrelloSubsystem()

        // Run method under test
        val actRes = sut.invalidData(res)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}