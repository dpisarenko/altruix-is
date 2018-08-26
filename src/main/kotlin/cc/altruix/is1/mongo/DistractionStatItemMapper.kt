package cc.altruix.is1.mongo

import org.bson.Document
import java.util.*
import com.mongodb.Function

/**
 * Created by pisarenko on 11.05.2017.
 */
object DistractionStatItemMapper : Function<Document, Date> {
    override fun apply(doc: Document): Date = doc.getDate(IMongoSubsystem.TimeStampField)
}