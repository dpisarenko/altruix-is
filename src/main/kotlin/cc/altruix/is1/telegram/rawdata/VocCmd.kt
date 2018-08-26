package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 20.04.2017.
 */
open class VocCmd(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/voc"
        val Help = "Vocabulary stats"
        val MongoCollection = "VocStats"
        val Form = Teleform(listOf(
                StaticText("1", "English vocabulary statistics (Anki)"),
                IntegerInput("2", "total", "Total cards?"),
                IntegerInput("3", "unseen", "Unseen cards?"),
                IntegerInput("4", "youngLearn", "Young+Learn?"),
                IntegerInput("5", "mature", "Mature?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help
}