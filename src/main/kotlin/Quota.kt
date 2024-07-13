package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Quota {
    @SerialName("бви") WithoutExam,
    @SerialName("особая") Special,
    @SerialName("отдельная") Separate,
    @SerialName("целевая") Target,
    @SerialName("платная") Paid,
    @SerialName("платная-иностранцы") PaidForeign,
    @SerialName("общая") Common,
    @SerialName("всего-бюджет") Total,
    @SerialName("общежитие") Dormitory,
}

fun Quota.Companion.byName(name: String): Quota {
    return when (name) {
        "бви" -> Quota.WithoutExam
        "особая" -> Quota.Special
        "отдельная" -> Quota.Separate
        "целевая" -> Quota.Target
        "платная" -> Quota.Paid
        "платная-иностранцы" -> Quota.PaidForeign
        "общая" -> Quota.Common
        else -> throw IllegalArgumentException("Unknown quota name: $name")
    }
}

fun Quota.Companion.byLongName(name: String): Quota {
    return when (name) {
        "из них по особой квоте" -> Quota.Special
        "из них по отдельной квоте" -> Quota.Separate
        "из них по целевой квоте" -> Quota.Target
        "Платная" -> Quota.Paid
        "Платная иностранцы" -> Quota.PaidForeign
        "мест по общему конкурсу на данный момент" -> Quota.Common
        "мест в общежитии" -> Quota.Dormitory
        "Количество мест" -> Quota.Total
        else -> throw IllegalArgumentException("Unknown quota long name: $name")
    }
}