package cc.altruix.is1.telegram.cmd.bp2cb

import java.io.InputStream

/**
 * Created by 1 on 25.02.2017.
 */
interface ICompanyIdExtractor {
    fun extractCompanyIds(stream:InputStream):List<String>
}