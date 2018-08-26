package cc.altruix.is1.mongo

import cc.altruix.is1.telegram.cmd.r.WritingStatRow
import cc.altruix.is1.telegram.rawdata.DCmd
import cc.altruix.is1.tpt.TotalProductiveTimeJob
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoIterable
import org.bson.Document
import org.bson.conversions.Bson
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Created by pisarenko on 12.04.2017.
 */
class AltruixIs1MongoSubsystemTests {
    @Test
    fun insertDatabaseNull() {
        // Prepare
        val sut = AltruixIs1MongoSubsystem()
        val data = emptyMap<String,Any>()
        val coll = "coll"

        // Run method under test
        val actRes = sut.insert(data, coll)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun insertExceptionInInsertLogic() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AltruixIs1MongoSubsystem(logger))
        val data = emptyMap<String,Any>()
        val coll = "coll"
        val db = mock<MongoDatabase>()
        sut.db = db
        val msg = "msg"
        val throwable = RuntimeException(msg)
        doThrow(throwable).`when`(sut).insertLogic(coll, data, db)

        // Run method under test
        val actRes = sut.insert(data, coll)

        // Verify
        verify(sut).insertLogic(coll, data, db)
        verify(logger).error("insert(coll='$coll', data='$data')", throwable)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(msg)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun insertSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AltruixIs1MongoSubsystem(logger))
        val data = emptyMap<String,Any>()
        val coll = "coll"
        val db = mock<MongoDatabase>()
        sut.db = db
        doNothing().`when`(sut).insertLogic(coll, data, db)

        // Run method under test
        val actRes = sut.insert(data, coll)

        // Verify
        verify(sut).insertLogic(coll, data, db)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNull()
    }
    @Test
    fun close() {
        // Prepare
        val sut = AltruixIs1MongoSubsystem()
        val mc = mock<MongoClient>()
        sut.mc = mc

        // Run method under test
        sut.close()

        // Verify
        verify(mc).close()
    }
    @Test
    fun insertLogic() {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        val cname = "coll"
        val data = emptyMap<String,Any>()
        val db = mock<MongoDatabase>()
        val coll = mock<MongoCollection<Document>>()
        `when`(db.getCollection(cname)).thenReturn(coll)

        val doc = mock<Document>()
        doReturn(doc).`when`(sut).createDocument(data)

        val inOrder = inOrder(sut, db, coll)

        // Run method under test
        sut.insertLogic(cname, data, db)

        // Verify
        inOrder.verify(db).getCollection(cname)
        inOrder.verify(sut).createDocument(data)
        inOrder.verify(coll).insertOne(doc)
    }
    @Test
    fun createDocument() {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        val zoneId = ZoneId.of("Europe/Moscow")
        val zdt = ZonedDateTime.of(2017, 4, 12, 10, 8, 32, 0, zoneId)
        val dateKey = "dateKey"
        val textKey = "textKey"
        val text = "text"
        val data = mapOf<String,Any>(
                dateKey to zdt,
                textKey to text
        )
        val date = Date()
        doReturn(date).`when`(sut).toDate(zdt)
        val docData = spy(HashMap<String,Any>())
        doReturn(docData).`when`(sut).createMap()

        // Run method under test
        val actRes = sut.createDocument(data)

        // Verify
        verify(docData).put(textKey, text)
        verify(sut).toDate(zdt)
        verify(docData).put(dateKey, date)
        assertThat(actRes[textKey]).isEqualTo(text)
        assertThat(actRes[dateKey]).isEqualTo(date)
    }
    @Test
    fun toDate() {
        // Prepare
        val sut = AltruixIs1MongoSubsystem()
        val zone = ZoneId.of("Europe/Moscow")
        val zdt = ZonedDateTime.of(2017, 4, 12, 13, 34, 13, 0, zone)

        // Run method under test
        val actRes = sut.toDate(zdt)

        // Verify
        assertThat(actRes.toInstant().toEpochMilli()).isEqualTo(1491993253000L)
    }
    @Test
    fun initSunnyDay() {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        val altruixIs1 = MongoCredential.createScramSha1Credential(
                AltruixIs1MongoSubsystem.UserName,
                "admin",
                AltruixIs1MongoSubsystem.Password.toCharArray())
        doReturn(altruixIs1).`when`(sut).createMongoCredential(
                AltruixIs1MongoSubsystem.UserName,
                "admin",
                AltruixIs1MongoSubsystem.Password
        )

        val mc = mock<MongoClient>()


        val srv = mock<ServerAddress>()
        doReturn(srv).`when`(sut).createServerAddress(AltruixIs1MongoSubsystem.Host,
                AltruixIs1MongoSubsystem.Port)
        doReturn(mc).`when`(sut).createMongoClient(srv, listOf(altruixIs1))
        val db = mock<MongoDatabase>()
        `when`(mc.getDatabase(AltruixIs1MongoSubsystem.AltruixIs1Db)).thenReturn(db)

        // Run method under test
        val actRes = sut.init()

        // Verify
        verify(sut).createMongoCredential(AltruixIs1MongoSubsystem.UserName,
                "admin", AltruixIs1MongoSubsystem.Password)
        verify(sut).createServerAddress(AltruixIs1MongoSubsystem.Host,
                AltruixIs1MongoSubsystem.Port)
        verify(mc).getDatabase(AltruixIs1MongoSubsystem.AltruixIs1Db)
        assertThat(sut.mc).isSameAs(mc)
        assertThat(sut.db).isSameAs(db)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNull()
    }
    @Test
    fun initRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AltruixIs1MongoSubsystem(logger))
        val msg = "msg"
        val throwable = RuntimeException(msg)
        doThrow(throwable).`when`(sut).createMongoCredential(
                AltruixIs1MongoSubsystem.UserName,
                "admin",
                AltruixIs1MongoSubsystem.Password
        )

        // Run method under test
        val actRes = sut.init()

        // Verify
        verify(sut).createMongoCredential(
                AltruixIs1MongoSubsystem.UserName,
                "admin",
                AltruixIs1MongoSubsystem.Password
        )
        verify(logger).error("Initialization of MongoDB subsystem", throwable)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(msg)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun readWritingStatsDbNull() {
        // Prepare
        val sut = AltruixIs1MongoSubsystem()

        // Run method under test
        val actRes = sut.readWritingStats()

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun readWritingStatsException() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AltruixIs1MongoSubsystem(logger))
        val db = mock<MongoDatabase>()
        sut.db = db
        val msg = "msg"
        val throwable = RuntimeException(msg)
        doThrow(throwable).`when`(sut).readWritingStatsLogic(db)

        // Run method under test
        val actRes = sut.readWritingStats()

        // Verify
        verify(sut).readWritingStatsLogic(db)
        verify(logger).error("readWritingStats", throwable)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(msg)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun readWritingSunnyDay() {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        val db = mock<MongoDatabase>()
        sut.db = db
        val expRes = FailableOperationResult<List<WritingStatRow>>(true, "", emptyList())
        doReturn(expRes).`when`(sut).readWritingStatsLogic(db)

        // Run method under test
        val actRes = sut.readWritingStats()

        // Verify
        verify(sut).readWritingStatsLogic(db)
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    fun readWritingStatsLogic() {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        val db = mock<MongoDatabase>()
        val coll = mock<MongoCollection<Document>>()
        `when`(db.getCollection(IMongoSubsystem.WritingStatsColl)).thenReturn(coll)
        val findRes = mock<FindIterable<Document>>()
        `when`(coll.find()).thenReturn(findRes)
        val descending = mock<Bson>()
        doReturn(descending).`when`(sut).ascending(IMongoSubsystem.TimeStampField)
        val sortRes = mock<FindIterable<Document>>()
        `when`(findRes.sort(descending)).thenReturn(sortRes)
        val mapRes = mock<MongoIterable<WritingStatRow>>()
        `when`(sortRes.map(WritingStatRowMapper)).thenReturn(mapRes)
        val res = mock<MutableList<WritingStatRow>>()
        doReturn(res).`when`(sut).createWritingStatRowList()
        doNothing().`when`(sut).addToList(mapRes, res)

        val inOrder = inOrder(sut, db, coll, findRes, descending, res, mapRes,
                sortRes)

        // Run method under test
        val actRes = sut.readWritingStatsLogic(db)

        // Verify
        inOrder.verify(db).getCollection(IMongoSubsystem.WritingStatsColl)
        inOrder.verify(sut).createWritingStatRowList()
        inOrder.verify(coll).find()
        inOrder.verify(sut).ascending(IMongoSubsystem.TimeStampField)
        inOrder.verify(findRes).sort(descending)
        inOrder.verify(sortRes).map(WritingStatRowMapper)
        inOrder.verify(sut).addToList(mapRes, res)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isSameAs(res)
    }
    @Test
    fun writingItems() {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        val coll = mock<MongoCollection<Document>>()
        val findRes = mock<FindIterable<Document>>()
        `when`(coll.find()).thenReturn(findRes)
        val ascending = mock<Bson>()
        doReturn(ascending).`when`(sut).ascending(IMongoSubsystem.TimeStampField)
        val sortRes = mock<FindIterable<Document>>()
        `when`(findRes.sort(ascending)).thenReturn(sortRes)
        val expRes = mock<MongoIterable<WritingStatRow>>()
        `when`(sortRes.map(WritingStatRowMapper)).thenReturn(expRes)

        // Run method under test
        val actRes:MongoIterable<WritingStatRow> = sut.writingItems(coll)

        // Verify
        verify(coll).find()
        verify(sut).ascending(IMongoSubsystem.TimeStampField)
        verify(findRes).sort(ascending)
        verify(sortRes).map(WritingStatRowMapper)
        assertThat(actRes as Any).isSameAs(expRes)
    }
    @Test
    fun distractionStatsDbNotInitialized() {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        assertThat(sut.db).isNull()

        // Run method under test
        val actRes = sut.distractionStats()

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun distractionStatsException() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AltruixIs1MongoSubsystem(logger))
        val db = mock<MongoDatabase>()
        sut.db = db

        val msg = "msg"
        val throwable = RuntimeException(msg)
        doThrow(throwable).`when`(sut).distractionStatsLogic(db)

        // Run method under test
        val actRes = sut.distractionStats()

        // Verify
        verify(sut).distractionStatsLogic(db)
        verify(logger).error("distractionStats", throwable)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(msg)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun distractionStatsSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AltruixIs1MongoSubsystem(logger))
        val db = mock<MongoDatabase>()
        sut.db = db

        val expRes = FailableOperationResult<List<Date>>(true, "", emptyList())
        doReturn(expRes).`when`(sut).distractionStatsLogic(db)

        // Run method under test
        val actRes = sut.distractionStats()

        // Verify
        verify(sut).distractionStatsLogic(db)
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    fun distractionStatsLogic() {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        val db = mock<MongoDatabase>()

        val coll = mock<MongoCollection<Document>>()
        `when`(db.getCollection(DCmd.MongoCollection)).thenReturn(coll)

        val findRes = mock<FindIterable<Document>>()
        `when`(coll.find()).thenReturn(findRes)
        val mapRes = mock<MongoIterable<Date>>()
        `when`(findRes.map(DistractionStatItemMapper)).thenReturn(mapRes)

        val toListRes = mock<List<Date>>()
        doReturn(toListRes).`when`(sut).toList(mapRes)

        // Run method under test
        val actRes = sut.distractionStatsLogic(db)

        // Verify
        verify(db).getCollection(DCmd.MongoCollection)
        verify(coll).find()
        verify(findRes).map(DistractionStatItemMapper)
        verify(sut).toList(mapRes)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isSameAs(toListRes)
    }
    @Test
    fun totalProductiveTimeNotInitialized() {
        // Prepare
        val sut = AltruixIs1MongoSubsystem()
        val start = LocalDate.of(2017, 5, 15)
        val end = LocalDate.of(2017, 5, 21)

        // Run method under test
        val actRes = sut.totalProductiveTime(start, end, TotalProductiveTimeJob.TotalColumn)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun totalProductiveTimeException() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AltruixIs1MongoSubsystem(logger))
        val start = LocalDate.of(2017, 5, 15)
        val end = LocalDate.of(2017, 5, 21)
        val db = mock<MongoDatabase>()
        sut.db = db
        val msg = "msg"
        val t = RuntimeException(msg)
        doThrow(t).`when`(sut).totalProductiveTimeLogic(db, start, end, TotalProductiveTimeJob.TotalColumn)

        // Run method under test
        val actRes = sut.totalProductiveTime(start, end, TotalProductiveTimeJob.TotalColumn)

        // Verify
        verify(sut).totalProductiveTimeLogic(db, start, end, TotalProductiveTimeJob.TotalColumn)
        verify(logger).error("totalProductiveTime", t)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(msg)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun totalProductiveTimeSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(AltruixIs1MongoSubsystem(logger))
        val start = LocalDate.of(2017, 5, 15)
        val end = LocalDate.of(2017, 5, 21)
        val db = mock<MongoDatabase>()
        sut.db = db
        val msg = "msg"
        val res = FailableOperationResult<Double>(true, "", 3133.1)
        doReturn(res).`when`(sut).totalProductiveTimeLogic(db, start, end, TotalProductiveTimeJob.TotalColumn)

        // Run method under test
        val actRes = sut.totalProductiveTime(start, end, TotalProductiveTimeJob.TotalColumn)

        // Verify
        verify(sut).totalProductiveTimeLogic(db, start, end, TotalProductiveTimeJob.TotalColumn)
        assertThat(actRes).isSameAs(res)
    }
    @Test
    fun totalProductiveTimeLogic() {
        totalProductiveTimeLogicTestLogic(TotalProductiveTimeJob.TotalColumn)
        totalProductiveTimeLogicTestLogic(TotalProductiveTimeJob.SvWorldBuilding)
        totalProductiveTimeLogicTestLogic(TotalProductiveTimeJob.ScreenWritingMarketing)
    }

    private fun totalProductiveTimeLogicTestLogic(inputCol: String) {
        // Prepare
        val sut = spy(AltruixIs1MongoSubsystem())
        val db = mock<MongoDatabase>()
        val start = LocalDate.of(2017, 5, 15)
        val end = LocalDate.of(2017, 5, 21)
        val coll = mock<MongoCollection<Document>>()
        `when`(db.getCollection(TotalProductiveTimeJob.TotalProductiveTimeColl)).thenReturn(coll)
        val condition = mock<BasicDBObject>()
        doReturn(condition).`when`(sut).createBasicDBObject(2)
        val morning = mock<Date>()
        doReturn(morning).`when`(sut).morning(start)
        val night = mock<Date>()
        doReturn(night).`when`(sut).night(end)
        val query = mock<BasicDBObject>()
        doReturn(query).`when`(sut).createBasicDBObject(TotalProductiveTimeJob.StartTimeColumn, condition)
        val fields = mock<Bson>()
        doReturn(fields).`when`(sut).fields(inputCol)
        val findRes = mock<FindIterable<Document>>()
        `when`(coll.find(query)).thenReturn(findRes)
        val projectionRes = mock<FindIterable<Document>>()
        `when`(findRes.projection(fields)).thenReturn(projectionRes)
        val sum = 12.34
        doReturn(sum).`when`(sut).sum(projectionRes, inputCol)

        val inOrder = inOrder(sut, db, condition, morning, night, query, fields,
                findRes, projectionRes, coll)


        // Run method under test
        val actRes = sut.totalProductiveTimeLogic(db, start, end, inputCol)

        // Verify
        inOrder.verify(db).getCollection(TotalProductiveTimeJob.TotalProductiveTimeColl)
        inOrder.verify(sut).createBasicDBObject(2)
        inOrder.verify(sut).morning(start)
        inOrder.verify(condition).put(AltruixIs1MongoSubsystem.GTE, morning)
        inOrder.verify(sut).night(end)
        inOrder.verify(condition).put(AltruixIs1MongoSubsystem.LTE, night)
        inOrder.verify(sut).createBasicDBObject(TotalProductiveTimeJob.StartTimeColumn, condition)
        inOrder.verify(coll).find(query)
        inOrder.verify(sut).fields(inputCol)
        inOrder.verify(findRes).projection(fields)
        inOrder.verify(sut).sum(projectionRes, inputCol)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo(sum)
    }
}