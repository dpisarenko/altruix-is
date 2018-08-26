package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update

/**
 * Created by pisarenko on 31.01.2017.
 */
class InvalidUpdateFormatter : IInvalidUpdateFormatter {
    override fun format(update: Update, msg: Message, textMsg:String):String {
        val sender = msg.from
        return String.format("%s Update ID: %d. Sender ID: %d. First name: '%s'. Last name: '%s'. User name: '%s'",
                textMsg, update.updateId, sender.id, sender.firstName, sender.lastName, sender.userName)

    }
}