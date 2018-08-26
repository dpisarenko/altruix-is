package cc.altruix.is1.mongo

import cc.altruix.is1.telegram.cmd.r.WritingStatRow
import com.mongodb.Function
import org.bson.Document
import java.util.*

/**
 * Created by pisarenko on 13.04.2017.
 */
object WritingStatRowMapper : Function<Document, WritingStatRow> {
    override fun apply(doc: Document): WritingStatRow =
            WritingStatRow(
                    doc.getDate(IMongoSubsystem.TimeStampField),
                    doc.getString(IMongoSubsystem.WritingStats_workName),
                    doc.getString(IMongoSubsystem.WritingStats_partName),
                    doc.getInteger(IMongoSubsystem.WritingStats_wordCount)
            )
}