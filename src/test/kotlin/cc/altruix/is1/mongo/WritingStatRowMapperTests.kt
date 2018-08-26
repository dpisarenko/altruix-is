package cc.altruix.is1.mongo

import cc.altruix.mock
import org.bson.Document
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.`when`
import java.util.*

/**
 * Created by pisarenko on 13.04.2017.
 */
class WritingStatRowMapperTests {
    @Test
    fun apply() {
        // Prepare
        val sut = WritingStatRowMapper
        val doc = mock<Document>()
        val now = Date()
        `when`(doc.getDate(IMongoSubsystem.TimeStampField)).thenReturn(now)
        val work = "work"
        `when`(doc.getString(IMongoSubsystem.WritingStats_workName)).thenReturn(work)
        val part = "part"
        `when`(doc.getString(IMongoSubsystem.WritingStats_partName)).thenReturn(part)
        val wordCount = 1223
        `when`(doc.getInteger(IMongoSubsystem.WritingStats_wordCount)).thenReturn(wordCount)

        // Run method under test
        val actRes = sut.apply(doc)

        // Verify
        assertThat(actRes.timestamp).isEqualTo(now)
        assertThat(actRes.work).isEqualTo(work)
        assertThat(actRes.part).isEqualTo(part)
        assertThat(actRes.wordCount).isEqualTo(wordCount)
    }
}