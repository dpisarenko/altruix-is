package cc.altruix.is1.capsulecrm

import com.beust.klaxon.JsonObject
import java.io.InputStream

/**
 * Created by pisarenko on 01.02.2017.
 */
interface TestableJsonParser {
    fun parse(input: InputStream):JsonObject
}