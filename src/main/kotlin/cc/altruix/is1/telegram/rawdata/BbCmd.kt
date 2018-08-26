package cc.altruix.is1.telegram.rawdata

import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.is1.telegram.forms.*

/**
 * Created by pisarenko on 21.04.2017.
 */
open class BbCmd(
        af: IAutomatonFactory,
        tu: ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/bb"
        val Help = "Enter *bank balance*"
        val MongoCollection = "BankBalance"
        val Form = Teleform(listOf(
                StaticText("1", "Bank balance"),
                TextInput("2", "account", "Bank, account?"),
                IntegerInput("3", "amount", "Current amount?")
        ), MongoCollection)
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}