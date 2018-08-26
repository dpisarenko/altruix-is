package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by 1 on 30.04.2017.
 */
open class EdfCmd(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/edf"
        val Help = "*Editing finished* event"
        val MongoCollection = "EditingFinishedRecord"
        val Form = Teleform(listOf(
                StaticText("1", "Finished editing a scene"),
                StaticText("2", "Please enter the ID of the work (e. g. \"SS-1.5\" for scene nr. 5 of " +
                        "short story 1, N-1.10 for \"chapter 10 of novel 1\")."),
                TextInput("3", "workAndPart", "Work and part?"),
                DoubleInput("4", "totalTime", "Total time for editing this scene?"),
                IntegerInput("5", "wordCountAfterEdit", "Word count after editing?")
        ), MongoCollection)
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}