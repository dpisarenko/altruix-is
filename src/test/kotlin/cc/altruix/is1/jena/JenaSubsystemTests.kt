package cc.altruix.is1.jena

import cc.altruix.is1.telegram.User
import cc.altruix.is1.telegram.cmd.Bp2BatchStatus
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmd
import cc.altruix.is1.telegram.cmd.bp2cc.Bp2CompanyData
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import org.apache.jena.query.*
import org.apache.jena.rdf.model.*
import org.apache.jena.vocabulary.VCARD
import org.fest.assertions.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.slf4j.Logger
import java.util.*

/**
 * Created by pisarenko on 02.02.2017.
 */
class JenaSubsystemTests {
    @Test
    fun createNewUserSunnyDay() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val dataSet = mock<Dataset>()
        doReturn(dataSet).`when`(sut).createDataset()

        val nick = "dp118m"
        val email = "dp@altruix.co"
        val telegramUserId = 302
        val telegramChatId = 1301L

        val valRes = mock<ValidationResult>()
        doReturn(valRes).`when`(sut).createPerson(dataSet, email, nick, telegramUserId)

        val userExistsRes = FailableOperationResult<Boolean>(true, "", false)
        doReturn(userExistsRes).`when`(sut).userExists(email, dataSet)

        // Run method under test
        sut.init()
        val actRes = sut.createNewUser(nick, email, telegramUserId, telegramChatId)
        sut.close()

        // Verify
        assertThat(actRes).isSameAs(valRes)
        verify(sut).userExists(email, dataSet)
        verify(sut).createPerson(dataSet, email, nick, telegramUserId)
    }
    @Test
    fun createNewUserUserAlreadyExists() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val dataSet = mock<Dataset>()
        doReturn(dataSet).`when`(sut).createDataset()

        val nick = "dp118m"
        val email = "dp@altruix.co"
        val telegramUserId = 302
        val telegramChatId = 1301L

        val valRes = mock<ValidationResult>()
        doReturn(valRes).`when`(sut).createPerson(dataSet, email, nick, telegramUserId)

        val userExistsRes = FailableOperationResult<Boolean>(true, "", true)
        doReturn(userExistsRes).`when`(sut).userExists(email, dataSet)

        // Run method under test
        sut.init()
        val actRes = sut.createNewUser(nick, email, telegramUserId, telegramChatId)
        sut.close()

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("User with e-mail '${email}' already exists")
        assertThat(actRes.result).isNull()
        verify(sut).userExists(email, dataSet)
        verify(sut, never()).createPerson(dataSet, email, nick, telegramUserId)
    }

    @Test
    fun userExistsSunnyDay() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val dataSet = mock<Dataset>()
        val email = "dp@altruix.co"
        val res = true
        doReturn(res).`when`(sut).userExistsLogic(email, dataSet)

        // Run method under test
        val actRes = sut.userExists(email, dataSet)

        // Verify
        verify(dataSet).begin(ReadWrite.READ)
        verify(sut).userExistsLogic(email, dataSet)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEqualTo("")
        assertThat(actRes.result).isSameAs(res)
        verify(dataSet).end()
    }
    @Test
    fun userExistsLogic() {
        userExistsLogicTestLogic(true)
        userExistsLogicTestLogic(false)
    }
    @Test
    fun composeErrorMessage() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val msg = "msg"
        val throwable = mock<Throwable>()
        val place = "placeInCode"

        // Run method under test
        val actRes = sut.composeErrorMessage(msg, throwable, place)

        // Verify
        verify(logger).error(place, throwable)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isSameAs(msg)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createPersonSunnyDay() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val dataSet = mock<Dataset>()
        val nick = "dp118m"
        val email = "dp@altruix.co"
        val telegramUserId = 302
        val inOrder = Mockito.inOrder(sut, dataSet)
        doNothing().`when`(sut).createPersonLogic(dataSet, email, nick, telegramUserId)

        // Run method under test
        val actRes = sut.createPerson(dataSet, email, nick, telegramUserId)

        // Verify
        inOrder.verify(dataSet).begin(ReadWrite.WRITE)
        inOrder.verify(sut).createPersonLogic(dataSet, email, nick, telegramUserId)
        inOrder.verify(dataSet).end()
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEqualTo("")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createPersonLogic() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val dataSet = mock<Dataset>()
        val nick = "dp118m"
        val email = "dp@altruix.co"
        val telegramUserId = 302

        val model = mock<Model>()
        `when`(dataSet.defaultModel).thenReturn(model)

        val uuid = "938f6ec8-674d-4c56-8e0e-6aa289a721d7"
        doReturn(UUID.fromString(uuid)).`when`(sut).randomUuid()
        val uri = "http://altruix.cc/data/p-${uuid}"
        val person = mock<Resource>()
        `when`(model.createResource(uri)).thenReturn(person)
        doNothing().`when`(sut).setNickname(model, nick, person)
        val now = mock<Date>()
        doReturn(now).`when`(sut).now()
        doNothing().`when`(sut).addDateProperty(model, person, JenaSubsystem.FirstContactTime, now)
        doNothing().`when`(sut).addIntProperty(model, person, JenaSubsystem.TelegramUserId, telegramUserId)

        val inOrder = Mockito.inOrder(sut, dataSet, person, now, model)
        // Run method under test
        sut.createPersonLogic(dataSet, email, nick, telegramUserId)

        // Verify
        inOrder.verify(sut).randomUuid()
        inOrder.verify(model).createResource(uri)
        inOrder.verify(person).addProperty(VCARD.EMAIL, email)
        inOrder.verify(sut).setNickname(model, nick, person)
        inOrder.verify(sut).addDateProperty(model, person, JenaSubsystem.FirstContactTime, now)
        inOrder.verify(sut).addIntProperty(model, person, JenaSubsystem.TelegramUserId, telegramUserId)
        inOrder.verify(dataSet).commit()
    }
    @Test
    fun setNickname() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val nick = "dp118m"
        val model = mock<Model>()
        val person = mock<Resource>()
        val name = mock<Resource>()
        `when`(model.createResource()).thenReturn(name)
        val p2 = mock<Resource>()
        `when`(name.addProperty(VCARD.NICKNAME, nick)).thenReturn(p2)

        // Run method under test
        sut.setNickname(model, nick, person)

        // Verify
        verify(model).createResource()
        verify(name).addProperty(VCARD.NICKNAME, nick)
        verify(person).addProperty(VCARD.N, p2)
    }
    @Test
    fun addIntProperty() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val model = mock<Model>()
        val person = mock<Resource>()
        val propName = "propName"
        val value = 10
        val prop = mock<Property>()
        `when`(model.createProperty(propName)).thenReturn(prop)
        val literal = mock<Literal>()
        `when`(model.createTypedLiteral(value)).thenReturn(literal)

        // Run method under test
        sut.addIntProperty(model, person, propName, value)

        // Verify
        verify(model).createProperty(propName)
        verify(model).createTypedLiteral(value)
        verify(person).addProperty(prop, literal)
    }
    @Test
    fun addDateProperty() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val model = mock<Model>()
        val person = mock<Resource>()
        val propName = "propName"
        val value = mock<Date>()
        val prop = mock<Property>()
        `when`(model.createProperty(propName)).thenReturn(prop)
        val literal = mock<Literal>()
        val cal = mock<Calendar>()
        doReturn(cal).`when`(sut).toCalendar(value)
        `when`(model.createTypedLiteral(cal)).thenReturn(literal)

        // Run method under test
        sut.addDateProperty(model, person, propName, value)

        // Verify
        verify(model).createProperty(propName)
        verify(model).createTypedLiteral(cal)
        verify(person).addProperty(prop, literal)
        verify(sut).toCalendar(value)
    }
    @Test
    fun activateUserSunnyDay() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val dataSet = mock<Dataset>()
        doReturn(dataSet).`when`(sut).createDataset()
        val email = "dp@altruix.co"
        val usrExistsRes = FailableOperationResult<Boolean>(true, "", true)
        doReturn(usrExistsRes).`when`(sut).userExists(email, dataSet)
        val pres = mock<ValidationResult>()
        doReturn(pres).`when`(sut).givePermissionToUser(dataSet, email, Bp1AddCmd.Name, true)

        // Run method under test
        sut.init()
        val actRes = sut.activateUser(email)

        // Verify
        verify(sut).userExists(email, dataSet)
        verify(sut).givePermissionToUser(dataSet, email, Bp1AddCmd.Name, true)
        assertThat(actRes).isSameAs(pres)
    }

    @Test
    fun givePermissionToUser() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val dataSet = mock<Dataset>()
        val email = "dp@altruix.co"
        val name = Bp1AddCmd.Name
        doNothing().`when`(sut).givePermissionToUserLogic(dataSet, email, name, true)
        val inOrder = inOrder(sut, dataSet)

        // Run method under test
        sut.givePermissionToUser(dataSet, "dp@altruix.co", Bp1AddCmd.Name, true)

        // Verify
        inOrder.verify(dataSet).begin(ReadWrite.WRITE)
        inOrder.verify(sut).givePermissionToUserLogic(dataSet, email, name, true)
        inOrder.verify(dataSet).end()
    }
    @Test
    fun givePermissionToUserLogicChangesExistingProperty() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        val query = mock<Query>()
        val varn = "x"
        val email = "dp@altruix.co"
        doReturn(query).`when`(sut).createQuery("""SELECT ?${varn}
                            WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL>  "${email}" }""")
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val solution = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(solution)
        val rec = mock<Resource>()
        `when`(solution[varn]).thenReturn(rec)
        val prop = mock<Statement>()
        val permission = JenaSubsystem.PermissionBp1AddCmd
        doReturn(prop).`when`(sut).findStatement(permission, rec)
        val recModel = mock<Model>()
        `when`(rec.model).thenReturn(recModel)
        doNothing().`when`(sut).addBooleanProperty(recModel, rec, permission, true)

        val inOrder = inOrder(sut, ds, query, qexec, rs, solution, rec, prop, recModel)

        // Run method under test
        sut.givePermissionToUser(ds, email, permission, true)

        // Verify
        inOrder.verify(sut).createQuery("""SELECT ?${varn}
                            WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL>  "${email}" }""")
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(solution).get(varn) // `when`(solution[varn]).thenReturn(rec)
        inOrder.verify(sut).findStatement(permission, rec)
        inOrder.verify(prop).changeLiteralObject(true)
        inOrder.verify(sut, never()).addBooleanProperty(recModel, rec, permission, true)
        inOrder.verify(ds).commit()
    }
    @Test
    fun givePermissionToUserLogicAddsNewProperty() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        val query = mock<Query>()
        val varn = "x"
        val email = "dp@altruix.co"
        doReturn(query).`when`(sut).createQuery("""SELECT ?${varn}
                            WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL>  "${email}" }""")
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val solution = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(solution)
        val rec = mock<Resource>()
        `when`(solution[varn]).thenReturn(rec)
        val prop = null
        val permission = JenaSubsystem.PermissionBp1AddCmd
        doReturn(prop).`when`(sut).findStatement(permission, rec)
        val recModel = mock<Model>()
        `when`(rec.model).thenReturn(recModel)
        doNothing().`when`(sut).addBooleanProperty(recModel, rec, permission, true)

        val inOrder = inOrder(sut, ds, query, qexec, rs, solution, rec, recModel)

        // Run method under test
        sut.givePermissionToUser(ds, email, permission, true)

        // Verify
        inOrder.verify(sut).createQuery("""SELECT ?${varn}
                            WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL>  "${email}" }""")
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(solution).get(varn) // `when`(solution[varn]).thenReturn(rec)
        inOrder.verify(sut).findStatement(permission, rec)
        inOrder.verify(sut).addBooleanProperty(recModel, rec, permission, true)
        inOrder.verify(ds).commit()
    }

    private fun userExistsLogicTestLogic(rsHasNextResult: Boolean) {
        // Prepare
        val sut = spy(JenaSubsystem())
        val dataSet = mock<Dataset>()
        val email = "dp@altruix.co"
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery("""SELECT ?x
                            WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL>  "dp@altruix.co" }""")
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(dataSet, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(rsHasNextResult)

        // Run method under test
        val actRes = sut.userExistsLogic(email, dataSet)

        // Verify
        verify(sut).createQuery("""SELECT ?x
                            WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#EMAIL>  "dp@altruix.co" }""")
        verify(sut).createQueryExecution(dataSet, query)
        verify(qexec).execSelect()
        verify(rs).hasNext()
        assertThat(actRes).isSameAs(rsHasNextResult)
    }
    @Test
    fun findStatement() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val resource = mock<Resource>()
        val model = mock<Model>()
        `when`(resource.model).thenReturn(model)

        val property = "property"
        val stmts = mock<StmtIterator>()
        `when`(stmts.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false)

        val stmt1 = mockStatement(property)
        val stmt2 = mockStatement("bla")

        `when`(stmts.nextStatement()).thenReturn(stmt2).thenReturn(stmt1)
        `when`(model.listStatements()).thenReturn(stmts)

        val inOrder = inOrder(sut, resource, model, stmts)

        // Run method under test
        val actRes = sut.findStatement(property, resource)

        // Verify
        inOrder.verify(model).listStatements()
        assertThat(actRes).isSameAs(stmt1)
    }
    @Test
    fun addBooleanProperty() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val resource = mock<Resource>()
        val model = mock<Model>()
        val propName = "propName"
        val value = true
        val prop = mock<Property>()
        `when`(model.createProperty(propName)).thenReturn(prop)
        val literal = mock<Literal>()
        `when`(model.createTypedLiteral(value)).thenReturn(literal)

        // Run method under test
        sut.addBooleanProperty(model, resource, propName, value)

        // Verify
        verify(model).createProperty(propName)
        verify(model).createTypedLiteral(value)
        verify(resource).addProperty(prop, literal)
    }
    @Test
    fun fetchAllUsersSunnyDay() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()

        val fetchRes:FailableOperationResult<List<User>> = mock()
        doReturn(fetchRes).`when`(sut).fetchAllUsersLogic(ds)
        val inOrder = inOrder(sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.fetchAllUsers()

        // Verify
        inOrder.verify(sut).createDataset()
        inOrder.verify(ds).begin(ReadWrite.READ)
        inOrder.verify(sut).fetchAllUsersLogic(ds)
        inOrder.verify(ds).end()
        assertThat(actRes).isSameAs(fetchRes)
    }
    @Test
    fun fetchAllUsersErrorHandling() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val errorMsg = "errorMsg"
        val throwable = RuntimeException(errorMsg)
        doThrow(throwable).`when`(sut).fetchAllUsersLogic(ds)

        val inOrder = inOrder(sut, ds, logger, ds)

        // Run method under test
        sut.init()
        val actRes = sut.fetchAllUsers()

        // Verify
        inOrder.verify(sut).createDataset()
        inOrder.verify(ds).begin(ReadWrite.READ)
        inOrder.verify(sut).fetchAllUsersLogic(ds)
        inOrder.verify(logger).error("fetchAllUsers", throwable)
        inOrder.verify(ds).end()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Database error ('${errorMsg}')")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun fetchAllUsersLogic() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val queryTxt = """SELECT ?nick ?email
                                WHERE {
                                ?x <${VCARD.EMAIL}> ?email .
                                ?x <${VCARD.N}> ?n .
                                ?n <${VCARD.NICKNAME}> ?nick
                                }"""
        val ds = mock<Dataset>()
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(queryTxt)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true).thenReturn(false)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val email = "dp@altruix.co"
        val emailNode = mockRdfNode(email)
        val nick = "dp118m"
        val nickNode = mockRdfNode(nick)

        `when`(sol["email"]).thenReturn(emailNode)
        `when`(sol["nick"]).thenReturn(nickNode)

        val inOrder = inOrder(sut, ds, query, qexec, rs, emailNode, nickNode)

        // Run method under test
        val actRes = sut.fetchAllUsersLogic(ds)

        // Verify
        inOrder.verify(sut).createQuery(queryTxt)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNotNull
        assertThat(actRes.result).contains(User(nick, email))
    }
    @Test
    fun hasPermissionSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val userId = 844
        val command = Bp1AddCmd.Name
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val expRes = FailableOperationResult(true, "", true)
        doReturn(expRes).`when`(sut).hasPermissionLogic(ds, userId, command)
        val inOrder = inOrder(logger, sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.hasPermission(userId, command)

        // Verify
        inOrder.verify(ds).begin(ReadWrite.READ)
        inOrder.verify(sut).hasPermissionLogic(ds, userId, command)
        inOrder.verify(ds).end()
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    fun hasPermissionRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val userId = 844
        val command = Bp1AddCmd.Name
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val errorMsg = "Fake fuck-up"
        val exception = RuntimeException(errorMsg)
        doThrow(exception).`when`(sut).hasPermissionLogic(ds, userId, command)
        val inOrder = inOrder(logger, sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.hasPermission(userId, command)

        // Verify
        inOrder.verify(ds).begin(ReadWrite.READ)
        inOrder.verify(sut).hasPermissionLogic(ds, userId, command)
        inOrder.verify(logger).error("hasPermission", exception)
        inOrder.verify(ds).end()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Database error ('$errorMsg')")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun hasPermissionLogic() {
        hasPermissionLogicTestLogic(false)
        hasPermissionLogicTestLogic(true)
    }
    @Test
    fun createBatchSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val companyIds = mock<List<String>>()
        val expRes = mock<FailableOperationResult<String>>()
        val persona = "persona"
        doReturn(expRes).`when`(sut).createBatchLogic(ds, companyIds, persona)

        val inOrder = inOrder(logger, sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.createBatch(companyIds, persona)

        // Verify
        inOrder.verify(sut).createDataset()
        inOrder.verify(ds).begin(ReadWrite.WRITE)
        inOrder.verify(sut).createBatchLogic(ds, companyIds, persona)
        inOrder.verify(ds).end()
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    fun createBatchRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val companyIds = mock<List<String>>()
        val errorMsg = "Fake fuck-up"
        val exception = RuntimeException(errorMsg)
        doThrow(exception).`when`(sut).createBatchLogic(ds, companyIds, "")

        val inOrder = inOrder(logger, sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.createBatch(companyIds, "")

        // Verify
        inOrder.verify(sut).createDataset()
        inOrder.verify(ds).begin(ReadWrite.WRITE)
        inOrder.verify(sut).createBatchLogic(ds, companyIds, "")
        inOrder.verify(logger).error("createBatch", exception)
        inOrder.verify(ds).end()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Database error ('$errorMsg')")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createBatchLogic() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val companyIds = mock<List<String>>()
        val maxBatchNumber = 10
        doReturn(maxBatchNumber).`when`(sut).calculateHighestBatchNumber(ds)
        val model = mock<Model>()
        `when`(ds.defaultModel).thenReturn(model)
        val uuid = "938f6ec8-674d-4c56-8e0e-6aa289a721d7"
        doReturn(UUID.fromString(uuid)).`when`(sut).randomUuid()
        val batch = mock<Resource>()
        val uri = "${JenaSubsystem.Bp2BatchPrefix}$uuid"
        `when`(model.createResource(uri)).thenReturn(batch)
        val newBatchNumber = 11
        doNothing().`when`(sut).addIntProperty(
                model,
                batch,
                JenaSubsystem.Bp2BatchNumber,
                newBatchNumber
        )
        val persona = "persona"
        doNothing().`when`(sut).addCompanyIdsList(batch, companyIds, model)
        doNothing().`when`(sut).addStringProperty(model, batch, JenaSubsystem.Bp2Persona, persona)

        val inOrder = inOrder(logger, sut, ds, model)

        // Run method under test
        val actRes = sut.createBatchLogic(ds, companyIds, persona )

        // Verify
        inOrder.verify(sut).calculateHighestBatchNumber(ds)
        inOrder.verify(ds).defaultModel
        inOrder.verify(sut).randomUuid()
        inOrder.verify(model).createResource(uri)
        inOrder.verify(sut).addIntProperty(
                model,
                batch,
                JenaSubsystem.Bp2BatchNumber,
                newBatchNumber
        )
        inOrder.verify(sut).addStringProperty(model, batch, JenaSubsystem.Bp2Persona, persona)
        inOrder.verify(sut).addCompanyIdsList(batch, companyIds, model)
        inOrder.verify(ds).commit()
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo(newBatchNumber)
    }
    @Test
    @Ignore
    fun addCompanyIdsList() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val batch = mock<Resource>()
        val companyIds = listOf("a", "01032017")
        val model = mock<Model>()
        val prop = mock<Property>()
        `when`(model.createProperty(JenaSubsystem.Bp2BatchCompanyIds)).thenReturn(prop)
        val list = mock<RDFList>()
        `when`(model.createList()).thenReturn(list)

        val literal = mock<Literal>()

        `when`(model.createTypedLiteral(1032017)).thenReturn(literal)

        val inOrder = inOrder(logger, sut, batch, model, prop, list)

        // Run method under test
        sut.addCompanyIdsList(batch, companyIds, model)

        // Verify
        inOrder.verify(model).createProperty(JenaSubsystem.Bp2BatchCompanyIds)
        inOrder.verify(model).createList()
        inOrder.verify(model).createTypedLiteral(1032017)
        inOrder.verify(list).add(literal)
        inOrder.verify(batch).addProperty(prop, list)
    }
    @Test
    fun calculateHighestBatchNumber() {
        calculateHighestBatchNumberTestLogic(createResultSet1(), 0)
        calculateHighestBatchNumberTestLogic(createResultSet2(), 2)
        calculateHighestBatchNumberTestLogic(createResultSet3(), 2)
    }

    private fun createResultSet1(): ResultSet {
        val rs1 = mock<ResultSet>()
        `when`(rs1.hasNext()).thenReturn(false)
        return rs1
    }

    private fun createResultSet3(): ResultSet {
        val rs3 = mock<ResultSet>()
        `when`(rs3.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false)
        val sol3 = createSolution("batchNr", 1)
        val sol4 = createSolution("batchNr", 2)
        `when`(rs3.nextSolution())
                .thenReturn(sol3)
                .thenReturn(sol4)
        return rs3
    }

    private fun createResultSet2(): ResultSet {
        val rs2 = mock<ResultSet>()
        `when`(rs2.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false)
        val sol1 = createSolution("batchNr", 1)
        val sol2 = createSolution("batchNr", 2)
        `when`(rs2.nextSolution())
                .thenReturn(sol1).thenReturn(sol2)
        return rs2
    }

    private fun createSolution(varName: String, value: Int): QuerySolution {
        val sol = mock<QuerySolution>()
        val node = mock<RDFNode>()
        `when`(sol[varName]).thenReturn(node)
        val literal = mock<Literal>()
        `when`(node.asLiteral()).thenReturn(literal)
        `when`(literal.int).thenReturn(value)
        return sol
    }

    private fun calculateHighestBatchNumberTestLogic(resultSet: ResultSet, expRes: Int) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val queryTxt =
"""SELECT ?batchNr
WHERE {
?x <${JenaSubsystem.Bp2BatchNumber}> ?batchNr
}"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(queryTxt)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        `when`(qexec.execSelect()).thenReturn(resultSet)

        val inOrder = inOrder(logger, sut, ds, query, qexec, resultSet)

        // Run method under test
        val actRes = sut.calculateHighestBatchNumber(ds)

        // Verify
        inOrder.verify(sut).createQuery(queryTxt)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(resultSet).hasNext()
        assertThat(actRes).isEqualTo(expRes)
    }
    @Test
    fun activateUserCallsActivateUserLogic() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val email = "email"
        doReturn(ValidationResult(true, "")).`when`(sut).activateUserLogic(email, true)

        // Run method under test
        sut.activateUser(email)

        // Verify
        verify(sut).activateUserLogic(email, true)
    }
    @Test
    fun deActivateUserCallsActivateUserLogic() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val email = "email"
        doReturn(ValidationResult(true, "")).`when`(sut).activateUserLogic(email, false)

        // Run method under test
        sut.deActivateUser(email)

        // Verify
        verify(sut).activateUserLogic(email, false)
    }
    @Test
    fun givePermissionToUserSunnyDay() {
        givePermissionToUserSunnyDayTestLogic(true)
        givePermissionToUserSunnyDayTestLogic(false)
    }
    @Test
    fun addStringProperty() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val model = mock<Model>()
        val person = mock<Resource>()
        val propName = "propName"
        val value = "persona"
        val prop = mock<Property>()
        `when`(model.createProperty(propName)).thenReturn(prop)
        val literal = mock<Literal>()
        `when`(model.createTypedLiteral(value)).thenReturn(literal)

        // Run method under test
        sut.addStringProperty(model, person, propName, value)

        // Verify
        verify(model).createProperty(propName)
        verify(model).createTypedLiteral(value)
        verify(person).addProperty(prop, literal)
    }
    @Test
    fun fetchNextCompanyToContactSunnyDay() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 1141
        val expRes = FailableOperationResult<Bp2CompanyData>(true, "", Bp2CompanyData("1", emptyList(), emptyList()))
        doReturn(expRes).`when`(sut).fetchNextCompanyToContactLogic(ds, batchId)

        val inOrder = inOrder(sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.fetchNextCompanyIdToContact(batchId)

        // Verify
        inOrder.verify(ds).begin(ReadWrite.WRITE)
        inOrder.verify(sut).fetchNextCompanyToContactLogic(ds, batchId)
        inOrder.verify(ds).end()
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    fun fetchNextCompanyToContactRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 1141
        val errorMsg = "errorMsg"
        val throwable = RuntimeException(errorMsg)
        doThrow(throwable).`when`(sut).fetchNextCompanyToContactLogic(ds, batchId)

        val inOrder = inOrder(sut, ds, logger)

        // Run method under test
        sut.init()
        val actRes = sut.fetchNextCompanyIdToContact(batchId)

        // Verify
        inOrder.verify(ds).begin(ReadWrite.WRITE)
        inOrder.verify(sut).fetchNextCompanyToContactLogic(ds, batchId)
        inOrder.verify(logger).error("fetchNextCompanyIdToContact", throwable)
        inOrder.verify(ds).end()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Database error ('$errorMsg')")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun fetchNextCompanyToContactLogicBatchNotFound() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        val batchId = 1920
        val sparql = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.Bp2BatchNumber}>  $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(false)

        val inOrder = inOrder(sut, ds, query, qexec, rs)

        // Run method under test
        val actRes = sut.fetchNextCompanyToContactLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Batch not found.")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun fetchNextCompanyToContactLogicPropNull() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        val batchId = 1920
        val sparql = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.Bp2BatchNumber}>  $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val rec = mock<Resource>()
        `when`(sol["x"]).thenReturn(rec)
        val prop = null
        doReturn(prop).`when`(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )

        val inOrder = inOrder(sut, ds, query, qexec, rs, sol, rec)

        // Run method under test
        val actRes = sut.fetchNextCompanyToContactLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["x"]
        inOrder.verify(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Company list not found.")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun fetchNextCompanyToContactLogicCompanyListEmpty() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        val batchId = 1920
        val sparql = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.Bp2BatchNumber}>  $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val rec = mock<Resource>()
        `when`(sol["x"]).thenReturn(rec)
        val prop = mock<Statement>()
        doReturn(prop).`when`(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )
        val companyIds = mock<RDFList>()
        val objRes = mock<Resource>()
        `when`(objRes.`as`(RDFList::class.java)).thenReturn(companyIds)
        `when`(prop.`object`).thenReturn(objRes)
        `when`(companyIds.isEmpty).thenReturn(true)

        val inOrder = inOrder(sut, ds, query, qexec, rs, sol, rec, prop,
                companyIds, objRes)

        // Run method under test
        val actRes = sut.fetchNextCompanyToContactLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["x"]
        inOrder.verify(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )
        inOrder.verify(prop).`object`
        inOrder.verify(objRes).`as`(RDFList::class.java)
        inOrder.verify(companyIds).isEmpty
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("No companies left in the batch.")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun fetchNextCompanyToContactLogicSunnyDay() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        val batchId = 1920
        val sparql = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.Bp2BatchNumber}>  $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val rec = mock<Resource>()
        `when`(sol["x"]).thenReturn(rec)
        val prop = mock<Statement>()
        doReturn(prop).`when`(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )
        val companyIds = mock<RDFList>()
        val objRes = mock<Resource>()
        `when`(objRes.`as`(RDFList::class.java)).thenReturn(companyIds)
        `when`(prop.`object`).thenReturn(objRes)
        `when`(companyIds.isEmpty).thenReturn(false)
        val resultNode = mock<RDFNode>()
        `when`(companyIds.head).thenReturn(resultNode)
        val literal = mock<Literal>()
        `when`(resultNode.asLiteral()).thenReturn(literal)
        val companyId = 1958L
        `when`(literal.long).thenReturn(companyId)

        val inOrder = inOrder(sut, ds, query, qexec, rs, sol, rec, prop,
                companyIds, resultNode, literal)

        // Run method under test
        val actRes = sut.fetchNextCompanyToContactLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["x"]
        inOrder.verify(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )
        inOrder.verify(prop).`object`
        inOrder.verify(companyIds).isEmpty
        inOrder.verify(companyIds).head
        inOrder.verify(resultNode).asLiteral()
        inOrder.verify(literal).long
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEqualTo("")
        assertThat(actRes.result).isEqualTo("1958")
    }
    @Test
    fun removeCompanyFromBatchSunnyDay() {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 1802
        val companyId = "2007"
        val expRes = mock<ValidationResult>()
        doReturn(expRes).`when`(sut).removeCompanyFromBatchLogic(ds, batchId, companyId)

        val inOrder = inOrder(sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.removeCompanyFromBatch(batchId, companyId)

        // Verify
        inOrder.verify(sut).createDataset()
        inOrder.verify(ds).begin(ReadWrite.WRITE)
        inOrder.verify(sut).removeCompanyFromBatchLogic(ds, batchId, companyId)
        inOrder.verify(ds).end()
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    fun removeCompanyFromBatchRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 1802
        val companyId = "2007"
        val errMsg = "errMsg"
        val throwable = RuntimeException(errMsg)
        doThrow(throwable).`when`(sut).removeCompanyFromBatchLogic(ds, batchId, companyId)

        val inOrder = inOrder(sut, ds, logger)

        // Run method under test
        sut.init()
        val actRes = sut.removeCompanyFromBatch(batchId, companyId)

        // Verify
        inOrder.verify(sut).createDataset()
        inOrder.verify(ds).begin(ReadWrite.WRITE)
        inOrder.verify(sut).removeCompanyFromBatchLogic(ds, batchId, companyId)
        inOrder.verify(logger).error("removeCompanyFromBatch", throwable)
        inOrder.verify(ds).end()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Database error ('$errMsg')")
    }
    @Test
    @Ignore
    fun removeCompanyFromBatchLogicBatchNotFound() {
        // Prepare
        val ds = mock<Dataset>()
        val sut = spy(JenaSubsystem())
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 2033
        val companyId = "18032017"
        val sparql = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.Bp2BatchNumber}>  $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(false)

        val inOrder = inOrder(ds, sut, query, qexec, rs)

        // Run method under test
        val actRes = sut.removeCompanyFromBatchLogic(ds, batchId, companyId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Batch not found.")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun removeCompanyFromBatchLogicCompanyListNotFound() {
        // Prepare
        val ds = mock<Dataset>()
        val sut = spy(JenaSubsystem())
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 2033
        val companyId = "18032017"
        val sparql = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.Bp2BatchNumber}>  $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val rec = mock<Resource>()
        `when`(sol["x"]).thenReturn(rec)
        val prop = null
        doReturn(prop).`when`(sut).findStatement(JenaSubsystem.Bp2BatchCompanyIds, rec)

        val inOrder = inOrder(ds, sut, query, qexec, rs, sol)

        // Run method under test
        val actRes = sut.removeCompanyFromBatchLogic(ds, batchId, companyId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["x"]
        inOrder.verify(sut).findStatement(JenaSubsystem.Bp2BatchCompanyIds, rec)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Company list not found.")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun removeCompanyFromBatchLogicCompanyListDoesNotContainCompanyId() {
        // Prepare
        val ds = mock<Dataset>()
        val sut = spy(JenaSubsystem())
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 2033
        val companyId = "18032017"
        val sparql = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.Bp2BatchNumber}>  $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val rec = mock<Resource>()
        `when`(sol["x"]).thenReturn(rec)
        val prop = mock<Statement>()
        doReturn(prop).`when`(sut).findStatement(JenaSubsystem.Bp2BatchCompanyIds, rec)
        val companyIds = mock<RDFList>()
        val objRes = mock<Resource>()
        `when`(objRes.`as`(RDFList::class.java)).thenReturn(companyIds)
        `when`(prop.`object`).thenReturn(objRes)
        doReturn(-1).`when`(sut).companyIndex(companyIds, companyId)

        val inOrder = inOrder(ds, sut, query, qexec, rs, sol, prop, objRes, companyIds)

        // Run method under test
        val actRes = sut.removeCompanyFromBatchLogic(ds, batchId, companyId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["x"]
        inOrder.verify(sut).findStatement(JenaSubsystem.Bp2BatchCompanyIds, rec)
        inOrder.verify(prop).`object`
        inOrder.verify(objRes).`as`(RDFList::class.java)
        inOrder.verify(sut).companyIndex(companyIds, companyId)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Company not found in the batch.")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun removeCompanyFromBatchLogicCompanyListSunnyDay() {
        // Prepare
        val ds = mock<Dataset>()
        val sut = spy(JenaSubsystem())
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 2033
        val companyId = "18032017"
        val sparql = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.Bp2BatchNumber}>  $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val rec = mock<Resource>()
        `when`(sol["x"]).thenReturn(rec)
        val prop = mock<Statement>()
        doReturn(prop).`when`(sut).findStatement(JenaSubsystem.Bp2BatchCompanyIds, rec)
        val companyIds = mock<RDFList>()
        val objRes = mock<Resource>()
        `when`(objRes.`as`(RDFList::class.java)).thenReturn(companyIds)
        `when`(prop.`object`).thenReturn(objRes)
        val idx = 10
        doReturn(idx).`when`(sut).companyIndex(companyIds, companyId)
        val elemToRemove = mock<Literal>()
        `when`(companyIds[idx]).thenReturn(elemToRemove)
        val newList = mock<RDFList>()
        `when`(companyIds.remove(elemToRemove)).thenReturn(newList)

        val inOrder = inOrder(ds, sut, query, qexec, prop,
                companyIds, elemToRemove, rs, sol, objRes)

        // Run method under test
        val actRes = sut.removeCompanyFromBatchLogic(ds, batchId, companyId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["x"]
        inOrder.verify(sut).findStatement(JenaSubsystem.Bp2BatchCompanyIds, rec)
        inOrder.verify(prop).`object`
        inOrder.verify(objRes).`as`(RDFList::class.java)
        inOrder.verify(sut).companyIndex(companyIds, companyId)
        inOrder.verify(companyIds)[idx]
        inOrder.verify(companyIds).remove(elemToRemove)
        inOrder.verify(prop).changeObject(newList)
        inOrder.verify(ds).commit()
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEqualTo("")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun companyIndex() {
        // Prepare
        val sut = JenaSubsystem()
        val model = ModelFactory.createDefaultModel()
        var companyIds = model.createList()
        val companyId1 = "companyId1"
        val companyId2 = "companyId2"
        val companyId3 = "companyId3"
        companyIds = companyIds.with(model.createTypedLiteral(companyId1))
        companyIds.add(model.createTypedLiteral(companyId2))

        // Run method under test
        val companyId1ActRes = sut.companyIndex(companyIds, companyId1)
        val companyId2ActRes = sut.companyIndex(companyIds, companyId2)
        val companyId3ActRes = sut.companyIndex(companyIds, companyId3)

        // Verify
        assertThat(companyId1ActRes).isEqualTo(0)
        assertThat(companyId2ActRes).isEqualTo(1)
        assertThat(companyId3ActRes).isEqualTo(-1)
    }
    @Test
    fun fetchPersonaRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 1659
        val errMsg = "errMsg"
        val throwable = RuntimeException(errMsg)
        doThrow(throwable).`when`(sut).fetchPersonaLogic(ds, batchId)
        val inOrder = inOrder(logger, sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.fetchPersona(batchId)

        // Verify
        inOrder.verify(ds).begin(ReadWrite.READ)
        inOrder.verify(sut).fetchPersonaLogic(ds, batchId)
        inOrder.verify(logger).error("fetchPersona(batchId=$batchId)", throwable)
        inOrder.verify(ds).end()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Database error ('${throwable.message}')")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun fetchPersonaSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        doReturn(ds).`when`(sut).createDataset()
        val batchId = 1659
        val expRes = FailableOperationResult<String>(true, "", "DP")
        doReturn(expRes).`when`(sut).fetchPersonaLogic(ds, batchId)
        val inOrder = inOrder(logger, sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.fetchPersona(batchId)

        // Verify
        inOrder.verify(ds).begin(ReadWrite.READ)
        inOrder.verify(sut).fetchPersonaLogic(ds, batchId)
        inOrder.verify(ds).end()
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    fun fetchPersonaLogicNoDataFound() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val batchId = 1728
        val sparql =
                """SELECT ?persona
WHERE {
?x <${JenaSubsystem.Bp2BatchNumber}> ?$batchId .
?x <${JenaSubsystem.Bp2Persona}> ?persona
}"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(false)
        val inOrder = inOrder(logger, sut, ds, query, qexec, rs)

        // Run method under test
        val actRes = sut.fetchPersonaLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Data not found.")
    }
    @Test
    fun fetchPersonaLogicSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val batchId = 1728
        val sparql =
                """SELECT ?persona
WHERE {
?x <${JenaSubsystem.Bp2BatchNumber}> ?$batchId .
?x <${JenaSubsystem.Bp2Persona}> ?persona
}"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val personaRes = mock<Resource>()
        `when`(sol["persona"]).thenReturn(personaRes)
        val personaVal = "persona value"
        `when`(personaRes.toString()).thenReturn(personaVal)

        val inOrder = inOrder(logger, sut, ds, query, qexec, rs, sol)

        // Run method under test
        val actRes = sut.fetchPersonaLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["persona"]
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo(personaVal)
    }
    @Test
    fun batchStatusRainyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val batchId = 1843
        doReturn(ds).`when`(sut).createDataset()
        val error = "error"
        val throwable = RuntimeException(error)
        doThrow(throwable).`when`(sut).batchStatusLogic(ds, batchId)

        val inOrder = inOrder(logger, sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.batchStatus(batchId)

        // Verify
        inOrder.verify(ds).begin(ReadWrite.READ)
        inOrder.verify(sut).batchStatusLogic(ds, batchId)
        inOrder.verify(logger).error("fetchPersona(batchId=$batchId)", throwable)
        inOrder.verify(ds).end()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Database error ('$error')")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun batchStatusSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val batchId = 1843
        doReturn(ds).`when`(sut).createDataset()
        val expRes = FailableOperationResult<Bp2BatchStatus>(
                true,
                "",
                Bp2BatchStatus(batchId, 10))
        doReturn(expRes).`when`(sut).batchStatusLogic(ds, batchId)

        val inOrder = inOrder(logger, sut, ds)

        // Run method under test
        sut.init()
        val actRes = sut.batchStatus(batchId)

        // Verify
        inOrder.verify(ds).begin(ReadWrite.READ)
        inOrder.verify(sut).batchStatusLogic(ds, batchId)
        inOrder.verify(ds).end()
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    @Ignore
    fun batchStatusLogicBatchNotFound() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val batchId = 1843
        val sparql = """SELECT ?x
                            WHERE { ?x <${JenaSubsystem.Bp2BatchNumber}> $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(false)

        val inOrder = inOrder(logger, sut, ds, query, qexec, rs)

        // Run method under test
        val actRes = sut.batchStatusLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Batch not found.")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun batchStatusLogicCompanyListNotFound() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val batchId = 1843
        val sparql = """SELECT ?x
                            WHERE { ?x <${JenaSubsystem.Bp2BatchNumber}> $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val rec = mock<Resource>()
        `when`(sol["x"]).thenReturn(rec)
        val prop = null
        doReturn(prop).`when`(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )

        val inOrder = inOrder(logger, sut, ds, query, qexec, rs,
                sol, rec)

        // Run method under test
        val actRes = sut.batchStatusLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["x"]
        inOrder.verify(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Company list not found.")
        assertThat(actRes.result).isNull()
    }
    @Test
    @Ignore
    fun batchStatusLogicSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(JenaSubsystem(logger))
        val ds = mock<Dataset>()
        val batchId = 1843
        val sparql = """SELECT ?x
                            WHERE { ?x <${JenaSubsystem.Bp2BatchNumber}> $batchId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(sparql)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val sol = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(sol)
        val rec = mock<Resource>()
        `when`(sol["x"]).thenReturn(rec)
        val prop = mock<Statement>()
        val objRes = mock<Resource>()
        val obj = mock<RDFList>()
        `when`(prop.`object`).thenReturn(objRes)
        `when`(objRes.`as`(RDFList::class.java)).thenReturn(obj)
        doReturn(prop).`when`(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )
        val companyCount = 1134
        `when`(obj.size()).thenReturn(companyCount)

        val inOrder = inOrder(logger, sut, ds, query, qexec, rs,
                sol, rec, prop, objRes)

        // Run method under test
        val actRes:FailableOperationResult<Bp2BatchStatus> =
                sut.batchStatusLogic(ds, batchId)

        // Verify
        inOrder.verify(sut).createQuery(sparql)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).hasNext()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(sol)["x"]
        inOrder.verify(sut).findStatement(
                JenaSubsystem.Bp2BatchCompanyIds,
                rec
        )
        inOrder.verify(prop).`object`
        inOrder.verify(objRes).`as`(RDFList::class.java)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNotNull
        assertThat(actRes.result?.companiesCount).isEqualTo(companyCount)
        assertThat(actRes.result?.id).isEqualTo(batchId)
    }
    @Test
    @Ignore
    fun hasPermissionManualTest() {
        val sut = JenaSubsystem()
        val userId = 2002
        sut.init()
        // sut.createNewUser("dp118m", "d.a.pisarenko@bk.ru", userId, 807)
        // sut.activateUser("d.a.pisarenko@bk.ru")
        val actRes = sut.hasPermission(userId, Bp1AddCmd.Name)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.result).isTrue()
    }

    private fun givePermissionToUserSunnyDayTestLogic(grant: Boolean) {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        val email = "email"
        val name = Bp1AddCmd.Name
        doNothing().`when`(sut).givePermissionToUserLogic(ds, email, name, grant)

        // Run method under test
        val actRes = sut.givePermissionToUser(ds, email, name, grant)

        // Verify
        verify(sut).givePermissionToUserLogic(ds, email, name, grant)
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
    }

    private fun hasPermissionLogicTestLogic(hasPermission: Boolean) {
        // Prepare
        val sut = spy(JenaSubsystem())
        val ds = mock<Dataset>()
        val userId = 853
        val command = Bp1AddCmd.Name
        val queryTxt = """SELECT ?x
                            WHERE { ?x  <${JenaSubsystem.TelegramUserId}>  $userId }"""
        val query = mock<Query>()
        doReturn(query).`when`(sut).createQuery(queryTxt)
        val qexec = mock<QueryExecution>()
        doReturn(qexec).`when`(sut).createQueryExecution(ds, query)
        val rs = mock<ResultSet>()
        `when`(qexec.execSelect()).thenReturn(rs)
        `when`(rs.hasNext()).thenReturn(true)
        val solution = mock<QuerySolution>()
        `when`(rs.nextSolution()).thenReturn(solution)
        val rec = mock<Resource>()
        `when`(solution["x"]).thenReturn(rec)
        val prop = mock<Statement>()
        doReturn(prop).`when`(sut).findStatement(command, rec)
        val literal = mock<Literal>()
        `when`(literal.boolean).thenReturn(hasPermission)
        `when`(prop.`object`).thenReturn(literal)

        val inOrder = inOrder(sut, ds, query, qexec, rs, prop, literal, solution)

        // Run method under test
        val actRes = sut.hasPermissionLogic(ds, userId, command)

        // Verify
        inOrder.verify(sut).createQuery(queryTxt)
        inOrder.verify(sut).createQueryExecution(ds, query)
        inOrder.verify(qexec).execSelect()
        inOrder.verify(rs).nextSolution()
        inOrder.verify(solution).get("x")
        inOrder.verify(sut).findStatement(command, rec)
        inOrder.verify(prop).`object`
        inOrder.verify(literal).boolean
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isEqualTo(hasPermission)
    }

    private fun mockRdfNode(toStringResult: String): RDFNode {
        val node = mock<RDFNode>()
        `when`(node.toString()).thenReturn(toStringResult)
        return node
    }

    private fun mockStatement(property: String): Statement {
        val stmt = mock<Statement>()
        val predicate = mock<Property>()
        `when`(stmt.predicate).thenReturn(predicate)
        `when`(predicate.uri).thenReturn(property)
        return stmt
    }
}