package cc.altruix.common

import cc.altruix.is1.validation.ValidationResult
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.slf4j.Logger
import java.time.ZonedDateTime
import java.util.*

/**
 * Created by pisarenko on 02.05.2017.
 */
abstract class AbstractMongoSubsystem(val logger: Logger) {
    var mc: MongoClient? = null
    var db: MongoDatabase? = null
    open fun createMap(): MutableMap<String, Any> = HashMap<String, Any>()
    // Source: http://stackoverflow.com/questions/41127665/zoneddatetime-with-mongodb
    open fun toDate(zdt: ZonedDateTime): Date = Date.from(zdt.toInstant())

    open fun createServerAddress(host: String, port: Int) = ServerAddress(host, port)
    open fun createMongoClient(
            serverAddress: ServerAddress,
            credentials: List<MongoCredential>
    ): MongoClient {
        return MongoClient(
                serverAddress,
                credentials)
    }

    open fun createMongoCredential(
            user: String,
            dbName: String,
            password: String
    ): MongoCredential {
        return MongoCredential.createScramSha1Credential(
                user,
                dbName,
                password.toCharArray())
    }

    fun close() {
        mc?.close()
    }

    fun insert(data: Map<String, Any>, coll: String): ValidationResult {
        val db = this.db
        if (db == null) {
            return ValidationResult(false, "Internal error")
        }
        try {
            insertLogic(coll, data, db)
            return ValidationResult(true, "")
        }
        catch (exception: Throwable) {
            logger.error("insert(coll='$coll', data='$data')", exception)
            return ValidationResult(false, exception.message ?: "")
        }
    }

    open fun insertLogic(cname: String, data: Map<String, Any>, db: MongoDatabase) {
        val coll = db.getCollection(cname)
        coll.insertOne(createDocument(data))
    }

    open fun createDocument(data: Map<String, Any>): Document {
        val docData = createMap()
        data.entries.filter { it.value !is ZonedDateTime }.forEach {
            docData[it.key] = it.value
        }
        data.entries.filter { it.value is ZonedDateTime }.forEach {
            docData[it.key] = toDate(it.value as ZonedDateTime)
        }
        return Document(docData)
    }

    protected fun initLogic(conf: MongoConfiguration): ValidationResult {
        try {
            val altruixIs1 = createMongoCredential(
                    conf.userName,
                    "admin",
                    conf.password
            )
            val srv = createServerAddress(conf.host, conf.port)
            mc = createMongoClient(srv, listOf<MongoCredential>(altruixIs1))
            db = mc?.getDatabase(conf.dbName)
            return ValidationResult(true, "")
        } catch (exception: Throwable) {
            logger.error("Initialization of MongoDB subsystem", exception)
            return ValidationResult(false, exception.message ?: "")
        }
    }
}