package cc.altruix.is1.telegram.forms

import cc.altruix.is1.validation.FailableOperationResult
import org.apache.commons.lang3.StringUtils

class DoubleInput(id:String, targetProperty:String, msg:String) :
        InputField(id, targetProperty, msg) {
    override fun parse(text: String): FailableOperationResult<Any> {
        if (StringUtils.isBlank(text.trim())) {
            return FailableOperationResult(false, "Blank text", null)
        }
        try {
            val result = text.trim().toDouble()
            if (result < 0.0) {
                return FailableOperationResult(
                        false,
                        "Negative number",
                        null
                )
            }
            return FailableOperationResult(
                    true,
                    "",
                    result
            )
        } catch (e:NumberFormatException) {
            return FailableOperationResult(
                    false,
                    "Wrong number format",
                    null
            )
        }
    }
}