package cc.altruix.is1.telegram

/**
 * Created by pisarenko on 31.01.2017.
 */
interface ITelegramCommandRegistry {
    fun find(name:String): ITelegramCommand?
    fun init()
}