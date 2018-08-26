package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.validation.FailableOperationResult
import org.junit.Test
import cc.altruix.mock
import org.apache.commons.io.IOUtils
import org.fest.assertions.Assertions.assertThat
import org.mockito.Mockito.*

/**
 * Created by 1 on 19.03.2017.
 */
class GETTING_NEXT_COMPANY_DATA_HandlerTests {
    @Test
    fun fireNoCompanyFound() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                GETTING_NEXT_COMPANY_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val batchId = 1142
        `when`(parent.batchId()).thenReturn(batchId)
        val errorMsg = "errorMsg"
        val companyIdRes = FailableOperationResult<String>(false, errorMsg, null)
        `when`(jena.fetchNextCompanyIdToContact(batchId)).thenReturn(companyIdRes)
        doNothing().`when`(sut).printMessage("Couldn't find next company to process ('$errorMsg')")
        val inOrder = inOrder(parent, jena, capsule, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).batchId()
        inOrder.verify(jena).fetchNextCompanyIdToContact(batchId)
        inOrder.verify(sut).printMessage("Couldn't find next company to process ('$errorMsg')")
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.CANCELING)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.CANCELING }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
    @Test
    fun fireCrmInteractionFault() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                GETTING_NEXT_COMPANY_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val batchId = 1142
        `when`(parent.batchId()).thenReturn(batchId)
        val companyId = "1903"
        val companyIdRes = FailableOperationResult<String>(true, "", companyId)
        `when`(jena.fetchNextCompanyIdToContact(batchId)).thenReturn(companyIdRes)
        val errorMsg = "errorMsg"
        val compDataRes = FailableOperationResult<Bp2CompanyData>(false, errorMsg, null)
        `when`(capsule.fetchBp2CompanyData(companyId)).thenReturn(compDataRes)
        val inOrder = inOrder(parent, jena, capsule, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).batchId()
        inOrder.verify(jena).fetchNextCompanyIdToContact(batchId)
        inOrder.verify(capsule).fetchBp2CompanyData(companyId)
        inOrder.verify(sut).printMessage("CRM interaction fault ('$errorMsg')")
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.CANCELING)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.CANCELING }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
    @Test
    fun fireSunnyDay() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                GETTING_NEXT_COMPANY_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val batchId = 1142
        `when`(parent.batchId()).thenReturn(batchId)
        val companyId = "1903"
        val companyIdRes = FailableOperationResult<String>(true, "", companyId)
        `when`(jena.fetchNextCompanyIdToContact(batchId)).thenReturn(companyIdRes)
        val compData = Bp2CompanyData(companyId, emptyList(), emptyList())
        val compDataRes = FailableOperationResult<Bp2CompanyData>(true, "", compData)
        `when`(capsule.fetchBp2CompanyData(companyId)).thenReturn(compDataRes)
        doNothing().`when`(sut).printCompanyData(compData)
        val inOrder = inOrder(parent, jena, capsule, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).batchId()
        inOrder.verify(jena).fetchNextCompanyIdToContact(batchId)
        inOrder.verify(capsule).fetchBp2CompanyData(companyId)
        inOrder.verify(parent).setCompanyData(compData)
        inOrder.verify(sut).printCompanyData(compData)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.WAITING_FOR_CONTACT_ATTEMPT_RESULT)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.WAITING_FOR_CONTACT_ATTEMPT_RESULT }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
    @Test
    fun printCompanyData() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                GETTING_NEXT_COMPANY_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val companyId = "1223"
        val companyData = Bp2CompanyData(companyId, emptyList(), emptyList())
        val txt = "txt"
        doReturn(txt).`when`(sut).composeCompanyDataMessage(companyData)
        doNothing().`when`(sut).printMessage(txt)

        // Run method under test
        sut.printCompanyData(companyData)

        // Verify
        verify(sut).composeCompanyDataMessage(companyData)
        verify(sut).printMessage(txt)
    }
    @Test
    fun composeCompanyDataMessage() {
        composeCompanyDataMessageLogic(
                Bp2CompanyData(
                        "1903",
                        listOf("http://altruix.cc"),
                        listOf()
                ),
                "GETTING_NEXT_COMPANY_DATA_HandlerTests.composeCompanyDataMessage.1.txt"
        )
        composeCompanyDataMessageLogic(
                Bp2CompanyData(
                        "1903",
                        listOf("http://altruix.cc", "http://altruix-2.cc"),
                        listOf()
                ),
                "GETTING_NEXT_COMPANY_DATA_HandlerTests.composeCompanyDataMessage.2.txt"
        )
        composeCompanyDataMessageLogic(
                Bp2CompanyData(
                        "1903",
                        listOf("http://altruix.cc", "http://altruix-2.cc"),
                        listOf("mail1@provider1.com", "mail1@provider2.com")
                ),
                "GETTING_NEXT_COMPANY_DATA_HandlerTests.composeCompanyDataMessage.3.txt"
        )
        composeCompanyDataMessageLogic(
                Bp2CompanyData(
                        "1903",
                        listOf("http://altruix.cc", "http://altruix-2.cc"),
                        listOf("mail1@provider1.com")
                ),
                "GETTING_NEXT_COMPANY_DATA_HandlerTests.composeCompanyDataMessage.4.txt"
        )
        composeCompanyDataMessageLogic(
                Bp2CompanyData(
                        "1903",
                        listOf(),
                        listOf("mail1@provider1.com")
                ),
                "GETTING_NEXT_COMPANY_DATA_HandlerTests.composeCompanyDataMessage.5.txt"
        )

    }

    private fun composeCompanyDataMessageLogic(
            input: Bp2CompanyData,
            expResFilename: String
    ) {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                GETTING_NEXT_COMPANY_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )

        // Run method under test
        val actRes = sut.composeCompanyDataMessage(input)

        // Verify
        assertThat(actRes).isEqualTo(readFile(expResFilename))
    }

    private fun readFile(file: String) = IOUtils.toString(
            javaClass.classLoader.getResourceAsStream(
                    "cc/altruix/is1/telegram/cmd/bp2cc/" + file
            ),
            "UTF-8"
    )
}