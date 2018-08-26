package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 26.04.2017.
 */
open class DCmd(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
): AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/d"
        val Help = "Enter *distraction* incident data"
        val MongoCollection = "Distractions"
        val Form = Teleform(listOf(
                StaticText("1", "Distraction incident"),
                TextInput("2", "type", "Did you get distracted (D) or prevented a distraction from happening (P)?"),
                TextInput("3", "text", "Notes?")
        ), MongoCollection)
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}