package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update

/**
 * Created by pisarenko on 31.01.2017.
 */
interface IInvalidUpdateFormatter {
    fun format(update: Update, msg: Message, textMsg: String): String
}