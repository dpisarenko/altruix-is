package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.objects.Update

/**
 * Created by pisarenko on 10.02.2017.
 */
class AbstractBotForTesting : AbstractBot() {
    override fun getBotUsername(): String = ""

    override fun getBotToken(): String = ""

    override fun onUpdateReceived(update: Update?) {
    }
}