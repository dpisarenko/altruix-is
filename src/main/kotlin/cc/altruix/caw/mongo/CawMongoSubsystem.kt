package cc.altruix.caw.mongo

import cc.altruix.common.AbstractMongoSubsystem
import cc.altruix.common.MongoConfiguration
import cc.altruix.is1.App
import cc.altruix.is1.mongo.IMongoSubsystem
import cc.altruix.is1.mongo.AltruixIs1MongoSubsystem
import cc.altruix.is1.validation.ValidationResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by pisarenko on 02.05.2017.
 */
class CawMongoSubsystem(logger: Logger = LoggerFactory.getLogger(App.LoggerName)) :
        IMongoSubsystem,
        AbstractMongoSubsystem(logger)
{
    override fun init(): ValidationResult {
        return initLogic(
                MongoConfiguration(
                        "CAW",
                        AltruixIs1MongoSubsystem.UserName,
                        AltruixIs1MongoSubsystem.Password,
                        AltruixIs1MongoSubsystem.Host,
                        AltruixIs1MongoSubsystem.Port
                )
        )
    }
}