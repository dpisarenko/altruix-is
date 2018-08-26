package cc.altruix.is1.jena

import cc.altruix.is1.telegram.User
import cc.altruix.is1.telegram.cmd.Bp2BatchStatus
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmd
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.utils.isNumeric
import org.apache.commons.lang3.StringUtils
import org.apache.jena.query.*
import org.apache.jena.rdf.model.*
import org.apache.jena.tdb.TDBFactory
import org.apache.jena.vocabulary.VCARD
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


/**
 * Created by pisarenko on 02.02.2017.
 */
open class JenaSubsystem (val logger: Logger = LoggerFactory.getLogger("cc.altruix.is1.jena")) :IJenaSubsystem {
    companion object {
        val Dir = "data/Altruix-IS1"
        val FirstContactTime = "http://altruix.cc/data/person/firstContactDateTime"
        val TelegramUserId = "http://altruix.cc/data/person/telegramUserId"
        val PermissionBp1AddCmd = "http://altruix.cc/data/permissions/Bp1AddCmd"
        val PersonPrefix = "http://altruix.cc/data/p-"
        val Bp2BatchPrefix = "http://altruix.cc/data/bp-2/b-"
        val Bp2BatchNumber = "http://altruix.cc/data/bp-2/batch/batchNumber"
        val Bp2BatchCompanyIds = "http://altruix.cc/data/bp-2/batch/companyIds"
        val Bp2Persona = "http://altruix.cc/data/bp-2/batch/persona"

    }

    var dataSet:Dataset? = null

    override fun init() {
        dataSet = createDataset()
    }

    open fun createDataset() = TDBFactory.createDataset(Dir)

    override fun close() {
        dataSet?.close()
    }

    override fun createNewUser(
            nick: String,
            email: String,
            telegramUserId: Int,
            telegramChatId: Long
    ): ValidationResult {
        val ds = dataSet
        if (ds == null) {
            return ValidationResult(false, "Database failure")
        }
        val usrExistsRes = userExists(email, ds)
        if (!usrExistsRes.success || (usrExistsRes.result == null)) {
            return ValidationResult(false, "Database failure: '${usrExistsRes.error}'")
        }
        if (usrExistsRes.result) {
            return ValidationResult(false, "User with e-mail '${email}' already exists")
        }
        return createPerson(ds, email, nick, telegramUserId)
    }

    open fun userExists(email: String, ds: Dataset): FailableOperationResult<Boolean>  {
        try {
            ds.begin(ReadWrite.READ)
            val res = userExistsLogic(email, ds)
            return FailableOperationResult<Boolean>(true, "", res)
        }
        catch (throwable:Throwable) {
            logger.error("userExists", throwable)
            return FailableOperationResult<Boolean>(true, "Database error ('${throwable.message}')", null)
        }
        finally {
            ds.end()
        }
    }

    open fun userExistsLogic(email: String, ds: Dataset): Boolean {
        val query = createQuery("""SELECT ?x
                            WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL>  "${email}" }""")
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        return rs.hasNext()
    }

    open fun createQueryExecution(ds: Dataset, query: Query?) = QueryExecutionFactory.create(query, ds.defaultModel)

    open fun createQuery(query: String) = QueryFactory.create(query)


    open fun composeErrorMessage(msg: String, throwable: Throwable, place: String): ValidationResult {
        logger.error(place, throwable)
        return ValidationResult(false, msg)
    }

    open fun createPerson(ds: Dataset, email: String, nick: String, telegramUserId: Int): ValidationResult {
        try {
            ds.begin(ReadWrite.WRITE)
            createPersonLogic(ds, email, nick, telegramUserId)
            return ValidationResult(true, "")
        } catch (throwable:Throwable) {
            return composeErrorMessage("Database error ('${throwable.message}')", throwable, "createPerson")
        }
        finally {
            ds.end()
        }
    }

    open fun createPersonLogic(ds: Dataset, email: String, nick: String, telegramUserId: Int) {
        val model = ds.defaultModel
        val uuid = randomUuid()

        val uri = "$PersonPrefix$uuid"
        val person = model.createResource(uri)
        person.addProperty(VCARD.EMAIL, email)
        setNickname(model, nick, person)
        addDateProperty(model, person, FirstContactTime, now())
        addIntProperty(model, person, TelegramUserId, telegramUserId)
        ds.commit()
    }

    open fun now() = Date()

    open fun setNickname(model: Model, nick: String, person: Resource) {
        val name = model.createResource()
        person.addProperty(VCARD.N,
                name.addProperty(VCARD.NICKNAME, nick))
    }

    open fun randomUuid() = UUID.randomUUID()

    open fun addIntProperty(model: Model, person: Resource, propName: String, value: Int) {
        val prop = model.createProperty(propName)
        person.addProperty(prop, model.createTypedLiteral(value))
    }
    open fun addStringProperty(
            model: Model,
            person: Resource,
            propName: String,
            value: String
    ) {
        val prop = model.createProperty(propName)
        person.addProperty(prop, model.createTypedLiteral(value))
    }

    open fun addBooleanProperty(model: Model, resource: Resource, propName: String, value: Boolean) {
        val prop = model.createProperty(propName)
        resource.addProperty(prop, model.createTypedLiteral(value))
    }

    open fun addDateProperty(model: Model, person: Resource, propName: String, value: Date) {
        val cal = toCalendar(value)
        val prop = model.createProperty(propName)
        person.addProperty(prop, model.createTypedLiteral(cal))
    }

    open fun toCalendar(value: Date) = Calendar.Builder().setInstant(value).build()

    override fun activateUser(email: String) = activateUserLogic(email, true)

    override fun deActivateUser(email: String) = activateUserLogic(email, false)

    open fun activateUserLogic(email: String, grant: Boolean): ValidationResult {
        val ds = dataSet
        if (ds == null) {
            return ValidationResult(false, "Database failure")
        }
        val usrExistsRes = userExists(email, ds)
        if (!usrExistsRes.success || (usrExistsRes.result == null)) {
            return ValidationResult(false, "Database failure: '${usrExistsRes.error}'")
        }
        if (!usrExistsRes.result) {
            return ValidationResult(false, "User with e-mail '${email}' does not exit")
        }
        return givePermissionToUser(ds, email, Bp1AddCmd.Name, grant)

    }

    open fun givePermissionToUser(ds: Dataset, email: String, name: String, grant: Boolean): ValidationResult {
        try {
            ds.begin(ReadWrite.WRITE)
            givePermissionToUserLogic(ds, email, name, grant)
            return ValidationResult(true, "")
        } catch (throwable:Throwable) {
            return composeErrorMessage("Database error ('${throwable.message}')", throwable, "givePermissionToUser")
        }
        finally {
            ds.end()
        }
    }

    open fun givePermissionToUserLogic(ds: Dataset, email: String, permission: String, grant: Boolean) {
        val varn = "x"
        val query = createQuery("""SELECT ?${varn}
                            WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL>  "${email}" }""")
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        if (rs.hasNext()) {
            val solution = rs.nextSolution()
            val rec = solution[varn]
            val prop = findStatement(permission, rec as Resource)
            if (prop == null) {
                addBooleanProperty(rec.model, rec, permission, grant)
            } else {
                    prop.changeLiteralObject(grant)
            }
            ds.commit()
        }
    }
    open fun findStatement(property:String, resource:Resource):Statement? {
        val stmts = resource.model.listStatements()
        var res:Statement? = null
        while (stmts.hasNext() && (res == null)) {
            val stmt = stmts.nextStatement()
            if (stmt.predicate.uri == property) {
                res = stmt
            }
        }
        return res
    }
    override fun fetchAllUsers(): FailableOperationResult<List<User>> {
        val ds = this.dataSet
        if (ds == null) {
            return FailableOperationResult(false, "Database error", null)
        }
        ds.begin(ReadWrite.READ)
        try {
            return fetchAllUsersLogic(ds)
        }
        catch (throwable:Throwable) {
            logger.error("fetchAllUsers", throwable)
            return FailableOperationResult(false, "Database error ('${throwable.message}')", null)
        }
        finally {
            ds.end()
        }
    }

    open fun fetchAllUsersLogic(ds: Dataset): FailableOperationResult<List<User>> {
        val varEmail = "email"
        val varNick = "nick"
        val queryTxt = """SELECT ?${varNick} ?${varEmail}
                                WHERE {
                                ?x <${VCARD.EMAIL}> ?${varEmail} .
                                ?x <${VCARD.N}> ?n .
                                ?n <${VCARD.NICKNAME}> ?${varNick}
                                }"""
        val query = createQuery(queryTxt)
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        val res = mutableListOf<User>()
        while (rs.hasNext()) {
            val sol = rs.nextSolution()
            val email = sol[varEmail].toString()
            val nick = sol[varNick].toString()
            res.add(User(nick, email))
        }
        return FailableOperationResult(true, "", res)
    }
    override fun hasPermission(userId: Int, command: String): FailableOperationResult<Boolean> {
        val ds = this.dataSet
        if (ds == null) {
            return FailableOperationResult(false, "Database error", null)
        }
        ds.begin(ReadWrite.READ)
        try {
            return hasPermissionLogic(ds, userId, command)
        }
        catch (throwable:Throwable) {
            logger.error("hasPermission", throwable)
            return FailableOperationResult(false, "Database error ('${throwable.message}')", null)
        }
        finally {
            ds.end()
        }
    }

    open fun hasPermissionLogic(ds: Dataset, userId: Int, command: String): FailableOperationResult<Boolean> {
        val query = createQuery("""SELECT ?x
                            WHERE { ?x  <$TelegramUserId>  $userId }""")
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        if (rs.hasNext()) {
            val solution = rs.nextSolution()
            val rec = solution["x"]
            val prop = findStatement(command, rec as Resource)
            if (prop != null) {
                val hasPermission = (prop.`object` as Literal).boolean
                return FailableOperationResult(true, "", hasPermission)
            }
        }
        return FailableOperationResult(true, "", false)
    }
    override fun createBatch(companyIds: List<String>, persona: String): FailableOperationResult<Int> {
        val ds = this.dataSet
        if (ds == null) {
            return FailableOperationResult(false, "Database error", null)
        }
        ds.begin(ReadWrite.WRITE)
        try {
            return createBatchLogic(ds, companyIds, persona)
        }
        catch (throwable:Throwable) {
            logger.error("createBatch", throwable)
            return FailableOperationResult(false, "Database error ('${throwable.message}')", null)
        }
        finally {
            ds.end()
        }
    }

    open fun createBatchLogic(ds: Dataset, companyIds: List<String>, persona: String): FailableOperationResult<Int> {
        val maxBatchNumber = calculateHighestBatchNumber(ds)
        val model = ds.defaultModel
        val uuid = randomUuid()
        val uri = "$Bp2BatchPrefix$uuid"
        val batch = model.createResource(uri)
        val newBatchNumber = maxBatchNumber + 1
        addIntProperty(model, batch, Bp2BatchNumber, newBatchNumber)
        addStringProperty(model, batch, Bp2Persona, persona)
        addCompanyIdsList(batch, companyIds, model)
        ds.commit()
        return FailableOperationResult<Int>(true, "", newBatchNumber)
    }

    open fun addCompanyIdsList(
            batch: Resource,
            companyIds: List<String>,
            model: Model) {
        val prop = model.createProperty(Bp2BatchCompanyIds)
        val elems = companyIds
                .filter { it.isNumeric() }
                .map { it.toInt() }
                .map { id -> model.createTypedLiteral(id) }
                .toTypedArray()
                /*
                .forEach { literal ->
                    if (list.isEmpty) {
                        list = list.with(literal)
                    } else {
                        list.add(literal)
                    }
                }*/
        val list = model.createList(elems)
        batch.addProperty(prop, list)
    }

    open fun calculateHighestBatchNumber(ds:Dataset):Int {
        val varBatchNr = "batchNr"
        val queryTxt =
"""SELECT ?$varBatchNr
WHERE {
?x <$Bp2BatchNumber> ?$varBatchNr
}"""
        val query = createQuery(queryTxt)
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        var max = 0
        while (rs.hasNext()) {
            val sol = rs.nextSolution()
            val curBatchNr = sol[varBatchNr].asLiteral().int
            max = Math.max(max, curBatchNr)
        }
        return max
    }
    override fun fetchNextCompanyIdToContact(batchId: Int): FailableOperationResult<String> {
        val ds = this.dataSet
        if (ds == null) {
            return FailableOperationResult(false, "Database error", null)
        }
        ds.begin(ReadWrite.WRITE)
        try {
            return fetchNextCompanyToContactLogic(ds, batchId)
        }
        catch (throwable:Throwable) {
            logger.error("fetchNextCompanyIdToContact", throwable)
            return FailableOperationResult(false, "Database error ('${throwable.message}')", null)
        }
        finally {
            ds.end()
        }
    }

    open fun fetchNextCompanyToContactLogic(
            ds: Dataset,
            batchId: Int
    ): FailableOperationResult<String> {
        val sparql = """
            SELECT ?companyIds WHERE {
            ?x <$Bp2BatchNumber> $batchId .
            ?x <$Bp2BatchCompanyIds> ?companyIds
        }"""
        val query = createQuery(sparql)
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        if (!rs.hasNext()) {
            return FailableOperationResult(false, "Batch not found.", null)
        }
        val sol = rs.nextSolution()
        val obj = sol["companyIds"].`as`(RDFList::class.java)
        if (!(obj is RDFList)) {
            return FailableOperationResult(false, "Internal logic error.", null)
        }
        val companyIds = obj
        if (companyIds.isEmpty) {
            return FailableOperationResult(false, "No companies left in the batch.", null)
        }
        val resultNode = companyIds.head
        val result = resultNode.asLiteral().long.toString()
        return FailableOperationResult(true, "", result)
    }
    override fun removeCompanyFromBatch(
            batchId: Int,
            companyId: String
    ):ValidationResult {
        val ds = this.dataSet
        if (ds == null) {
            return ValidationResult(false, "Database error")
        }
        ds.begin(ReadWrite.WRITE)
        try {
            return removeCompanyFromBatchLogic(ds, batchId, companyId)
        }
        catch (throwable:Throwable) {
            logger.error("removeCompanyFromBatch", throwable)
            return ValidationResult(
                    false,
                    "Database error ('${throwable.message}')"
            )
        }
        finally {
            ds.end()
        }
    }
    open fun removeCompanyFromBatchLogic(ds:Dataset, batchId: Int, companyId: String):ValidationResult {
        val sparql = """SELECT ?x ?companyIds
                            WHERE {
                            ?x  <$Bp2BatchNumber>  $batchId .
                            ?x <$Bp2BatchCompanyIds> ?companyIds
        }"""
        val query = createQuery(sparql)
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        if (!rs.hasNext()) {
            return ValidationResult(false, "Batch not found.")
        }
        val sol = rs.nextSolution()
        val rec = sol["x"]
        val prop = findStatement(Bp2BatchCompanyIds, rec as Resource)
        if (prop == null) {
            return ValidationResult(false, "Company list not found.")
        }
        // val obj = prop.`object`.`as`(RDFList::class.java)
        val obj = sol["companyIds"].`as`(RDFList::class.java)
        if (!(obj is RDFList)) {
            return ValidationResult(false, "Internal logic error.")
        }
        val companyIds = obj
        val idx = companyIndex(companyIds, companyId)
        if (idx < 0) {
            return ValidationResult(false, "Company not found in the batch.")
        }
        //prop.changeObject()
        companyIds.remove(companyIds[idx])
        ds.commit()
        return ValidationResult(true, "")
    }

    open fun companyIndex(companyIds: RDFList, searchItem: String): Int {
        var i = 0
        var idx = -1
        while ((i < companyIds.size()) && (idx < 0)) {
            val node = companyIds[i]
            if (!node.isLiteral) {
                continue
            }
            val txt = node.asLiteral().string
            if (StringUtils.equals(txt, searchItem)) {
                idx = i
            }
            i++
        }
        return idx
    }
    override fun fetchPersona(batchId: Int): FailableOperationResult<String> {
        val ds = this.dataSet
        if (ds == null) {
            return FailableOperationResult(false, "Database error", null)
        }
        ds.begin(ReadWrite.READ)
        try {
            return fetchPersonaLogic(ds, batchId)
        } catch (throwable:Throwable) {
            logger.error("fetchPersona(batchId=$batchId)", throwable)
            return FailableOperationResult(
                    false,
                    "Database error ('${throwable.message}')",
                    null
            )
        }
        finally {
            ds.end()
        }
    }

    open fun fetchPersonaLogic(
            ds: Dataset,
            batchId: Int
    ): FailableOperationResult<String> {
        val sparql =
                """SELECT ?persona
WHERE {
?x <$Bp2BatchNumber> ?$batchId .
?x <$Bp2Persona> ?persona
}"""
        val query = createQuery(sparql)
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        if (!rs.hasNext()) {
            return FailableOperationResult(false, "Data not found.", null)
        }
        val sol = rs.nextSolution()
        val persona = sol["persona"].toString()
        return FailableOperationResult(true, "", persona)
    }
    override fun batchStatus(batchId: Int): FailableOperationResult<Bp2BatchStatus> {
        val ds = this.dataSet
        if (ds == null) {
            return FailableOperationResult(false, "Database error", null)
        }
        ds.begin(ReadWrite.READ)
        try {
            return batchStatusLogic(ds, batchId)
        } catch (throwable:Throwable) {
            logger.error("fetchPersona(batchId=$batchId)", throwable)
            return FailableOperationResult(
                    false,
                    "Database error ('${throwable.message}')",
                    null
            )
        }
        finally {
            ds.end()
        }
    }

    open fun batchStatusLogic(ds: Dataset, batchId: Int): FailableOperationResult<Bp2BatchStatus> {
        val sparql = """
            SELECT ?companyIds WHERE {
            ?x <$Bp2BatchNumber> $batchId .
            ?x <$Bp2BatchCompanyIds> ?companyIds
        }"""
        val query = createQuery(sparql)
        val qexec = createQueryExecution(ds, query)
        val rs = qexec.execSelect()
        if (!rs.hasNext()) {
            return FailableOperationResult<Bp2BatchStatus>(
                    false,
                    "Batch not found.",
                    null
            )
        }
        val sol = rs.nextSolution()
        val obj = sol["companyIds"].`as`(RDFList::class.java)
        if (!(obj is RDFList)) {
            return FailableOperationResult<Bp2BatchStatus>(false, "Internal logic error.", null)
        }
        val companyIds = obj
        return FailableOperationResult<Bp2BatchStatus>(
                true,
                "",
                Bp2BatchStatus(
                        batchId,
                        companyIds.size()
                )
        )
    }
}