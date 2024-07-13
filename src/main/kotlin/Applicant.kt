package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.Serializable


@Serializable
data class Applicant(
    val id: String,
    val applications: List<Application>,
    val applicationsCount: Int = applications.size
) {
    companion object {
        fun create(applications: ApplicantsGraph, id: String): Applicant {
            val myApplications = mutableListOf<Application>()
            val hash = id.hashCode()

            for (application in applications) {
                if (application.hash == hash) {
                    myApplications.add(application)
                }
            }

            return Applicant(id, myApplications)
        }

        private fun parseTime(time: String): String {
            return time.split(" ").subList(3, 5).joinToString(" ")
        }
    }
}