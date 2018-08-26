package cc.altruix.is1.mongo

import cc.altruix.common.AbstractMongoSubsystem
import cc.altruix.is1.App
import cc.altruix.is1.telegram.cmd.r.WritingStatRow
import cc.altruix.is1.telegram.cmd.radar.RadarChartData
import cc.altruix.is1.telegram.rawdata.DCmd
import cc.altruix.is1.tpt.TotalProductiveTimeJob
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.utils.toDate
import com.mongodb.BasicDBObject
import com.mongodb.MongoCredential
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.Sorts
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*


/**
 * Created by 1 on 09.04.2017.
 */
open class AltruixIs1MongoSubsystem(
        logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : IAltruixIs1MongoSubsystem, AbstractMongoSubsystem(logger) {
    companion object {
        val AltruixIs1Db = "CENSORED"
        val UserName = "CENSORED"
        val Password = "CENSORED"
        val Host = "CENSORED"
        val Port = 27017
        val RadarChartDataColl = "RadarChartData"
        val GTE = "\$gte"
        val LTE = "\$lte"
    }

    override fun init(): ValidationResult {
        try {
            val altruixIs1 = createMongoCredential(UserName, "admin", Password)
            val srv = createServerAddress(Host, Port)
            mc = createMongoClient(srv, listOf<MongoCredential>(altruixIs1))
            db = mc?.getDatabase(AltruixIs1Db)
            return ValidationResult(true, "")
        } catch (exception:Throwable) {
            logger.error("Initialization of MongoDB subsystem", exception)
            return ValidationResult(false, exception.message ?: "")
        }
    }
    override fun readWritingStats(): FailableOperationResult<List<WritingStatRow>> {
        val db = this.db
        if (db == null) {
            return FailableOperationResult(false, "Internal error", null)
        }
        try {
            return readWritingStatsLogic(db)
        }
        catch (exception:Throwable) {
            logger.error("readWritingStats", exception)
            return FailableOperationResult(false, exception.message ?: "", null)
        }
    }

    open fun readWritingStatsLogic(db: MongoDatabase): FailableOperationResult<List<WritingStatRow>> {
        val coll = db.getCollection(IMongoSubsystem.WritingStatsColl)
        val res = createWritingStatRowList()
        val data = writingItems(coll)
        addToList(data, res)
        return FailableOperationResult(true, "", res)
    }

    open fun createWritingStatRowList():MutableList<WritingStatRow> = ArrayList<WritingStatRow>()

    open fun addToList(data: MongoIterable<WritingStatRow>, res: MutableList<WritingStatRow>) {
        data.forEach {
            res.add(it)
        }
    }

    open fun writingItems(coll: MongoCollection<Document>):
            MongoIterable<WritingStatRow> =
            coll.find()
                    .sort(ascending(IMongoSubsystem.TimeStampField))
                    .map(WritingStatRowMapper)

    open fun ascending(criterion: String) = Sorts.ascending(criterion)

    override fun distractionStats(): FailableOperationResult<List<Date>> {
        val db = this.db
        if (db == null) {
            return FailableOperationResult(false, "Internal error", null)
        }
        try {
            return distractionStatsLogic(db)
        }
        catch (exception:Throwable) {
            logger.error("distractionStats", exception)
            return FailableOperationResult(false, exception.message ?: "", null)
        }
    }

    open fun distractionStatsLogic(db: MongoDatabase): FailableOperationResult<List<Date>> {
        val coll = db.getCollection(DCmd.MongoCollection)
        val list = toList(coll.find().map(DistractionStatItemMapper))
        return FailableOperationResult(true, "", list)
    }

    open fun toList(iterator: MongoIterable<Date>) = iterator.toList()

    override fun totalProductiveTime(
            start: LocalDate,
            end: LocalDate, col: String): FailableOperationResult<Double> {
        val db = this.db
        if (db == null) {
            return FailableOperationResult(false, "Internal error", null)
        }
        try {
            return totalProductiveTimeLogic(db, start, end, col)
        }
        catch (exception:Throwable) {
            logger.error("totalProductiveTime", exception)
            return FailableOperationResult(false, exception.message ?: "", null)
        }
    }

    open fun totalProductiveTimeLogic(
            db: MongoDatabase,
            start: LocalDate,
            end: LocalDate,
            col: String): FailableOperationResult<Double> {
        val coll = db.getCollection(TotalProductiveTimeJob.TotalProductiveTimeColl)
        val condition = createBasicDBObject(2)
        condition.put(GTE, morning(start))
        condition.put(LTE, night(end))
        val query = createBasicDBObject(TotalProductiveTimeJob.StartTimeColumn, condition)
        val results = coll.find(query).projection(fields(col))
        val sum = sum(results, col)
        return FailableOperationResult(true, "", sum)
    }

    open fun fields(column: String) = include(column)

    open fun createBasicDBObject(col: String, condition: BasicDBObject) =
            BasicDBObject(col, condition)

    open fun sum(results: FindIterable<Document>, col: String): Double {
        var sum = 0.0
        results
                .map { x -> x.getDouble(col) }
                .forEach { x -> sum += x }
        return sum
    }

    override fun recordsCount(collName:String,
                              start: LocalDate,
                              end: LocalDate) : FailableOperationResult<Int> {
        val db = this.db
        if (db == null) {
            return FailableOperationResult(false, "Internal error", null)
        }
        try {
            return recordsCountLogic(db, collName, start, end)
        }
        catch (exception:Throwable) {
            logger.error("recordsCount(collName='$collName')", exception)
            return FailableOperationResult(false, exception.message ?: "", null)
        }
    }

    open fun recordsCountLogic(
            db: MongoDatabase,
            collName: String,
            start: LocalDate,
            end: LocalDate
    ): FailableOperationResult<Int> {
        val coll = db.getCollection(collName)
        val condition = createBasicDBObject(2)
        condition.put(GTE, morning(start))
        condition.put("\$lt", night(end))
        val query = BasicDBObject(IMongoSubsystem.TimeStampField, condition)
        val res = coll.find(query).count()
        return FailableOperationResult(true, "", res)
    }

    open fun night(end: LocalDate) = end.atTime(23, 59).toDate()

    open fun createBasicDBObject(size: Int):BasicDBObject = BasicDBObject(size)

    override fun saveRadarData(
            now: LocalDate,
            targetsAbs: RadarChartData,
            actualAbs: RadarChartData
    ):ValidationResult {
        val db = this.db
        if (db == null) {
            return ValidationResult(false, "Internal error")
        }
        try {
            return saveRadarDataLogic(db, now, targetsAbs, actualAbs)
        }
        catch (exception:Throwable) {
            logger.error("saveRadarData", exception)
            return ValidationResult(false, exception.message ?: "")
        }
    }

    open fun saveRadarDataLogic(
            db: MongoDatabase,
            now: LocalDate,
            targetsAbs: RadarChartData,
            actualAbs: RadarChartData
    ):ValidationResult {
        val data = HashMap<String,Any>()
        targetsAbs.amountsByMetric.keys
                .map { metric -> Triple(
                        metric,
                        actualAbs.amountsByMetric[metric],
                        targetsAbs.amountsByMetric[metric]
                )
                }
                .filter { (metric, actual, target) -> (actual != null) && (target != null) }
                .forEach { (metric, actual, target) ->
                    val abbr = metric.abbr.toUpperCase()
                    data["${abbr}_TARGET"] = target!!
                    data["${abbr}_ACTUAL"] = actual!!
                    data["${abbr}_METRIC"] = "${abbr};${metric.unit};\"${metric.name}\""
                }
        data[IMongoSubsystem.TimeStampField] = morning(now)
        data[IMongoSubsystem.SystemVersion] = App.Version
        insert(data, RadarChartDataColl)
        return ValidationResult(true, "")
    }
    open fun morning(start: LocalDate) = start.atStartOfDay().toDate()
}