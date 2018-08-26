package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.App
import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.*
import cc.altruix.is1.telegram.cmd.bp2cc.Bp2CcCmdState.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by 1 on 04.03.2017.
 */
open class Bp2CcCmdAutomaton(
        val bot: IResponsiveBot,
        val chatId: Long,
        val jena: IJenaSubsystem,
        val batchId: Int,
        val capsule: ICapsuleCrmSubsystem,
        val tu: ITelegramUtils = TelegramUtils(), logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : AbstractAutomaton<Bp2CcCmdState>(
        AllowedTransitions,
        Bp2CcCmdState.NEW,
        logger
), IParentBp2CcCmdAutomaton, ITelegramCmdAutomaton {
    companion object {
        val AllowedTransitions = mapOf<Bp2CcCmdState, List<Bp2CcCmdState>>(
            NEW to listOf<Bp2CcCmdState>(GETTING_NEXT_COMPANY_DATA),
            GETTING_NEXT_COMPANY_DATA to listOf<Bp2CcCmdState>(WAITING_FOR_CONTACT_ATTEMPT_RESULT, CANCELING),
            WAITING_FOR_CONTACT_ATTEMPT_RESULT to listOf<Bp2CcCmdState>(WAITING_FOR_CONTACT_TEXT_AND_NOTE, CANCELING),
            WAITING_FOR_CONTACT_TEXT_AND_NOTE to listOf<Bp2CcCmdState>(SAVING_DATA, CANCELING),
            SAVING_DATA to listOf<Bp2CcCmdState>(END),
            CANCELING to listOf<Bp2CcCmdState>(END),
            END  to listOf<Bp2CcCmdState>()
        )
    }
    var companyToContact: Bp2CompanyData? = null
    var contactTextNote:String? = null

    override fun unsubscribe() {
        this.bot.unsubscribe(this)
    }

    override fun printMessage(msg: String) {
        tu.sendTextMessage(msg, chatId, bot)
    }

    override fun fire() {
    }

    override fun createHandlers(): Map<Bp2CcCmdState, AutomatonMessageHandler<Bp2CcCmdState>> =
        mapOf(
                GETTING_NEXT_COMPANY_DATA to GETTING_NEXT_COMPANY_DATA_Handler(this, jena, capsule),
                WAITING_FOR_CONTACT_ATTEMPT_RESULT to WAITING_FOR_CONTACT_ATTEMPT_RESULT_Handler(this),
                WAITING_FOR_CONTACT_TEXT_AND_NOTE to WAITING_FOR_CONTACT_TEXT_AND_NOTE_Handler(this),
                SAVING_DATA to SAVING_DATA_Handler(this, jena, capsule),
                CANCELING to CANCELING_Handler(this)
        )

    override fun start() {
        super.start()
        if (canChangeState(GETTING_NEXT_COMPANY_DATA)) {
            changeState(GETTING_NEXT_COMPANY_DATA)
        } else {
            tu.displayError(
                    "Invalid transition attempt ($state -> ${Bp2CcCmdState.GETTING_NEXT_COMPANY_DATA})",
                    chatId,
                    bot
            )
        }
    }

    override fun batchId(): Int = batchId

    override fun setCompanyData(comp: Bp2CompanyData) {
        this.companyToContact = comp
    }
    override fun getCompanyData(): Bp2CompanyData? = this.companyToContact
    override fun setContactTextAndNote(txt: String) {
        contactTextNote = txt
    }
    override fun getContactTextAndNote(): String? = contactTextNote
    override fun state(): Bp2CcCmdState = this.state

}