package cc.altruix.is1.telegram.rawdata.wordcount

import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.telegram.*
import cc.altruix.is1.telegram.forms.*
import cc.altruix.is1.validation.FailableOperationResult

/**
 * Created by pisarenko on 06.04.2017.
 */
open class WordCountCommand(
        af: IAutomatonFactory,
        tu:ITelegramUtils = TelegramUtils()
) : AbstractFormCommand(af, Form, tu) {
    companion object {
        val Name = "/wct"
        val Help = "Enter *word count* data (for calculating the number of words written per day)"
        val MongoCollection = IMongoSubsystem.WritingStatsColl
        val Form = Teleform(listOf(
                StaticText("1", "Please enter the data about the word count "+
                        "in a scene of the work you're currently work at."),
                TextInput("3", IMongoSubsystem.WritingStats_workName, "Work?"),
                TextInput("4", IMongoSubsystem.WritingStats_partName, "Part name (e. g. \"Scene 1\", \"Chapter 2\")?"),
                IntegerInput("5", IMongoSubsystem.WritingStats_wordCount, "Current word count?")
        ), MongoCollection)
    }

    override fun name(): String = Name

    override fun helpText(): String = Help
}