package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.Serializable

@Serializable
class Application(
    val filial: String,
    val level: String,
    val profile: String,
    val form: String,
    val budgetType: String,
    val fullName: String,
    val updateTime: String,
)