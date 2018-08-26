package cc.altruix.is1.capsule

import cc.altruix.is1.capsulecrm.*
import cc.altruix.is1.telegram.cmd.bp1add.Bp1CompanyData
import cc.altruix.is1.telegram.cmd.bp1add.ContactDataType
import cc.altruix.is1.telegram.cmd.bp2cc.Bp2CompanyData
import cc.altruix.is1.validation.FailableOperationResult
import cc.altruix.is1.validation.ValidationResult
import cc.altruix.mock
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.fest.assertions.Assertions.assertThat
import org.json.JSONObject
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.verification.VerificationMode
import org.slf4j.Logger
import java.io.InputStream
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Created by pisarenko on 01.02.2017.
 */
class CapsuleCrmSubsystemTests {
    @Test
    fun findPartiesByUrlFragment() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val req = mock<HttpUriRequest>()
        val httpClient = mock<CloseableHttpClient>()
        val res = mock<CloseableHttpResponse>()
        val urlFragment = "urlFragment"
        doReturn(req).`when`(sut).composeFindPartiesByUrlFragmentRequest(urlFragment)
        doReturn(httpClient).`when`(sut).createDefaultHttpClient()
        `when`(httpClient.execute(req)).thenReturn(res)
        val entity = mock<HttpEntity>()
        `when`(res.entity).thenReturn(entity)
        val content = mock<InputStream>()
        `when`(entity.content).thenReturn(content)
        val parser = mock<TestableJsonParser>()
        doReturn(parser).`when`(sut).createJsonParser()
        val json = JsonObject()
        `when`(parser.parse(content)).thenReturn(json)
        val partiesJson = mock<List<JsonObject>>()
        json["parties"] = partiesJson
        val parties = mock<List<Party>>()
        doReturn(parties).`when`(sut).composePartiesList(partiesJson, urlFragment)
        doNothing().`when`(sut).close(res)
        doNothing().`when`(sut).close(httpClient)
        val statusLine = mock<StatusLine>()
        `when`(statusLine.statusCode).thenReturn(200)
        `when`(res.statusLine).thenReturn(statusLine)

        // Run method under test
        sut.init()
        val actRes = sut.findPartiesByUrlFragment(urlFragment)

        // Verify
        verify(sut).composeFindPartiesByUrlFragmentRequest(urlFragment)
        verify(sut).createDefaultHttpClient()
        verify(httpClient).execute(req)
        verify(sut).createJsonParser()
        verify(parser).parse(content)
        verify(sut).composePartiesList(partiesJson, urlFragment)
        verify(sut).close(res)
        verify(sut, never()).close(httpClient)
        assertThat(actRes).isNotNull
        assertThat(actRes.success).isTrue()
        assertThat(actRes.errorMsg).isEqualTo("")
        assertThat(actRes.parties).isSameAs(parties)
    }
    @Test
    fun findPartiesByUrlFragmentHandlesExceptionsCorrectly() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val req = mock<HttpUriRequest>()
        val httpClient = mock<CloseableHttpClient>()
        val res = mock<CloseableHttpResponse>()
        val urlFragment = "urlFragment"
        doReturn(req).`when`(sut).composeFindPartiesByUrlFragmentRequest(urlFragment)
        doReturn(httpClient).`when`(sut).createDefaultHttpClient()
        `when`(httpClient.execute(req)).thenThrow(RuntimeException("Major fuck-up"))
        val entity = mock<HttpEntity>()
        `when`(res.entity).thenReturn(entity)
        val content = mock<InputStream>()
        `when`(entity.content).thenReturn(content)
        val parser = mock<TestableJsonParser>()
        doReturn(parser).`when`(sut).createJsonParser()
        val json = JsonObject()
        `when`(parser.parse(content)).thenReturn(json)
        val partiesJson = mock<List<JsonObject>>()
        json["parties"] = partiesJson
        val parties = mock<List<Party>>()
        doReturn(parties).`when`(sut).composePartiesList(partiesJson, urlFragment)
        doNothing().`when`(sut).close(res)
        doNothing().`when`(sut).close(httpClient)
        val statusLine = mock<StatusLine>()
        `when`(statusLine.statusCode).thenReturn(200)
        `when`(res.statusLine).thenReturn(statusLine)

        // Run method under test
        sut.init()
        val actRes = sut.findPartiesByUrlFragment(urlFragment)

        // Verify
        verify(sut).composeFindPartiesByUrlFragmentRequest(urlFragment)
        verify(sut).createDefaultHttpClient()
        verify(httpClient).execute(req)
        verify(sut).close(null as CloseableHttpResponse?)
        verify(sut, never()).close(httpClient)
        assertThat(actRes).isNotNull
        assertThat(actRes.success).isFalse()
        assertThat(actRes.errorMsg).isEqualTo("Major fuck-up")
        assertThat(actRes.parties).isEmpty()
    }

    @Test
    fun closeHttpClientHandlesNullCorrectly() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))

        // Run method under test
        sut.close(null as CloseableHttpClient?)
    }
    @Test
    fun closeHttpClientClosesNonNullClients() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val httpClient = mock<CloseableHttpClient>()

        // Run method under test
        sut.close(httpClient)

        // Verify
        verify(httpClient).close()
    }

    @Test
    fun closeHttpResponseHandlesNullCorrectly() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))

        // Run method under test
        sut.close(null as CloseableHttpResponse?)
    }
    @Test
    fun closeHttpResponseClosesNonNullResponses() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val res = mock<CloseableHttpResponse>()

        // Run method under test
        sut.close(res)

        // Verify
        verify(res).close()
    }

    @Test
    fun composePartiesList() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val jsonObj = JsonObject()
        val partiesJson:List<JsonObject> = listOf(jsonObj)
        val urlFragment = "urlFragment"
        val party = Party(1L, listOf("abc"))
        doReturn(party).`when`(sut).toPartyObject(jsonObj, urlFragment)

        // Run method under test
        val actRes = sut.composePartiesList(partiesJson, urlFragment)

        // Verify
        assertThat(actRes).isNotNull
        assertThat(actRes.size).isEqualTo(1)
        assertThat(actRes[0]).isSameAs(party)
    }
    @Test
    fun createJsonParser() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))

        // Run method under test
        val actRes = sut.createJsonParser()

        // Verify
        assertThat(actRes).isNotNull
        assertThat(actRes is KlaxonParserWrapper).isTrue()
    }

    @Test
    fun toPartyObject() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val json = JsonObject()
        val id = 1029L
        json.set("id", id)
        val urlFragment = "urlFragment"
        doReturn(id).`when`(sut).defaultValue(id, -1L)
        val websites = mock<List<String>>()
        doReturn(websites).`when`(sut).composeWebSites(json, urlFragment)

        // Run method under test
        val actRes = sut.toPartyObject(json, urlFragment)

        // Verify
        verify(sut).defaultValue(id, -1L)
        verify(sut).composeWebSites(json, urlFragment)
        assertThat(actRes).isNotNull
        assertThat(actRes.id).isEqualTo(id)
        assertThat(actRes.webSites).isSameAs(websites)
    }
    @Test
    fun composeWebSites() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val json = JsonObject()
        val websitesArr = JsonArray(
                createJsonUrl("http://ALTRUIX.cc"),
                createJsonUrl("http://altavista.com"),
                createJsonUrl(null)
        )
        json["websites"] = websitesArr
        val urlFragment = "altr"

        // Run method under test
        val actRes = sut.composeWebSites(json, urlFragment)

        // Verify
        assertThat(actRes).isNotNull
        assertThat(actRes.size).isEqualTo(1)
        assertThat(actRes[0]).isEqualTo("http://altruix.cc")
    }

    @Test
    fun defaultValue() {
        defaultValueTestLogic(null, -1)
        defaultValueTestLogic(0, 0)
        defaultValueTestLogic(1, 1)
    }

    @Test
    fun composeFindPartiesByUrlFragmentRequest() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val urlFragment = "urlFragment"

        // Run method under test
        val actRes = sut.composeFindPartiesByUrlFragmentRequest(urlFragment)

        // Verify
        assertThat(actRes).isNotNull
        assertThat(actRes.uri.toString()).isEqualTo("https://api.capsulecrm.com/api/v2/parties/search?q=urlFragment")
        assertThat(actRes.getFirstHeader("Accept").value).isEqualTo("application/json")
        assertThat(actRes.getFirstHeader("Authorization").value).isEqualTo("Bearer ${CapsuleCrmSubsystem.ApiToken}")
    }

    @Test
    fun composeErrorMsg() {
        composeErrorMsgTestLogic(null, "Unknown error")
        composeErrorMsgTestLogic("Known error", "Known error")
    }
    @Test
    fun extractHomepageUrl() {
        val data = listOf<Pair<String,String>>(
                Pair("", ""),
                Pair("http://altruix.cc/", "altruix"),
                Pair("http://alistapart.com/about/contribute/", "alistapart"),
                Pair("https://alamedaim.com/", "alamedaim"),
                Pair("http://www.aquarianonline.com/online-writers-guide/", "aquarianonline"),
                Pair("http://www.aztechit.co.uk/", "aztechit.co"),
                Pair("http://bayareaseospecialist.com/", "bayareaseospecialist"),
                Pair("http://www.baytechwebdesign.com/contact/", "baytechwebdesign"),
                Pair("http://benhunt.com/", "benhunt"),
                Pair("http://thestoryoftelling.com/about/", "thestoryoftelling"),
                Pair("http://blackhillswoman.com/", "blackhillswoman"),
                Pair("http://www.brokenpencil.com/submissions", "brokenpencil"),
                Pair("http://www.thepersuasionrevolution.com/10hacks", "thepersuasionrevolution"),
                Pair("http://buzzymag.com/submissions/", "buzzymag"),
                Pair("http://www.chicagoparent.com/staff", "chicagoparent"),
                Pair("http://www.chickensoup.com/story-submissions/story-guidelines", "chickensoup"),
                Pair("http://www.chiltern-silicon-valley.co.uk/", "chiltern-silicon-valley.co"),
                Pair("https://cultofcopy.com/about/", "cultofcopy"),
                Pair("https://davecool.ca/contact", "davecool"),
                Pair("http://www.dawsons.co.uk/", "dawsons.co"),
                Pair("https://www.reddit.com/user/dietdrpoeker", "reddit"),
                Pair("http://www.gear4music.com/", "gear4music"),
                Pair("http://www.guitarcenter.com/", "guitarcenter"),
                Pair("http://www.millionpersonproject.org/", "millionpersonproject"),
                Pair("https://hubstaff.com/", "hubstaff"),
                Pair("https://www.imarc.com/contact", "imarc"),
                Pair("http://www.indiebible.com/aboutus.html", "indiebible"),
                Pair("http://www.collaboratemarketing.com/james_cherkoff/", "collaboratemarketing"),
                Pair("https://www.jasonmcdonald.org/about/contact/", "jasonmcdonald"),
                Pair("http://katandmouse.com/contact/", "katandmouse"),
                Pair("http://www.keymusic.com/", "keymusic"),
                Pair("http://tinybuddha.com/about/", "tinybuddha"),
                Pair("http://swiped.co/", "swiped"),
                Pair("http://www.mr-seo.com/", "mr-seo"),
                Pair("musiciansfriend.com", "musiciansfriend"),
                Pair("https://bayareaseomarketing.com/", "bayareaseomarketing"),
                Pair("http://opensourcemarketingproject.org/", "opensourcemarketingproject"),
                Pair("re-compose.com", "re-compose"),
                Pair("http://www.seodesmoineseo.com/contact-us/", "seodesmoineseo"),
                Pair("http://www.seopro.pro/about-the-seo-pros/", "seopro"),
                Pair("http://socialmarketway.com/contact/", "socialmarketway"),
                Pair("https://online-sales-marketing.com/contact-us/", "online-sales-marketing"),
                Pair("https://coveragebook.com/resolution/book", "coveragebook"),
                Pair("http://www.contentfac.com/contact/", "contentfac"),
                Pair("http://thedreamersweb.com/contact-us/", "thedreamersweb"),
                Pair("http://uptrending.com/contact/", "uptrending"),
                Pair("Uscreen.tv", "uscreen"),
                Pair("http://visiblylocal.com/contact-us/", "visiblylocal"),
                Pair("zzounds.com", "zzounds"),
                Pair("https://www.reddit.com/user/__Julia", "reddit")
        )
        data.forEach { x -> extractHomepageUrlTestLogic(x.first, x.second) }
    }
    @Test
    fun initCallsCreateDefaultClient() {
        // Prepare
        val sut = spy(CapsuleCrmSubsystem())
        val httpClient = mock<CloseableHttpClient>()
        doReturn(httpClient).`when`(sut).createDefaultHttpClient()

        // Run method under test
        sut.init()

        // Verify
        verify(sut).createDefaultHttpClient()
        assertThat(sut.httpClient).isSameAs(httpClient)
    }
    @Test
    fun closeClosesHttpClient() {
        // Prepare
        val sut = spy(CapsuleCrmSubsystem())
        val httpClient = mock<CloseableHttpClient>()
        doReturn(httpClient).`when`(sut).createDefaultHttpClient()
        sut.init()
        assertThat(sut.httpClient).isSameAs(httpClient)

        // Run method under test
        sut.close()

        // Verify
        verify(httpClient).close()
    }

    @Test
    @Ignore // For manual testing only
    fun createCompanyProperManualTest() {
        // Prepare
        val sut = CapsuleCrmSubsystem()
        val data = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.CONTACT_FORM,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        // Run method under test
        // sut.listCustomFields()
        // sut.listTags()
        sut.init()
        val client = sut.httpClient
        if (client == null) {
            return
        }
        sut.createCompanyProper(data, client)

        // Verify
    }
    @Test
    fun createCompanyProperSunnyDay() {
        createCompanyProperTestLogic(true, times(1), 1347L, "note test")
    }
    @Test
    fun createCompanyProperDoesntAddNoteIfCompanyCreationFailed() {
        createCompanyProperTestLogic(false, never(), 1347L, "note test")
    }
    @Test
    fun createCompanyProperDoesntAddNoteIfCompIdNull() {
        createCompanyProperTestLogic(false, never(), null, "note test")
    }
    @Test
    fun createCompanyProperDoesntAddNoteIfNoteBlank() {
        createCompanyProperTestLogic(false, never(), 1347L, "")
    }
    @Test
    fun addNoteSunnyDay() {
        addNoteTestLogic(201, true, "", never(), false, never())
    }
    @Test
    fun addNoteWrongResponseCode() {
        addNoteTestLogic(200, false, "Wrong status code (CRM interaction)", times(1), false, never())
        addNoteTestLogic(202, false, "Wrong status code (CRM interaction)", times(1), false, never())
    }
    @Test
    fun addNoteException() {
        addNoteTestLogic(201, false, "Database error", never(), true, times(1))
    }
    @Test
    fun composeAddNoteRequest() {
        // Prepare
        val logger = mock<Logger>()
        val protocol = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger, protocol))
        val req = mock<HttpPost>()
        doReturn(req).`when`(sut).createHttpPost(CapsuleCrmSubsystem.AddNoteUrl)
        val text = "text"
        val compId = 1314L
        val jsonTxt = "jsonTxt"
        doReturn(jsonTxt).`when`(sut).composeAddNoteJson(compId, text)
        val entity = mock<StringEntity>()
        doReturn(entity).`when`(sut).createStringEntity(jsonTxt)
        val inOrder = inOrder(logger, protocol, sut, entity, req, entity)

        // Run method under test
        val actRes = sut.composeAddNoteRequest(text, compId)

        // Verify
        inOrder.verify(sut).createHttpPost(CapsuleCrmSubsystem.AddNoteUrl)
        inOrder.verify(req).setHeader("Authorization", "Bearer ${CapsuleCrmSubsystem.ApiToken}")
        inOrder.verify(req).setHeader("Accept", "application/json")
        inOrder.verify(req).setHeader("Content-Type", "application/json")
        inOrder.verify(sut).composeAddNoteJson(compId, text)
        inOrder.verify(protocol).info("addNote(note='$text', compId=$compId): $jsonTxt")
        inOrder.verify(logger).info("addNote(note='$text', compId=$compId): $jsonTxt")
        inOrder.verify(sut).createStringEntity(jsonTxt)
        inOrder.verify(req).setEntity(entity)
        assertThat(actRes).isSameAs(req)
    }
    @Test
    fun composeAddNoteJson() {
        // Prepare
        val sut = CapsuleCrmSubsystem()
        val compId = 1802L
        val text = "text"

        // Run method under test
        val actRes = sut.composeAddNoteJson(compId, text)

        // Verify
        val expRes = readFile("CapsuleCrmSubsystem.composeAddNoteJson.json")
        assertThat(actRes).isEqualTo(expRes)
    }
    @Test
    fun createCompanyInCapsuleWrongResponseCode() {
        createCompanyInCapsuleTestLogic(
                200,
                "createCompanyProper(compData=''): Wrong status code 200",
                times(1),
                false,
                "Wrong status code",
                never(), null
        )
        createCompanyInCapsuleTestLogic(
                202,
                "createCompanyProper(compData=''): Wrong status code 200",
                times(1),
                false,
                "Wrong status code",
                never(), null
        )
    }
    @Test
    fun createCompanyInCapsuleExceptionInClientExecute() {
        createCompanyInCapsuleTestLogic(
                201,
                "createCompanyProper(compData='Bp1CompanyData(url=http://altruix.cc-test7, ctype=CONTACT_FORM, email=dp@altruix.co, contactFormUrl=http://altruix.cc-cf, note=note test, agent=dp118m)')",
                times(1),
                false,
                "Database error",
                never(),
                RuntimeException("Oy!")
        )
    }
    @Test
    fun createCompanyInCapsuleSunnyDay() {
        createCompanyInCapsuleTestLogic(
                201,
                "",
                times(0),
                true,
                "",
                times(1),
                null
        )
    }
    @Test
    fun extractCompanyId() {
        // Prepare
        val sut = CapsuleCrmSubsystem()
        val res = mock<CloseableHttpResponse>()
        val id = 1406L
        val json = JsonObject(
                mapOf(
                        "party" to JSONObject(
                                mapOf(
                                        "id" to id
                                )
                        )
                )
        )
        `when`(res.entity).thenReturn(StringEntity(json.toJsonString()))

        // Run method under test
        val actRes = sut.extractCompanyId(res)

        // Verify
        assertThat(actRes).isEqualTo(id)
    }
    @Test
    fun createCreateCompanyRequest() {
        // Prepare
        val logger = mock<Logger>()
        val protocol = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger, protocol))
        val data = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.CONTACT_FORM,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        val req = mock<HttpPost>()
        doReturn(req).`when`(sut).createHttpPost(CapsuleCrmSubsystem.CreateCompanyUrl)
        val jsonTxt = "jsonTxt"
        doReturn(jsonTxt).`when`(sut).createCreateCompanyJson(data)
        val entity = mock<StringEntity>()
        doReturn(entity).`when`(sut).createStringEntity(jsonTxt)

        // Run method under test
        val actRes = sut.createCreateCompanyRequest(data)

        // Verify
        verify(sut).createHttpPost(CapsuleCrmSubsystem.CreateCompanyUrl)
        verify(req).setHeader("Authorization", "Bearer ${CapsuleCrmSubsystem.ApiToken}")
        verify(req).setHeader("Accept", "application/json")
        verify(req).setHeader("Content-Type", "application/json")
        verify(sut).createCreateCompanyJson(data)
        verify(protocol).info("createCreateCompanyRequest(${data.toString()}): $jsonTxt")
        verify(logger).info("createCreateCompanyRequest(${data.toString()}): $jsonTxt")
        verify(req).setEntity(entity)
        assertThat(actRes).isSameAs(req)
    }
    @Test
    fun createCreateCompanyJson() {
        val data1 = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.CONTACT_FORM,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        val data2 = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.EMAIL,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        createCreateCompanyJsonTestLogic(
                data1,
                "CapsuleCrmSubsystem.createCreateCompanyJson.1.json"
        )
        createCreateCompanyJsonTestLogic(
                data2,
                "CapsuleCrmSubsystem.createCreateCompanyJson.2.json"
        )
    }
    @Test
    fun createStringEntity() {
        // Prepare
        val sut = CapsuleCrmSubsystem()
        val jsonTxt = "jsonTxt"

        // Run method under test
        val actRes = sut.createStringEntity(jsonTxt)

        // Verify
        assertThat(actRes.contentType.toString()).isEqualTo("Content-Type: application/json; charset=UTF-8")
        assertThat(IOUtils.toString(actRes.content, "UTF-8")).isEqualTo(jsonTxt)
    }
    @Test
    fun composeFieldDefinition() {
        // Prepare
        val sut = CapsuleCrmSubsystem()
        val id = 1446

        // Run method under test
        val actRes = sut.composeFieldDefinition(id)

        // Verify
        assertThat(actRes["id"]).isEqualTo(id)
    }
    @Test
    fun composeWebSitesJson() {
        val data1 = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.CONTACT_FORM,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        val data2 = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.EMAIL,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        val data3 = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.UNKNOWN,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        val inputsAndExpResults:List<Pair<Bp1CompanyData,String>> = listOf(
                Pair(data1, "CapsuleCrmSubsystem.composeWebSitesJson.1.json"),
                Pair(data2, "CapsuleCrmSubsystem.composeWebSitesJson.2.json"),
                Pair(data3, "CapsuleCrmSubsystem.composeWebSitesJson.3.json")
        )
        inputsAndExpResults.forEach { x ->
            val input = x.first
            val expResFilename = x.second
            composeWebSitesJsonTestLogic(input, expResFilename)
        }
    }
    @Test
    fun urlToJson() {
        // Prepare
        val sut = CapsuleCrmSubsystem()
        val url = "http://altruix.cc"

        // Run method under test
        val actRes = sut.urlToJson(url)

        // Verify
        assertThat(actRes.toString()).isEqualTo(readFile("CapsuleCrmSubsystem.urlToJson.json"))
    }
    @Test
    fun createEmailAddressJson() {
        // Prepare
        val sut = CapsuleCrmSubsystem()
        val email = "dp@altruix.co"

        // Run method under test
        val actRes = sut.createEmailAddressJson(email)

        // Verify
        assertThat(actRes.toString()).isEqualTo(readFile("CapsuleCrmSubsystem.createEmailAddressJson.json"))
    }
    @Test
    fun companyExistsSunnyDay() {
        companyExistsTestLogic(true, emptyList(), true, "", false)
        companyExistsTestLogic(true, listOf(Party(1L, emptyList())), true, "", true)
    }

    @Test
    fun companyExistsRainyDay() {
        companyExistsTestLogic(false, emptyList(), false, "Database error", null)
    }
    @Test
    fun composeFetchBp2CompanyDataRequest() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val companyId = "18032017"

        // Run method under test
        val actRes = sut.composeFetchBp2CompanyDataRequest(companyId)

        // Verify
        assertThat(actRes).isNotNull
        assertThat(actRes.uri.toString()).isEqualTo("https://api.capsulecrm.com/api/v2/parties/$companyId")
        assertThat(actRes.getFirstHeader("Accept").value).isEqualTo("application/json")
        assertThat(actRes.getFirstHeader("Authorization").value).isEqualTo("Bearer ${CapsuleCrmSubsystem.ApiToken}")
    }
    @Test
    fun fetchBp2CompanyDataNullResponse() {
        // Prepare
        val client = mock<CloseableHttpClient>()
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        doReturn(client).`when`(sut).createDefaultHttpClient()
        val companyId = "1635"
        val req = mock<HttpUriRequest>()
        doReturn(req).`when`(sut).composeFetchBp2CompanyDataRequest(companyId)
        val res = null
        `when`(client.execute(req)).thenReturn(res)

        val inOrder = inOrder(client, logger, sut, req)

        // Run method under test
        sut.init()
        val actRes = sut.fetchBp2CompanyData(companyId)

        // Verify
        inOrder.verify(sut).createDefaultHttpClient()
        inOrder.verify(sut).composeFetchBp2CompanyDataRequest(companyId)
        inOrder.verify(client).execute(req)
        inOrder.verify(logger).error("fetchBp2CompanyData(companyId='$companyId'): Null response")
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Null response")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun fetchBp2CompanyDataWrongStatusCode() {
        // Prepare
        val client = mock<CloseableHttpClient>()
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        doReturn(client).`when`(sut).createDefaultHttpClient()
        val companyId = "1635"
        val req = mock<HttpUriRequest>()
        doReturn(req).`when`(sut).composeFetchBp2CompanyDataRequest(companyId)
        val res = mock<CloseableHttpResponse>()
        `when`(client.execute(req)).thenReturn(res)
        val statusLine = mock<StatusLine>()
        `when`(res.statusLine).thenReturn(statusLine)
        `when`(statusLine.statusCode).thenReturn(404)

        val inOrder = inOrder(client, logger, sut, req, res, statusLine)

        // Run method under test
        sut.init()
        val actRes = sut.fetchBp2CompanyData(companyId)

        // Verify
        inOrder.verify(sut).createDefaultHttpClient()
        inOrder.verify(sut).composeFetchBp2CompanyDataRequest(companyId)
        inOrder.verify(client).execute(req)
        inOrder.verify(res).statusLine
        inOrder.verify(statusLine).statusCode
        inOrder.verify(logger).error("fetchBp2CompanyData(companyId='$companyId'): Wrong status code 404")
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Wrong status code")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun fetchBp2CompanyDataNullEntity() {
        // Prepare
        val client = mock<CloseableHttpClient>()
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        doReturn(client).`when`(sut).createDefaultHttpClient()
        val companyId = "1635"
        val req = mock<HttpUriRequest>()
        doReturn(req).`when`(sut).composeFetchBp2CompanyDataRequest(companyId)
        val res = mock<CloseableHttpResponse>()
        `when`(client.execute(req)).thenReturn(res)
        val statusLine = mock<StatusLine>()
        `when`(res.statusLine).thenReturn(statusLine)
        `when`(statusLine.statusCode).thenReturn(200)
        `when`(res.entity).thenReturn(null)

        val inOrder = inOrder(client, logger, sut, req, res, statusLine)

        // Run method under test
        sut.init()
        val actRes = sut.fetchBp2CompanyData(companyId)

        // Verify
        inOrder.verify(sut).createDefaultHttpClient()
        inOrder.verify(sut).composeFetchBp2CompanyDataRequest(companyId)
        inOrder.verify(client).execute(req)
        inOrder.verify(res).statusLine
        inOrder.verify(statusLine).statusCode
        inOrder.verify(res).entity
        inOrder.verify(logger).error("fetchBp2CompanyData(companyId='$companyId'): Null entity")
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Null entity")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun fetchBp2CompanyDataNullContent() {
        // Prepare
        val client = mock<CloseableHttpClient>()
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        doReturn(client).`when`(sut).createDefaultHttpClient()
        val companyId = "1635"
        val req = mock<HttpUriRequest>()
        doReturn(req).`when`(sut).composeFetchBp2CompanyDataRequest(companyId)
        val res = mock<CloseableHttpResponse>()
        `when`(client.execute(req)).thenReturn(res)
        val statusLine = mock<StatusLine>()
        `when`(res.statusLine).thenReturn(statusLine)
        `when`(statusLine.statusCode).thenReturn(200)
        val entity = mock<HttpEntity>()
        `when`(res.entity).thenReturn(entity)
        val content = null
        `when`(entity.content).thenReturn(content)

        val inOrder = inOrder(
                client,
                logger,
                sut,
                req,
                res,
                statusLine,
                entity
        )

        // Run method under test
        sut.init()
        val actRes = sut.fetchBp2CompanyData(companyId)

        // Verify
        inOrder.verify(sut).createDefaultHttpClient()
        inOrder.verify(sut).composeFetchBp2CompanyDataRequest(companyId)
        inOrder.verify(client).execute(req)
        inOrder.verify(res).statusLine
        inOrder.verify(statusLine).statusCode
        inOrder.verify(res, times(2)).entity
        inOrder.verify(entity).content
        inOrder.verify(logger).error("fetchBp2CompanyData(companyId='$companyId'): Null content")
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Null content")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun fetchBp2CompanyDataSunnyDay() {
        // Prepare
        val client = mock<CloseableHttpClient>()
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        doReturn(client).`when`(sut).createDefaultHttpClient()
        val companyId = "1635"
        val req = mock<HttpUriRequest>()
        doReturn(req).`when`(sut).composeFetchBp2CompanyDataRequest(companyId)
        val res = mock<CloseableHttpResponse>()
        `when`(client.execute(req)).thenReturn(res)
        val statusLine = mock<StatusLine>()
        `when`(res.statusLine).thenReturn(statusLine)
        `when`(statusLine.statusCode).thenReturn(200)
        val entity = mock<HttpEntity>()
        `when`(res.entity).thenReturn(entity)
        val content = mock<InputStream>()
        `when`(entity.content).thenReturn(content)
        val parser = mock<TestableJsonParser>()
        doReturn(parser).`when`(sut).createJsonParser()
        val json = createJsonObject("CapsuleCrmSubsystem.fetchBp2CompanyDataSunnyDay.json")
        val party = json["party"] as JsonObject
        `when`(parser.parse(content)).thenReturn(json)
        val expRes = FailableOperationResult<Bp2CompanyData>(true, "",
                Bp2CompanyData(
                        companyId,
                        emptyList(),
                        emptyList()
                )
        )
        doReturn(expRes).`when`(sut).createFetchBp2CompanyData(party, companyId)

        val inOrder = inOrder(
                client,
                logger,
                sut,
                req,
                res,
                statusLine,
                entity,
                content,
                parser
        )

        // Run method under test
        sut.init()
        val actRes = sut.fetchBp2CompanyData(companyId)

        // Verify
        inOrder.verify(sut).createDefaultHttpClient()
        inOrder.verify(sut).composeFetchBp2CompanyDataRequest(companyId)
        inOrder.verify(client).execute(req)
        inOrder.verify(res).statusLine
        inOrder.verify(statusLine).statusCode
        inOrder.verify(res, times(2)).entity
        inOrder.verify(entity).content
        inOrder.verify(sut).createJsonParser()
        inOrder.verify(parser).parse(content)
        inOrder.verify(sut).createFetchBp2CompanyData(party, companyId)
        assertThat(actRes).isSameAs(expRes)
    }

    private fun createJsonObject(path: String): JsonObject {
        val jsonTxt = readFile(path)
        val content = IOUtils.toInputStream(jsonTxt, "UTF-8")
        val parser = KlaxonParserWrapper()
        return parser.parse(content)
    }

    @Test
    fun fetchBp2CompanyDataException() {
        // Prepare
        val client = mock<CloseableHttpClient>()
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        doReturn(client).`when`(sut).createDefaultHttpClient()
        val companyId = "1635"
        val req = mock<HttpUriRequest>()
        doReturn(req).`when`(sut).composeFetchBp2CompanyDataRequest(companyId)
        val res = mock<CloseableHttpResponse>()
        `when`(client.execute(req)).thenReturn(res)
        val statusLine = mock<StatusLine>()
        `when`(res.statusLine).thenReturn(statusLine)
        `when`(statusLine.statusCode).thenReturn(200)
        val entity = mock<HttpEntity>()
        `when`(res.entity).thenReturn(entity)
        val content = mock<InputStream>()
        `when`(entity.content).thenReturn(content)
        val parser = mock<TestableJsonParser>()
        doReturn(parser).`when`(sut).createJsonParser()
        val json = createJsonObject("CapsuleCrmSubsystem.fetchBp2CompanyDataException.json")
        `when`(parser.parse(content)).thenReturn(json)
        val msg = "msg"
        val throwable = RuntimeException(msg)
        doThrow(throwable).`when`(sut).createFetchBp2CompanyData(
                json["party"] as JsonObject,
                companyId
        )
        val errMsg = "errMsg"
        doReturn(errMsg).`when`(sut).composeErrorMsg(msg)

        val inOrder = inOrder(
                client,
                logger,
                sut,
                req,
                res,
                statusLine,
                entity,
                content,
                parser
        )

        // Run method under test
        sut.init()
        val actRes = sut.fetchBp2CompanyData(companyId)

        // Verify
        inOrder.verify(sut).createDefaultHttpClient()
        inOrder.verify(sut).composeFetchBp2CompanyDataRequest(companyId)
        inOrder.verify(client).execute(req)
        inOrder.verify(res).statusLine
        inOrder.verify(statusLine).statusCode
        inOrder.verify(res, times(2)).entity
        inOrder.verify(entity).content
        inOrder.verify(sut).createJsonParser()
        inOrder.verify(parser).parse(content)
        inOrder.verify(logger).error("fetchBp2CompanyData(companyId='$companyId')", throwable)
        inOrder.verify(sut).composeErrorMsg(msg)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(errMsg)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun createFetchBp2CompanyDataSunnyDay() {
        // Prepare
        val jsonTxt = readFile("CapsuleCrmSubsystem.createFetchBp2CompanyData.json")
        val sut = spy(CapsuleCrmSubsystem())
        val parser = sut.createJsonParser()
        val content = IOUtils.toInputStream(jsonTxt, "UTF-8")
        val json = parser.parse(content)
        val party = json["party"] as JsonObject
        assertThat(party).isNotNull
        val companyId = "1741"

        // Run method under test
        val actRes = sut.createFetchBp2CompanyData(party, companyId)

        // Verify
        assertThat(actRes.success).isTrue()
        assertThat(actRes.error).isEmpty()
        assertThat(actRes.result).isNotNull
        assertThat(actRes.result?.companyId).isEqualTo(companyId)
        assertThat(actRes.result?.webSites).isEqualTo(listOf(
                "https://geektimes.ru/post/287040/",
                "https://habrahabr.ru/post/323776/")
        )
        assertThat(actRes.result?.emails).isEqualTo(listOf(
                "scott@homestyleshop.co",
                "scotty@homestyleshop.co")
        )
    }
    @Test
    fun createFetchBp2CompanyDataNoWebSitesNoEmails() {
        // Prepare
        val sut = spy(CapsuleCrmSubsystem())
        val party = createJsonObject("CapsuleCrmSubsystem.createFetchBp2CompanyDataNoWebSitesNoEmails.json")
        doReturn(emptyList<String>()).`when`(sut).extractEmails(party)
        doReturn(emptyList<String>()).`when`(sut).extractWebSites(party)
        val companyId = "1842"

        // Run method under test
        val actRes = sut.createFetchBp2CompanyData(party, companyId)

        // Verify
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo("Neither web sites nor e-mails found for company $companyId")
        assertThat(actRes.result).isNull()
    }
    @Test
    fun attachContactResultBlankCompanyId() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val companyId = ""
        val persona = "DP"
        val contactTextNote = "I sent this message today: Bla-bla."
        val logMsg = "logMsg"

        doReturn(logMsg).`when`(sut).composeAttachContactResultLogMessage(
                CapsuleCrmSubsystem.CompanyIdBlankMessage,
                companyId,
                contactTextNote,
                persona
        )

        val inOrder = inOrder(logger, sut)

        // Run method under test
        val actRes = sut.attachContactResult(companyId, persona, contactTextNote)

        // Verify
        inOrder.verify(sut).composeAttachContactResultLogMessage(
                CapsuleCrmSubsystem.CompanyIdBlankMessage,
                companyId,
                contactTextNote,
                persona
        )
        inOrder.verify(logger).error(logMsg)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(CapsuleCrmSubsystem.CompanyIdBlankMessage)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun attachContactResultNonNumericCompanyId() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val companyId = "abc"
        val persona = "DP"
        val contactTextNote = "I sent this message today: Bla-bla."
        val logMsg = "logMsg"

        doReturn(logMsg).`when`(sut).composeAttachContactResultLogMessage(
                CapsuleCrmSubsystem.CompanyIdNonNumeric,
                companyId,
                contactTextNote,
                persona
        )

        val inOrder = inOrder(logger, sut)

        // Run method under test
        val actRes = sut.attachContactResult(companyId, persona, contactTextNote)

        // Verify
        inOrder.verify(sut).composeAttachContactResultLogMessage(
                CapsuleCrmSubsystem.CompanyIdNonNumeric,
                companyId,
                contactTextNote,
                persona
        )
        inOrder.verify(logger).error(logMsg)
        assertThat(actRes.success).isFalse()
        assertThat(actRes.error).isEqualTo(CapsuleCrmSubsystem.CompanyIdNonNumeric)
        assertThat(actRes.result).isNull()
    }
    @Test
    fun attachContactResultSunnyDay() {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val numericCompanyId = 1614L
        val companyId = numericCompanyId.toString()
        val persona = "DP"
        val contactTextNote = "I sent this message today: Bla-bla."
        val txt = "txt"
        doReturn(txt).`when`(sut).composeContactResultNote(persona, contactTextNote)
        val expRes = ValidationResult(true, "")
        doReturn(expRes).`when`(sut).addNote(txt, numericCompanyId)
        val inOrder = inOrder(logger, sut)

        // Run method under test
        val actRes = sut.attachContactResult(companyId, persona, contactTextNote)

        // Verify
        inOrder.verify(sut).composeContactResultNote(persona, contactTextNote)
        inOrder.verify(sut).addNote(txt, numericCompanyId)
        assertThat(actRes).isSameAs(expRes)
    }
    @Test
    fun composeAttachContactResultLogMessage() {
        // Prepare
        val sut = spy(CapsuleCrmSubsystem())
        val numericCompanyId = 1614L
        val companyId = numericCompanyId.toString()
        val persona = "DP"
        val contactTextNote = "I sent this message today: Bla-bla."
        val msg = CapsuleCrmSubsystem.CompanyIdBlankMessage

        // Run method under test
        val actRes = sut.composeAttachContactResultLogMessage(
                msg, companyId, contactTextNote, persona
        )

        // Verify
        assertThat(actRes).isEqualTo("attachContactResult(companyId='1614', persona='DP', contactTextNote='I sent this message today: Bla-bla.', contactTextNote='I sent this message today: Bla-bla.'): Company ID is blank")
    }
    @Test
    fun composeContactResultNote() {
        // Prepare
        val sut = spy(CapsuleCrmSubsystem())
        val persona = "DP"
        val contactTextNote = "Hi, this is my contact message."
        val now = ZonedDateTime.of(2017, 3, 19, 16, 45, 30, 0, ZoneId.of("Europe/Moscow"))
        doReturn(now).`when`(sut).now()

        // Run method under test
        val actRes = sut.composeContactResultNote(persona, contactTextNote)

        // Verify
        val expRes = readFile("CapsuleCrmSubsystem.composeContactResultNote.txt")
        assertThat(actRes).isEqualTo(expRes)
    }
    private fun companyExistsTestLogic(
            searchResSuccess: Boolean,
            searchResParties: List<Party>,
            expResSuccess: Boolean,
            error: String,
            expRes: Boolean?
    ) {
        // Prepare
        val sut = spy(CapsuleCrmSubsystem())
        val url = "http://altruix.cc-test7"
        val data = Bp1CompanyData(
                url,
                ContactDataType.CONTACT_FORM,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        val searchItem = "searchItem"
        doReturn(searchItem).`when`(sut).extractHomepageUrl(url)
        val searchRes = PartiesSearchResult(searchResSuccess, "", searchResParties)
        doReturn(searchRes).`when`(sut).findPartiesByUrlFragment(searchItem)

        val inOrder = inOrder(sut)

        // Run method under test
        val actRes = sut.companyExists(data)

        // Verify
        inOrder.verify(sut).extractHomepageUrl(url)
        inOrder.verify(sut).findPartiesByUrlFragment(searchItem)
        assertThat(actRes.success).isEqualTo(expResSuccess)
        assertThat(actRes.error).isEqualTo(error)
        assertThat(actRes.result).isEqualTo(expRes)
    }

    private fun composeWebSitesJsonTestLogic(
            data: Bp1CompanyData,
            expResFilename: String
    ) {
        // Prepare
        val sut = CapsuleCrmSubsystem()

        // Run method under test
        val actRes = sut.composeWebSitesJson(data)

        // Verify
        val actResTxt = actRes.toString()
        val expRes = readFile(expResFilename)
        assertThat(actResTxt).isEqualTo(expRes)
    }

    private fun createCreateCompanyJsonTestLogic(
            input: Bp1CompanyData,
            expResFile: String
    ) {
        // Prepare
        val sut = CapsuleCrmSubsystem()

        // Run method under test
        val actRes = sut.createCreateCompanyJson(input)

        // Verify
        val expRes = readFile(expResFile)
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun createCompanyInCapsuleTestLogic(
            statusCode: Int,
            loggerError: String,
            loggerErrorInvokations: VerificationMode,
            expSuccess: Boolean,
            expError: String,
            extractCompanyIdInvokations: VerificationMode,
            exception: Throwable?) {
        // Prepare
        val logger = mock<Logger>()
        val protocol = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger, protocol))
        val client = mock<CloseableHttpClient>()
        val compData = Bp1CompanyData(
                "http://altruix.cc-test7",
                ContactDataType.CONTACT_FORM,
                "dp@altruix.co",
                "http://altruix.cc-cf",
                "note test",
                "dp118m"
        )
        val req = mock<HttpUriRequest>()
        doReturn(req).`when`(sut).createCreateCompanyRequest(compData)
        val res = mock<CloseableHttpResponse>()
        if (exception != null) {
            `when`(client.execute(req)).thenThrow(exception)
        } else {
            `when`(client.execute(req)).thenReturn(res)
        }
        val statusLine = mock<StatusLine>()
        `when`(res.statusLine).thenReturn(statusLine)
        `when`(statusLine.statusCode).thenReturn(statusCode)
        val inOrder = inOrder(logger, protocol, sut, client, statusLine)
        val companyId = 1353L
        doReturn(companyId).`when`(sut).extractCompanyId(res)

        // Run method under test
        val actRes = sut.createCompanyInCapsule(client, compData)

        // Verify
        inOrder.verify(sut).createCreateCompanyRequest(compData)
        inOrder.verify(client).execute(req)
        if (statusCode != 201) {
            inOrder.verify(statusLine, times(2)).statusCode
        }
        if (exception != null) {
            inOrder.verify(logger, loggerErrorInvokations).error(loggerError, exception)
        } else {
            inOrder.verify(logger, never()).error(loggerError, exception)
        }
        inOrder.verify(sut, extractCompanyIdInvokations).extractCompanyId(res)
        if (exception != null) {
            verify(sut).close(null as CloseableHttpResponse?)
        } else {
            verify(sut).close(res)
        }
        verify(sut, never()).close(client)
        assertThat(actRes.success).isEqualTo(expSuccess)
        assertThat(actRes.error).isEqualToIgnoringCase(expError)
    }

    private fun readFile(file: String) = IOUtils.toString(javaClass.classLoader.getResourceAsStream("cc/altruix/is1/capsule/" + file), "UTF-8")
    private fun addNoteTestLogic(
            statusCode: Int,
            expSuccess: Boolean,
            expError: String,
            errorLogsWrongStatusCode: VerificationMode, throwException: Boolean, exceptionLogs: VerificationMode
    ) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))
        val note = "note"
        val compId = 1355L
        val req = mock<HttpUriRequest>()
        doReturn(req).`when`(sut).composeAddNoteRequest(note, compId)
        val client = mock<CloseableHttpClient>()
        doReturn(client).`when`(sut).createDefaultHttpClient()
        val res = mock<CloseableHttpResponse>()
        val throwable = RuntimeException("simulated error")
        if (throwException) {
            `when`(client.execute(req)).thenThrow(throwable)
        } else {
            `when`(client.execute(req)).thenReturn(res)
        }
        val statusLine = mock<StatusLine>()
        `when`(res.statusLine).thenReturn(statusLine)
        `when`(statusLine.statusCode).thenReturn(statusCode)

        sut.init()
        val inOrder = inOrder(sut, req, client, res, logger)

        // Run method under test
        val actRes = sut.addNote(note, compId)

        // Verify
        inOrder.verify(sut).composeAddNoteRequest(note, compId)
        inOrder.verify(client).execute(req)
        inOrder.verify(logger, errorLogsWrongStatusCode).error("addNote(note='$note', compId=$compId): Wrong status code $statusCode")
        inOrder.verify(logger, exceptionLogs).error("addNote(note='$note', compId=$compId)", throwable)
        if (throwException) {
            verify(sut).close(null as CloseableHttpResponse?)
        } else {
            verify(sut).close(res)
        }
        inOrder.verify(client, never()).close()
        inOrder.verify(sut, never()).close(client)
        assertThat(actRes.success).isEqualTo(expSuccess)
        assertThat(actRes.error).isEqualTo(expError)
    }

    private fun createCompanyProperTestLogic(
            createCompanySuccess: Boolean,
            addNoteInvocations: VerificationMode, compId: Long?, note: String) {
        // Prepare
        val sut = spy(CapsuleCrmSubsystem())
        val url = "http://altruix.cc-test7"
        val ctype = ContactDataType.CONTACT_FORM
        val email = "dp@altruix.co"
        val contactFormUrl = "http://altruix.cc-cf"
        val agent = "dp118m"
        val compData = Bp1CompanyData(url,
                ctype,
                email,
                contactFormUrl,
                note,
                agent)
        val client = mock<CloseableHttpClient>()
        val createCompanyRes = FailableOperationResult<Long>(createCompanySuccess, "", compId)
        doReturn(createCompanyRes).`when`(sut).createCompanyInCapsule(client, compData)
        val addNoteRes = ValidationResult(true, "")
        if (compId != null) {
            doReturn(addNoteRes).`when`(sut).addNote(note, compId)
        }

        val inOrder = inOrder(sut, client)

        // Run method under test
        val actRes = sut.createCompanyProper(compData, client)

        // Verify
        inOrder.verify(sut).createCompanyInCapsule(client, compData)
        if (createCompanySuccess && (compId != null) && StringUtils.isNotBlank(note)) {
            inOrder.verify(sut, addNoteInvocations).addNote(note, compId)
            assertThat(actRes).isSameAs(addNoteRes)
        } else if ((compId != null) && createCompanySuccess) {
            assertThat(actRes.success).isEqualTo(true)
            assertThat(actRes.error).isEqualTo("")
        } else {
            assertThat(actRes.success).isEqualTo(false)
            assertThat(actRes.error).isEqualTo("Couldn't create company")
        }
    }

    private fun extractHomepageUrlTestLogic(input: String, expRes: String) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))

        // Run method under test
        val actRes = sut.extractHomepageUrl(input)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }

    private fun composeErrorMsgTestLogic(input: String?, expectedResult: String) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))

        // Run method under test
        val actRes = sut.composeErrorMsg(input)

        // Verify
        assertThat(actRes).isEqualTo(expectedResult)
    }

    private fun defaultValueTestLogic(input: Long?, expectedResult: Long) {
        // Prepare
        val logger = mock<Logger>()
        val sut = spy(CapsuleCrmSubsystem(logger))

        // Run method under test
        val actRes = sut.defaultValue(input, -1)

        // Verify
        assertThat(actRes).isEqualTo(expectedResult)
    }

    private fun  createJsonUrl(url: String?): JsonObject {
        val res = JsonObject()
        res["url"] = url
        return res
    }
}