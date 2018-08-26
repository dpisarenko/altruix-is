package cc.altruix.is1.telegram.cmd.bp2cc

/**
 * Created by pisarenko on 15.03.2017.
 */
data class Bp2CompanyData(
        val companyId:String,
        val webSites:List<String>,
        val emails:List<String>
)