package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 12.05.2017.
 */
open class WexCmd(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/wex"
        val Help = "Writing exercise (skill improvement operation)"
        val MongoCollection = "WritingExercises"
        val Form = Teleform(listOf(
                StaticText("1", "Writing exercise"),
                TextInput("2", "description", "Description?"),
                TextInput("3", "source", "Source of the exercise (book)?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help

}