package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 17.05.2017.
 */
class SspCmd(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/ssp"
        val Help = "Short story *published* event"
        val MongoCollection = "ShortStoryPublishedRecord"
        val Form = Teleform(listOf(
                StaticText("1", "Published a short story"),
                StaticText("2", "Please enter the ID of the short story (e. g. \"SS-1\")."),
                TextInput("3", "work", "Work?"),
                TextInput("4", "name", "Name of the story?")
        ), MongoCollection)
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}