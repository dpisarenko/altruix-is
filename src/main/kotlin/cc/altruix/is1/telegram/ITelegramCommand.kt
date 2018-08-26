package cc.altruix.is1.telegram

/**
 * Created by pisarenko on 31.01.2017.
 */
interface ITelegramCommand {
    fun execute(text: String, bot: IResponsiveBot, chatId: Long, userId: Int)
    fun name():String
    fun helpText():String
}