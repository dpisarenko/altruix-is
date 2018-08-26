package cc.altruix.caw.dsl

import cc.altruix.caw.dsl.v1.TimesPrinter
import cc.altruix.caw.dsl.v1.UniversePrinter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import java.io.File

/**
 * Created by pisarenko on 10.05.2017.
 */
class PlotFileParserTests {
    @Test
    fun spike() {
        val sut = PlotFileParser()
        val plot = sut.parse(readStream("plot.dot"))
        val printer = UniversePrinter()
        val txt = printer.toString(plot)
        FileUtils.writeStringToFile(
                File("src/test/resources/cc/altruix/caw/dsl/PlotFileParserTests.spike.act.kt"),
                txt,
                Charsets.UTF_8
        )

        val tp = TimesPrinter()
        val times = tp.toString(plot.timeInstants)
        FileUtils.writeStringToFile(
                File("src/test/resources/cc/altruix/caw/dsl/PlotFileParserTests.times.act.csv"),
                times,
                Charsets.UTF_8
        )
        assertThat(txt).isEqualTo(readFile("PlotFileParserTests.spike.exp.kt"))
        assertThat(times).isEqualTo(readFile("PlotFileParserTests.times.exp.csv"))
    }

    private fun readStream(file: String) = javaClass.classLoader.getResourceAsStream("cc/altruix/caw/dsl/" + file)

    private fun readFile(file: String) =
            IOUtils.toString(javaClass.classLoader.getResourceAsStream("cc/altruix/caw/dsl/" + file), "UTF-8")
}