package cc.altruix.is1.validation

/**
 * Created by pisarenko on 03.02.2017.
 */
open class FailableOperationResult<C> (
        val success:Boolean,
        val error:String,
        val result:C?
) {

}