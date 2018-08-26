package cc.altruix.common

import cc.altruix.is1.telegram.Authenticator
import cc.altruix.is1.telegram.bots.herrKarl.Wolf
import org.telegram.telegrambots.ApiContextInitializer

/**
 * Created by pisarenko on 02.05.2017.
 */
abstract class AbstractTelegramSubsystem {
    open fun initApiContextInitializer() {
        ApiContextInitializer.init()
    }

    open fun createWolf(): Authenticator = Wolf()
}