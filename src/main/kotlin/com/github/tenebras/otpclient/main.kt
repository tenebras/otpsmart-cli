package com.github.tenebras.otpclient

import kotlinx.cli.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExperimentalCli
fun main(args: Array<String>) {
    val otpClient = OtpClient()

    val parser = ArgParser("otpsmart-cli")
    val sessionId by parser.option(
        ArgType.String,
        shortName = "s",
        description = "Existing sessionId to skip authorization flow"
    )
    val userId by parser.option(
        ArgType.String,
        shortName = "u",
        description = "OTPSmart contract id (login). Could be omitted if sessionId provided"
    )
    val password by parser.option(
        ArgType.String,
        shortName = "p",
        description = "OTPSmart raw password. Could be omitted if sessionId or passwordHash provided"
    )
    val passwordHash by parser.option(
        ArgType.String,
        shortName = "ph",
        description = "Password hashed with MD5 algorithm"
    )

    val isJson by parser.option(
        ArgType.Boolean,
        fullName = "json",
        shortName = "j",
        description = "Output as JSON"
    ).default(false)

    class Accounts : Subcommand("accounts", "List card accounts") {
        override fun execute() {
            otpClient.authenticate(sessionId, userId, passwordHash, password)

            val accounts = otpClient.accounts()

            val response = if (isJson) {
                Json { prettyPrint = true }.encodeToString(accounts).also {
                    val jsonFile = File("./accounts.json")
                    jsonFile.writeText(it)
                    println("JSON file stored: ${jsonFile.canonicalPath}")
                }

            } else {
                accounts.joinToString("\n") {
                    "${(it.iban ?: it.accountNo).padEnd(29, ' ')} ${it.maskedPan} ${it.balance} ${it.currencyCode}"
                }
            }

            println(response)
        }
    }

    class Statement : Subcommand("statement", "Fetch account statement") {
        val account by argument(ArgType.String, description = "IBAN or masked card number")
        val rawFrom by argument(ArgType.String, fullName = "from", description = "From dd.MM.YYYY")
        val rawTo by argument(ArgType.String, fullName = "to", description = "To dd.MM.YYYY")

        override fun execute() {
            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val from = LocalDate.parse(rawFrom, dateFormatter)
            val to = LocalDate.parse(rawTo, dateFormatter)

            otpClient.authenticate(sessionId, userId, passwordHash, password)
            val account = otpClient.accounts().first {
                it.iban?.contains(account) == true || it.maskedPan.contains(account) || it.accountNo.contains(account)
            }

            val xlsxFile = otpClient.statement(account, from, to);
            println("XLSX file stored: ${xlsxFile.canonicalPath}")

            if (isJson) {
                val parsed = XlsxParser().parse(xlsxFile)
                val json = Json { prettyPrint = true }

                val jsonFile = File("./${xlsxFile.nameWithoutExtension}.json")
                jsonFile.writeText(json.encodeToString(parsed))

                println("JSON file stored: ${jsonFile.canonicalPath}")
            }
        }
    }

    parser.subcommands(Accounts(), Statement())
    parser.parse(args)
}
