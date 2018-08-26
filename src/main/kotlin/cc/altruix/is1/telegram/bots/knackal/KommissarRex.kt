package cc.altruix.is1.telegram.bots.knackal

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.Authenticator
import cc.altruix.is1.telegram.bots.herrKarl.Wolf
import cc.altruix.is1.telegram.cmd.IntroCmd
import cc.altruix.is1.validation.FailableOperationResult
import org.telegram.telegrambots.api.objects.Update

/**
 * Created by pisarenko on 31.01.2017.
 */
open class KommissarRex(
        val jena: IJenaSubsystem,
        val wolf:Wolf = Wolf()
) : Authenticator {
    override fun rightUser(update: Update): Boolean {
        when {
            isIntroCommand(update) -> return true
            wolf.rightUser(update) -> return true
            else -> return userActivated(update)
        }
    }

    open fun userActivated(update: Update): Boolean {
        val cmd = extractCommand(update)
        val res: FailableOperationResult<Boolean> = jena.hasPermission(
                update.message.from.id,
                cmd)
        if (res.success && (res.result != null)) {
            return res.result
        }
        return false
    }

    open fun extractCommand(update: Update): String {
        val msgTxt = update.message.text.trim()
        if (msgTxt.contains(' ')) {
            return msgTxt.split(' ')[0]
        }
        return msgTxt
    }

    open fun isIntroCommand(update: Update): Boolean {
        val txt = update.message.text
        val parts = txt.split(' ')
        if (parts.size < 1) {
            return false
        }
        return parts[0].equals(IntroCmd.Name)
    }
}