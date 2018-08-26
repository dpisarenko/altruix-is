package cc.altruix.is1.telegram

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.bots.herrKarl.HerrKarl
import cc.altruix.is1.telegram.bots.herrKarl.Wolf
import cc.altruix.mock
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.generics.LongPollingBot

/**
 * Created by pisarenko on 31.01.2017.
 */
class TelegramSubsystemTests {
    @Test
    fun initRegistersAllBots() {
        // Prepare
        val botApi = mock<TelegramBotsApi>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val jena = mock<IJenaSubsystem>()
        val sut = Mockito.spy(TelegramSubsystem(botApi, capsule, jena, mock<IAltruixIs1MongoSubsystem>()))
        doNothing().`when`(sut).initApiContextInitializer()
        val rex = mock<Authenticator>()
        doReturn(rex).`when`(sut).createAuthenticator(jena)
        val protocol = mock<Logger>()
        doReturn(protocol).`when`(sut).createProtocolLogger()
        val wolf = mock<Wolf>()
        doReturn(wolf).`when`(sut).createWolf()
        val herrKarl = mock<HerrKarl>()
        val herrKarlCmdReg = mock<ITelegramCommandRegistry>()
        doReturn(herrKarlCmdReg).`when`(sut).createHerrKarlCmdRegistry(capsule, jena)
        doReturn(herrKarl).`when`(sut).createHerrKarl(protocol, wolf, herrKarlCmdReg)
        val commandRegistry = mock<ITelegramCommandRegistry>()

        doReturn(commandRegistry).`when`(sut).createKnackalCommandRegistry(capsule, jena, herrKarl)
        val frauKnackal = mock<LongPollingBot>()
        doReturn(frauKnackal).`when`(sut).createFrauKnackal(protocol, rex, commandRegistry)


        // Run method under test
        sut.init()

        // Verify
        verify(sut).initApiContextInitializer()
        verify(sut).createAuthenticator(jena)
        verify(sut).createWolf()
        verify(sut).createHerrKarlCmdRegistry(capsule, jena)
        verify(herrKarlCmdReg).init()
        verify(sut).createHerrKarl(protocol, wolf, herrKarlCmdReg)
        verify(sut).createFrauKnackal(protocol, rex, commandRegistry)
        verify(botApi).registerBot(herrKarl)
        verify(botApi).registerBot(frauKnackal)
        verify(sut).createKnackalCommandRegistry(capsule, jena, herrKarl)
        verify(commandRegistry).init()
    }
}