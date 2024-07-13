package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.Serializable

@Serializable
data class Prediction(
    val place: Int,
    val placesAvailable: Int,
)

fun Application.makePrediction(applications: List<Application>, directions: List<Direction>): Prediction {
    val prediction = Prediction(
        place=applications
            .filter { it.directionQuotaHash == this.directionQuotaHash }
            .sortedBy { -it.testScore }
            .indexOfFirst { it.id == this.id } + 1,
        placesAvailable=directions
            .first { it.hash == this.directionHash }
            .getQuota(this.quota)
    )

    return prediction
}