package cc.altruix.is1.telegram.bots.knackal

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.*
import cc.altruix.mock
import org.junit.Assert
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito.*
import org.slf4j.Logger
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User

/**
 * Created by pisarenko on 31.01.2017.
 */
class KnackalTests {
    @Test
    fun onUpdateReceivedReactsCorrectlyToNonCommandMessage() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(Knackal(protocol, logger, auth, KnackalCommandRegistry(capsule, mock<IJenaSubsystem>(), mock<IResponsiveBot>())))
        val msg = createMessage(false)
        val update = createUpdate(msg)
        doNothing().`when`(sut).logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.NoCommand)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(update).hasMessage()
        verify(update).message
        verify(msg).isCommand
        verify(sut).logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.NoCommand)
        verify(sut, never()).handleIncomingMessage(msg)
    }
    @Test
    fun onUpdateReceivedReactsCorrectlyToCommandMessageFromWrongUser() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(Knackal(protocol, logger, auth, KnackalCommandRegistry(capsule, mock<IJenaSubsystem>(), mock<IResponsiveBot>())))
        val msg = createMessage(true)
        val update = createUpdate(msg)
        doNothing().`when`(sut).logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.WrongUser)
        `when`(auth.rightUser(update)).thenReturn(false)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(update).hasMessage()
        verify(update).message
        verify(msg).isCommand
        verify(sut, never()).logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.NoCommand)
        verify(sut).logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.WrongUser)
        verify(sut, never()).handleIncomingMessage(msg)
        verify(auth).rightUser(update)
    }
    @Test
    fun onUpdateReceivedHandlesCommandsFromRightUsers() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(Knackal(protocol, logger, auth, KnackalCommandRegistry(capsule, mock<IJenaSubsystem>(), mock<IResponsiveBot>())))
        val msg = createMessage(true)
        val update = createUpdate(msg)
        `when`(auth.rightUser(update)).thenReturn(true)

        // Run method under test
        sut.onUpdateReceived(update)

        // Verify
        verify(sut).handleIncomingMessage(msg)
        verify(msg).isCommand
        verify(auth).rightUser(update)
        verify(sut, never()).logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.NoCommand)
        verify(sut, never()).logInvalidAccessAttempt(update, msg, InvalidAccessAttemptType.WrongUser)
    }
    @Test
    fun logInvalidAccessAttempt() {
        logInvalidAccessAttemptTestLogic(InvalidAccessAttemptType.NoCommand)
        logInvalidAccessAttemptTestLogic(InvalidAccessAttemptType.WrongUser)
    }
    @Test
    fun composeInvalidAccessComment() {
        composeInvalidAccessCommentTestLogic(
                InvalidAccessAttemptType.NoCommand,
                "Jössas, irgendwer will was von mir! Invalid access type: 'NoCommand'."
        )
        composeInvalidAccessCommentTestLogic(
                InvalidAccessAttemptType.WrongUser,
                "Jössas, irgendwer will was von mir! Invalid access type: 'WrongUser'."
        )
    }
    @Test
    fun handleIncomingMessageProcessesUnrecognizedCommandErrors() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val cmdRegistry = mock<ITelegramCommandRegistry>()
        val sut = spy(Knackal(protocol, logger, auth, cmdRegistry))
        val msg = mock<Message>()
        val cmdTxt = "/fpw indieMusic"
        `when`(msg.text).thenReturn(cmdTxt)
        val cmdName = "/fpw"
        `when`(cmdRegistry.find(cmdName)).thenReturn(null)
        doNothing().`when`(sut).processUnrecognizedCommandError(cmdName, msg)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(sut).processUnrecognizedCommandError(cmdName, msg)
        verify(msg).text
        verify(cmdRegistry).find(cmdName)
    }
    @Test
    fun handleIncomingMessageExecutesRecognizedCommand() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val cmdRegistry = mock<ITelegramCommandRegistry>()
        val sut = spy(Knackal(protocol, logger, auth, cmdRegistry))
        val msg = mock<Message>()
        val cmdTxt = "/fpw indieMusic"
        `when`(msg.text).thenReturn(cmdTxt)
        val cmdName = "/fpw"
        doReturn(cmdName).`when`(sut).extractCmdName(msg)
        val cmd = mock<ITelegramCommand>()
        `when`(cmdRegistry.find(cmdName)).thenReturn(cmd)
        doNothing().`when`(sut).processUnrecognizedCommandError(cmdName, msg)
        val userId = 332
        val user = mock<User>()
        `when`(msg.from).thenReturn(user)
        `when`(user.id).thenReturn(userId)

        // Run method under test
        sut.handleIncomingMessage(msg)

        // Verify
        verify(sut).extractCmdName(msg)
        verify(sut, never()).processUnrecognizedCommandError(cmdName, msg)
        verify(msg).text
        verify(cmdRegistry).find(cmdName)
        verify(cmd).execute(cmdTxt, sut, 0L, userId)
    }

    @Test
    fun processUnrecognizedCommandError() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val cmdRegistry = mock<ITelegramCommandRegistry>()
        val sut = spy(Knackal(protocol, logger, auth, cmdRegistry))
        val cmdName = "cmdName"
        val msg = mock<Message>()
        doNothing().`when`(sut).sendErrorMessageToUser(cmdName, msg)
        doNothing().`when`(sut).logUnrecognizedCommand(cmdName)

        // Run method under test
        sut.processUnrecognizedCommandError(cmdName, msg)

        // Verify
        verify(sut).sendErrorMessageToUser(cmdName, msg)
        verify(sut).logUnrecognizedCommand(cmdName)
    }

    @Test
    fun logUnrecognizedCommand() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val cmdRegistry = mock<ITelegramCommandRegistry>()
        val sut = spy(Knackal(protocol, logger, auth, cmdRegistry))
        doAnswer {
            Assert.assertEquals("Was soll der Schas - 'stuff' ?", it.arguments[0])
        }.`when`(logger).error(Matchers.anyString())
        // Run method under test
        sut.logUnrecognizedCommand("stuff")

        // Verify
        verify(logger).error(Matchers.anyString())
    }

    @Test
    fun sendErrorMessageToUser() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val cmdRegistry = mock<ITelegramCommandRegistry>()
        val sut = spy(Knackal(protocol, logger, auth, cmdRegistry))
        val cmdName = "cmdName"
        val msg = mock<Message>()
        val reply = mock<SendMessage>()
        doReturn(reply).`when`(sut).createUnrecognizedCommandReply(cmdName, msg)
        doNothing().`when`(sut).sendTelegramMessage(reply)

        // Run method under test
        sut.sendErrorMessageToUser(cmdName, msg)

        // Verify
        verify(sut).createUnrecognizedCommandReply(cmdName, msg)
        verify(sut).sendTelegramMessage(reply)
    }
    @Test
    fun createUnrecognizedCommandReply() {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val cmdRegistry = mock<ITelegramCommandRegistry>()
        val sut = spy(Knackal(protocol, logger, auth, cmdRegistry))
        val cmdName = "cmdName"
        val req = mock<Message>()
        `when`(req.chatId).thenReturn(430L)

        // Run method under test
        val actRes = sut.createUnrecognizedCommandReply(cmdName, req)

        // Verify
        Assert.assertNotNull(actRes)
        Assert.assertEquals("Unrecognized command 'cmdName'", actRes.text)
        Assert.assertEquals("430", actRes.chatId)
    }

    private fun composeInvalidAccessCommentTestLogic(input: InvalidAccessAttemptType, expectedResult: String) {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(Knackal(protocol, logger, auth, KnackalCommandRegistry(capsule, mock<IJenaSubsystem>(), mock<IResponsiveBot>())))

        // Run method under test
        val actRes = sut.composeInvalidAccessComment(input)

        // Verify
        Assert.assertEquals(expectedResult, actRes)
    }

    private fun logInvalidAccessAttemptTestLogic(type: InvalidAccessAttemptType) {
        // Prepare
        val protocol = mock<Logger>()
        val logger = mock<Logger>()
        val auth = mock<Authenticator>()
        val iuf = mock<IInvalidUpdateFormatter>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(Knackal(protocol, logger, auth,
                KnackalCommandRegistry(capsule, mock<IJenaSubsystem>(), mock<IResponsiveBot>()),
                iuf))
        val msg = createMessage(true)
        val update = createUpdate(msg)
        val invalidAccessComment = "invalidAccessComment"
        doReturn(invalidAccessComment).`when`(sut).composeInvalidAccessComment(type)
        val iufResponse = "IUF RESPONSE"
        `when`(iuf.format(update, msg, invalidAccessComment)).thenReturn(iufResponse)
        doAnswer {
            Assert.assertEquals(iufResponse, it.arguments[0])
        }.`when`(logger).error(Matchers.anyString())

        // Run method under test
        sut.logInvalidAccessAttempt(update, msg, type)

        // Verify
        verify(logger).error(iufResponse)
        verify(sut).composeInvalidAccessComment(type)
        verify(iuf).format(update, msg, invalidAccessComment)
    }

    private fun createUpdate(msg: Message): Update {
        val update = mock<Update>()
        `when`(update.message).thenReturn(msg)
        `when`(update.hasMessage()).thenReturn(true)
        return update
    }

    private fun createMessage(isCommand: Boolean): Message {
        val msg = mock<Message>()
        `when`(msg.isCommand).thenReturn(isCommand)
        return msg
    }
}