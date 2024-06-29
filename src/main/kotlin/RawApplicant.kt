package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.Serializable

@Serializable
class RawApplicant(
    val id: String,
    val testType: String,
    val testScore: String,
    val listNumber: Int,

    val filial: String,
    val level: String,
    val profile: String,
    val form: String,
    val budgetType: String,
    val lastUpdate: String,
    val fullName: String,
) {
    val hash = id.hashCode()
}