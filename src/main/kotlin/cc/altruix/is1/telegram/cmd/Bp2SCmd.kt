package cc.altruix.is1.telegram.cmd

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramCommand
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.TelegramUtils
import cc.altruix.utils.isNumeric

/**
 * Created by 1 on 25.02.2017.
 *
 * /bp2s: Status. Parameter: Batch ID. Shows the status of the batch
 * (active or not) and the percentage of the companies contacted.
 * Target user: DP
 */
open class Bp2SCmd(
        val jena: IJenaSubsystem,
        val tu: ITelegramUtils = TelegramUtils()
) : ITelegramCommand {
    companion object {
        val Name = "/bp2s"
        val Help = "Shows status of the batch (SEO company contacting, obsolete)"
    }
    override fun execute(
            text: String,
            bot: IResponsiveBot,
            chatId: Long,
            userId: Int
    ) {
        val args = tu.extractArgs(text, Name)
        if (args.isNullOrBlank()) {
            tu.sendTextMessage(
                    ITelegramUtils.OneParameterOnlyAllowed,
                    chatId,
                    bot
            )
            return
        }
        val batchIdTxt = args.trim()
        if (!batchIdTxt.isNumeric()) {
            tu.sendTextMessage(
                    "Non-numeric batch ID. Fuck you!",
                    chatId,
                    bot
            )
            return
        }
        val batchId = batchIdTxt.toInt()
        val statusRes = jena.batchStatus(batchId)
        val status = statusRes.result
        if (!statusRes.success || (status == null)) {
            tu.sendTextMessage(
                    "Database error ('${statusRes.error}')",
                    chatId,
                    bot
            )
            return
        }
        tu.sendTextMessage(
                "There are ${status.companiesCount} companies in batch ${status.id}.",
                chatId,
                bot)
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}