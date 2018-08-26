package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 19.05.2017.
 */
class MovCmd(af: IAutomatonFactory,
             tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/mov"
        val Help = "Movie watchings"
        val MongoCollection = "MovieWatchings"
        val Form = Teleform(listOf(
                StaticText("1", "You watched a movie"),
                TextInput("2", "notes", "Notes (e. g. title, year)?")
        ), MongoCollection)
    }
    override fun name(): String = Name

    override fun helpText(): String = Help

}