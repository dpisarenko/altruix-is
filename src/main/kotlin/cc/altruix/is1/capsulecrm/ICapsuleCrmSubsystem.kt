package cc.altruix.is1.capsulecrm

import cc.altruix.is1.telegram.cmd.bp1add.Bp1CompanyData
import cc.altruix.is1.telegram.cmd.bp2cc.Bp2CompanyData
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult

/**
 * Created by pisarenko on 31.01.2017.
 */
interface ICapsuleCrmSubsystem {
    fun init()
    fun findPartiesByUrlFragment(urlFragment: String) : PartiesSearchResult
    fun createCompany(compData: Bp1CompanyData): ValidationResult
    fun close()
    fun fetchBp2CompanyData(companyId:String):FailableOperationResult<Bp2CompanyData>
    fun attachContactResult(companyId: String, persona: String, contactTextNote: String): ValidationResult
}