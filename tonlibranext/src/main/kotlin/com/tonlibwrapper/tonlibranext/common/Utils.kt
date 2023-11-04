package com.tonlibwrapper.tonlibranext.common

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


fun utcNow(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

fun utcLongNow() = utcNow().toEpochSecond(ZoneOffset.UTC)

fun utcTsNow() = Timestamp.valueOf(utcNow())

fun ipv4IntToStr(ip: Int): String {
    return String.format(
        Locale.US, "%d.%d.%d.%d",
        ip shr 24 and 0xff,
        ip shr 16 and 0xff,
        ip shr 8 and 0xff,
        ip and 0xff
    )
}

