package cc.altruix.common

/**
 * Created by pisarenko on 02.05.2017.
 */
data class MongoConfiguration(
        val dbName:String,
        val userName:String,
        val password:String,
        val host:String,
        val port:Int
)