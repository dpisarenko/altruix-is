package cc.altruix.is1.adr

/**
 * Created by pisarenko on 30.05.2017.
 */
data class AdPerformanceTuple(
        val id:String,
        val text:String,
        val performance:Double
) {
    fun termsCount():Double {
        return text.split(" ").count().toDouble()
    }
}