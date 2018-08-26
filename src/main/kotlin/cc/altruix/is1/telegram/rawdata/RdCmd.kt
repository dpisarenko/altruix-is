package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 21.04.2017.
 */
open class RdCmd(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/rd"
        val Help = "Reading stats"
        val MongoCollection = "ReadingStats"
        val Form = Teleform(listOf(
                StaticText("1", "Reading statistics"),
                TextInput("2", "work", "What work (book, article) do these data apply to?"),
                TextInput("3", "unit", "Unit (L for location in Kindle, P for pages)?"),
                IntegerInput("4", "current", "At what page or location are you currently in the book?"),
                IntegerInput("5", "max", "Farthest location or number of pages in the work?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help
}