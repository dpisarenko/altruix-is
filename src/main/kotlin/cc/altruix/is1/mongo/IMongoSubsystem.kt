package cc.altruix.is1.mongo

import cc.altruix.is1.telegram.cmd.r.WritingStatRow
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult

/**
 * Created by pisarenko on 06.04.2017.
 */
interface IMongoSubsystem {
    companion object {
        val TimeStampField = "AltruixIS1TimeStamp"
        val WritingStatsColl = "WritingStats"
        val WritingStats_workName = "workName"
        val WritingStats_partName = "partName"
        val WritingStats_wordCount = "wordCount"
        val SystemVersion="AltruixIS1Version"

    }
    fun insert(data:Map<String,Any>, coll:String):ValidationResult

    fun init():ValidationResult
    fun close()
}