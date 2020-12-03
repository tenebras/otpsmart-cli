package com.github.tenebras.otpclient.ext

import java.math.BigInteger
import java.security.MessageDigest

fun String.toMD5String(): String {
    val md = MessageDigest.getInstance("MD5")

    return BigInteger(1, md.digest(this.toByteArray(Charsets.UTF_16LE))).toString(16).padStart(32, '0')
}