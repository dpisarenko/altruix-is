package cc.altruix.is1.telegram.forms

import cc.altruix.is1.validation.FailableOperationResult

/**
 * Created by pisarenko on 12.04.2017.
 */
open class InputFieldForTesting2(
        id:String, targetProperty:String, msg:String) : InputField(id, targetProperty, msg) {
    override fun parse(text: String): FailableOperationResult<Any> =
            FailableOperationResult(false, "not impelemented", null)
}