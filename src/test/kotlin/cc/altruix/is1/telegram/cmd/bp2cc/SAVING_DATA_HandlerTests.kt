package cc.altruix.is1.telegram.cmd.bp2cc

import cc.altruix.is1.capsulecrm.ICapsuleCrmSubsystem
import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by 1 on 19.03.2017.
 */
class SAVING_DATA_HandlerTests {
    @Test
    fun fireNoCompData() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                SAVING_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val batchId = 1331
        `when`(parent.batchId()).thenReturn(batchId)
        val compData = null
        `when`(parent.getCompanyData()).thenReturn(compData)
        doNothing().`when`(sut).printMessage(SAVING_DATA_Handler.NoCompanyDataMessage)
        val inOrder = inOrder(parent, jena, capsule, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).batchId()
        inOrder.verify(parent).getCompanyData()
        inOrder.verify(sut).printMessage(SAVING_DATA_Handler.NoCompanyDataMessage)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.CANCELING)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.CANCELING }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
    @Test
    fun firePersonaNotFound() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                SAVING_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val batchId = 1331
        `when`(parent.batchId()).thenReturn(batchId)
        val compId = "1435"
        val compData = Bp2CompanyData(compId, emptyList(), emptyList())
        `when`(parent.getCompanyData()).thenReturn(compData)
        val contactTextNote = "contactTextNote"
        `when`(parent.getContactTextAndNote()).thenReturn(contactTextNote)
        val personaRes = FailableOperationResult<String>(false, "error", null)
        `when`(jena.fetchPersona(batchId)).thenReturn(personaRes)
        doNothing().`when`(sut).printMessage(SAVING_DATA_Handler.PersonaNotFound)
        val crmRes = ValidationResult(true, "")
        `when`(
                capsule.attachContactResult(
                        compId,
                        SAVING_DATA_Handler.UnknownPersona,
                        contactTextNote
                )
        ).thenReturn(crmRes)
        val compRemRes = ValidationResult(true, "")
        `when`(jena.removeCompanyFromBatch(batchId, compId)).thenReturn(compRemRes)

        val inOrder = inOrder(parent, jena, capsule, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).batchId()
        inOrder.verify(parent).getCompanyData()
        inOrder.verify(parent).getContactTextAndNote()
        inOrder.verify(jena).fetchPersona(batchId)
        inOrder.verify(sut).printMessage(SAVING_DATA_Handler.PersonaNotFound)
        inOrder.verify(capsule).attachContactResult(
                compId,
                SAVING_DATA_Handler.UnknownPersona,
                contactTextNote
        )
        inOrder.verify(jena).removeCompanyFromBatch(batchId, compId)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.END)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.END }
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
                SAVING_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val batchId = 1331
        `when`(parent.batchId()).thenReturn(batchId)
        val compId = "1435"
        val compData = Bp2CompanyData(compId, emptyList(), emptyList())
        `when`(parent.getCompanyData()).thenReturn(compData)
        val contactTextNote = "contactTextNote"
        `when`(parent.getContactTextAndNote()).thenReturn(contactTextNote)
        val persona = "persona"
        val personaRes = FailableOperationResult<String>(true, "", persona)
        `when`(jena.fetchPersona(batchId)).thenReturn(personaRes)
        val crmRes = ValidationResult(false, "error")
        `when`(
                capsule.attachContactResult(
                        compId,
                        persona,
                        contactTextNote
                )
        ).thenReturn(crmRes)
        doNothing().`when`(sut).printMessage(SAVING_DATA_Handler.CrmInteractionFaultMessage)
        val compRemRes = ValidationResult(true, "")
        `when`(jena.removeCompanyFromBatch(batchId, compId)).thenReturn(compRemRes)

        val inOrder = inOrder(parent, jena, capsule, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).batchId()
        inOrder.verify(parent).getCompanyData()
        inOrder.verify(parent).getContactTextAndNote()
        inOrder.verify(jena).fetchPersona(batchId)
        inOrder.verify(capsule).attachContactResult(
                compId,
                persona,
                contactTextNote
        )
        inOrder.verify(sut).printMessage(SAVING_DATA_Handler.CrmInteractionFaultMessage)
        inOrder.verify(jena).removeCompanyFromBatch(batchId, compId)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.END)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.END }
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
                SAVING_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val batchId = 1331
        `when`(parent.batchId()).thenReturn(batchId)
        val compId = "1435"
        val compData = Bp2CompanyData(compId, emptyList(), emptyList())
        `when`(parent.getCompanyData()).thenReturn(compData)
        val contactTextNote = "contactTextNote"
        `when`(parent.getContactTextAndNote()).thenReturn(contactTextNote)
        val persona = "persona"
        val personaRes = FailableOperationResult<String>(true, "", persona)
        `when`(jena.fetchPersona(batchId)).thenReturn(personaRes)
        val crmRes = ValidationResult(true, "")
        `when`(
                capsule.attachContactResult(
                        compId,
                        persona,
                        contactTextNote
                )
        ).thenReturn(crmRes)
        val compRemRes = ValidationResult(true, "")
        `when`(jena.removeCompanyFromBatch(batchId, compId)).thenReturn(compRemRes)
        val ackMsg = "ackMsg"
        doReturn(ackMsg).`when`(sut).composeOperationCompletedMessage(compId)
        doNothing().`when`(sut).printMessage(ackMsg)

        val inOrder = inOrder(parent, jena, capsule, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).batchId()
        inOrder.verify(parent).getCompanyData()
        inOrder.verify(parent).getContactTextAndNote()
        inOrder.verify(jena).fetchPersona(batchId)
        inOrder.verify(capsule).attachContactResult(
                compId,
                persona,
                contactTextNote
        )
        inOrder.verify(jena).removeCompanyFromBatch(batchId, compId)
        inOrder.verify(sut).composeOperationCompletedMessage(compId)
        inOrder.verify(sut).printMessage(ackMsg)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.END)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.END }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
    @Test
    fun fireBatchUpdateFault() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                SAVING_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )
        val batchId = 1331
        `when`(parent.batchId()).thenReturn(batchId)
        val compId = "1435"
        val compData = Bp2CompanyData(compId, emptyList(), emptyList())
        `when`(parent.getCompanyData()).thenReturn(compData)
        val contactTextNote = "contactTextNote"
        `when`(parent.getContactTextAndNote()).thenReturn(contactTextNote)
        val persona = "persona"
        val personaRes = FailableOperationResult<String>(true, "", persona)
        `when`(jena.fetchPersona(batchId)).thenReturn(personaRes)
        val crmRes = ValidationResult(true, "")
        `when`(
                capsule.attachContactResult(
                        compId,
                        persona,
                        contactTextNote
                )
        ).thenReturn(crmRes)
        val compRemError = "compRemError"
        val compRemRes = ValidationResult(false, compRemError)
        `when`(jena.removeCompanyFromBatch(batchId, compId)).thenReturn(compRemRes)
        val compRemResMsg = "Batch update problem ('$compRemError'). Please contact Dmitri Pisarenko."
        doNothing().`when`(sut).printMessage(compRemResMsg)
        val inOrder = inOrder(parent, jena, capsule, sut)

        // Run method under test
        sut.fire()

        // Verify
        inOrder.verify(parent).batchId()
        inOrder.verify(parent).getCompanyData()
        inOrder.verify(parent).getContactTextAndNote()
        inOrder.verify(jena).fetchPersona(batchId)
        inOrder.verify(capsule).attachContactResult(
                compId,
                persona,
                contactTextNote
        )
        inOrder.verify(jena).removeCompanyFromBatch(batchId, compId)
        inOrder.verify(sut).printMessage(compRemResMsg)
        inOrder.verify(parent).goToStateIfPossible(Bp2CcCmdState.END)
        Bp2CcCmdState.values()
                .filter { it != Bp2CcCmdState.END }
                .forEach {
                    inOrder.verify(parent, never()).goToStateIfPossible(it)
                }
    }
    @Test
    fun composeOperationCompletedMessage() {
        // Prepare
        val parent = mock<IParentBp2CcCmdAutomaton>()
        val jena = mock<IJenaSubsystem>()
        val capsule = mock<ICapsuleCrmSubsystem>()
        val sut = spy(
                SAVING_DATA_Handler(
                        parent,
                        jena,
                        capsule
                )
        )

        // Run method under test
        val actRes = sut.composeOperationCompletedMessage("2136")

        // Verify
        assertThat(actRes).isEqualTo("Die Sache mit Firma '2136' hob' I jetzt umebogn.")
    }
}