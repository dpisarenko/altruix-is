package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.App
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.validation.FailableOperationResult
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.objects.Message
import java.io.File
import java.io.InputStream

/**
 * Created by 1 on 25.02.2017.
 */
open class WAITING_FOR_FILE_UPLOAD_Handler(
        val parent: IParentBp2CbCmdAutomaton,
        val jena: IJenaSubsystem,
        val tu: ITelegramUtils = TelegramUtils(),
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName),
        val companyIdExtractor: ICompanyIdExtractor = CompanyIdExtractor()
): AutomatonMessageHandler<Bp2CbCmdState>(
        parent
)  {
    companion object {
        val NoDocument = "Message has no document"
    }
    override fun handleIncomingMessage(msg: Message) {
        if (tu.cancelCommand(msg)) {
            printMessage(ITelegramUtils.CancelMessage)
            parentAutomaton.goToStateIfPossible(Bp2CbCmdState.CANCELING)
            return
        }
        if (!msg.hasDocument()) {
            printMessage(NoDocument)
            parentAutomaton.goToStateIfPossible(Bp2CbCmdState.CANCELING)
            return
        }
        val fileId = msg.document.fileId
        processDocument(fileId)
    }

    open fun processDocument(fileId: String) {
        val file = parent.readFileContents(fileId)
        var stream: InputStream? = null

        try {
            stream = openInputStream(file)
            val companyIds = companyIdExtractor.extractCompanyIds(stream)
            val res: FailableOperationResult<Int> = jena.createBatch(companyIds, parent.persona())
            if (res.success && (res.result != null)) {
                printMessage("BP 2 batch nr. ${res.result} successfully created.")
                parentAutomaton.goToStateIfPossible(Bp2CbCmdState.END)
            } else {
                logger.error("WAITING_FOR_FILE_UPLOAD_Handler, error: '${res.error}'")
                printMessage("An error occured processing file and/or creating a batch")
                parentAutomaton.goToStateIfPossible(Bp2CbCmdState.CANCELING)
            }
        } catch (throwable: Throwable) {
            logger.error("WAITING_FOR_FILE_UPLOAD_Handler", throwable)
            printMessage("An error occured processing file")
            parentAutomaton.goToStateIfPossible(Bp2CbCmdState.CANCELING)
            return
        } finally {
            closeQuietly(stream)
        }
    }

    open fun closeQuietly(stream: InputStream?) {
        IOUtils.closeQuietly(stream)
    }

    open fun openInputStream(file: File?) = FileUtils.openInputStream(file)
}