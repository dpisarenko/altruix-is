package cc.altruix.is1.jena

import cc.altruix.is1.telegram.User
import cc.altruix.is1.telegram.cmd.Bp2BatchStatus
import cc.altruix.is1.telegram.cmd.bp2cc.Bp2CompanyData
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult

/**
 * Created by pisarenko on 02.02.2017.
 */
interface IJenaSubsystem {
    fun init()
    fun createNewUser(nick: String, email: String, telegramUserId: Int, telegramChatId: Long): ValidationResult
    fun close()
    fun activateUser(email:String):ValidationResult
    fun deActivateUser(email:String):ValidationResult
    fun fetchAllUsers():FailableOperationResult<List<User>>
    fun hasPermission(userId: Int, command: String): FailableOperationResult<Boolean>
    fun createBatch(companyIds: List<String>, persona: String): FailableOperationResult<Int>
    fun fetchNextCompanyIdToContact(batchId:Int):FailableOperationResult<String>
    fun removeCompanyFromBatch(batchId:Int, companyId:String):ValidationResult
    fun fetchPersona(batchId: Int): FailableOperationResult<String>
    fun batchStatus(batchId: Int): FailableOperationResult<Bp2BatchStatus>
}