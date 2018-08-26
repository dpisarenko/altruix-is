package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.mock
import org.junit.Test
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.telegram.telegrambots.api.objects.Document
import org.telegram.telegrambots.api.objects.Message
import java.io.File
import java.io.FileInputStream

/**
 * Created by pisarenko on 03.03.2017.
 */
class WAITING_FOR_FILE_UPLOAD_HandlerTests {
    @Test
    fun handleIncomingMessageCancelHandling() {
        // Prepare
        val parent = mock<IParentBp2CbCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val companyIdExtractor = mock<ICompanyIdExtractor>()
        val sut = spy(WAITING_FOR_FILE_UPLOAD_Handler(
                parent,
                jena,
                tu,
                logger,
                companyIdExtractor
        ))
        val msg = mock<Message>()
        val inOrder = inOrder(parent, jena, tu, logger, companyIdExtractor, sut, msg)
        `when`(tu.cancelCommand(msg)).thenReturn(true)
        doNothing().`when`(sut).printMessage(ITelegramUtils.CancelMessage)
        val document = mock<Document>()
        val fileId = "fileId"
        `when`(msg.document).thenReturn(document)
        `when`(document.fileId).thenReturn(fileId)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut).printMessage(ITelegramUtils.CancelMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp2CbCmdState.CANCELING)
        inOrder.verify(sut, never()).processDocument(fileId)
    }
    @Test
    fun handleIncomingMessageNoDocument() {
        // Prepare
        val parent = mock<IParentBp2CbCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val companyIdExtractor = mock<ICompanyIdExtractor>()
        val sut = spy(WAITING_FOR_FILE_UPLOAD_Handler(
                parent,
                jena,
                tu,
                logger,
                companyIdExtractor
        ))
        val msg = mock<Message>()
        val inOrder = inOrder(parent, jena, tu, logger, companyIdExtractor, sut, msg)
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        doNothing().`when`(sut).printMessage(ITelegramUtils.CancelMessage)
        val document = mock<Document>()
        val fileId = "fileId"
        `when`(msg.document).thenReturn(document)
        `when`(document.fileId).thenReturn(fileId)
        `when`(msg.hasDocument()).thenReturn(false)
        doNothing().`when`(sut).printMessage(WAITING_FOR_FILE_UPLOAD_Handler.NoDocument)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut, never()).printMessage(ITelegramUtils.CancelMessage)
        inOrder.verify(sut, never()).processDocument(fileId)
        inOrder.verify(msg).hasDocument()
        inOrder.verify(sut).printMessage(WAITING_FOR_FILE_UPLOAD_Handler.NoDocument)
        inOrder.verify(parent).goToStateIfPossible(Bp2CbCmdState.CANCELING)
    }
    @Test
    fun handleIncomingMessageSunnyDay() {
        // Prepare
        val parent = mock<IParentBp2CbCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val companyIdExtractor = mock<ICompanyIdExtractor>()
        val sut = spy(WAITING_FOR_FILE_UPLOAD_Handler(
                parent,
                jena,
                tu,
                logger,
                companyIdExtractor
        ))
        val msg = mock<Message>()
        val inOrder = inOrder(parent, jena, tu, logger, companyIdExtractor, sut, msg)
        `when`(tu.cancelCommand(msg)).thenReturn(false)
        doNothing().`when`(sut).printMessage(ITelegramUtils.CancelMessage)
        val document = mock<Document>()
        val fileId = "fileId"
        `when`(msg.document).thenReturn(document)
        `when`(document.fileId).thenReturn(fileId)
        `when`(msg.hasDocument()).thenReturn(true)
        doNothing().`when`(sut).printMessage(WAITING_FOR_FILE_UPLOAD_Handler.NoDocument)
        doNothing().`when`(sut).processDocument(fileId)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        inOrder.verify(tu).cancelCommand(msg)
        inOrder.verify(sut, never()).printMessage(ITelegramUtils.CancelMessage)
        inOrder.verify(msg).hasDocument()
        inOrder.verify(sut, never()).printMessage(WAITING_FOR_FILE_UPLOAD_Handler.NoDocument)
        inOrder.verify(parent, never()).goToStateIfPossible(Bp2CbCmdState.CANCELING)
        inOrder.verify(sut).processDocument(fileId)
    }
    @Test
    fun processDocumentSunnyDay() {
        // Prepare
        val persona = "persona"
        val parent = mock<IParentBp2CbCmdAutomaton>()
        `when`(parent.persona()).thenReturn(persona)
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val companyIdExtractor = mock<ICompanyIdExtractor>()
        val sut = spy(WAITING_FOR_FILE_UPLOAD_Handler(
                parent,
                jena,
                tu,
                logger,
                companyIdExtractor
        ))
        val fileId = "fileId"
        val file = mock<File>()
        `when`(parent.readFileContents(fileId)).thenReturn(file)
        val stream = mock<FileInputStream>()
        doReturn(stream).`when`(sut).openInputStream(file)
        val companyIds:List<String>  = mock()
        `when`(companyIdExtractor.extractCompanyIds(stream)).thenReturn(companyIds)
        val batchId = 1310
        val res = FailableOperationResult<Int>(true, "", batchId)
        `when`(jena.createBatch(companyIds, persona)).thenReturn(res)
        val inOrder = inOrder(parent, jena, tu, logger, companyIdExtractor, sut)
        doNothing().`when`(sut).printMessage("BP 2 batch nr. $batchId successfully created.")
        doNothing().`when`(sut).closeQuietly(stream)

        // Run method under test
        sut.processDocument(fileId)

        // Verify
        inOrder.verify(parent).readFileContents(fileId)
        inOrder.verify(sut).openInputStream(file)
        inOrder.verify(companyIdExtractor).extractCompanyIds(stream)
        inOrder.verify(parent).persona()
        inOrder.verify(jena).createBatch(companyIds, persona)
        inOrder.verify(sut).printMessage("BP 2 batch nr. $batchId successfully created.")
        inOrder.verify(parent).goToStateIfPossible(Bp2CbCmdState.END)
        inOrder.verify(sut).closeQuietly(stream)
        inOrder.verify(parent, never()).goToStateIfPossible(Bp2CbCmdState.CANCELING)
    }
    @Test
    fun processDocumentJenaFailure() {
        processDocumentJenaFailureTestLogic(FailableOperationResult<Int>(false, "err", 1))
        processDocumentJenaFailureTestLogic(FailableOperationResult<Int>(true, "err", null))
    }
    @Test
    fun processDocumentExceptionInJena() {
        // Prepare
        val parent = mock<IParentBp2CbCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val companyIdExtractor = mock<ICompanyIdExtractor>()
        val sut = spy(WAITING_FOR_FILE_UPLOAD_Handler(
                parent,
                jena,
                tu,
                logger,
                companyIdExtractor
        ))
        val fileId = "fileId"
        val file = mock<File>()
        `when`(parent.readFileContents(fileId)).thenReturn(file)
        val stream = mock<FileInputStream>()
        doReturn(stream).`when`(sut).openInputStream(file)
        val companyIds:List<String>  = mock<List<String>>()
        `when`(companyIdExtractor.extractCompanyIds(stream)).thenReturn(companyIds)
        val inOrder = inOrder(parent, jena, tu, logger, companyIdExtractor, sut)
        doNothing().`when`(sut).printMessage("An error occured processing file")
        doNothing().`when`(sut).closeQuietly(stream)

        val message = "fuck-up"
        val throwable = RuntimeException(message)
        val persona = "persona"
        `when`(parent.persona()).thenReturn(persona)
        `when`(jena.createBatch(companyIds, persona)).thenThrow(throwable)

        // Run method under test
        sut.processDocument(fileId)

        // Verify
        inOrder.verify(parent).readFileContents(fileId)
        inOrder.verify(sut).openInputStream(file)
        inOrder.verify(companyIdExtractor).extractCompanyIds(stream)
        inOrder.verify(jena).createBatch(companyIds, persona)
        inOrder.verify(logger).error("WAITING_FOR_FILE_UPLOAD_Handler", throwable)
        inOrder.verify(sut).printMessage("An error occured processing file")
        inOrder.verify(parent).goToStateIfPossible(Bp2CbCmdState.CANCELING)
        inOrder.verify(sut).closeQuietly(stream)
        inOrder.verify(parent, never()).goToStateIfPossible(Bp2CbCmdState.END)
    }

    private fun processDocumentJenaFailureTestLogic(res: FailableOperationResult<Int>) {
        // Prepare
        val parent = mock<IParentBp2CbCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val tu = mock<ITelegramUtils>()
        val logger = mock<Logger>()
        val companyIdExtractor = mock<ICompanyIdExtractor>()
        val sut = spy(WAITING_FOR_FILE_UPLOAD_Handler(
                parent,
                jena,
                tu,
                logger,
                companyIdExtractor
        ))
        val fileId = "fileId"
        val file = mock<File>()
        `when`(parent.readFileContents(fileId)).thenReturn(file)
        val stream = mock<FileInputStream>()
        doReturn(stream).`when`(sut).openInputStream(file)
        val companyIds: List<String> = mock<List<String>>()
        `when`(companyIdExtractor.extractCompanyIds(stream)).thenReturn(companyIds)
        val batchId = 1310
        val persona = "persona"
        `when`(parent.persona()).thenReturn(persona)
        `when`(jena.createBatch(companyIds, persona)).thenReturn(res)
        val inOrder = inOrder(parent, jena, tu, logger, companyIdExtractor, sut)
        doNothing().`when`(sut).printMessage("An error occured processing file and/or creating a batch")
        doNothing().`when`(sut).closeQuietly(stream)

        // Run method under test
        sut.processDocument(fileId)

        // Verify
        inOrder.verify(parent).readFileContents(fileId)
        inOrder.verify(sut).openInputStream(file)
        inOrder.verify(companyIdExtractor).extractCompanyIds(stream)
        inOrder.verify(jena).createBatch(companyIds, persona)
        inOrder.verify(logger).error("WAITING_FOR_FILE_UPLOAD_Handler, error: '${res.error}'")
        inOrder.verify(sut).printMessage("An error occured processing file and/or creating a batch")
        inOrder.verify(parent).goToStateIfPossible(Bp2CbCmdState.CANCELING)
        inOrder.verify(parent, never()).goToStateIfPossible(Bp2CbCmdState.END)
        inOrder.verify(sut).closeQuietly(stream)
    }

}