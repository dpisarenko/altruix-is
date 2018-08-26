package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.telegram.*
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmdState.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.objects.User

/**
 * Created by pisarenko on 10.02.2017.
 */
open class Bp1AddCmdAutomaton(
        val bot: IResponsiveBot,
        val chatId: Long,
        val capsule: ICapsuleCrmSubsystem,
        val tu: ITelegramUtils = TelegramUtils(),
        logger: Logger = LoggerFactory.getLogger("Bp1AddCmdAutomaton")
) : AbstractAutomaton<Bp1AddCmdState>(
        AllowedTransitions,
        NEW,
        logger
), ITelegramCmdAutomaton, IParentBp1AddCmdAutomaton {
    companion object {
        val AllowedTransitions = mapOf<Bp1AddCmdState, List<Bp1AddCmdState>>(
                NEW to listOf(WAITING_FOR_COMPANY_URL),
                WAITING_FOR_COMPANY_URL to listOf(WAITING_FOR_CONTACT_DATA_TYPE, CANCELING),
                WAITING_FOR_CONTACT_DATA_TYPE to listOf(WAITING_FOR_EMAIL, WAITING_FOR_CONTACT_FORM_URL, CANCELING),
                WAITING_FOR_EMAIL to listOf(WAITING_FOR_NOTE, CANCELING),
                WAITING_FOR_CONTACT_FORM_URL to listOf(WAITING_FOR_NOTE, CANCELING),
                WAITING_FOR_NOTE to listOf(SAVING_DATA_IN_CAPSULE, CANCELING),
                SAVING_DATA_IN_CAPSULE to listOf(END),
                CANCELING to listOf(END),
                END to listOf()
                )
    }

    var mainUrl = ""
    var contactDataType = ContactDataType.UNKNOWN
    var email = ""
    var contactFormUrl = ""
    var note = ""
    var agent = ""

    override fun start() {
        super.start()
        if (canChangeState(WAITING_FOR_COMPANY_URL)) {
            changeState(WAITING_FOR_COMPANY_URL)
        } else {
            tu.displayError("Invalid transition attempt ($state -> $WAITING_FOR_COMPANY_URL)", chatId, bot)
        }
    }
    override fun createHandlers(): Map<Bp1AddCmdState, AutomatonMessageHandler<Bp1AddCmdState>> = mapOf(
            WAITING_FOR_COMPANY_URL to WAITING_FOR_COMPANY_URL_Handler(this),
            WAITING_FOR_CONTACT_DATA_TYPE to WAITING_FOR_CONTACT_DATA_TYPE_Handler(this),
            WAITING_FOR_EMAIL to WAITING_FOR_EMAIL_Handler(this),
            WAITING_FOR_CONTACT_FORM_URL to WAITING_FOR_CONTACT_FORM_URL_Handler(this),
            WAITING_FOR_NOTE to WAITING_FOR_NOTE_Handler(this),
            SAVING_DATA_IN_CAPSULE to SAVING_DATA_IN_CAPSULE_Handler(this, capsule),
            CANCELING to CANCELING_Handler(this)
    )
    override fun saveContactDataType(type: ContactDataType) {
        this.contactDataType = type
    }
    override fun saveEmail(email: String) {
        this.email = email
    }

    override fun saveContactFormUrl(url: String) {
        this.contactFormUrl = url
    }
    override fun saveNote(note: String) {
        this.note = note
    }
    override fun fire() {

    }
    override fun saveMainUrl(url: String) {
        this.mainUrl = url
    }

    override fun companyData(): Bp1CompanyData = Bp1CompanyData(
            this.mainUrl,
            this.contactDataType,
            this.email,
            this.contactFormUrl,
            this.note,
            this.agent
    )
    override fun unsubscribe() {
        this.bot.unsubscribe(this)
    }
    override fun printMessage(msg: String) {
        tu.sendTextMessage(msg, chatId, bot)
    }
    override fun saveAgent(agent: User) {
        this.agent = "First name = '${agent.firstName}', last name = '${agent.lastName}', user name = '${agent.userName}', id = ${agent.id}"
    }
    override fun state(): Bp1AddCmdState = state

}