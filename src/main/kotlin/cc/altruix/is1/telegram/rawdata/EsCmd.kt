package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 02.05.2017.
 */
open class EsCmd(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/es"
        val Help = "*Editing started* event"
        val MongoCollection = "EditingStartedRecord"
        val Form = Teleform(listOf(
                StaticText("1", "Start of editing"),
                StaticText("2", "Please enter the ID of the work (e. g. \"SS-1.5\" for scene nr. 5 of " +
                        "short story 1, N-1.10 for \"chapter 10 of novel 1\")."),
                TextInput("3", "workAndPart", "Work and part?"),
                IntegerInput("4", "wordCountAfterEdit", "Word count before editing (after first draft)?")
        ), MongoCollection)

    }
    override fun name(): String = Name

    override fun helpText(): String = Help
}