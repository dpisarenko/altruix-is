package cc.altruix.is1.telegram.forms

/**
 * Created by pisarenko on 06.04.2017.
 */
open abstract class InputField(
        val id:String,
        val targetProperty:String,
        val msg:String
) : ITeleformInputElement {
    override fun id(): String = id
}