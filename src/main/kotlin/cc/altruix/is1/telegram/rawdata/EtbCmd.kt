package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 19.05.2017.
 */
class EtbCmd(af: IAutomatonFactory,
             tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/etb"
        val Help = "Brushed the teeth in the evening"
        val MongoCollection = "EveningTeethBrushings"
        val Form = Teleform(listOf(
                StaticText("1", "Brushed the teeth in the evening"),
                TextInput("2", "notes", "Notes?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help

}