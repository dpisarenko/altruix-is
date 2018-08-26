package cc.altruix.is1.telegram.cmd.bp2cb

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import cc.altruix.mock
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.mockito.Mockito.*
import org.slf4j.Logger
import java.io.InputStream

/**
 * Created by 1 on 25.02.2017.
 */
class CompanyIdExtractorTests {
    @Test
    fun extractCompanyIds() {
        // Prepare
        val sut = CompanyIdExtractor()
        val stream = javaClass.classLoader.getResourceAsStream(
                "cc/altruix/is1/telegram/cmd/bp2cb/capsuleContacts.xls")

        // Run method under test
        val actRes = sut.extractCompanyIds(stream)

        // Verify
        val expectedCompanyIds = listOf("131605150",
                "131605567",
                "131605870",
                "131605939",
                "131606042",
                "131606267",
                "131606425",
                "131606471",
                "131606527",
                "131606840",
                "131606871",
                "131606944",
                "131607243",
                "131607273",
                "131607327",
                "131607547",
                "131607620"
        )
        assertThat(actRes.size).isEqualTo(expectedCompanyIds.size)
        expectedCompanyIds.forEach { expRes ->
            assertThat(actRes).contains(expRes)
        }
    }
    @Test
    fun extractCompanyIdsErrorInReadDataFromExcel() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CompanyIdExtractor(logger))
        val stream = mock<InputStream>()
        val wb = HSSFWorkbook()
        doReturn(wb).`when`(sut).createHssfWorkbook(stream)
        doNothing().`when`(sut).closeWorkBook(wb)
        val throwable = RuntimeException("test")
        doThrow(throwable).`when`(sut).readDataFromExcel(wb)

        val inOrder = inOrder(logger, sut, stream)

        // Run method under test
        val actRes = sut.extractCompanyIds(stream)

        // Verify
        inOrder.verify(sut).createHssfWorkbook(stream)
        inOrder.verify(sut).readDataFromExcel(wb)
        inOrder.verify(logger).error("extractCompanyIds", throwable)
        inOrder.verify(sut).closeWorkBook(wb)
        assertThat(actRes).isEmpty()
    }
    @Test
    fun extractCompanyIdsSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CompanyIdExtractor(logger))
        val stream = mock<InputStream>()
        val wb = HSSFWorkbook()
        doReturn(wb).`when`(sut).createHssfWorkbook(stream)
        doNothing().`when`(sut).closeWorkBook(wb)
        val expRes = emptyList<String>()
        doReturn(expRes).`when`(sut).readDataFromExcel(wb)

        val inOrder = inOrder(logger, sut, stream)

        // Run method under test
        val actRes = sut.extractCompanyIds(stream)

        // Verify
        inOrder.verify(sut).createHssfWorkbook(stream)
        inOrder.verify(sut).readDataFromExcel(wb)
        inOrder.verify(sut).closeWorkBook(wb)
        assertThat(actRes).isSameAs(expRes)
    }

}