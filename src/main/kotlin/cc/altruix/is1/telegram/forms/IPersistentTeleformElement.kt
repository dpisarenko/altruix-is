package cc.altruix.is1.telegram.forms

/**
 * Created by pisarenko on 06.04.2017.
 */
interface IPersistentTeleformElement<T> : ITeleformElement {
    fun targetPropertyName():String
    fun value():T
}