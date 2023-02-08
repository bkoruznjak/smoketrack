package hr.flowable.smoketrack.blu

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Long.asLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
