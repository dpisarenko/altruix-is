package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 19.05.2017.
 */
class CafCmd(af: IAutomatonFactory,
             tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/caf"
        val Help = "Café visits"
        val MongoCollection = "CafeVisits"
        val Form = Teleform(listOf(
                StaticText("1", "Café visit"),
                TextInput("2", "notes", "Notes (e. g. location)?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help

}