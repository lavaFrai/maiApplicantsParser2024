package ru.lavafrai.mai.applicantsparser

import kotlinx.serialization.Serializable


@Serializable
class Applicant(
    val id: String,
    val testType: String,
    val testScore: Int,
    val applications: List<Application>,
    val applicationsCount: Int = applications.size
) {
    companion object {
        fun create(graph: ApplicantsGraph, id: String): Applicant {
            val applications = mutableListOf<Application>()
            var me: RawApplicant? = null
            val hash = id.hashCode()

            graph.bypass { filial, level, profile, type, budget, raw ->
                if (raw.hash == hash) {
                    applications.add(Application(filial, level, profile, type, budget, raw.fullName, parseTime(raw.lastUpdate)))
                    me = raw
                }
            }
            me ?: error("Applicant with id $id not found")

            return Applicant(id.lowercase(), me!!.testType, me!!.testScore.toIntOrNull() ?: 0, applications)
        }

        private fun parseTime(time: String): String {
            return time.split(" ").subList(3, 5).joinToString(" ")
        }
    }
}