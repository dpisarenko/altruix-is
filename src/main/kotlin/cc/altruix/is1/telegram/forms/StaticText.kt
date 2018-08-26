package cc.altruix.is1.telegram.forms

/**
 * Created by pisarenko on 06.04.2017.
 */
open class StaticText(val id:String, val text:String) : ITeleformElement {
    override fun id(): String  = id
}