package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.IParentAutomaton
import org.telegram.telegrambots.api.objects.User

/**
 * Created by pisarenko on 14.02.2017.
 */
interface IParentBp1AddCmdAutomaton : IParentAutomaton<Bp1AddCmdState> {
    fun saveMainUrl(url:String)
    fun saveContactDataType(type:ContactDataType)
    fun saveEmail(email:String)
    fun saveContactFormUrl(url:String)
    fun saveNote(note:String)
    fun companyData():Bp1CompanyData
    fun saveAgent(agent: User)
}