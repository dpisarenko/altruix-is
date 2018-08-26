package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.App
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.*

/**
 * Created by 1 on 25.02.2017.
 */
open class CompanyIdExtractor(
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : ICompanyIdExtractor {
    override fun extractCompanyIds(stream: InputStream): List<String> {
        var wb:HSSFWorkbook? = null
        try {
            wb = createHssfWorkbook(stream)
            return readDataFromExcel(wb)
        }
        catch (throwable:Throwable) {
            logger.error("extractCompanyIds", throwable)
            return emptyList()
        }
        finally {
            closeWorkBook(wb)
        }
        return emptyList()
    }

    open fun createHssfWorkbook(stream: InputStream) = HSSFWorkbook(stream)

    open fun readDataFromExcel(wb: HSSFWorkbook): List<String> {
        val sheet = wb.getSheetAt(0)
        val res = ArrayList<String>(sheet.lastRowNum)
        for (i in 0..sheet.lastRowNum) {
            val row = sheet.getRow(i)
            val cell = row.getCell(1)
            if (cell.cellTypeEnum == CellType.NUMERIC) {
                res.add(cell.numericCellValue.toInt().toString())
            }
        }
        return res
    }

    open fun closeWorkBook(wb: HSSFWorkbook?) {
        wb?.close()
    }
}