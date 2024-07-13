package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Application(
    val id: String,
    val quota: Quota,
    // val testType: String,
    val testScore: Int,
    // val listNumber: Int,
    val additionalScore: Int,
    val priority: Int,
    val requiresDormitory: Boolean,
    val hasOriginal: HasOriginal,

    val filial: String,
    val level: String,
    val profile: String,
    val form: String,
    val budgetType: String,
    val lastUpdate: String,
    val fullName: String,
) {
    @Transient val hash = id.hashCode()
}