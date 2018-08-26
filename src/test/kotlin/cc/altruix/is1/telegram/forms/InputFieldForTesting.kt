package cc.altruix.is1.telegram.forms

import cc.altruix.is1.validation.FailableOperationResult

/**
 * Created by 1 on 09.04.2017.
 */
class InputFieldForTesting(id:String) : InputField(id, "", "") {
    override fun parse(text: String): FailableOperationResult<Any>
        = FailableOperationResult(false, "not implemented", null)
}