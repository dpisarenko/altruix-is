package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 19.05.2017.
 */
class SsrCmd(af: IAutomatonFactory,
             tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/ssr"
        val Help = "Short story read"
        val MongoCollection = "ShortStoryReadings"
        val Form = Teleform(listOf(
                StaticText("1", "Short story read"),
                TextInput("2", "notes", "Notes (e. g. name, author)?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help

}