package ru.lavafrai.mai.applicantsparser

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun parseApplicants() {
    val data = runBlocking {
        return@runBlocking ApplicantParser.getApplicants()
    }

    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }

    println("Applicants count: ${data.size}")
    File("applicants.json").printWriter().use { out ->
        out.println(json.encodeToString(data))
    }
}

suspend fun parseApplications() {
    val data = runBlocking {
        return@runBlocking ApplicantParser.getApplications()
    }

    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }

    println("Applications count: ${data.size}")
    File("applications.json").printWriter().use { out ->
        out.println(json.encodeToString(data))
    }
}

suspend fun parseDirections() {
    val data =  ApplicantParser.getDirections()

    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }

    println("Directions count: ${data.size}")
    File("directions.json").printWriter().use { out ->
        out.println(json.encodeToString(data))
    }
}

fun main() {
    runBlocking {
        println("Parsing applicants.")
        parseApplicants()

        println("\nParsing applications.")
        parseApplications()

        println("\nParsing directions.")
        parseDirections()
    }

    /*
    val directions = runBlocking {
        return@runBlocking ApplicantParser.getDirections()
    }

    println(directions)
    */
}