package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.App
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.*
import cc.altruix.is1.telegram.cmd.bp2cb.Bp2CbCmdState.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Created by 1 on 25.02.2017.
 */
open class Bp2CbCmdAutomaton(
        val bot: IResponsiveBot,
        val chatId: Long,
        val jena: IJenaSubsystem,
        val persona: String,
        val tu: ITelegramUtils = TelegramUtils(),
        logger: Logger = LoggerFactory.getLogger(App.LoggerName)
): AbstractAutomaton<Bp2CbCmdState>(
        AllowedTransitions,
        NEW,
        logger
), IParentBp2CbCmdAutomaton, ITelegramCmdAutomaton {
    companion object {
        val AllowedTransitions = mapOf<Bp2CbCmdState, List<Bp2CbCmdState>>(
            NEW to listOf(WAITING_FOR_FILE_UPLOAD),
            WAITING_FOR_FILE_UPLOAD to listOf(END)
        )
        val FileUploadPrompt = "Please upload the file with the companies you want the worker to contact in scope of this batch."
    }
    override fun start() {
        super.start()
        if (canChangeState(WAITING_FOR_FILE_UPLOAD)) {
            printMessage(FileUploadPrompt)
            changeState(WAITING_FOR_FILE_UPLOAD)
        } else {
            tu.displayError("Invalid transition attempt ($state -> $WAITING_FOR_FILE_UPLOAD)", chatId, bot)
        }
    }

    override fun createHandlers(): Map<Bp2CbCmdState, AutomatonMessageHandler<Bp2CbCmdState>> = mapOf(
        WAITING_FOR_FILE_UPLOAD to WAITING_FOR_FILE_UPLOAD_Handler(this, jena),
        CANCELING to CANCELING_Handler(this)
    )
    override fun unsubscribe() {
        this.bot.unsubscribe(this)
    }

    override fun printMessage(msg: String) {
        tu.sendTextMessage(msg, chatId, bot)
    }
    override fun fire() {

    }
    override fun readFileContents(fileId: String): File? =
            bot.readFileContents(fileId)
    override fun persona(): String = persona

    override fun state(): Bp2CbCmdState = this.state

}