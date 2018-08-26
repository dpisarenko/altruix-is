package cc.altruix.is1.adr

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder



/**
 * Created by pisarenko on 30.05.2017.
 */
class Term(val word:String) {
    var tfByAdIds:MutableMap<String,Double> = HashMap()
    var idf:Double = 0.0
    var beta:Double = 0.0

    override fun equals(other: Any?): Boolean {
        if (other == null) { return false; }
        if (other === this) { return true; }
        if (!(other is Term)) {
            return false;
        }
        val rhs = other
        return EqualsBuilder().append(word, rhs.word).isEquals()
    }
    override fun hashCode(): Int =
            HashCodeBuilder(17, 37).append(word).toHashCode()
}