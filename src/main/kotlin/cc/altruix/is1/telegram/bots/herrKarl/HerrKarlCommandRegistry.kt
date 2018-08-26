package cc.altruix.is1.telegram.bots.herrKarl

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.AbstractCommandRegistry
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.cmd.Bp1SuCmd
import cc.altruix.is1.telegram.cmd.Bp1UActCmd
import cc.altruix.is1.telegram.cmd.Bp1UDActCmd
import cc.altruix.is1.telegram.cmd.*
import cc.altruix.is1.telegram.cmd.bp2cb.Bp2CbCmd
import cc.altruix.is1.telegram.cmd.r.RCmd
import cc.altruix.is1.telegram.cmd.radar.RadarCmd
import cc.altruix.is1.telegram.forms.AutomatonFactory
import cc.altruix.is1.telegram.forms.IAutomatonFactory
import cc.altruix.is1.telegram.rawdata.*
import cc.altruix.is1.telegram.rawdata.edt.EdtStatsV2Command
import cc.altruix.is1.telegram.rawdata.wordcount.WordCountCommand

/**
 * Created by pisarenko on 10.02.2017.
 */
open class HerrKarlCommandRegistry(
        val capsule: ICapsuleCrmSubsystem,
        val jena: IJenaSubsystem,
        val mongo: IAltruixIs1MongoSubsystem
) : AbstractCommandRegistry() {

    override fun init() {
        commandsByName[Bp1UActCmd.Name] = createBp1UActCmd(jena)
        commandsByName[Bp1SuCmd.Name] = createBp1SuCmd(jena)
        commandsByName[Bp1UDActCmd.Name] = createBp1UDActCmd(jena)
        commandsByName[RCmd.Name] = createReportCommand(mongo)
        val af = createAutomatonFactory()
        commandsByName[WordCountCommand.Name] = createWordCountCommand(af)
        commandsByName[EdtStatsV2Command.Name] = createEdtStatsV2Command(af)
        commandsByName[VocCmd.Name] = createVocCmd(af)
        commandsByName[SwCmd.Name] = createSwCmd(af)
        commandsByName[BbCmd.Name] = createBbCmd(af)
        commandsByName[RdCmd.Name] = createRdCmd(af)
        commandsByName[DCmd.Name] = createDCmd(af)
        commandsByName[EdfCmd.Name] = createEdfCmd(af)
        commandsByName[EsCmd.Name] = createEsCmd(af)
        commandsByName[HCmd.Name] = createHCmd()
        commandsByName[WexCmd.Name] = createWexCmd(af)
        commandsByName[DaCmd.Name] = createDaCmd(mongo)
        commandsByName[RadarCmd.Name] = RadarCmd(mongo)
        commandsByName[SspCmd.Name] = SspCmd(af)
        commandsByName[VosCmd.Name] = VosCmd(af)
        commandsByName[SsrCmd.Name] = SsrCmd(af)
        commandsByName[CafCmd.Name] = CafCmd(af)
        commandsByName[MovCmd.Name] = MovCmd(af)
        commandsByName[EtbCmd.Name] = EtbCmd(af)
        commandsByName[MxCmd.Name] = MxCmd(af)
    }

    open fun createDaCmd(mongo: IAltruixIs1MongoSubsystem) = DaCmd(mongo)

    open fun createWexCmd(af: IAutomatonFactory) = WexCmd(af)

    open fun createHCmd() = HCmd(this)

    open fun createReportCommand(mongo: IAltruixIs1MongoSubsystem): ITelegramCommand =
            RCmd(mongo)

    open fun createEsCmd(af: IAutomatonFactory): ITelegramCommand = EsCmd(af)

    open fun createEdfCmd(af: IAutomatonFactory): ITelegramCommand = EdfCmd(af)

    open fun createDCmd(af: IAutomatonFactory): ITelegramCommand = DCmd(af)

    open fun createRdCmd(af: IAutomatonFactory): ITelegramCommand = RdCmd(af)

    open fun createBbCmd(af: IAutomatonFactory): ITelegramCommand = BbCmd(af)

    open fun createVocCmd(af: IAutomatonFactory) = VocCmd(af)

    open fun createSwCmd(af: IAutomatonFactory) = SwCmd(af)

    open fun createEdtStatsV2Command(af: IAutomatonFactory) = EdtStatsV2Command(af)

    open fun createWordCountCommand(af: IAutomatonFactory) = WordCountCommand(af)

    open fun createAutomatonFactory(): IAutomatonFactory = AutomatonFactory(mongo)

    open fun createBp2CbCmd(jena:IJenaSubsystem): ITelegramCommand = Bp2CbCmd(jena)

    open fun createBp2SCmd(jena: IJenaSubsystem): ITelegramCommand = Bp2SCmd(jena)

    open fun createBp1SuCmd(jena: IJenaSubsystem): ITelegramCommand = Bp1SuCmd(jena)

    open fun createBp1UActCmd(jena: IJenaSubsystem): ITelegramCommand = Bp1UActCmd(jena)

    open fun createBp1UDActCmd(jena: IJenaSubsystem) : ITelegramCommand = Bp1UDActCmd(jena)
}