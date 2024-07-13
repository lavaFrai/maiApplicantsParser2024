package ru.lavafrai.mai.applicantsparser


typealias ApplicantsGraph = List<Application>

fun ApplicantsGraph.bypass(action: (filial: String, level: String, profile: String, type: String, budget: String, applicant: Application) -> Unit) {
    map { raw ->
        action(raw.filial, raw.level, raw.direction, raw.form, raw.budgetType, raw)
    }
}