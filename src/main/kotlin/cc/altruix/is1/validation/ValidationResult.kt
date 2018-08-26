package cc.altruix.is1.validation

/**
 * Created by pisarenko on 02.02.2017.
 */
open class ValidationResult(success:Boolean, errorMsg:String): FailableOperationResult<Unit>(success, errorMsg, null) {

}