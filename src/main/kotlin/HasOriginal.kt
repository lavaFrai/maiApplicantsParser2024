package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class HasOriginal {
    @SerialName("Копия") COPY,
    @SerialName("Оригинал") ORIGINAL,
    @SerialName("Не требуется") NOT_REQUIRED,
}

fun HasOriginal.Companion.byName(name: String): HasOriginal {
    return when(name) {
        "Копия" -> HasOriginal.COPY
        "Подлинник" -> HasOriginal.ORIGINAL
        "Подлинник (ЕПГУ)" -> HasOriginal.ORIGINAL
        "бви" -> HasOriginal.NOT_REQUIRED
        else -> throw IllegalArgumentException("Unknown HasOriginal name: $name")
    }
}
