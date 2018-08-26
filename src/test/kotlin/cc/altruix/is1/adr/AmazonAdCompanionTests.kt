package cc.altruix.is1.adr

import org.apache.commons.io.IOUtils
import org.fest.assertions.Assertions.assertThat
import org.junit.Test

/**
 * Created by pisarenko on 30.05.2017.
 */
class AmazonAdCompanionTests {
    companion object {
        val Ad1 = "Ad-1"
        val Ad2 = "Ad-2"
        val Ad3 = "Ad-3"
        val Ad4 = "Ad-4"
        val SampleAds = setOf(
                AdPerformanceTuple(
                        Ad1,
                        "Super-duper short story.",
                        0.5
                ),
                AdPerformanceTuple(
                        Ad2,
                        "A refugee drama in the heart of Europe.",
                        0.6
                ),
                AdPerformanceTuple(
                        Ad3,
                        "Wanna go on vacation to Europe? Read this thriller!",
                        0.4
                ),
                AdPerformanceTuple(
                        Ad4,
                        "Street fights! Foreign people! Gothic vaults! All this and more in the new short story X.",
                        0.8
                )
        )
    }
    @Test
    fun extractTerms() {
        // Prepare
        val sut = AmazonAdCompanion()

        // Run method under test
        val actRes = sut.extractTerms(SampleAds)

        // Verify
        val actWords = actRes.map { it.word }.toSet()
        val expWords = setOf<String>("super-duper",
                "short",
                "story",
                "a",
                "refugee",
                "drama",
                "in",
                "the",
                "heart",
                "of",
                "europe",
                "wanna",
                "go",
                "on",
                "vacation",
                "to",
                "europe",
                "read",
                "this",
                "thriller",
                "street",
                "fights",
                "foreign",
                "people",
                "gothic",
                "vaults",
                "all",
                "this",
                "and",
                "more",
                "in",
                "the",
                "new",
                "short",
                "story",
                "x")
        assertThat(actWords).isEqualTo(expWords)
    }
    @Test
    fun spike() {
        // Prepare
        val sut = AmazonAdCompanion()
        val ads = SampleAds
        val terms = sut.extractTerms(ads)

        // Run method under test
        sut.calculateTfIdf(terms, ads)
        sut.runLinearRegression(terms, ads)

        // Verify
        val expRes = readFile("AmazonAdCompanionTests.termsTfIdf.exp.csv")
        assertThat(termsToCsv(terms)).isEqualTo(expRes)
    }

    private fun termsToCsv(terms: Set<Term>): String {
        val sb = StringBuilder()
        sb.append("Word")
        sb.append(";")
        sb.append("Beta (slope)")
        sb.append(";")
        sb.append("IDF")
        sb.append(";")
        sb.append("TF($Ad1)")
        sb.append(";")
        sb.append("TF($Ad2)")
        sb.append(";")
        sb.append("TF($Ad3)")
        sb.append(";")
        sb.append("TF($Ad4)")
        sb.append(";")
        sb.append("\n")

        terms.toList().sortedBy { it.word }.forEach { term ->
            sb.append(term.word)
            sb.append(";")
            sb.append(term.beta)
            sb.append(";")
            sb.append(term.idf)
            sb.append(";")
            sb.append(term.tfByAdIds[Ad1])
            sb.append(";")
            sb.append(term.tfByAdIds[Ad2])
            sb.append(";")
            sb.append(term.tfByAdIds[Ad3])
            sb.append(";")
            sb.append(term.tfByAdIds[Ad4])
            sb.append(";")
            sb.append("\n")
        }
        return sb.toString()
    }
    private fun readFile(file: String) =
            IOUtils.toString(
                    javaClass.classLoader.getResourceAsStream(
                            "cc/altruix/is1/adr/" + file
                    ),
                    "UTF-8"
            )

}