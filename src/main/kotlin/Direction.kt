package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.Serializable

@Serializable
class Direction(
    val filial: String,
    val level: String,
    val form: String,
    val name: String,
    val code: String = name.split(" ").first(),
    val quotas: Map<Quota, Int>,
) {
    val hash = "$filial:$level:$form:$name".hashCode()

    fun getQuota(quota: Quota): Int {
        return this.quotas[quota] ?: 0
    }
}