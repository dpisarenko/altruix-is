package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 19.05.2017.
 */
class VosCmd(af: IAutomatonFactory,
             tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/vos"
        val Help = "Vocabulary training session"
        val MongoCollection = "VocabularyTrainingSessions"
        val Form = Teleform(listOf(
                StaticText("1", "Vocabulary training session"),
                TextInput("2", "notes", "Notes?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help

}