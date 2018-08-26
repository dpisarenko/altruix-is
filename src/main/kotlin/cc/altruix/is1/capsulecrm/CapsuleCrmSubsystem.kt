package cc.altruix.is1.capsulecrm

import cc.altruix.is1.telegram.cmd.bp1add.Bp1CompanyData
import cc.altruix.is1.telegram.cmd.bp1add.ContactDataType
import cc.altruix.is1.telegram.cmd.bp2cc.Bp2CompanyData
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.utils.isNumeric
import com.beust.klaxon.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Created by pisarenko on 31.01.2017.
 */
open class CapsuleCrmSubsystem(
        val logger: Logger = LoggerFactory.getLogger("cc.altruix.is1"),
        val protocol:Logger = LoggerFactory.getLogger("protocol")
) : ICapsuleCrmSubsystem {
    companion object {
        val WwwPrefix = "www."
        val ApiToken = "4jZjBseBK+URsqqSVuinylUs7JkLrr0NfD+yeW8TIi/o4Ydxtsc3VkoIMrxUNHav"
        val AgentFieldId = 402850
        val AddNoteUrl = "https://api.capsulecrm.com/api/v2/entries"
        val CreateCompanyUrl = "https://api.capsulecrm.com/api/v2/parties"
        val CompanyIdBlankMessage = "Company ID is blank"
        val CompanyIdNonNumeric = "Company ID is not numeric"
    }
    var httpClient:CloseableHttpClient? = null
    override fun init() {
        httpClient = createDefaultHttpClient()
    }
    override fun close() {
        httpClient?.close()
    }

    override fun findPartiesByUrlFragment(urlFragment: String): PartiesSearchResult {
        val client = httpClient as CloseableHttpClient
        if (client == null) {
            return PartiesSearchResult(false, "Internal error", emptyList())
        }
        var res:CloseableHttpResponse? = null
        var req: HttpUriRequest?

        try {
            req = composeFindPartiesByUrlFragmentRequest(urlFragment)
            res = client.execute(req)

            if (res == null) {
                logger.error("findPartiesByUrlFragment(urlFragment='$urlFragment'): Null resspone")
                return PartiesSearchResult(false, "Null response", emptyList())
            }
            if (res.statusLine.statusCode != 200) {
                logger.error("findPartiesByUrlFragment(urlFragment='$urlFragment'): Wrong status code ${res.statusLine.statusCode}")
                return PartiesSearchResult(false, "Wrong status code", emptyList())
            }
            if (res.entity == null) {
                logger.error("findPartiesByUrlFragment(urlFragment='$urlFragment'): Null entity")
                return PartiesSearchResult(false, "Null entity", emptyList())
            }
            val content = res.entity.content
            if (content == null) {
                logger.error("findPartiesByUrlFragment(urlFragment='$urlFragment'): Null content")
                return PartiesSearchResult(false, "Null content", emptyList())
            }
            val parser = createJsonParser()
            val json:JsonObject = parser.parse(content)
            val partiesJson:List<JsonObject> = json["parties"] as List<JsonObject>
            if (partiesJson == null) {
                logger.error("findPartiesByUrlFragment(urlFragment='$urlFragment'): Null parties object")
                return PartiesSearchResult(false, "Null parties object", emptyList())
            }
            val parties = composePartiesList(partiesJson, urlFragment)
            return PartiesSearchResult(true, "", parties)
        }
        catch (throwable:Throwable) {
            logger.error("findPartiesByUrlFragment(urlFragment='$urlFragment')", throwable)
            return PartiesSearchResult(false, composeErrorMsg (throwable.message), emptyList())
        }
        finally {
            close(res)
        }
        return PartiesSearchResult(false, "", emptyList())
    }

    open fun close(httpClient: CloseableHttpClient?) {
        httpClient?.close()
    }

    open fun close(res: CloseableHttpResponse?) {
        res?.close()
    }

    open fun composePartiesList(partiesJson: List<JsonObject>, urlFragment: String) =
            partiesJson
                    .map { toPartyObject(it, urlFragment) }
                    .toList()

    open fun createJsonParser():TestableJsonParser = KlaxonParserWrapper()

    open fun toPartyObject(json: JsonObject, urlFragment: String): Party {
        val id = defaultValue(json.long("id"), -1L)
        val websites: List<String> = composeWebSites(json, urlFragment)
        return Party(id, websites)
    }

    open fun composeWebSites(json: JsonObject, urlFragment: String): List<String> {
        val websitesArr = json.array<JsonObject>("websites")
        val websites: List<String>
        if (websitesArr != null) {
            val searchItem = urlFragment.toLowerCase()
            websites = websitesArr.filter { it != null }
                    .map { it.string("url") }
                    .filter { it != null }
                    .map { it as String }
                    .map(String::toLowerCase)
                    .filter { x -> (x != null) && x.contains(searchItem) }
                    .toList()
        } else {
            websites = emptyList()
        }
        return websites
    }

    open fun defaultValue(maybeNull: Long?, defaultValue: Long): Long {
        if (maybeNull == null) {
            return defaultValue
        }
        return maybeNull
    }

    open fun createDefaultHttpClient() = HttpClients.createDefault()

    open fun composeFindPartiesByUrlFragmentRequest(urlFragment: String): HttpUriRequest {
        val req = HttpGet("https://api.capsulecrm.com/api/v2/parties/search?q=${urlFragment}")
        req.setHeader("Authorization", "Bearer ${ApiToken}")
        req.setHeader("Accept", "application/json");
        return req
    }

    open fun composeErrorMsg(message: String?): String {
        if (StringUtils.isBlank(message) || (message == null)) {
            return "Unknown error"
        }
        return message
    }

    override fun createCompany(compData: Bp1CompanyData): ValidationResult {
        val client = httpClient
        if (client == null) {
            return ValidationResult(false, "Internal error")
        }

        val companyExistsRes:FailableOperationResult<Boolean> = companyExists(compData)
        if (!companyExistsRes.success || (companyExistsRes.result == null)) {
            return ValidationResult(false, "Database error")
        }
        if (companyExistsRes.result) {
            return ValidationResult(false, "Company already exists in the database")
        }
        return createCompanyProper(compData, client)
    }

    open fun createCompanyProper(compData: Bp1CompanyData, client: CloseableHttpClient): ValidationResult {
        val createCompanyRes: FailableOperationResult<Long> = createCompanyInCapsule(client, compData)
        val compId = createCompanyRes.result
        when {
            (createCompanyRes.success && (compId != null) && StringUtils.isNotBlank(compData.note)) ->
                return addNote(compData.note, compId)
            (createCompanyRes.success && (compId != null)) ->
                return ValidationResult(true, "")
            else -> return ValidationResult(false, "Couldn't create company")
        }
    }

    open fun addNote(note: String, compId: Long): ValidationResult {
        val client = httpClient as CloseableHttpClient
        if (client == null) {
            return ValidationResult(false, "Internal error")
        }

        var res: CloseableHttpResponse? = null
        var req: HttpUriRequest?
        try {
            req = composeAddNoteRequest(note, compId)
            res = client.execute(req)
            if (res.statusLine.statusCode != 201) {
                logger.error("addNote(note='$note', compId=$compId): Wrong status code ${res.statusLine.statusCode}")
                return ValidationResult(false, "Wrong status code (CRM interaction)")
            }
            return ValidationResult(true, "")
        }
        catch (throwable: Throwable) {
            logger.error("addNote(note='$note', compId=$compId)", throwable)
            return ValidationResult(false, "Database error")
        } finally {
            close(res)
        }
        return ValidationResult(false, "Internal logic error")
    }

    open fun composeAddNoteRequest(text: String, compId: Long): HttpUriRequest {
        val req = createHttpPost(AddNoteUrl)
        req.setHeader("Authorization", "Bearer $ApiToken")
        req.setHeader("Accept", "application/json");
        req.setHeader("Content-Type", "application/json");

        val jsonTxt = composeAddNoteJson(compId, text)
        protocol.info("addNote(note='$text', compId=$compId): $jsonTxt")
        logger.info("addNote(note='$text', compId=$compId): $jsonTxt")
        req.entity = createStringEntity(jsonTxt)
        return req
    }

    open fun createHttpPost(url: String) = HttpPost(url)

    open fun composeAddNoteJson(compId: Long, text: String): String {
        val entryJson = JsonObject(
                mapOf(
                        "party" to JsonObject(
                                mapOf(
                                        "id" to compId
                                )
                        ),
                        "type" to "note",
                        "content" to text
                )
        )
        val partyJson = JsonObject(
                mapOf(
                        "entry" to entryJson
                )
        )
        val jsonTxt = partyJson.toJsonString()
        return jsonTxt
    }

    open fun createCompanyInCapsule(
            client: CloseableHttpClient,
            compData: Bp1CompanyData
    ): FailableOperationResult<Long> {
        var res: CloseableHttpResponse? = null
        var req: HttpUriRequest?
        try {
            req = createCreateCompanyRequest(compData)
            res = client.execute(req)
            if (res == null) {
                logger.error("createCompanyProper(compData='${compData.toString()}'): Null resspone")
                return FailableOperationResult<Long>(false, "Null response", null)
            }
            if (res.statusLine.statusCode != 201) {
                logger.error("createCompanyProper(compData='${compData.toString()}'): Wrong status code ${res.statusLine.statusCode}")
                return FailableOperationResult<Long>(false, "Wrong status code", null)
            }
            val companyId:Long = extractCompanyId(res)
            return FailableOperationResult<Long>(true, "", companyId)
        } catch (throwable: Throwable) {
            logger.error("createCompanyProper(compData='${compData.toString()}')", throwable)
            return FailableOperationResult<Long>(false, "Database error", null)
        } finally {
            close(res)
        }
        return FailableOperationResult<Long>(false, "Logic error", null)
    }

    open fun extractCompanyId(res: CloseableHttpResponse): Long {
        val json = ObjectMapper().readTree(res.entity.content)
        val party = json["party"]
        val id = party["id"]
        return id.asLong()
    }

    open fun composeListFieldsRequest(): HttpUriRequest {
        val req = HttpGet("https://api.capsulecrm.com/api/v2/parties/fields/definitions")
        req.setHeader("Authorization", "Bearer $ApiToken")
        return req
    }

    open fun composeTagsRequest(): HttpUriRequest {
        val req = HttpGet("https://api.capsulecrm.com/api/v2/parties/tags")
        req.setHeader("Authorization", "Bearer $ApiToken")
        return req
    }
    open fun createCreateCompanyRequest(data: Bp1CompanyData): HttpUriRequest {
        val req = createHttpPost(CreateCompanyUrl)
        req.setHeader("Authorization", "Bearer $ApiToken")
        req.setHeader("Accept", "application/json");
        req.setHeader("Content-Type", "application/json");
        val jsonTxt = createCreateCompanyJson(data)
        protocol.info("createCreateCompanyRequest(${data.toString()}): $jsonTxt")
        logger.info("createCreateCompanyRequest(${data.toString()}): $jsonTxt")
        req.entity = createStringEntity(jsonTxt)
        return req
    }

    open fun createCreateCompanyJson(data: Bp1CompanyData): String {
        val orjJson = JSONObject()
        orjJson.put("type", "organisation")
        orjJson.put("title", data.url)
        orjJson.put("name", data.url)
        if (data.ctype == ContactDataType.EMAIL) {
            orjJson.put("emailAddresses", createEmailAddressJson(data.email))
        }
        orjJson.put("websites", composeWebSitesJson(data))

        val agentJson = JSONObject()
        agentJson.put("value", data.agent)
        agentJson.put("definition", composeFieldDefinition(AgentFieldId))

        val fieldsList = JSONArray(listOf(agentJson))
        orjJson.put("fields", fieldsList)

        val tags = JSONArray(listOf(JSONObject(mapOf(
                "name" to "SEO"
        ))))
        orjJson.put("tags", tags)

        val partyJson = JsonObject(
                mapOf(
                        "party" to orjJson
                )
        )

        val jsonTxt = partyJson.toJsonString()
        return jsonTxt
    }

    open fun createStringEntity(jsonTxt: String) = StringEntity(jsonTxt, ContentType.APPLICATION_JSON)

    open fun composeFieldDefinition(id: Int): JSONObject =
            JSONObject(
                    mapOf(
                            "id" to id
                    )
            )

    open fun composeWebSitesJson(data: Bp1CompanyData): JSONArray {
        val urls:List<String>
        when (data.ctype) {
            ContactDataType.EMAIL -> urls = listOf(data.url)
            ContactDataType.CONTACT_FORM -> urls = listOf<String>(data.url, data.contactFormUrl)
            ContactDataType.UNKNOWN -> urls = emptyList()
        }
        val res = JSONArray()
        urls.map { urlToJson(it) }.forEach { res.put(it) }
        return res
    }

    open fun urlToJson(url: String): JSONObject = JSONObject(mapOf(
            "service" to "URL",
            "address" to url,
            "type" to "Work"
    ))

    open fun createEmailAddressJson(email: String): JSONArray {
        val node = JSONObject()
        node.put("type", "Work")
        node.put("address", email)
        val res = JSONArray()
        res.put(node)
        return res
    }

    open fun companyExists(compData: Bp1CompanyData): FailableOperationResult<Boolean> {
        val searchItem = extractHomepageUrl(compData.url)
        val searchRes = this.findPartiesByUrlFragment(searchItem)
        if (!searchRes.success || (searchRes.parties == null)) {
            return FailableOperationResult(false, "Database error", null)
        }
        val res = !searchRes.parties.isEmpty()
        return FailableOperationResult(true, "", res)
    }

    open fun extractHomepageUrl(url: String): String {
        var rightUrl = url
        if (!rightUrl.startsWith("http")) {
            rightUrl = "http://${url}"
        }
        try {
            val urlObj = URL(rightUrl)
            var host = urlObj.host
            if (host.startsWith(WwwPrefix)) {
                host = host.substring(WwwPrefix.length)
            }
            val lastDotIdx = host.lastIndexOf(".")
            if (lastDotIdx > 0) {
                return host.toLowerCase().substring(0, lastDotIdx)
            } else {
                return host.toLowerCase()
            }
        } catch (throwable:Throwable) {
            return url
        }
    }
    override fun fetchBp2CompanyData(companyId: String): FailableOperationResult<Bp2CompanyData> {
        val client = httpClient as CloseableHttpClient
        if (client == null) {
            return FailableOperationResult(false, "Internal error", null)
        }
        var res:CloseableHttpResponse? = null
        var req: HttpUriRequest?
        try {
            req = composeFetchBp2CompanyDataRequest(companyId)
            res = client.execute(req)
            if (res == null) {
                logger.error("fetchBp2CompanyData(companyId='$companyId'): Null response")
                return FailableOperationResult(false, "Null response", null)
            }
            if (res.statusLine.statusCode != 200) {
                logger.error("fetchBp2CompanyData(companyId='$companyId'): Wrong status code ${res.statusLine.statusCode}")
                return FailableOperationResult(false, "Wrong status code", null)
            }
            if (res.entity == null) {
                logger.error("fetchBp2CompanyData(companyId='$companyId'): Null entity")
                return FailableOperationResult(false, "Null entity", null)
            }
            val content = res.entity.content
            if (content == null) {
                logger.error("fetchBp2CompanyData(companyId='$companyId'): Null content")
                return FailableOperationResult(false, "Null content", null)
            }
            val parser = createJsonParser()
            val json:JsonObject = parser.parse(content)
            val party = json["party"] as JsonObject
            return createFetchBp2CompanyData(party, companyId)
        }
        catch (throwable:Throwable) {
            logger.error("fetchBp2CompanyData(companyId='$companyId')", throwable)
            return FailableOperationResult(false, composeErrorMsg (throwable.message), null)
        }
        finally {
            close(res)
        }
        return FailableOperationResult(false, "", null)
    }

    open fun createFetchBp2CompanyData(json: JsonObject, companyId: String): FailableOperationResult<Bp2CompanyData> {
        val webSites = extractWebSites(json)
        val emails = extractEmails(json)
        if (webSites.isEmpty() && emails.isEmpty()) {
            return FailableOperationResult(
                    false,
                    "Neither web sites nor e-mails found for company $companyId",
                    null
            )
        }
        return FailableOperationResult(true, "", Bp2CompanyData(
                companyId,
                webSites,
                emails
        ))
    }

    open fun extractEmails(party: JsonObject): List<String> =
            extractElementsFromJsonArray(party, "emailAddresses", "address")

    open fun extractWebSites(party: JsonObject): List<String> =
            extractElementsFromJsonArray(party, "websites", "address")

    protected fun extractElementsFromJsonArray(
            json: JsonObject,
            arrayNodeName: String,
            fieldToExtract: String
    ): List<String> {
        val root = json[arrayNodeName] as JsonArray<JsonObject>
        return root
                .map { node -> node[fieldToExtract] as String }
                .filter { StringUtils.isNotEmpty(it) }
                .map { address -> address.trim() }
                .toList()
    }

    open fun composeFetchBp2CompanyDataRequest(companyId: String): HttpUriRequest {
        val req = HttpGet("https://api.capsulecrm.com/api/v2/parties/$companyId")
        req.setHeader("Authorization", "Bearer ${ApiToken}")
        req.setHeader("Accept", "application/json");
        return req
    }

    /**
     * Don't fucking delete this method.
     */
    fun listCustomFields() {
        var res:CloseableHttpResponse? = null
        var req: HttpUriRequest?
        try {
            req = composeListFieldsRequest()
            val httpClient = createDefaultHttpClient()
            res = httpClient.execute(req)
            if (res == null) {
                logger.error("listCustomFields")
                return
            }
            if (res.statusLine.statusCode != 200) {
                logger.error("listCustomFields")
                return
            }
            val result = IOUtils.toString(res.entity.content)
            logger.info("result: $result")
            return
        }
        catch (throwable:Throwable) {
            logger.error("listCustomFields')", throwable)
        }
        finally {
            close(res)
        }
    }

    /**
     * Don't fucking delete this method.
     */
    fun listTags() {
        var httpClient:CloseableHttpClient? = null
        var res:CloseableHttpResponse? = null
        var req: HttpUriRequest?
        try {
            req = composeTagsRequest()
            val httpClient:CloseableHttpClient = createDefaultHttpClient() as CloseableHttpClient
            res = httpClient.execute(req)
            if (res == null) {
                logger.error("listCustomFields")
                return
            }
            if (res.statusLine.statusCode != 200) {
                logger.error("listCustomFields")
                return
            }
            val result = IOUtils.toString(res.entity.content)
            logger.info("result: $result")
            return
        }
        catch (throwable:Throwable) {
            logger.error("listCustomFields')", throwable)
        }
        finally {
            close(res)
        }
    }
    override fun attachContactResult(
            companyId: String,
            persona: String,
            contactTextNote: String
    ): ValidationResult {
        if (StringUtils.isBlank(companyId)) {
            val msg = CompanyIdBlankMessage
            logger.error(
                    composeAttachContactResultLogMessage(
                            msg,
                            companyId,
                            contactTextNote,
                            persona
                    )
            )
            return ValidationResult(false, msg)
        }
        if (!companyId.isNumeric()) {
            val msg = CompanyIdNonNumeric
            logger.error(
                    composeAttachContactResultLogMessage(msg, companyId, contactTextNote, persona))
            return ValidationResult(false, msg)
        }
        val numericCompanyId = companyId.toLong()
        val txt = composeContactResultNote(persona, contactTextNote)
        return addNote(txt, numericCompanyId)
    }

    open fun composeAttachContactResultLogMessage(msg: String, companyId: String, contactTextNote: String, persona: String) = "attachContactResult(companyId='$companyId', persona='$persona', " +
            "contactTextNote='$contactTextNote', contactTextNote='$contactTextNote'): $msg"

    open fun composeContactResultNote(
            persona: String,
            contactTextNote: String
    ): String {
        val sb = StringBuilder()
        val timestamp = now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        sb.append("This company has been contacted at '$timestamp'.")
        sb.append("\n")
        sb.append("Persona: $persona")
        sb.append("\n")
        sb.append("Contact text and/or notes:")
        sb.append("\n")
        sb.append(contactTextNote)
        return sb.toString()
    }

    open fun now() = ZonedDateTime.now()
}