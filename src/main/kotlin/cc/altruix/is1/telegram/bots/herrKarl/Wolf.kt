package cc.altruix.is1.telegram.bots.herrKarl

import cc.altruix.is1.telegram.Authenticator
import org.telegram.telegrambots.api.objects.Update

/**
 * Created by 1 on 20.02.2017.
 */
open class Wolf : Authenticator {
    override fun rightUser(update: Update): Boolean =
            update.message.from.id == CENSORED
}