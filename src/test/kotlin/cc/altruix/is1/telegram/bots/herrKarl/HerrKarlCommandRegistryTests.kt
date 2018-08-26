package cc.altruix.is1.telegram.bots.herrKarl

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.cmd.*
import cc.altruix.is1.telegram.cmd.bp2cb.Bp2CbCmd
import cc.altruix.is1.telegram.cmd.r.RCmd
import cc.altruix.is1.telegram.forms.IAutomatonFactory
import cc.altruix.is1.telegram.rawdata.*
import cc.altruix.is1.telegram.rawdata.edt.EdtStatsV2Command
import cc.altruix.is1.telegram.rawdata.wordcount.WordCountCommand
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by pisarenko on 10.02.2017.
 */
class HerrKarlCommandRegistryTests {
    @Test
    fun init() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val jena = mock<IJenaSubsystem>()
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val sut = spy(HerrKarlCommandRegistry(capsule, jena, mongo))
        val bp1uact = mock<ITelegramCommand>()
        doReturn(bp1uact).`when`(sut).createBp1UActCmd(jena)
        val bp1su = mock<ITelegramCommand>()
        doReturn(bp1su).`when`(sut).createBp1SuCmd(jena)
        val bp1udact = mock<ITelegramCommand>()
        doReturn(bp1udact).`when`(sut).createBp1UDActCmd(jena)
        val wct = mock<WordCountCommand>()
        val automatonFactory = mock< IAutomatonFactory>()
        doReturn(automatonFactory).`when`(sut).createAutomatonFactory()
        doReturn(wct).`when`(sut).createWordCountCommand(automatonFactory)
        val rcmd = mock<RCmd>()
        doReturn(rcmd).`when`(sut).createReportCommand(mongo)
        val af = mock<IAutomatonFactory>()
        doReturn(af).`when`(sut).createAutomatonFactory()
        doReturn(wct).`when`(sut).createWordCountCommand(af)
        val eds = mock<EdtStatsV2Command>()
        doReturn(eds).`when`(sut).createEdtStatsV2Command(af)
        val voc = mock<VocCmd>()
        doReturn(voc).`when`(sut).createVocCmd(af)
        val sw = mock<SwCmd>()
        doReturn(sw).`when`(sut).createSwCmd(af)
        val bb = mock<BbCmd>()
        doReturn(bb).`when`(sut).createBbCmd(af)
        val rd = mock<RdCmd>()
        doReturn(rd).`when`(sut).createRdCmd(af)
        val d = mock<DCmd>()
        doReturn(d).`when`(sut).createDCmd(af)
        val edf = mock<EdfCmd>()
        doReturn(edf).`when`(sut).createEdfCmd(af)
        val es = mock<EsCmd>()
        doReturn(es).`when`(sut).createEsCmd(af)
        val h = mock<HCmd>()
        doReturn(h).`when`(sut).createHCmd()
        val wex = mock<WexCmd>()
        doReturn(wex).`when`(sut).createWexCmd(af)
        val da = mock<DaCmd>()
        doReturn(da).`when`(sut).createDaCmd(mongo)

        // Run method under test
        sut.init()
        val actBp1UAct = sut.find(Bp1UActCmd.Name)
        val actBp1Su = sut.find(Bp1SuCmd.Name)
        val actBp1UDact = sut.find(Bp1UDActCmd.Name)
        val actWct = sut.find(WordCountCommand.Name)
        val actRcmd = sut.find(RCmd.Name)
        val actEds = sut.find(EdtStatsV2Command.Name)
        val actVoc = sut.find(VocCmd.Name)
        val actSw = sut.find(SwCmd.Name)
        val actBb = sut.find(BbCmd.Name)
        val actRd = sut.find(RdCmd.Name)
        val actD = sut.find(DCmd.Name)
        val actEdf = sut.find(EdfCmd.Name)
        val actEs = sut.find(EsCmd.Name)
        val actH = sut.find(HCmd.Name)
        val actWex = sut.find(WexCmd.Name)
        val actDa = sut.find(DaCmd.Name)

        // Verify
        verify(sut).createBp1UActCmd(jena)
        verify(sut).createBp1SuCmd(jena)
        verify(sut).createBp1UDActCmd(jena)
        verify(sut).createWordCountCommand(af)
        verify(sut).createEdtStatsV2Command(af)
        verify(sut).createVocCmd(af)
        verify(sut).createSwCmd(af)
        verify(sut).createRdCmd(af)
        verify(sut).createBbCmd(af)
        verify(sut).createDCmd(af)
        verify(sut).createEdfCmd(af)
        verify(sut).createEsCmd(af)
        verify(sut).createHCmd()
        verify(sut).createWexCmd(af)
        verify(sut).createDaCmd(mongo)
        assertThat(actBp1UAct).isSameAs(bp1uact)
        assertThat(actBp1Su).isSameAs(bp1su)
        assertThat(actBp1UDact).isSameAs(bp1udact)
        assertThat(actWct).isSameAs(wct)
        assertThat(actRcmd).isSameAs(actRcmd)
        assertThat(actEds).isSameAs(eds)
        assertThat(actVoc).isSameAs(voc)
        assertThat(actSw).isSameAs(sw)
        assertThat(actBb).isSameAs(bb)
        assertThat(actRd).isSameAs(rd)
        assertThat(actD).isSameAs(d)
        assertThat(actEdf).isSameAs(edf)
        assertThat(actEs).isSameAs(es)
        assertThat(actH).isSameAs(h)
        assertThat(actWex).isSameAs(wex)
        assertThat(actDa).isSameAs(da)
    }
    @Test
    fun initRegistersBp2Commands() {
        // Prepare
        val capsule = mock<ICapsuleCrmSubsystem>()
        val jena = mock<IJenaSubsystem>()
        val sut = spy(HerrKarlCommandRegistry(capsule, jena, mock<IAltruixIs1MongoSubsystem>()))

        val bp2SCmd = mock<ITelegramCommand>()
        doReturn(bp2SCmd).`when`(sut).createBp2SCmd(jena)

        val bp2CbCmd = mock<ITelegramCommand>()
        doReturn(bp2CbCmd).`when`(sut).createBp2CbCmd(jena)

        // Run method under test
        sut.init()

        // Verify
        verify(sut, never()).createBp2SCmd(jena)
        assertThat(sut.find(Bp2SCmd.Name)).isNull()
        verify(sut, never()).createBp2CbCmd(jena)
        assertThat(sut.find(Bp2CbCmd.Name)).isNull()
    }
}