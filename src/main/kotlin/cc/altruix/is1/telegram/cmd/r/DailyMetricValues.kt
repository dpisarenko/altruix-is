package cc.altruix.is1.telegram.cmd.r

/**
 * Created by pisarenko on 13.04.2017.
 */
data class DailyMetricValues(
        val metric:String,
        val unit:String,
        val dailyValues:List<DailyMetricValue>
)