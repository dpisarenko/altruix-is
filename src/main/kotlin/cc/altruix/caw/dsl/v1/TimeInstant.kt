package cc.altruix.caw.dsl.v1

import java.time.Instant

/**
 * Created by 1 on 14.05.2017.
 */
class TimeInstant(val id:String, val name:String) {
    var dateTime : Instant? = null
    fun setInstant(x:Instant) {
        dateTime = x
    }
}