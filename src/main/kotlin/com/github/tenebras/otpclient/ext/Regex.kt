package com.github.tenebras.otpclient.ext

fun Regex.firstMatch(value: String): String? = find(value)?.groupValues?.get(1)