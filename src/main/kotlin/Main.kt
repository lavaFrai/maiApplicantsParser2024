package ru.lavafrai.mai.applicantsparser

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    val data = runBlocking {
        return@runBlocking ApplicantParser.parse()
    }

    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }

    File("applicants.json").printWriter().use { out ->
        out.println(json.encodeToString(data))
    }
}