package cc.altruix.caw.dsl.v1

/**
 * Created by pisarenko on 25.05.2017.
 */
class TimesPrinter {
    fun toString(timeInstants: List<TimeInstant>): String {
        val sb = StringBuilder()
        sb.append("ID;Time\n")
        timeInstants.sortedBy { it.id }.map {
            "${it.id};${it?.dateTime.toString()}\n"
        }.joinTo(sb, "")
        return sb.toString()
    }
}