package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.Serializable
import ru.lavafrai.mai.applicantsparser.Applicant
import ru.lavafrai.mai.applicantsparser.Application

@Serializable
data class ApplicantWithPrediction(
    val id: String,
    val applications: List<Application>,
    val predictions: Map<Int, Prediction>,
)

fun Applicant.makePredictions(applications: List<Application>, directions: List<Direction>): ApplicantWithPrediction {
    val predictions = mutableMapOf<Int, Prediction>()

    for (application in this.applications) {
        predictions[application.priority] = application.makePrediction(applications, directions)
    }

    return ApplicantWithPrediction(id, this.applications, predictions)
}