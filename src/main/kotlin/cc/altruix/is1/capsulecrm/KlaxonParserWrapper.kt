package cc.altruix.is1.capsulecrm

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.InputStream

/**
 * Created by pisarenko on 01.02.2017.
 */
class KlaxonParserWrapper(val parser: Parser = Parser() ) : TestableJsonParser {
    override fun parse(input: InputStream): JsonObject = parser.parse(input) as JsonObject
}