package cc.altruix.is1.telegram.forms

import cc.altruix.is1.validation.FailableOperationResult

/**
 * Created by pisarenko on 06.04.2017.
 */
interface ITeleformInputElement : ITeleformElement {
    fun  parse(text: String): FailableOperationResult<Any>
}