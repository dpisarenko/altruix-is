package cc.altruix.is1.telegram.cmd.r

import cc.altruix.is1.App
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.utils.composeWeekText2
import cc.altruix.utils.toLocalDate
import org.apache.commons.lang3.time.DateUtils
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFRow
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

open class ReportThread(
        val mongo: IAltruixIs1MongoSubsystem,
        val bot: IResponsiveBot,
        val chatId: Long,
        val tu: ITelegramUtils,
        val dataCreator: IReportDataCreator = ReportDataCreator(mongo),
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName)) : Thread() {
    companion object {
        val StartDate = "2017-04-13"
        val Password = "PeJp)okkbx4X3lkkm55clI15_RYmOe"
        val Host = "mail.altruix.cc"
        val SenderAddress = "is1@altruix.cc"
        val RecipientAddress = "dp@altruix.co"
        val AltruixISWeekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
    }
    override fun run() {
        val start = createStartDate()
        val reportDataRes = dataCreator.createData(start)
        if (reportDataRes.success && (reportDataRes.result != null)) {
            val reportRes = createReport(reportDataRes.result)
            if (reportRes.success && (reportRes.result != null)) {
                val sendRes = sendReport(reportRes.result, bot, chatId)
                reportRes.result.delete()
                reportRes.result.deleteOnExit()
                if (sendRes.success) {
                    tu.sendTextMessage("Report sent.", chatId, bot)
                } else {
                    tu.displayError(
                            "Excel file could not be sent.",
                            chatId,
                            bot
                    )
                }
            } else {
                tu.displayError(
                        "Excel file could not be prepared.",
                        chatId,
                        bot
                )
            }
        } else {
            tu.displayError(
                    "Report data could not be prepared.",
                    chatId,
                    bot
            )
        }
    }

    open fun sendReport(report: File, bot: IResponsiveBot, chatId: Long):ValidationResult {
        var transport: Transport? = null
        try {
            val body = createBody()
            val attachment = createAttachment(report)
            val multipart = createMultipart(attachment, body)
            val props = createProperties()
            val session = getSession(props)
            val msg = createMimeMessage(multipart, session)
            transport = getTransport(session)
            transport.connect(
                    Host,
                    SenderAddress,
                    Password
            )
            transport.sendMessage(msg, msg.allRecipients)
            return ValidationResult(true, "")
        } catch (t:Throwable) {
            logger.error(
                    "sendReport(chatId=$chatId)",
                    t
            )
            return ValidationResult(
                    false,
                    "Could not send the report ('${t.message}')."
            )
        }
        finally {
            transport?.close()
        }
    }

    open fun getTransport(session: Session) = session.getTransport("smtps")

    open fun getSession(props: Properties) = Session.getInstance(props, null)

    open fun createMimeMessage(
            multipart: MimeMultipart,
            session: Session
    ): MimeMessage {
        val msg = createMimeMessage(session)
        msg.setFrom(createInternetAddress(SenderAddress))
        msg.setRecipients(Message.RecipientType.TO,
                parse(RecipientAddress))
        msg.setSubject("Report")
        msg.setContent(multipart)
        msg.sentDate = now()
        return msg
    }

    open fun now() = Date()

    open fun parse(address: String) = InternetAddress.parse(address, false)

    open fun createInternetAddress(sender: String) = InternetAddress(sender)

    open fun createMimeMessage(session: Session) = MimeMessage(session)

    open fun createMultipart(
            attachment: MimeBodyPart,
            body: MimeBodyPart
    ): MimeMultipart {
        val multipart = createMimeMultipart()
        multipart.addBodyPart(body)
        multipart.addBodyPart(attachment)
        return multipart
    }

    open fun createMimeMultipart() = MimeMultipart()

    open fun createAttachment(report: File): MimeBodyPart {
        val attachment = createMimeBodyPart()
        val src = createFileDataSource(report)
        attachment.dataHandler = createDataHandler(src)
        attachment.fileName = "Report.xls"
        return attachment
    }

    open fun createDataHandler(src: FileDataSource) = DataHandler(src)

    open fun createFileDataSource(report: File) = FileDataSource(report)

    open fun createBody(): MimeBodyPart {
        val body = createMimeBodyPart()
        body.setText("Report with all time series generated with the /r command.")
        return body
    }

    open fun createMimeBodyPart() = MimeBodyPart()

    open fun createProperties(): Properties {
        val props = System.getProperties()
        props.setProperty("mail.smtps.host", Host)
        props.setProperty("mail.smtps.auth", "true");
        return props
    }

    open fun createReport(reportData: List<DailyMetricValues>):
            FailableOperationResult<File> {
        var wb: SXSSFWorkbook? = null
        try {
            wb = createSxssfWorkbook()
            if (wb == null) {
                return FailableOperationResult<File>(
                        false,
                        "Internal error.",
                        null
                )
            }
            val dateCellStyle = createDateCellStyle(wb)
            val percentCellStyle = createPercentCellStyle(wb)
            val absSheet = wb.createSheet("Absolute")
            val percentSheet = wb.createSheet("Percent")
            var rctr = createAtomicInteger()
            val absHeaderRow = absSheet.createRow(rctr.get())
            val percentHeaderRow = percentSheet.createRow(rctr.get())

            rctr.incrementAndGet()
            fillHeaderAbs(reportData, absHeaderRow)
            fillHeaderPercent(reportData, percentHeaderRow)

            val startDate = calculateStartDate(reportData)
            if (startDate == null) {
                return FailableOperationResult<File>(
                        false,
                        "No data.",
                        null
                )
            }
            val endDate = calculateEndDate()
            var dayReport = startDate!!
            val rowByMetricIdx = createRowByMetricIdx(reportData)
            while (dayReport.compareTo(endDate) <= 0) {
                dayReport = printRow(
                        absSheet,
                        rctr,
                        percentSheet,
                        dayReport,
                        dateCellStyle,
                        reportData,
                        rowByMetricIdx,
                        percentCellStyle
                )
            }

            val file = writeToTempFile(wb)
            if (file != null) {
                return FailableOperationResult<File>(
                        true,
                        "",
                        file
                )
            } else {
                return FailableOperationResult<File>(
                        false,
                        "Temp. file writing failure",
                        null
                )
            }
        } catch (t:Throwable) {
            logger.error(
                    "createReport",
                    t
            )
            return FailableOperationResult<File>(
                    false,
                    "Could not create the report ('${t.message}').",
                    null
            )
        }
        finally {
            wb?.dispose()
        }
    }

    open fun createAtomicInteger() = AtomicInteger(0)

    open fun printRow(
            absSheet: SXSSFSheet,
            rctr: AtomicInteger,
            percentSheet: SXSSFSheet,
            dayReport: Date,
            dateCellStyle: CellStyle,
            reportData: List<DailyMetricValues>,
            rowByMetricIdx: MutableMap<Int, Int>,
            percentCellStyle: CellStyle
    ): Date {
        var col = 0
        val absRow = absSheet.createRow(rctr.get())
        val percentRow = percentSheet.createRow(rctr.get())
        val rows = createRowPair(absRow, percentRow)
        val week = composeWeekText(dayReport)
        writeText(rows, week, col)
        col++
        writeDate(rows, dayReport, col, dateCellStyle)
        col++

        for (i in 0..(reportData.size - 1)) {
            val start = rowByMetricIdx[i]!!
            var (j, curIdx) = findIdxByDate(dayReport, start, reportData[i].dailyValues)
            if (curIdx != -1) {
                writeNumber(absRow, reportData[i].dailyValues[curIdx].abs, col)
                writeNumberWithStyle(
                        percentRow,
                        reportData[i].dailyValues[curIdx].percent,
                        col,
                        percentCellStyle
                )
            }
            rowByMetricIdx[i] = j
            col++
        }
        rctr.incrementAndGet()
        return tomorrow(dayReport)
    }

    open fun createRowPair(absRow: SXSSFRow, percentRow: SXSSFRow): Set<SXSSFRow> =
            setOf(absRow, percentRow)

    open fun tomorrow(day:Date):Date = DateUtils.addDays(day, 1)


    open fun writeToTempFile(wb: SXSSFWorkbook): File? {
        var stream: FileOutputStream? = null
        try {
            val file = createTempFile()
            stream = createFileOutputStream(file)
            wb.write(stream)
            return file
        }
        catch (t:Throwable) {
            logger.error(
                    "writeToTempFile",
                    t
            )
            return null
        }
        finally {
            stream?.close()
        }
    }

    open fun createRowByMetricIdx(
            reportData: List<DailyMetricValues>
    ): MutableMap<Int, Int> {
        val rowByMetricIdx = HashMap<Int, Int>()
        for (i in 0..(reportData.size - 1)) {
            rowByMetricIdx[i] = 0
        }
        return rowByMetricIdx
    }

    open fun calculateStartDate(reportData: List<DailyMetricValues>): Date? =
            reportData
                    .filter { it.dailyValues.isNotEmpty() }
                    .map { it.dailyValues[0].day }
                    .min()

    open fun calculateEndDate() =
            DateUtils.truncate(
                    now(),
                    Calendar.DATE
            )

    open fun createSxssfWorkbook() = SXSSFWorkbook(100)

    open fun createFileOutputStream(file: File?) = FileOutputStream(file)

    open fun createTempFile() = File.createTempFile("Altruix-IS1-Report-", ".tmp")

    open fun createPercentCellStyle(wb: SXSSFWorkbook): CellStyle {
        val dateCellStyle = wb.createCellStyle()
        val helper = wb.creationHelper
        dateCellStyle.dataFormat = helper.createDataFormat().getFormat("0.00%")
        return dateCellStyle
    }

    open fun createDateCellStyle(wb: SXSSFWorkbook): CellStyle {
        val style = wb.createCellStyle()
        val helper = wb.creationHelper
        style.dataFormat = helper.createDataFormat().getFormat("yyyy-mm-dd")
        return style
    }

    open fun findIdxByDate(
            dayReport: Date,
            start: Int,
            src: List<DailyMetricValue>
    ): Pair<Int, Int> {
        var j = start
        var curIdx = -1
        var lookFurther = true
        val src = src
        while ((j < src.size) && (curIdx == -1) && lookFurther) {
            val dayTimeSeries = src[j].day
            val comp = dayReport.compareTo(dayTimeSeries)
            if (comp == 0) {
                curIdx = j
            } else if (comp < 0) {
                lookFurther = false
            } else {
                j++
            }
        }
        return Pair(j, curIdx)
    }
    open fun writeNumber(row: SXSSFRow, value: Double, col: Int) {
        val cell = row.createCell(col)
        cell.setCellValue(value)
    }

    open fun writeNumberWithStyle(
            row: SXSSFRow,
            value: Double,
            col: Int,
            style: CellStyle
    ) {
        val cell = row.createCell(col)
        cell.cellStyle = style
        cell.setCellValue(value)
    }

    open fun composeWeekText(day: Date): String {
        return composeWeekText2(day)
    }

    open fun writeText(rows: Set<SXSSFRow>, txt: String, col: Int) {
        rows.forEach { row ->
            val cell = row.createCell(col)
            cell.setCellValue(txt)
        }
    }

    open fun writeDate(
            rows: Set<SXSSFRow>,
            date: Date,
            col: Int,
            style: CellStyle
    ) {
        rows.forEach { row ->
            val cell = row.createCell(col)
            cell.cellStyle = style
            cell.setCellValue(date)
        }
    }

    open fun fillHeaderAbs(
            reportData: List<DailyMetricValues>,
            row: SXSSFRow
    ) {
        var colCtr = 0
        val dayCell = row.createCell(colCtr)
        dayCell.setCellValue("Week")
        colCtr++
        val weekCell = row.createCell(colCtr)
        weekCell.setCellValue("Day")
        colCtr++

        reportData.map { x -> "${x.metric} [${x.unit}]" }.forEach { header ->
            val cell = row.createCell(colCtr)
            cell.setCellValue(header)
            colCtr++
        }
    }
    open fun fillHeaderPercent(reportData: List<DailyMetricValues>, row: SXSSFRow) {
        var colCtr = 0
        val dayCell = row.createCell(colCtr)
        dayCell.setCellValue("Week")
        colCtr++
        val weekCell = row.createCell(colCtr)
        weekCell.setCellValue("Day")
        colCtr++

        reportData.map { x -> "${x.metric} [%]" }.forEach { header ->
            val cell = row.createCell(colCtr)
            cell.setCellValue(header)
            colCtr++
        }
    }

    open fun createStartDate():Date {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.parse(StartDate);
    }
}