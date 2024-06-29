package ru.lavafrai.mai.applicantsparser


typealias ApplicantsGraph = List<RawApplicant>

fun ApplicantsGraph.bypass(action: (filial: String, level: String, profile: String, type: String, budget: String, applicant: RawApplicant) -> Unit) {
    forEach { raw ->
        action(raw.filial, raw.level, raw.profile, raw.form, raw.budgetType, raw)
    }
}
