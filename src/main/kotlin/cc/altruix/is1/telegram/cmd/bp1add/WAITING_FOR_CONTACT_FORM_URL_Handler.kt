package cc.altruix.is1.telegram.cmd.bp1add

import cc.altruix.is1.telegram.AutomatonMessageHandler
import cc.altruix.is1.telegram.IParentAutomaton
import cc.altruix.is1.telegram.cmd.bp1add.Bp1AddCmdState
import org.apache.commons.validator.routines.UrlValidator

/**
 * Created by pisarenko on 10.02.2017.
 */
open class WAITING_FOR_CONTACT_FORM_URL_Handler(val parent: IParentBp1AddCmdAutomaton) :
        Bp1ValidatedTextInputHandler(
                parent,
                errorMsg = "Wrong URL") {

    override fun inputValid(url: String?): Boolean {
        if (url == null) {
            return false
        }
        return UrlValidator.getInstance().isValid(url)
    }

    override fun saveData(url: String) {
        parent.saveContactFormUrl(url)
    }
}