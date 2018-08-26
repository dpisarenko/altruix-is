package cc.altruix.is1.telegram.bots.herrKarl

import cc.altruix.is1.telegram.*
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.telegram.telegrambots.api.methods.GetFile
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.send.SendPhoto
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import java.io.File
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

open class HerrKarl(
        val protocol: Logger,
        val logger: Logger,
        val auth: Authenticator,
        val cmdRegistry: ITelegramCommandRegistry,
        val invalidUpdateFormatter: InvalidUpdateFormatter = InvalidUpdateFormatter(),
        val telegramUtils: ITelegramUtils = TelegramUtils()
) : AbstractBot(), IResponsiveBot {
	val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:ss")
	val dateFormat2 = SimpleDateFormat("yyyy-MM-dd HH:MM:ss")
	override fun getBotUsername(): String? = "HerrKarl"

	override fun getBotToken(): String? = "CENSORED"

	var chatId:Long? = null

	override fun onUpdateReceived(update: Update?) {
		if (update == null) {
			return
		}
		if (!update.hasMessage()) {
			return
		}
		val msg = update.message
		if (msg == null) {
			return
		}
		if (StringUtils.isBlank(msg.text) && !msg.hasDocument()) {
			return
		}
		if (!auth.rightUser(update)) {
			logInvalidAccessAttempt(update, msg)
			return
		}

		chatId = update.message.chatId
		val automatonWaitingForResponse = automatonWaitingForResponse()
		if (automatonWaitingForResponse == null) {
			val cmdExecuted = executeCommand(msg, chatId)
			if (cmdExecuted) {
				return
			}
		}
		if (automatonWaitingForResponse != null) {
			automatonWaitingForResponse.handleIncomingMessage(msg)
			return
		}
		handleIncomingMessage(msg)
	}

	open fun executeCommand(msg: Message, chatId: Long?) : Boolean {
		if (msg.isCommand) {
			val msgName = extractCmdName(msg)
			val cmd = cmdRegistry.find(msgName)
			if ((cmd != null) && (chatId != null)) {
				cmd.execute(msg.text, this, chatId, msg.from.id)
				return true
			}
		}
		return false
	}

	open fun logInvalidAccessAttempt(update: Update, msg: Message) {
		val logMsg = invalidUpdateFormatter.format(update, msg, "Some asshole tried to contact me.")
		logger.error(logMsg)
	}

	open fun handleIncomingMessage(msg: Message) {
		protocol.info(composeMessageToPrint(msg))	
		sendTelegramMessage(createAckReply(msg))
	}
	override fun sendTelegramMessage(msg: SendMessage) {
		sendMessage(msg)
	}
	open fun composeMessageToPrint(msg: Message):String {
		val localTime = now()
		val zoneId = systemTimeZone()	
		val utcTime = ZonedDateTime.ofInstant(localTime.toInstant(), ZoneId.of("UTC"))
		val mskTime = ZonedDateTime.ofInstant(localTime.toInstant(), ZoneId.of("Europe/Moscow"))
		val msgTime = Date(msg.date*1000L)
		val sb = StringBuilder()
		sb.append("% MSK: ${dateFormat.format(mskTime)}, UTC: ${dateFormat.format(utcTime)}, server ('${zoneId}') time: ${dateFormat2.format(localTime)}, message time: ${dateFormat2.format(msgTime)}")
		sb.append(lineSeparator())
		sb.append(lineSeparator())
		sb.append(msg.text)
		sb.append(lineSeparator())
		sb.append(lineSeparator())
		sb.append(lineSeparator())
		sb.append(lineSeparator())
		return sb.toString()
	}
	
	open fun now() = Date()
	
	open fun lineSeparator() = System.lineSeparator()
	
	open fun systemTimeZone() = ZoneId.systemDefault()
	
	open fun createAckReply(req: Message): SendMessage {
		val txt = StringUtils.abbreviate(StringUtils.defaultString(req.text), 10)
		val msg = SendMessage()
		msg.text = "ACK '${txt}'"
		msg.chatId = req.chatId.toString()
		msg.enableMarkdown(false)
		return msg
	}
	override fun sendBroadcast(msg: String) {
		val chatId = this.chatId
		if (chatId != null) {
			telegramUtils.sendTextMessage(msg, chatId, this)
		}
	}
	override fun readFileContents(fileId: String): File? {
		val getFile = createGetFile()
		getFile.fileId = fileId
		try {
			val file = this.testableGetFile(getFile)
			return testableDownloadFile(file)
		} catch (throwable:Throwable) {
			logger.error("readFileContents(fileId='$fileId')", throwable)
			return null
		}
	}

    open fun createGetFile() = GetFile()

	open fun testableGetFile(getFile:GetFile ): org.telegram.telegrambots.api.objects.File
		= getFile(getFile)
	open fun testableDownloadFile(file: org.telegram.telegrambots.api.objects.File) =
			downloadFile(file)

	override fun sendImage(msg: SendPhoto) {
		this.sendPhoto(msg)
	}
}