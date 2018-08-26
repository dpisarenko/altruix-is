package cc.altruix.is1.telegram

/**
 * Created by pisarenko on 10.02.2017.
 */
abstract class AbstractCommandRegistry : ITelegramCommandRegistry {
    val commandsByName: MutableMap<String, ITelegramCommand> = mutableMapOf()
    override fun find(name: String): ITelegramCommand? = commandsByName[name]
}