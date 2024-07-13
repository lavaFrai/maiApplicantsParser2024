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
)