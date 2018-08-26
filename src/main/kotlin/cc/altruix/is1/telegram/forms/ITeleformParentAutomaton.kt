package cc.altruix.is1.telegram.forms

import cc.altruix.is1.telegram.IParentAutomaton

/**
 * Created by 1 on 09.04.2017.
 */
interface ITeleformParentAutomaton : IParentAutomaton<TeleformElementState> {
    fun saveInMemory(key:String, value:Any)
    fun stuffToSave():Map<String,Any>
}