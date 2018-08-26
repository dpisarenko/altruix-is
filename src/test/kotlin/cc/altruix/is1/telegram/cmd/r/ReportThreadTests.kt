package cc.altruix.is1.telegram.cmd.r

import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import cc.altruix.utils.toDate
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.DataFormat
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.slf4j.Logger
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Address
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * Created by pisarenko on 13.04.2017.
 */
class ReportThreadTests {
    @Test
    fun createStartDate() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = ReportThread(
                mongo,
                bot,
                chatId,
                mock<ITelegramUtils>(),
                dataCreator,
                logger
        )

        // Run method under test
        val actRes = sut.createStartDate()

        // Verify
        val cal = Calendar.getInstance()
        cal.time = actRes
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2017)
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.APRIL)
        assertThat(cal.get(Calendar.DATE)).isEqualTo(13)
    }
    @Test
    fun run() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                tu,
                dataCreator,
                logger
        ))
        val start = Date()
        Mockito.doReturn(start).`when`(sut).createStartDate()
        val reportData:List<DailyMetricValues> = emptyList()
        `when`(dataCreator.createData(start)).thenReturn(
                FailableOperationResult(true, "", reportData)
        )
        val report = mock<File>()
        val createReportRes = FailableOperationResult<File>(true, "", report)
        doReturn(createReportRes).`when`(sut).createReport(reportData)
        val sendRes = ValidationResult(true, "")
        doReturn(sendRes).`when`(sut).sendReport(report, bot, chatId)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createStartDate()
        verify(dataCreator).createData(start)
        verify(sut).createReport(reportData)
        verify(sut).sendReport(report, bot, chatId)
        verify(report).delete()
        verify(report).deleteOnExit()
        verify(tu).sendTextMessage("Report sent.", chatId, bot)
    }
    @Test
    fun runSendingFailure() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                tu,
                dataCreator,
                logger
        ))
        val start = Date()
        Mockito.doReturn(start).`when`(sut).createStartDate()
        val reportData:List<DailyMetricValues> = emptyList()
        `when`(dataCreator.createData(start)).thenReturn(
                FailableOperationResult(true, "", reportData)
        )
        val report = mock<File>()
        val createReportRes = FailableOperationResult<File>(
                true,
                "",
                report
        )
        doReturn(createReportRes).`when`(sut).createReport(reportData)
        val sendRes = ValidationResult(false, "")
        doReturn(sendRes).`when`(sut).sendReport(report, bot, chatId)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createStartDate()
        verify(dataCreator).createData(start)
        verify(sut).createReport(reportData)
        verify(sut).sendReport(report, bot, chatId)
        verify(report).delete()
        verify(report).deleteOnExit()
        verify(tu).displayError("Excel file could not be sent.",
                chatId,
                bot)
    }
    @Test
    fun runExcelFileCreationFailure() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                tu,
                dataCreator,
                logger
        ))
        val start = Date()
        Mockito.doReturn(start).`when`(sut).createStartDate()
        val reportData:List<DailyMetricValues> = emptyList()
        `when`(dataCreator.createData(start)).thenReturn(
                FailableOperationResult(true, "", reportData)
        )
        val reportRes = FailableOperationResult<File>(false, "err", null)
        doReturn(reportRes).`when`(sut).createReport(reportData)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createStartDate()
        verify(dataCreator).createData(start)
        verify(sut).createReport(reportData)
        verify(tu).displayError("Excel file could not be prepared.",
                chatId,
                bot)
    }

    @Test
    fun runDataCreationFailure() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                tu,
                dataCreator,
                logger
        ))
        val start = Date()
        doReturn(start).`when`(sut).createStartDate()
        val errMsg = "errMsg"
        val reportDataRes = FailableOperationResult<List<DailyMetricValues>>(
                false,
                errMsg,
                null
        )
        `when`(dataCreator.createData(start)).thenReturn(reportDataRes)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createStartDate()
        verify(dataCreator).createData(start)
        verify(tu).displayError("Report data could not be prepared.",
                chatId,
                bot)
    }
    @Test
    fun runSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                tu,
                dataCreator,
                logger
        ))
        val start = Date()
        Mockito.doReturn(start).`when`(sut).createStartDate()
        val res = emptyList<DailyMetricValues>()
        val reportDataRes = FailableOperationResult<List<DailyMetricValues>>(
                true,
                "",
                res
        )
        `when`(dataCreator.createData(start)).thenReturn(reportDataRes)
        val report = mock<File>()
        val createReportRes = FailableOperationResult<File>(true, "", report)
        doReturn(createReportRes).`when`(sut).createReport(res)
        doReturn(ValidationResult(true, "")).`when`(sut).sendReport(report, bot, chatId)

        // Run method under test
        sut.run()

        // Verify
        verify(sut).createStartDate()
        verify(dataCreator).createData(start)
        verify(sut).createReport(res)
        verify(sut).sendReport(report, bot, chatId)
    }
    @Test
    fun createProperties() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = ReportThread(
                mongo,
                bot,
                chatId,
                tu,
                dataCreator,
                logger
        )

        // Run method under test
        val actRes = sut.createProperties()

        // Verify
        assertThat(actRes.getProperty("mail.smtps.host")).isEqualTo(ReportThread.Host)
        assertThat(actRes.getProperty("mail.smtps.auth")).isEqualTo("true")
    }
    @Test
    fun createBody() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                tu,
                dataCreator,
                logger
        ))
        val body = mock<MimeBodyPart>()
        doReturn(body).`when`(sut).createMimeBodyPart()

        // Run method under test
        val actRes = sut.createBody()

        // Verify
        verify(sut).createMimeBodyPart()
        verify(body).setText("Report with all time series generated with the /r command.")
        assertThat(actRes).isSameAs(body)
    }
    @Test
    fun createAttachment() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                tu,
                dataCreator,
                logger
        ))
        val attachment = mock<MimeBodyPart>()
        doReturn(attachment).`when`(sut).createMimeBodyPart()
        val report = mock<File>()
        val src = mock<FileDataSource>()
        doReturn(src).`when`(sut).createFileDataSource(report)
        val dataHandler = mock<DataHandler>()
        doReturn(dataHandler).`when`(sut).createDataHandler(src)

        // Run method under test
        val actRes = sut.createAttachment(report)

        // Verify
        verify(sut).createMimeBodyPart()
        verify(sut).createFileDataSource(report)
        verify(sut).createDataHandler(src)
        verify(attachment).dataHandler = dataHandler
        verify(attachment).fileName = "Report.xls"
        assertThat(actRes).isSameAs(attachment)
    }
    @Test
    fun createMultipart() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val body = mock<MimeBodyPart>()
        val attachment = mock<MimeBodyPart>()
        val multipart = mock<MimeMultipart>()
        doReturn(multipart).`when`(sut).createMimeMultipart()

        val inOrder = inOrder(sut, attachment, body, multipart)

        // Run method under test
        val actRes = sut.createMultipart(attachment, body)

        // Verify
        inOrder.verify(sut).createMimeMultipart()
        inOrder.verify(multipart).addBodyPart(body)
        inOrder.verify(multipart).addBodyPart(attachment)
        assertThat(actRes).isSameAs(multipart)
    }
    @Test
    fun createMimeMessage() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val multipart = mock<MimeMultipart>()
        val session = Session.getInstance(Properties())
        val msg = mock<MimeMessage>()
        doReturn(msg).`when`(sut).createMimeMessage(session)
        val senderAddress = mock<InternetAddress>()
        doReturn(senderAddress).`when`(sut).createInternetAddress(ReportThread.SenderAddress)
        val recipientAddress:Array<InternetAddress> = emptyArray()
        doReturn(recipientAddress).`when`(sut).parse(ReportThread.RecipientAddress)
        val now = Date()
        doReturn(now).`when`(sut).now()

        // Run method under test
        val actRes = sut.createMimeMessage(multipart, session)

        // Verify
        verify(sut).createMimeMessage(session)
        verify(sut).createInternetAddress(ReportThread.SenderAddress)
        verify(msg).setFrom(senderAddress)
        verify(sut).parse(ReportThread.RecipientAddress)
        verify(msg).setRecipients(Message.RecipientType.TO, recipientAddress)
        verify(msg).setSubject("Report")
        verify(msg).setContent(multipart)
        verify(sut).now()
        verify(msg).sentDate = now
        assertThat(actRes).isSameAs(msg)
    }
    @Test
    fun sendReportSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val report = mock<File>()
        val body = mock<MimeBodyPart>()
        doReturn(body).`when`(sut).createBody()
        val attachment = mock<MimeBodyPart>()
        doReturn(attachment).`when`(sut).createAttachment(report)
        val multipart = mock<MimeMultipart>()
        doReturn(multipart).`when`(sut).createMultipart(attachment, body)
        val props = mock<Properties>()
        doReturn(props).`when`(sut).createProperties()
        val session = Session.getInstance(props, null)
        doReturn(session).`when`(sut).getSession(props)
        val msg = mock<MimeMessage>()
        doReturn(msg).`when`(sut).createMimeMessage(multipart, session)
        val transport = mock<Transport>()
        doReturn(transport).`when`(sut).getTransport(session)
        val allRecipients = emptyArray<Address>()
        `when`(msg.allRecipients).thenReturn(allRecipients)


        val inOrder = inOrder(mongo, bot, dataCreator, logger,
                tu, sut, report, body, transport, msg)

        // Run method under test
        val actRes = sut.sendReport(report, bot, chatId)

        // Verify
        inOrder.verify(sut).createBody()
        inOrder.verify(sut).createAttachment(report)
        inOrder.verify(sut).createMultipart(attachment, body)
        inOrder.verify(sut).createProperties()
        inOrder.verify(sut).getSession(props)
        inOrder.verify(sut).createMimeMessage(multipart, session)
        inOrder.verify(sut).getTransport(session)
        inOrder.verify(transport).connect(ReportThread.Host,
                ReportThread.SenderAddress, ReportThread.Password)
        inOrder.verify(msg).allRecipients
        inOrder.verify(transport).sendMessage(msg, allRecipients)
        inOrder.verify(transport).close()
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNull()
    }
    @Test
    fun sendReportRainyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val report = mock<File>()
        val body = mock<MimeBodyPart>()
        doReturn(body).`when`(sut).createBody()
        val attachment = mock<MimeBodyPart>()
        doReturn(attachment).`when`(sut).createAttachment(report)
        val multipart = mock<MimeMultipart>()
        doReturn(multipart).`when`(sut).createMultipart(attachment, body)
        val props = mock<Properties>()
        doReturn(props).`when`(sut).createProperties()
        val session = Session.getInstance(props, null)
        doReturn(session).`when`(sut).getSession(props)
        val msg = mock<MimeMessage>()
        doReturn(msg).`when`(sut).createMimeMessage(multipart, session)
        val transport = mock<Transport>()
        doReturn(transport).`when`(sut).getTransport(session)
        val allRecipients = emptyArray<Address>()
        `when`(msg.allRecipients).thenReturn(allRecipients)
        val errMsg = "errMsg"
        val throwable = RuntimeException(errMsg)
        `when`(transport.sendMessage(msg, allRecipients)).thenThrow(throwable)


        val inOrder = inOrder(mongo, bot, dataCreator,
                logger, tu, sut, report, body, transport, msg)

        // Run method under test
        val actRes = sut.sendReport(report, bot, chatId)

        // Verify
        inOrder.verify(sut).createBody()
        inOrder.verify(sut).createAttachment(report)
        inOrder.verify(sut).createMultipart(attachment, body)
        inOrder.verify(sut).createProperties()
        inOrder.verify(sut).getSession(props)
        inOrder.verify(sut).createMimeMessage(multipart, session)
        inOrder.verify(sut).getTransport(session)
        inOrder.verify(transport).connect(ReportThread.Host,
                ReportThread.SenderAddress, ReportThread.Password)
        inOrder.verify(msg).allRecipients
        inOrder.verify(transport).sendMessage(msg, allRecipients)
        inOrder.verify(logger).error("sendReport(chatId=$chatId)",
                throwable)
        inOrder.verify(transport).close()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Could not send the report ('$errMsg').")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun composeWeekText() {
        composeWeekTextTestLogic(day(2016, 12, 25), "2016-51")
        composeWeekTextTestLogic(day(2016, 12, 26), "2016-52")
        composeWeekTextTestLogic(day(2016, 12, 31), "2016-52")
        composeWeekTextTestLogic(day(2017, 1, 1), "2017-00")
        composeWeekTextTestLogic(day(2017, 1, 2), "2017-01")
        composeWeekTextTestLogic(day(2017, 1, 3), "2017-01")
        composeWeekTextTestLogic(day(2017, 1, 4), "2017-01")
        composeWeekTextTestLogic(day(2017, 1, 5), "2017-01")
        composeWeekTextTestLogic(day(2017, 1, 6), "2017-01")
        composeWeekTextTestLogic(day(2017, 1, 7), "2017-01")
        composeWeekTextTestLogic(day(2017, 1, 8), "2017-01")
        composeWeekTextTestLogic(day(2017, 2, 27), "2017-09")
        composeWeekTextTestLogic(day(2017, 3, 27), "2017-13")
        composeWeekTextTestLogic(day(2017, 5, 8), "2017-19")
    }
    @Test
    fun fillHeaderPercent() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val dmv = DailyMetricValues(
                RCmd.MetricWordCount,
                RCmd.UnitWords,
                emptyList()
        )
        val reportData = listOf(dmv)
        val row = mock<SXSSFRow>()
        val dayCell = mock<SXSSFCell>()
        `when`(row.createCell(0)).thenReturn(dayCell)
        val weekCell = mock<SXSSFCell>()
        `when`(row.createCell(1)).thenReturn(weekCell)
        val metricCell = mock<SXSSFCell>()
        `when`(row.createCell(2)).thenReturn(metricCell)
        val inOrder = inOrder(mongo, bot, dataCreator,
                logger, tu, sut, row, dayCell, weekCell, metricCell)

        // Run method under test
        sut.fillHeaderPercent(reportData, row)

        // Verify
        inOrder.verify(row).createCell(0)
        inOrder.verify(dayCell).setCellValue("Week")
        inOrder.verify(row).createCell(1)
        inOrder.verify(weekCell).setCellValue("Day")
        inOrder.verify(row).createCell(2)
        inOrder.verify(metricCell).setCellValue("${RCmd.MetricWordCount} [%]")
    }
    @Test
    fun fillHeaderAbs() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val dmv = DailyMetricValues(
                RCmd.MetricWordCount,
                RCmd.UnitWords,
                emptyList()
        )
        val reportData = listOf(dmv)
        val row = mock<SXSSFRow>()
        val dayCell = mock<SXSSFCell>()
        `when`(row.createCell(0)).thenReturn(dayCell)
        val weekCell = mock<SXSSFCell>()
        `when`(row.createCell(1)).thenReturn(weekCell)
        val metricCell = mock<SXSSFCell>()
        `when`(row.createCell(2)).thenReturn(metricCell)
        val inOrder = inOrder(mongo, bot, dataCreator,
                logger, tu, sut, row, dayCell, weekCell, metricCell)

        // Run method under test
        sut.fillHeaderAbs(reportData, row)

        // Verify
        inOrder.verify(row).createCell(0)
        inOrder.verify(dayCell).setCellValue("Week")
        inOrder.verify(row).createCell(1)
        inOrder.verify(weekCell).setCellValue("Day")
        inOrder.verify(row).createCell(2)
        inOrder.verify(metricCell).setCellValue("${RCmd.MetricWordCount} [${RCmd.UnitWords}]")
    }
    @Test
    fun writeDate() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val row1 = mock<SXSSFRow>()
        val row2 = mock<SXSSFRow>()
        val rows = setOf(row1, row2)
        val date = Date()
        val col = 123
        val style = mock<CellStyle>()

        val cell1 = mock<SXSSFCell>()
        val cell2 = mock<SXSSFCell>()

        `when`(row1.createCell(col)).thenReturn(cell1)
        `when`(row2.createCell(col)).thenReturn(cell2)

        // Run method under test
        sut.writeDate(rows, date, col, style)

        // Verify
        verify(row1).createCell(col)
        verify(row2).createCell(col)
        verify(cell1).cellStyle = style
        verify(cell2).cellStyle = style
        verify(cell1).setCellValue(date)
        verify(cell2).setCellValue(date)
    }
    @Test
    fun writeText() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val row1 = mock<SXSSFRow>()
        val row2 = mock<SXSSFRow>()
        val rows = setOf(row1, row2)
        val col = 123

        val cell1 = mock<SXSSFCell>()
        val cell2 = mock<SXSSFCell>()

        `when`(row1.createCell(col)).thenReturn(cell1)
        `when`(row2.createCell(col)).thenReturn(cell2)

        val txt = "txt"

        // Run method under test
        sut.writeText(rows, txt, col)

        // Verify
        verify(row1).createCell(col)
        verify(row2).createCell(col)
        verify(cell1).setCellValue(txt)
        verify(cell2).setCellValue(txt)
    }
    @Test
    fun writeNumberWithStyle() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val row = mock<SXSSFRow>()
        val value = 18.5
        val col = 123
        val style = mock<CellStyle>()
        val cell = mock<SXSSFCell>()

        `when`(row.createCell(col)).thenReturn(cell)

        // Run method under test
        sut.writeNumberWithStyle(row, value, col, style)

        // Verify
        verify(row).createCell(col)
        verify(cell).cellStyle = style
        verify(cell).setCellValue(value)
    }
    @Test
    fun writeNumber() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val row = mock<SXSSFRow>()
        val value = 18.5
        val col = 123
        val cell = mock<SXSSFCell>()

        `when`(row.createCell(col)).thenReturn(cell)

        // Run method under test
        sut.writeNumber(row, value, col)

        // Verify
        verify(row).createCell(col)
        verify(cell).setCellValue(value)
    }
    @Test
    fun createDateCellStyle() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val wb = mock<SXSSFWorkbook>()
        val style = mock<CellStyle>()
        `when`(wb.createCellStyle()).thenReturn(style)
        val helper = mock<CreationHelper>()
        `when`(wb.creationHelper).thenReturn(helper)
        val dataFormat = mock<DataFormat>()
        `when`(helper.createDataFormat()).thenReturn(dataFormat)
        val dataFormat2:Short = 1900
        `when`(dataFormat.getFormat("yyyy-mm-dd")).thenReturn(dataFormat2)

        // Run method under test
        val actRes = sut.createDateCellStyle(wb)

        // Verify
        verify(wb).createCellStyle()
        verify(wb).creationHelper
        verify(helper).createDataFormat()
        verify(dataFormat).getFormat("yyyy-mm-dd")
        verify(style).dataFormat = dataFormat2
        assertThat(actRes).isSameAs(style)
    }
    @Test
    fun createPercentCellStyle() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )

        val wb = mock<SXSSFWorkbook>()
        val style = mock<CellStyle>()
        `when`(wb.createCellStyle()).thenReturn(style)
        val helper = mock<CreationHelper>()
        `when`(wb.creationHelper).thenReturn(helper)
        val dataFormat = mock<DataFormat>()
        `when`(helper.createDataFormat()).thenReturn(dataFormat)
        val dataFormat2:Short = 1900
        `when`(dataFormat.getFormat("0.00%")).thenReturn(dataFormat2)

        // Run method under test
        val actRes = sut.createPercentCellStyle(wb)

        // Verify
        verify(wb).createCellStyle()
        verify(wb).creationHelper
        verify(helper).createDataFormat()
        verify(dataFormat).getFormat("0.00%")
        verify(style).dataFormat = dataFormat2
        assertThat(actRes).isSameAs(style)
    }
    @Test
    fun createReportWorkBookCreationFailure() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val reportData = mock<List<DailyMetricValues>>()
        doReturn(null).`when`(sut).createSxssfWorkbook()

        // Run method under test
        val actRes = sut.createReport(reportData)

        // Verify
        verify(sut).createSxssfWorkbook()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Internal error.")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createReportWorkBookException() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val reportData = mock<List<DailyMetricValues>>()
        val msg = "msg"
        val throwable = RuntimeException(msg)
        doThrow(throwable).`when`(sut).createSxssfWorkbook()

        // Run method under test
        val actRes = sut.createReport(reportData)

        // Verify
        verify(sut).createSxssfWorkbook()
        verify(logger).error("createReport", throwable)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Could not create the report ('$msg').")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createReportWorkBookStartDateNull() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val reportData = mock<List<DailyMetricValues>>()
        val wb = mock<SXSSFWorkbook>()
        doReturn(wb).`when`(sut).createSxssfWorkbook()
        val dateCellStyle = mock<CellStyle>()
        doReturn(dateCellStyle).`when`(sut).createDateCellStyle(wb)
        val percentCellStyle = mock<CellStyle>()
        doReturn(percentCellStyle).`when`(sut).createPercentCellStyle(wb)
        val absSheet = mock<SXSSFSheet>()
        `when`(wb.createSheet("Absolute")).thenReturn(absSheet)
        val percentSheet = mock<SXSSFSheet>()
        `when`(wb.createSheet("Percent")).thenReturn(percentSheet)
        val absHeaderRow = mock<SXSSFRow>()
        `when`(absSheet.createRow(0)).thenReturn(absHeaderRow)
        val percentHeaderRow = mock<SXSSFRow>()
        `when`(percentSheet.createRow(0)).thenReturn(percentHeaderRow)
        doNothing().`when`(sut).fillHeaderAbs(reportData, absHeaderRow)
        doNothing().`when`(sut).fillHeaderPercent(reportData, percentHeaderRow)
        val startDate = null
        doReturn(startDate).`when`(sut).calculateStartDate(reportData)

        // Run method under test
        val actRes = sut.createReport(reportData)

        // Verify
        verify(sut).createSxssfWorkbook()
        verify(sut).createDateCellStyle(wb)
        verify(sut).createPercentCellStyle(wb)
        verify(wb).createSheet("Absolute")
        verify(wb).createSheet("Percent")
        verify(absSheet).createRow(0)
        verify(percentSheet).createRow(0)
        verify(sut).fillHeaderAbs(reportData, absHeaderRow)
        verify(sut).fillHeaderPercent(reportData, percentHeaderRow)
        verify(sut).calculateStartDate(reportData)

        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("No data.")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createReportWorkBookFileWritingFailure() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val reportData = mock<List<DailyMetricValues>>()
        val wb = mock<SXSSFWorkbook>()
        doReturn(wb).`when`(sut).createSxssfWorkbook()
        val dateCellStyle = mock<CellStyle>()
        doReturn(dateCellStyle).`when`(sut).createDateCellStyle(wb)
        val percentCellStyle = mock<CellStyle>()
        doReturn(percentCellStyle).`when`(sut).createPercentCellStyle(wb)
        val absSheet = mock<SXSSFSheet>()
        `when`(wb.createSheet("Absolute")).thenReturn(absSheet)
        val percentSheet = mock<SXSSFSheet>()
        `when`(wb.createSheet("Percent")).thenReturn(percentSheet)
        val absHeaderRow = mock<SXSSFRow>()
        `when`(absSheet.createRow(0)).thenReturn(absHeaderRow)
        val percentHeaderRow = mock<SXSSFRow>()
        `when`(percentSheet.createRow(0)).thenReturn(percentHeaderRow)
        doNothing().`when`(sut).fillHeaderAbs(reportData, absHeaderRow)
        doNothing().`when`(sut).fillHeaderPercent(reportData, percentHeaderRow)

        val rctr = spy(AtomicInteger())
        doReturn(rctr).`when`(sut).createAtomicInteger()

        val startDate = LocalDate.of(2017, 5, 9).toDate()
        val endDate = LocalDate.of(2017, 5, 10).toDate()
        doReturn(endDate).`when`(sut).calculateEndDate()
        val rowByMetricIdx = mock<MutableMap<Int,Int>>()
        doReturn(rowByMetricIdx).`when`(sut).createRowByMetricIdx(reportData)


        val dayReport1 = startDate
        val dayReport2 = LocalDate.of(2017, 5, 10).toDate()
        val dayReport3 = LocalDate.of(2017, 5, 11).toDate()

        doReturn(dayReport2).`when`(sut).printRow(
                absSheet,
                rctr,
                percentSheet,
                dayReport1,
                dateCellStyle,
                reportData,
                rowByMetricIdx,
                percentCellStyle
        )
        doReturn(dayReport3).`when`(sut).printRow(
                absSheet,
                rctr,
                percentSheet,
                dayReport2,
                dateCellStyle,
                reportData,
                rowByMetricIdx,
                percentCellStyle
        )

        doReturn(startDate).`when`(sut).calculateStartDate(reportData)
        val file = null
        doReturn(file).`when`(sut).writeToTempFile(wb)
        assertThat(rctr.get()).isZero

        val inOrder = inOrder(sut, wb, absSheet, percentSheet, rctr)

        // Run method under test
        val actRes = sut.createReport(reportData)

        // Verify
        inOrder.verify(sut).createSxssfWorkbook()
        inOrder.verify(sut).createDateCellStyle(wb)
        inOrder.verify(sut).createPercentCellStyle(wb)
        inOrder.verify(wb).createSheet("Absolute")
        inOrder.verify(wb).createSheet("Percent")
        inOrder.verify(sut).createAtomicInteger()
        inOrder.verify(absSheet).createRow(0)
        inOrder.verify(percentSheet).createRow(0)
        inOrder.verify(sut).fillHeaderAbs(reportData, absHeaderRow)
        inOrder.verify(sut).fillHeaderPercent(reportData, percentHeaderRow)
        inOrder.verify(sut).calculateStartDate(reportData)
        inOrder.verify(sut).calculateEndDate()
        inOrder.verify(sut).createRowByMetricIdx(reportData)
        inOrder.verify(sut).printRow(
                absSheet,
                rctr,
                percentSheet,
                dayReport1,
                dateCellStyle,
                reportData,
                rowByMetricIdx,
                percentCellStyle
        )
        inOrder.verify(sut).printRow(
                absSheet,
                rctr,
                percentSheet,
                dayReport2,
                dateCellStyle,
                reportData,
                rowByMetricIdx,
                percentCellStyle
        )
        inOrder.verify(sut).writeToTempFile(wb)
        inOrder.verify(wb).dispose()
        assertThat(rctr.get()).isEqualTo(1)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Temp. file writing failure")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createReportWorkBookSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val reportData = mock<List<DailyMetricValues>>()
        val wb = mock<SXSSFWorkbook>()
        doReturn(wb).`when`(sut).createSxssfWorkbook()
        val dateCellStyle = mock<CellStyle>()
        doReturn(dateCellStyle).`when`(sut).createDateCellStyle(wb)
        val percentCellStyle = mock<CellStyle>()
        doReturn(percentCellStyle).`when`(sut).createPercentCellStyle(wb)
        val absSheet = mock<SXSSFSheet>()
        `when`(wb.createSheet("Absolute")).thenReturn(absSheet)
        val percentSheet = mock<SXSSFSheet>()
        `when`(wb.createSheet("Percent")).thenReturn(percentSheet)
        val absHeaderRow = mock<SXSSFRow>()
        `when`(absSheet.createRow(0)).thenReturn(absHeaderRow)
        val percentHeaderRow = mock<SXSSFRow>()
        `when`(percentSheet.createRow(0)).thenReturn(percentHeaderRow)
        doNothing().`when`(sut).fillHeaderAbs(reportData, absHeaderRow)
        doNothing().`when`(sut).fillHeaderPercent(reportData, percentHeaderRow)

        val rctr = spy(AtomicInteger())
        doReturn(rctr).`when`(sut).createAtomicInteger()

        val startDate = LocalDate.of(2017, 5, 9).toDate()
        val endDate = LocalDate.of(2017, 5, 10).toDate()
        doReturn(endDate).`when`(sut).calculateEndDate()
        val rowByMetricIdx = mock<MutableMap<Int,Int>>()
        doReturn(rowByMetricIdx).`when`(sut).createRowByMetricIdx(reportData)


        val dayReport1 = startDate
        val dayReport2 = LocalDate.of(2017, 5, 10).toDate()
        val dayReport3 = LocalDate.of(2017, 5, 11).toDate()

        doReturn(dayReport2).`when`(sut).printRow(
                absSheet,
                rctr,
                percentSheet,
                dayReport1,
                dateCellStyle,
                reportData,
                rowByMetricIdx,
                percentCellStyle
        )
        doReturn(dayReport3).`when`(sut).printRow(
                absSheet,
                rctr,
                percentSheet,
                dayReport2,
                dateCellStyle,
                reportData,
                rowByMetricIdx,
                percentCellStyle
        )

        doReturn(startDate).`when`(sut).calculateStartDate(reportData)
        val file = mock<File>()
        doReturn(file).`when`(sut).writeToTempFile(wb)

        val inOrder = inOrder(sut, wb, absSheet, percentSheet, rctr,
                percentHeaderRow, absHeaderRow)
        assertThat(rctr.get()).isEqualTo(0)

        // Run method under test
        val actRes = sut.createReport(reportData)

        // Verify
        inOrder.verify(sut).createSxssfWorkbook()
        inOrder.verify(sut).createDateCellStyle(wb)
        inOrder.verify(sut).createPercentCellStyle(wb)
        inOrder.verify(wb).createSheet("Absolute")
        inOrder.verify(wb).createSheet("Percent")
        inOrder.verify(sut).createAtomicInteger()
        inOrder.verify(absSheet).createRow(0)
        inOrder.verify(percentSheet).createRow(0)

        inOrder.verify(sut).fillHeaderAbs(reportData, absHeaderRow)
        inOrder.verify(sut).fillHeaderPercent(reportData, percentHeaderRow)
        inOrder.verify(sut).calculateStartDate(reportData)
        inOrder.verify(sut).calculateEndDate()
        inOrder.verify(sut).createRowByMetricIdx(reportData)
        inOrder.verify(sut).printRow(
                absSheet,
                rctr,
                percentSheet,
                dayReport1,
                dateCellStyle,
                reportData,
                rowByMetricIdx,
                percentCellStyle
        )
        inOrder.verify(sut).printRow(
                absSheet,
                rctr,
                percentSheet,
                dayReport2,
                dateCellStyle,
                reportData,
                rowByMetricIdx,
                percentCellStyle
        )
        inOrder.verify(sut).writeToTempFile(wb)
        inOrder.verify(wb).dispose()

        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEqualTo("")
        assertThat(actRes.result).isSameAs(file)

        assertThat(rctr.get()).isEqualTo(1)
    }
    @Test
    fun createAtomicInteger() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = ReportThread(
                mongo,
                bot,
                chatId,
                mock<ITelegramUtils>(),
                dataCreator,
                logger
        )

        // Run method under test
        val actRes = sut.createAtomicInteger()

        // Verify
        assertThat(actRes.get()).isEqualTo(0)
    }
    @Test
    fun tomorrow() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = ReportThread(
                mongo,
                bot,
                chatId,
                mock<ITelegramUtils>(),
                dataCreator,
                logger
        )
        val day = LocalDate.of(2017, 5, 9).toDate()

        // Run method under test
        val actRes = sut.tomorrow(day)

        // Verify
        assertThat(actRes).isEqualTo(LocalDate.of(2017, 5, 10).toDate())
    }
    @Test
    fun printRowCurIdxPositive() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                mock<ITelegramUtils>(),
                dataCreator,
                logger
        ))
        val absSheet = mock<SXSSFSheet>()
        val rctr = AtomicInteger(1605)
        val percentSheet = mock<SXSSFSheet>()
        val dayReport = mock<Date>()
        val dateCellStyle = mock<CellStyle>()
        val abs = 16.16
        val percent = 0.5
        val reportData = listOf(DailyMetricValues(
                RCmd.MetricWordCount,
                RCmd.UnitWords,
                listOf(
                        DailyMetricValue(dayReport, abs, percent)
                )))
        val rowByMetricIdx = mock<MutableMap<Int, Int>>()
        val percentCellStyle = mock<CellStyle>()
        val absRow = mock<SXSSFRow>()
        `when`(absSheet.createRow(1605)).thenReturn(absRow)
        val percentRow = mock<SXSSFRow>()
        `when`(percentSheet.createRow(1605)).thenReturn(percentRow)

        val rows = mock<Set<SXSSFRow>>()
        doReturn(rows).`when`(sut).createRowPair(absRow, percentRow)
        val week = "2017-20"
        doReturn(week).`when`(sut).composeWeekText(dayReport)
        doNothing().`when`(sut).writeText(rows, week, 0)
        doNothing().`when`(sut).writeDate(rows, dayReport, 0, dateCellStyle)

        val start = 1611
        `when`(rowByMetricIdx[0]).thenReturn(start)
        val j = 1613
        val curIdx = 0
        val jCurIdx = Pair<Int,Int>(j, curIdx)
        doReturn(jCurIdx).`when`(sut).findIdxByDate(
                dayReport,
                start,
                reportData[0].dailyValues
        )

        doNothing().`when`(sut).writeNumber(absRow, abs, 2)
        doNothing().`when`(sut).writeNumberWithStyle(
                percentRow,
                percent,
                2,
                percentCellStyle
        )
        val tomorrow = LocalDate.of(2017, 5, 10).toDate()
        doReturn(tomorrow).`when`(sut).tomorrow(dayReport)
        doNothing().`when`(sut).writeText(rows, week, 0)
        doNothing().`when`(sut).writeDate(rows, dayReport, 1, dateCellStyle)
        val inOrder = inOrder(mongo, bot, dataCreator, sut, logger,
                absSheet, percentSheet, rowByMetricIdx)

        // Run method under test
        val actRes = sut.printRow(
                absSheet, rctr, percentSheet, dayReport, dateCellStyle,
                reportData, rowByMetricIdx, percentCellStyle
        )

        // Verify
        inOrder.verify(absSheet).createRow(1605)
        inOrder.verify(percentSheet).createRow(1605)
        inOrder.verify(sut).createRowPair(absRow, percentRow)
        inOrder.verify(sut).composeWeekText(dayReport)
        inOrder.verify(sut).writeText(rows, week, 0)
        inOrder.verify(sut).writeDate(rows, dayReport, 1, dateCellStyle)
        inOrder.verify(rowByMetricIdx).get(0)
        inOrder.verify(sut).findIdxByDate(
                dayReport,
                start,
                reportData[0].dailyValues
        )
        inOrder.verify(sut).writeNumber(absRow, abs, 2)
        inOrder.verify(sut).writeNumberWithStyle(
                percentRow,
                percent,
                2,
                percentCellStyle
        )
        inOrder.verify(rowByMetricIdx).put(0, j)
        inOrder.verify(sut).tomorrow(dayReport)
        assertThat(rctr.get()).isEqualTo(1606)
        assertThat(actRes).isSameAs(tomorrow)
    }
    @Test
    fun printRowCurIdxNegative() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                mock<ITelegramUtils>(),
                dataCreator,
                logger
        ))
        val absSheet = mock<SXSSFSheet>()
        val rctr = AtomicInteger(1605)
        val percentSheet = mock<SXSSFSheet>()
        val dayReport = mock<Date>()
        val dateCellStyle = mock<CellStyle>()
        val abs = 16.16
        val percent = 0.5
        val reportData = listOf(DailyMetricValues(
                RCmd.MetricWordCount,
                RCmd.UnitWords,
                listOf(
                        DailyMetricValue(dayReport, abs, percent)
                )))
        val rowByMetricIdx = mock<MutableMap<Int, Int>>()
        val percentCellStyle = mock<CellStyle>()
        val absRow = mock<SXSSFRow>()
        `when`(absSheet.createRow(1605)).thenReturn(absRow)
        val percentRow = mock<SXSSFRow>()
        `when`(percentSheet.createRow(1605)).thenReturn(percentRow)

        val rows = mock<Set<SXSSFRow>>()
        doReturn(rows).`when`(sut).createRowPair(absRow, percentRow)
        val week = "2017-20"
        doReturn(week).`when`(sut).composeWeekText(dayReport)
        doNothing().`when`(sut).writeText(rows, week, 0)
        doNothing().`when`(sut).writeDate(rows, dayReport, 1, dateCellStyle)

        val start = 1611
        `when`(rowByMetricIdx[0]).thenReturn(start)
        val j = 1613
        val curIdx = -1
        val jCurIdx = Pair<Int,Int>(j, curIdx)
        doReturn(jCurIdx).`when`(sut).findIdxByDate(
                dayReport,
                start,
                reportData[0].dailyValues
        )

        doNothing().`when`(sut).writeNumber(absRow, abs, 2)
        doNothing().`when`(sut).writeNumberWithStyle(
                percentRow,
                percent,
                2,
                percentCellStyle
        )
        val tomorrow = LocalDate.of(2017, 5, 10).toDate()
        doReturn(tomorrow).`when`(sut).tomorrow(dayReport)
        doNothing().`when`(sut).writeDate(rows, dayReport, 1, dateCellStyle)

        val inOrder = inOrder(mongo, bot, dataCreator, sut, logger, absSheet,
                percentSheet, rowByMetricIdx)

        // Run method under test
        val actRes = sut.printRow(
                absSheet, rctr, percentSheet, dayReport, dateCellStyle,
                reportData, rowByMetricIdx, percentCellStyle
        )

        // Verify
        inOrder.verify(absSheet).createRow(1605)
        inOrder.verify(percentSheet).createRow(1605)
        inOrder.verify(sut).createRowPair(absRow, percentRow)
        inOrder.verify(sut).composeWeekText(dayReport)
        inOrder.verify(sut).writeText(rows, week, 0)
        inOrder.verify(sut).writeDate(rows, dayReport, 1, dateCellStyle)
        inOrder.verify(rowByMetricIdx).get(0)
        inOrder.verify(sut).findIdxByDate(
                dayReport,
                start,
                reportData[0].dailyValues
        )
        inOrder.verify(sut, never()).writeNumber(absRow, abs, 2)
        inOrder.verify(sut, never()).writeNumberWithStyle(
                percentRow,
                percent,
                2,
                percentCellStyle
        )
        inOrder.verify(rowByMetricIdx).put(0, j)
        inOrder.verify(sut).tomorrow(dayReport)
        assertThat(rctr.get()).isEqualTo(1606)
        assertThat(actRes).isSameAs(tomorrow)

    }
    @Test
    fun createRowPair() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = ReportThread(
                mongo,
                bot,
                chatId,
                mock<ITelegramUtils>(),
                dataCreator,
                logger
        )
        val absRow = mock<SXSSFRow>()
        val percentRow = mock<SXSSFRow>()

        // Run method under test
        val actRes = sut.createRowPair(absRow, percentRow)

        // Verify
        assertThat(actRes.size).isEqualTo(2)
        assertThat(actRes).contains(absRow)
        assertThat(actRes).contains(percentRow)
    }
    @Test
    fun writeToTempFileRainyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = spy(
                ReportThread(
                    mongo,
                    bot,
                    chatId,
                    mock<ITelegramUtils>(),
                    dataCreator,
                    logger
                )
        )
        val wb = mock<SXSSFWorkbook>()
        val msg = "msg"
        val throwable = RuntimeException(msg)
        doThrow(throwable).`when`(sut).createTempFile()

        // Run method under test
        val actRes = sut.writeToTempFile(wb)

        // Verify
        verify(sut).createTempFile()
        verify(logger).error("writeToTempFile", throwable)
        assertThat(actRes).isNull()
    }
    @Test
    fun writeToTempFileSunnyDay() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = spy(ReportThread(
                mongo,
                bot,
                chatId,
                mock<ITelegramUtils>(),
                dataCreator,
                logger
        ))
        val wb = mock<SXSSFWorkbook>()
        val file = mock<File>()
        doReturn(file).`when`(sut).createTempFile()
        val stream = mock<FileOutputStream>()
        doReturn(stream).`when`(sut).createFileOutputStream(file)

        // Run method under test
        val actRes = sut.writeToTempFile(wb)

        // Verify
        verify(sut).createTempFile()
        verify(sut).createFileOutputStream(file)
        verify(wb).write(stream)
        verify(stream).close()
        assertThat(actRes).isSameAs(file)
    }
    @Test
    fun createRowByMetricIdx() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val sut = ReportThread(
                mongo,
                bot,
                chatId,
                mock<ITelegramUtils>(),
                dataCreator,
                logger
        )
        val reportData = mock<List<DailyMetricValues>>()
        val size = 2
        `when`(reportData.size).thenReturn(size)

        // Run method under test
        val actRes = sut.createRowByMetricIdx(reportData)

        // Verify
        assertThat(actRes[0]).isNotNull
        assertThat(actRes[1]).isNotNull
        assertThat(actRes[0]).isEqualTo(0)
        assertThat(actRes[1]).isEqualTo(0)
        assertThat(actRes.keys.size).isEqualTo(size)
    }
    @Test
    fun calculateStartDate() {
        val mv1 = DailyMetricValues(
                "Metric1",
                "Unit1",
                emptyList()
        )
        val date1 = LocalDate.of(2017, 5, 9).toDate()
        val mv2 = DailyMetricValues(
                "Metric2",
                "Unit2",
                listOf(
                        DailyMetricValue(
                                date1,
                                1.0,
                                2.0
                        ),
                        DailyMetricValue(
                                LocalDate.of(2017, 5, 10).toDate(),
                                1.0,
                                2.0
                        )
                )
        )
        val date2 = LocalDate.of(2017, 5, 7).toDate()
        val mv3 = DailyMetricValues(
                "Metric3",
                "Unit3",
                listOf(
                        DailyMetricValue(
                                date2,
                                1.0,
                                2.0
                        )
                )
        )
        calculateStartDateTestLogic(listOf(mv1), null)
        calculateStartDateTestLogic(listOf(mv2), date1)
        calculateStartDateTestLogic(listOf(mv1, mv2, mv3), date2)
        calculateStartDateTestLogic(listOf(mv2, mv3), date2)
        calculateStartDateTestLogic(listOf(mv3), date2)
    }
    @Test
    fun calculateEndDate() {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )
        val now = LocalDateTime.of(2017, 5, 9, 16, 53, 30).toDate()
        doReturn(now).`when`(sut).now()

        // Run method under test
        val actRes = sut.calculateEndDate()

        // Verify
        verify(sut).now()
        val expRes = LocalDateTime.of(2017, 5, 9, 0, 0, 0).toDate()
        assertThat(actRes).isEqualTo(expRes)
    }
    @Test
    fun findIdxByDate() {
        val day0 = LocalDate.of(2017, 4, 30).toDate()
        val day1 = LocalDate.of(2017, 5, 1).toDate()
        val day2 = LocalDate.of(2017, 5, 4).toDate()
        val day3 = LocalDate.of(2017, 5, 5).toDate()
        val day4 = LocalDate.of(2017, 5, 6).toDate()

        val input = listOf(day1, day2, day3).map {
            DailyMetricValue(it, 0.0, 0.0)
        }.toList()
        findIdxByDateTestLogic(input, day0, 0, -1)
        findIdxByDateTestLogic(input, day1, 0, 0)
        findIdxByDateTestLogic(input, day2, 1, 1)
        findIdxByDateTestLogic(input, day3, 2, 2)
        findIdxByDateTestLogic(input, day4, 3, -1)
    }

    private fun findIdxByDateTestLogic(
            input: List<DailyMetricValue>,
            dayReport: Date,
            expJ: Int,
            expCurIdx: Int
    ) {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )

        // Run method under test
        val (actJ, actCurIdx) = sut.findIdxByDate(dayReport, 0, input)

        // Verify
        assertThat(actJ).isEqualTo(expJ)
        assertThat(actCurIdx).isEqualTo(expCurIdx)
    }

    private fun calculateStartDateTestLogic(
            input: List<DailyMetricValues>,
            expRes: Date?
    ) {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )

        // Run method under test
        val actRes = sut.calculateStartDate(input)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun day(year: Int, month: Int, day: Int) =
            LocalDateTime.of(year, month, day, 0, 0).toDate()

    private fun composeWeekTextTestLogic(day: Date, expRes: String) {
        // Prepare
        val mongo = mock<IAltruixIs1MongoSubsystem>()
        val bot = mock<IResponsiveBot>()
        val chatId = 1433L
        val dataCreator = mock<IReportDataCreator>()
        val logger = mock<Logger>()
        val tu = mock<ITelegramUtils>()
        val sut = spy(
                ReportThread(
                        mongo,
                        bot,
                        chatId,
                        tu,
                        dataCreator,
                        logger
                )
        )

        // Run method under test
        val actRes = sut.composeWeekText(day)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}
