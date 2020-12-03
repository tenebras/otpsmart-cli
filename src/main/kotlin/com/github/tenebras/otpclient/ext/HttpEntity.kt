package com.github.tenebras.otpclient.ext

import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils

fun HttpEntity.toStringAndConsume(): String {
    return EntityUtils.toString(this).also {
        EntityUtils.consume(this)
    }
}