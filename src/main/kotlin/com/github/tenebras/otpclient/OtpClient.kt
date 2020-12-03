package com.github.tenebras.otpclient

import com.github.tenebras.otpclient.ext.firstMatch
import com.github.tenebras.otpclient.ext.toMD5String
import com.github.tenebras.otpclient.ext.toStringAndConsume
import com.github.tenebras.otpclient.model.AccountInfo
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.random.Random

class OtpClient(apiBase: String = "https://otpsmart.com.ua/ifobsClientOtp") {

    private val httpClient = HttpClients.custom().disableRedirectHandling().build()
    private var sessionId: String? = null
    private val scriptSessionId by lazy {
        "701D7E54040D4B07F522B646C7A75D5B" + floor( Random.nextDouble( 0.0, 1.0 ) * 1000 )
    }
    private val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.67 Safari/537.36"

    private val url = object {
        val checkLoginForSms = "$apiBase/dwr-open/call/plaincall/DwrFacadeOpen.checkLoginForSmsOtp.dwr"
        val login = "$apiBase/Login.action"
        val accountStatements = "$apiBase/AccountStatementShow.action"
        val downloadStatement = "$apiBase/AccountStatementExecute.action"
    }

    fun authenticate(sessionId: String?, userId: String?, passwordHash: String?, password: String?) {
        if (sessionId != null) {
            this.sessionId = sessionId
        } else if (userId != null && (passwordHash != null || password != null)) {
            val hashedPassword = passwordHash ?: password!!.toMD5String()
            requestSMSToken(userId, hashedPassword)

            print("SMS token: ")

            login(userId, hashedPassword, readLine()!!)
        } else {
            println("Error: sessionId or login credentials should be provided")
            kotlin.system.exitProcess(1)
        }
    }

    fun accounts(): List<AccountInfo> {

        val request = HttpGet(url.accountStatements).apply {
            setHeader("User-Agent", userAgent)
            setHeader("Cookie", "JSESSIONID=$sessionId")
        }

        return httpClient.execute(request).use { response ->
            val body = response.entity.toStringAndConsume()
            val json = Json { ignoreUnknownKeys = true }

            json.decodeFromString(
                ListSerializer(AccountInfo.serializer()),
                Regex("var user_cards_map =(.+?);").firstMatch(body)!!
            )
        }
    }

    fun statement(account: AccountInfo, from: LocalDate, to: LocalDate): File {
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        val request = httpPostOf(url.downloadStatement, urlEncodedForm = mapOf(
            "accountid" to account.accountId.toString(),
            "period" to "8",
            "cardno" to account.maskedPan,
            "cardid" to account.bcid,
            "accountno" to account.accountNo,
            "iban" to (account.iban ?: ""),
            "type" to "card",
            "branchid" to account.branchId.toString(),
            "contractid" to account.contractId.toString(),
            "reserved1" to account.reserved1,
            "statementext" to "true",
            "statemendatesep" to "true",
            "statemendigsep" to "true",
            "statemenrevorder" to "true",
            "bookeddatesort" to "false",
            "doctype" to "1",
            "from" to dateFormatter.format(from),//"01.11.2020",
            "till" to dateFormatter.format(to),
            "pdfStatement" to "false",
            "xlsstatement" to "true"
        ))

        return httpClient.execute(request).use { response ->
            val fileName = Regex("filename=\"(.+)\"").firstMatch(response.getFirstHeader("Content-Disposition").toString())

            File("./$fileName").also {
                response.entity.writeTo(it.outputStream())

                EntityUtils.consume(response.entity)
            }
        }
    }

    private fun requestSMSToken(userId: String, passwordHash: String) {
        val sendSmsTokenRequest = httpPostOf(url.checkLoginForSms, urlEncodedForm = mapOf(
            "callCount" to "1",
            "page" to "/ifobsClientOtp/LoginForm.action",
            "httpSessionId" to "",
            "scriptSessionId" to scriptSessionId,
            "c0-scriptName" to "DwrFacadeOpen",
            "c0-methodName" to "checkLoginForSmsOtp",
            "c0-id" to "0",
            "c0-param0" to "string:$userId",
            "c0-param1" to "string:$passwordHash",
            "c0-param2" to "string:72",
            "batchId" to "1"
        ))

        httpClient.execute(sendSmsTokenRequest).use { response ->
            val body = response.entity.toStringAndConsume()

            if (!body.contains("""dwr.engine._remoteHandleCallback('1','0',3);""")) {
                throw RuntimeException("Can't fetch initial session for specified userId=$userId, body=$body")
            }

            val cookieHeader = response.getFirstHeader("Set-Cookie").toString()

            sessionId = Regex("JSESSIONID=([^;]+)").firstMatch(cookieHeader)!!

            println("sessionId=$sessionId")
        }
    }

    private fun login(userId: String, passwordHash: String, smsCode: String) {
        val loginRequest = httpPostOf(url.login, urlEncodedForm = mapOf(
            "user" to userId,
            "otpcode" to smsCode,
            "digipass" to "",
            "md5psw" to passwordHash,
            "loginOS" to "",
            "hostname" to "",
            "hostaddress" to "",
            "sms" to ""
        ))

        httpClient.execute(loginRequest).use { response ->
            //Location: https://otpsmart.com.ua/ifobsClientOtp/LoginForm.action?errorCode=login_error_login
            //Location: https://otpsmart.com.ua/ifobsClientOtp/MainAccountsShow.action
            val isSuccessfulRedirect = response.getFirstHeader("Location")?.toString()?.contains("MainAccountsShow") == true

            if (response.statusLine.statusCode != 302 || !isSuccessfulRedirect) {
                throw RuntimeException("Failed to authenticate")
            }
        }
    }

    private fun httpPostOf(url: String, urlEncodedForm: Map<String, String>): HttpPost {
        return HttpPost(url).apply {
            setHeader("User-Agent", userAgent)
            setHeader("Origin", "https://otpsmart.com.ua")
            setHeader("Referer", "https://otpsmart.com.ua/ifobsClientOtp/LoginForm.action")
            setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            setHeader("Accept-Language", "en-US,en;q=0.9,ru-RU;q=0.8,ru;q=0.7,uk;q=0.6,de;q=0.5")
            sessionId?.let{ setHeader("Cookie", "JSESSIONID=$sessionId") }

            entity = UrlEncodedFormEntity(urlEncodedForm.map { BasicNameValuePair(it.key, it.value) })
        }
    }
}