package cc.altruix.is1.telegram.cmd.bp1add

import org.apache.commons.validator.routines.EmailValidator

/**
 * Created by pisarenko on 10.02.2017.
 */
open class WAITING_FOR_EMAIL_Handler(val parent: IParentBp1AddCmdAutomaton) :
        Bp1ValidatedTextInputHandler(
                parent,
                errorMsg = "Incorrect e-mail"
        ) {

    override fun inputValid(email: String?): Boolean {
        if (email == null) {
            return false
        }
        return EmailValidator.getInstance().isValid(email)
    }

    override fun saveData(email: String) {
        parent.saveEmail(email)
    }
}