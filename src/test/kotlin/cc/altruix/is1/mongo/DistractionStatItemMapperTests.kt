package cc.altruix.is1.mongo

import cc.altruix.mock
import org.bson.Document
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import java.util.*

/**
 * Created by pisarenko on 12.05.2017.
 */
class DistractionStatItemMapperTests {
    @Test
    fun apply() {
        // Prepare
        val doc = mock<Document>()
        val date = Date()
        `when`(doc.getDate(IMongoSubsystem.TimeStampField)).thenReturn(date)

        // Run method under test
        val actRes = DistractionStatItemMapper.apply(doc)

        // Verify
        verify(doc).getDate(IMongoSubsystem.TimeStampField)
        assertThat(actRes).isSameAs(date)
    }
}