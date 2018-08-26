package cc.altruix.is1.adr

import org.apache.commons.lang3.StringUtils
import org.apache.commons.math3.stat.regression.SimpleRegression
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by pisarenko on 30.05.2017.
 */
class AmazonAdCompanion : IAmazonAdCompanion {
    open fun runLinearRegression(terms: Set<Term>, ads: Set<AdPerformanceTuple>) {
        val adsByIds = ads.associateBy { it.id }
        terms.forEach { term ->
            val reg = SimpleRegression(false)
            term.tfByAdIds.entries.forEach { (id:String, tf:Double) ->
                val ad = adsByIds[id]
                if (ad == null) {
                    return@forEach
                }
                val adPerformance = ad.performance
                val tfIdf = tf * term.idf

                reg.addData(tfIdf, adPerformance)
            }
            term.beta = reg.slope
        }
    }

    open fun calculateTfIdf(terms: Set<Term>, ads: Set<AdPerformanceTuple>) {
        val docCountByTerm:MutableMap<Term,AtomicInteger> = HashMap()

        terms.forEach { term ->
            docCountByTerm[term] = AtomicInteger(0)
        }

        terms.forEach { term ->
            ads.forEach { ad ->
                val numberOfAppearances =
                        StringUtils.countMatches(ad.text.toLowerCase(), term.word).toDouble()
                val totalTermsCount = ad.termsCount()
                term.tfByAdIds[ad.id] = numberOfAppearances / totalTermsCount

                if (numberOfAppearances > 0) {
                    docCountByTerm[term]?.incrementAndGet()
                }
            }
        }
        val totalNumberOfDocuments = ads.size.toDouble()

        terms.forEach { term ->
            val documentsWithTermInIt = docCountByTerm[term]?.get()?.toDouble() ?: 0.0
            if (documentsWithTermInIt > 0.0) {
                term.idf = Math.log(totalNumberOfDocuments / documentsWithTermInIt)
            } else {
                throw RuntimeException("Unfixable internal error")
            }
        }
    }

    open fun extractTerms(ads: Set<AdPerformanceTuple>): Set<Term> =
            ads
                    .map { it.text }
                    .map { it.split(" ") }
                    .flatten()
                    .map { it.toLowerCase() }
                    .map { removeSpecialCharacters(it) }
                    .filter { StringUtils.isNotBlank(it) }
                    .map { Term(it) }
                    .toSet()

    private fun removeSpecialCharacters(txt: String): String {
        var res =
                txt.replace(",", "")
                        .replace(".", "")
                        .replace("?", "")
                        .replace("!", "")
        return res
    }
}