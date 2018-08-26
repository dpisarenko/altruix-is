package cc.altruix.is1.telegram.forms

import cc.altruix.is1.validation.FailableOperationResult
import org.apache.commons.lang3.StringUtils

/**
 * Created by pisarenko on 06.04.2017.
 */
open class TextInput(id:String, targetProperty:String, msg:String) :
        InputField(id, targetProperty, msg) {
    override fun parse(text: String): FailableOperationResult<Any> {
        if (StringUtils.isBlank(text.trim())) {
            return FailableOperationResult(false, "Blank text", null)
        }
        return FailableOperationResult(true, "", text.trim())
    }
}