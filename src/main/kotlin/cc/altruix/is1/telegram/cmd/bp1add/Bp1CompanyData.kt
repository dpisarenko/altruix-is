package cc.altruix.is1.telegram.cmd.bp1add

/**
 * Created by pisarenko on 15.02.2017.
 */
data class Bp1CompanyData (val url:String,
                           val ctype:ContactDataType,
                           val email:String,
                           val contactFormUrl:String,
                           val note:String,
                           val agent:String)