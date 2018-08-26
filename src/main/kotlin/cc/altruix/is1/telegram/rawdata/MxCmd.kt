package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 19.05.2017.
 */
class MxCmd(af: IAutomatonFactory,
            tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/mx"
        val Help = "Marketing experiment conducted"
        val MongoCollection = "MarketingExperimentConducted"
        val Form = Teleform(listOf(
                StaticText("1", "Marketing experiment conducted"),
                TextInput("2", "notes", "Notes (e. g. path to results)?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help

}