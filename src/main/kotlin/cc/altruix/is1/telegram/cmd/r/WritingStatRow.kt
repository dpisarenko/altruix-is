package cc.altruix.is1.telegram.cmd.r

import java.util.*

/**
 * Created by pisarenko on 13.04.2017.
 */
data class WritingStatRow(
        val timestamp: Date,
        val work:String,
        val part:String,
        val wordCount:Int
)