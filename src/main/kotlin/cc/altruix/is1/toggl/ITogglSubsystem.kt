package cc.altruix.is1.toggl

import cc.altruix.is1.tpt.TotalProductiveTime
import cc.altruix.is1.validation.FailableOperationResult
import java.util.*

/**
 * Created by pisarenko on 24.04.2017.
 */
interface ITogglSubsystem {
    fun init()
    fun close()
    fun totalProductiveTime(day: Date):FailableOperationResult<TotalProductiveTime>
}