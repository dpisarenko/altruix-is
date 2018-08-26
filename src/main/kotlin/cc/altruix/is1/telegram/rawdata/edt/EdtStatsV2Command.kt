package cc.altruix.is1.telegram.rawdata.edt

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 19.04.2017.
 */
open class EdtStatsV2Command(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu){
    companion object {
        val Name = "/eds"
        val Help = "*Editing standard statistics* (use this command, when a step of the editing standard was useful)"
        val MongoCollection = "EditingStatsV2"
        val Form = Teleform(listOf(
                StaticText("1", "Editing session statistics. Editing standard v. 2."),
                StaticText("2", "Please enter the ID of the work (e. g. \"SS-1.5\" for scene nr. 5 of " +
                        "short story 1, N-1.10 for \"chapter 10 of novel 1\")."),
                TextInput("3", "workAndPart", "Work and part?"),
                StaticText("4", "Now enter the part of the editing standard that was"+
                        " helpful in editing this text (which you applied) in the format " +
                        "\"C.N\" where C is the chapter of the standard and N the number " +
                        "of the item inside this chapter."),
                TextInput("5", "item", "Standard item (e. g. \"C2.4\")?")
        ), MongoCollection)
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}