package cc.altruix.caw

import cc.altruix.caw.telegram.CawTelegramSubsystem
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.telegram.telegrambots.TelegramBotsApi

/**
 * Created by pisarenko on 05.05.2017.
 */
class CawAppTests {
    @Test
    fun runMongoFailure() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CawApp(logger))
        val mongo = mock<IMongoSubsystem>()
        doReturn(mongo).`when`(sut).createMongo()
        val error = "error"
        val mongoStatus = ValidationResult(false, error)
        `when`(mongo.init()).thenReturn(mongoStatus)

        val inOrder = inOrder(logger, sut, mongo)

        // Run method under test
        sut.run()

        // Verify
        inOrder.verify(sut).createMongo()
        inOrder.verify(mongo).init()
        verify(logger).error("Mongo hat an Patsch'n ('$error').")
        verify(logger).error("Shutting down after a failed start.")
    }
    @Test
    fun runSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CawApp(logger))
        val mongo = mock<IMongoSubsystem>()
        doReturn(mongo).`when`(sut).createMongo()
        val mongoStatus = ValidationResult(true, "")
        `when`(mongo.init()).thenReturn(mongoStatus)
        val botApi = mock<TelegramBotsApi>()
        doReturn(botApi).`when`(sut).createTelegramBotsApi()
        val telegram = mock<CawTelegramSubsystem>()
        doReturn(telegram).`when`(sut).createTelegramSubsystem(botApi, mongo)
        val inOrder = inOrder(logger, sut, mongo, telegram)

        // Run method under test
        sut.run()

        // Verify
        inOrder.verify(sut).createMongo()
        inOrder.verify(mongo).init()
        inOrder.verify(sut).createTelegramBotsApi()
        inOrder.verify(sut).createTelegramSubsystem(botApi, mongo)
        inOrder.verify(telegram).init()
    }
}