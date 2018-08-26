package cc.altruix.is1.telegram.bots.herrKarl

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.*
import cc.altruix.is1.telegram.bots.knackal.KommissarRex
import cc.altruix.is1.telegram.cmd.Bp1SuCmd
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.telegram.telegrambots.api.methods.GetFile
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User
import java.io.File

/**
 * Created by pisarenko on 31.01.2017.
 */
class HerrKarlTests {
    @Test
    fun onUpdateReceivedHandlesRightUserCorrectly() {
        // Prepare
        val auth = mock<Authenticator>()
        val sut = spy(HerrKarl(mock<Logger>(), mock<Logger>(), auth, mock<ITelegramCommandRegistry>()))
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(msg.text).thenReturn("Cars drive faster because they have brakes")
        doReturn(true).`when`(auth).rightUser(update)
        doNothing().`when`(sut).logInvalidAccessAttempt(update, msg)
        doNothing().`when`(sut).handleIncomingMessage(msg)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(auth).rightUser(update)
        verify(sut, never()).logInvalidAccessAttempt(update, msg)
        verify(sut).handleIncomingMessage(msg)
    }
    @Test
    fun onUpdateReceivedHandlesWrongUserCorrectly() {
        // Prepare
        val auth = mock<Authenticator>()
        val sut = spy(HerrKarl(mock<Logger>(), mock<Logger>(), auth, mock<ITelegramCommandRegistry>()))
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(msg.text).thenReturn("Cars drive faster because they have brakes")
        doReturn(true).`when`(auth).rightUser(update)
        doNothing().`when`(sut).logInvalidAccessAttempt(update, msg)
        doNothing().`when`(sut).handleIncomingMessage(msg)

        // Run
        sut.onUpdateReceived(update)

        // Verify
        verify(auth).rightUser(update)
        verify(sut, never()).logInvalidAccessAttempt(update, msg)
        verify(sut).handleIncomingMessage(msg)
    }

    @Test
    fun logInvalidAccessAttempt() {
        logInvalidAccessAttemptTestLogic(createUpdateWithNullFields(),
                "Some asshole tried to contact me. Update ID: 0. Sender ID: 1131. First name: 'null'. Last name: 'null'. User name: 'null'"
        )
        logInvalidAccessAttemptTestLogic(createUpdateWithFromMrHankey(),
                "Some asshole tried to contact me. Update ID: 12345. Sender ID: 1120. First name: 'Bill'. Last name: 'Hankey'. User name: 'MrHankey23'"
        )
    }

    @Test
    fun sendBroadcastSendsMessageIfChatNotNull() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        mock<ITelegramCommandRegistry>(),
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val msg = mock<Message>()
        val chatId = 1242L
        `when`(msg.chatId).thenReturn(chatId)
        `when`(msg.text).thenReturn("Hallöchen")
        val update = mock<Update>()
        `when`(update.message).thenReturn(msg)
        `when`(update.hasMessage()).thenReturn(true)
        `when`(auth.rightUser(update)).thenReturn(true)
        doNothing().`when`(sut).handleIncomingMessage(msg)

        val msgToSend = "msg"

        // Run method under test
        sut.onUpdateReceived(update)
        sut.sendBroadcast(msgToSend)

        // Verify
        verify(tu).sendTextMessage(msgToSend, chatId, sut)
    }
    @Test
    fun sendBroadcastDoesNotSendMessageIfChatNull() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        mock<ITelegramCommandRegistry>(),
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val msgToSend = "msg"

        // Run method under test
        sut.sendBroadcast(msgToSend)

        // Verify
        // If it attempts to send it (without null check), we'll have NPE here
    }
    @Test
    fun onUpdateReceivedDoesNotHandleIncomingMessageIfItsCommand() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        mock<ITelegramCommandRegistry>(),
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val update = mock<Update>()
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(update.hasMessage()).thenReturn(true)
        `when`(msg.text).thenReturn(Bp1SuCmd.Name)
        `when`(auth.rightUser(update)).thenReturn(true)
        val chatId = 1630L
        `when`(msg.chatId).thenReturn(chatId)
        doReturn(true).`when`(sut).executeCommand(msg, chatId)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(sut).executeCommand(msg, chatId)
        verify(sut, never()).handleIncomingMessage(msg)
    }
    @Test
    fun onUpdateReceivedHandlesIncomingMessageIfItsNotCommand() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        mock<ITelegramCommandRegistry>(),
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val update = mock<Update>()
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(update.hasMessage()).thenReturn(true)
        `when`(msg.text).thenReturn(Bp1SuCmd.Name)
        `when`(auth.rightUser(update)).thenReturn(true)
        val chatId = 1630L
        `when`(msg.chatId).thenReturn(chatId)
        doReturn(false).`when`(sut).executeCommand(msg, chatId)
        doNothing().`when`(sut).handleIncomingMessage(msg)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(sut).executeCommand(msg, chatId)
        verify(sut).handleIncomingMessage(msg)
    }
    @Test
    fun executeCommandReturnsTrueIfCommandExecuted() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val cmdReg = mock<ITelegramCommandRegistry>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        cmdReg,
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val msg = mock<Message>()
        `when`(msg.isCommand).thenReturn(true)
        val msgName = "msgName"
        doReturn(msgName).`when`(sut).extractCmdName(msg)
        val cmd = mock<ITelegramCommand>()
        `when`(cmdReg.find(msgName)).thenReturn(cmd)
        val chatId = 1717L
        val msgTxt = "msgTxt"
        `when`(msg.text).thenReturn(msgTxt)
        val from = mock<User>()
        `when`(msg.from).thenReturn(from)
        val userId = 1002
        `when`(from.id).thenReturn(userId)
        val inOrder = inOrder(auth, tu, sut, msg, cmdReg, cmd)

        // Run method under test
        val actRes = sut.executeCommand(msg, chatId)

        // Verify
        assertThat(actRes).isTrue()
        inOrder.verify(sut).extractCmdName(msg)
        inOrder.verify(cmdReg).find(msgName)
        inOrder.verify(cmd).execute(msgTxt, sut, chatId, userId)
    }
    @Test
    fun executeCommandReturnsFalseIfNotCommand() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val cmdReg = mock<ITelegramCommandRegistry>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        cmdReg,
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val msg = mock<Message>()
        `when`(msg.isCommand).thenReturn(false)
        val chatId = 1717L

        // Run method under test
        val actRes = sut.executeCommand(msg, chatId)

        // Verify
        assertThat(actRes).isFalse()
        verify(sut, never()).extractCmdName(msg)
    }
    @Test
    fun executeCommandReturnsFalseIfChatIdNull() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val cmdReg = mock<ITelegramCommandRegistry>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        cmdReg,
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val msg = mock<Message>()
        `when`(msg.isCommand).thenReturn(true)
        val msgName = "msgName"
        doReturn(msgName).`when`(sut).extractCmdName(msg)
        val cmd = mock<ITelegramCommand>()
        `when`(cmdReg.find(msgName)).thenReturn(cmd)
        val chatId = null

        // Run method under test
        val actRes = sut.executeCommand(msg, chatId)

        // Verify
        assertThat(actRes).isFalse()
        verify(sut).extractCmdName(msg)
        verify(cmdReg).find(msgName)
    }
    @Test
    fun executeCommandReturnsFalseIfCmdNull() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val cmdReg = mock<ITelegramCommandRegistry>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        cmdReg,
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val msg = mock<Message>()
        `when`(msg.isCommand).thenReturn(true)
        val msgName = "msgName"
        doReturn(msgName).`when`(sut).extractCmdName(msg)
        val cmd = null
        `when`(cmdReg.find(msgName)).thenReturn(cmd)
        val chatId = 1013L

        // Run method under test
        val actRes = sut.executeCommand(msg, chatId)

        // Verify
        assertThat(actRes).isFalse()
        verify(sut).extractCmdName(msg)
        verify(cmdReg).find(msgName)
    }
    @Test
    fun herrKarlIgnoresAutomataNotWaitingForResponse() {
        // Prepare
        val auth = mock<Authenticator>()
        val tu = mock<ITelegramUtils>()
        val cmdReg = mock<ITelegramCommandRegistry>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        cmdReg,
                        InvalidUpdateFormatter(),
                        tu
                )
        )
        val automaton = mock<ITelegramCmdAutomaton>()
        `when`(automaton.waitingForResponse()).thenReturn(false)

        // Run method under test
        sut.subscribe(automaton)
        val actRes = sut.automatonWaitingForResponse()

        // Verify
        assertThat(actRes).isNull()
    }
    @Test
    fun onUpdateReceivedCallsExecuteCommandIfMessageHasDocument() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val cmdRegistry = mock<ITelegramCommandRegistry>()
        val sut = spy(HerrKarl(protocol, logger, auth, cmdRegistry))
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(msg.text).thenReturn("")
        `when`(msg.hasDocument()).thenReturn(true)
        `when`(auth.rightUser(update)).thenReturn(true)
        val chatId = 1600L
        `when`(msg.chatId).thenReturn(chatId)
        doReturn(true).`when`(sut).executeCommand(msg, chatId)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(sut).executeCommand(msg, chatId)
        verify(sut, never()).handleIncomingMessage(msg)
    }
    @Test
    fun subscribeUnsubscribe() {
        // Create object under test
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val cmdRegistry = mock<ITelegramCommandRegistry>()
        val sut = HerrKarl(protocol, logger, auth, cmdRegistry)

        val automaton = mock<ITelegramCmdAutomaton>()
        `when`(automaton.waitingForResponse()).thenReturn(true)

        // Subscribe
        sut.subscribe(automaton)

        // Verify that automatonWaitingForResponse finds it
        val actRes1 = sut.automatonWaitingForResponse()
        assertThat(actRes1).isSameAs(automaton)

        // Unsubscribe
        sut.unsubscribe(automaton)

        // Verify that automatonWaitingForResponse doesn't find it
        val actRes2 = sut.automatonWaitingForResponse()
        assertThat(actRes2).isNull()
    }
    @Test
    fun readFileContentsSunnyDay() {
        // Prepare
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        mock<Authenticator>(),
                        mock<ITelegramCommandRegistry>()
                )
        )
        val fileId = "fileId"
        val getFile = mock<GetFile>()
        doReturn(getFile).`when`(sut).createGetFile()
        val file = mock<org.telegram.telegrambots.api.objects.File>()
        doReturn(file).`when`(sut).testableGetFile(getFile)
        val downloadedFile = mock<File>()
        doReturn(downloadedFile).`when`(sut).testableDownloadFile(file)
        val inOrder = inOrder(sut, getFile, file, downloadedFile)

        // Run method under test
        val actRes = sut.readFileContents(fileId)

        // Verify
        inOrder.verify(sut).createGetFile()
        inOrder.verify(getFile).setFileId(fileId)
        inOrder.verify(sut).testableGetFile(getFile)
        assertThat(actRes).isSameAs(downloadedFile)
    }
    @Test
    fun readFileContentsRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        logger,
                        mock<Authenticator>(),
                        mock<ITelegramCommandRegistry>()
                )
        )
        val fileId = "fileId"
        val getFile = mock<GetFile>()
        doReturn(getFile).`when`(sut).createGetFile()
        val file = mock<org.telegram.telegrambots.api.objects.File>()
        doReturn(file).`when`(sut).testableGetFile(getFile)
        val throwable = RuntimeException("error")
        doThrow(throwable).`when`(sut).testableDownloadFile(file)
        val inOrder = inOrder(sut, getFile, file, logger)

        // Run method under test
        val actRes = sut.readFileContents(fileId)

        // Verify
        inOrder.verify(sut).createGetFile()
        inOrder.verify(sut).testableDownloadFile(file)
        inOrder.verify(logger).error("readFileContents(fileId='$fileId')", throwable)
        assertThat(actRes).isNull()
    }
    @Test
    fun onUpdateReceivedLetsAutomatonHandleIncomingMessage() {
        // Prepare
        val auth = mock<Authenticator>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        mock<ITelegramCommandRegistry>()
                )
        )
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(msg.hasDocument()).thenReturn(true)
        `when`(auth.rightUser(update)).thenReturn(true)
        val chatId = 1908L
        `when`(msg.chatId).thenReturn(chatId)
        val cmdExecuted = false
        doReturn(cmdExecuted).`when`(sut).executeCommand(msg, chatId)
        val automatonWaitingForResponse = mock<ITelegramCmdAutomaton>()
        doReturn(automatonWaitingForResponse).`when`(sut).automatonWaitingForResponse()

        val inOrder = inOrder(sut, update, msg, auth, automatonWaitingForResponse)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        inOrder.verify(sut).automatonWaitingForResponse()
        inOrder.verify(automatonWaitingForResponse).handleIncomingMessage(msg)
        inOrder.verify(sut, never()).handleIncomingMessage(msg)
    }
    @Test
    fun onUpdateReceivedLetsHandlesIncomingMessageInAbsenceOfWaitingAutomata() {
        // Prepare
        val auth = mock<Authenticator>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        mock<ITelegramCommandRegistry>()
                )
        )
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(msg.hasDocument()).thenReturn(true)
        `when`(auth.rightUser(update)).thenReturn(true)
        val chatId = 1908L
        `when`(msg.chatId).thenReturn(chatId)
        val cmdExecuted = false
        doReturn(cmdExecuted).`when`(sut).executeCommand(msg, chatId)
        val automatonWaitingForResponse = null
        doReturn(automatonWaitingForResponse).`when`(sut).automatonWaitingForResponse()
        doNothing().`when`(sut).handleIncomingMessage(msg)

        val inOrder = inOrder(sut, update, msg, auth)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        inOrder.verify(sut).automatonWaitingForResponse()
        inOrder.verify(sut).handleIncomingMessage(msg)
    }
    @Test
    fun onUpdateReceivedDoesntExecuteCommandIfAutomatonWaiting() {
        // Prepare
        val auth = mock<Authenticator>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        mock<ITelegramCommandRegistry>()
                )
        )
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(msg.hasDocument()).thenReturn(true)
        `when`(auth.rightUser(update)).thenReturn(true)
        val chatId = 1908L
        `when`(msg.chatId).thenReturn(chatId)
        val automatonWaitingForResponse = mock<ITelegramCmdAutomaton>()
        doReturn(automatonWaitingForResponse).`when`(sut).automatonWaitingForResponse()

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(sut).automatonWaitingForResponse()
        verify(sut, never()).executeCommand(msg, chatId)
        verify(automatonWaitingForResponse).handleIncomingMessage(msg)
    }

    @Test
    fun onUpdateReceivedExecutesCommandIfAutomatonNotWaiting() {
        // Prepare
        val auth = mock<Authenticator>()
        val sut = spy(
                HerrKarl(
                        mock<Logger>(),
                        mock<Logger>(),
                        auth,
                        mock<ITelegramCommandRegistry>()
                )
        )
        val update = mock<Update>()
        `when`(update.hasMessage()).thenReturn(true)
        val msg = mock<Message>()
        `when`(update.message).thenReturn(msg)
        `when`(msg.hasDocument()).thenReturn(true)
        `when`(auth.rightUser(update)).thenReturn(true)
        val chatId = 1908L
        `when`(msg.chatId).thenReturn(chatId)
        val automatonWaitingForResponse = null
        doReturn(automatonWaitingForResponse).`when`(sut).automatonWaitingForResponse()
        doReturn(true).`when`(sut).executeCommand(msg, chatId)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(sut).automatonWaitingForResponse()
        verify(sut).executeCommand(msg, chatId)
        verify(sut, never()).handleIncomingMessage(msg)
    }

    private fun logInvalidAccessAttemptTestLogic(update: Update, expectedResult: String) {
        // Prepare
        val logger = mock<Logger>()
        val sut = HerrKarl(mock<Logger>(), logger, KommissarRex(mock<IJenaSubsystem>()), mock<ITelegramCommandRegistry>())
        doAnswer {
            Assert.assertEquals(expectedResult, it.arguments[0])
        }.`when`(logger).error(Matchers.anyString())

        // Run
        sut.logInvalidAccessAttempt(update, update.message)

        // Verify
        verify(logger).error(Matchers.anyString())
    }

    private fun createUpdateWithNullFields(): Update {
        val update = mock<Update>()
        val message = mock<Message>()
        `when`(update.message).thenReturn(message)
        val from = mock<User>()
        `when`(message.from).thenReturn(from)
        val userId = 1131
        `when`(from.id).thenReturn(userId)
        return update
    }
    private fun createUpdateWithFromMrHankey(): Update {
        val update = mock<Update>()
        `when`(update.updateId).thenReturn(12345)
        val message = mock<Message>()
        `when`(update.message).thenReturn(message)
        val from = mock<User>()
        `when`(message.from).thenReturn(from)
        val userId = 1120
        `when`(from.id).thenReturn(userId)
        `when`(from.firstName).thenReturn("Bill")
        `when`(from.lastName).thenReturn("Hankey")
        `when`(from.userName).thenReturn("MrHankey23")
        return update
    }

}