package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.telegram.IParentAutomaton

/**
 * Created by 1 on 04.03.2017.
 */
interface IParentBp2CcCmdAutomaton : IParentAutomaton<Bp2CcCmdState> {
    fun batchId():Int
    fun setCompanyData(companyToContact: Bp2CompanyData)
    fun getCompanyData(): Bp2CompanyData?
    fun setContactTextAndNote(txt: String)
    fun getContactTextAndNote():String?
}