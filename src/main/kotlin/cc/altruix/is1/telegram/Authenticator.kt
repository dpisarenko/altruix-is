package cc.altruix.is1.telegram

import org.telegram.telegrambots.api.objects.Update

/**
 * Created by pisarenko on 31.01.2017.
 */
interface Authenticator {
    fun rightUser(update: Update): Boolean
}