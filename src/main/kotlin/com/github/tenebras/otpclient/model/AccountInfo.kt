package com.github.tenebras.otpclient.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountInfo(
    @SerialName("ACCOUNTNO") val accountNo: String,
    @SerialName("ACCOUNTID") val accountId: Int,
    @SerialName("BRANCHID") val branchId: Int,
    @SerialName("expYYMM") val expiration: String,
    @SerialName("BCID") val bcid: String,//?
    @SerialName("RESERVED1") val reserved1: String,//?
    @SerialName("CONTRACTID") val contractId: Int,
    @SerialName("CURRENCYID") val currencyId: Int,
    @SerialName("SYMBOLCODE") val currencyCode: String,
    @SerialName("IBAN") val iban: String? = null,
    val CORRIDENTIFYCODE: String,//?
    @SerialName("AGREEMENTID") val agreementId: Int,
    @SerialName("CARDNO") val maskedPan: String,
    @SerialName("CONTRAGENTNAME") val contractor: String,
    @SerialName("SUMMANOW") val balance: String,
    @SerialName("SUMMAMAX") val maxBalance: String,//?
    @SerialName("TYPE") val type: String
)