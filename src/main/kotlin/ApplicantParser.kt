package ru.lavafrai.mai.applicantsparser

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jsoup.Jsoup

object ApplicantParser {
    private val supportedLevels = listOf("Базовое высшее образование", "Специалитет", "Бакалавриат")

    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 30_000
        }
    }

    suspend fun getApplicants(): List<Applicant> {
        val applications = getApplications()

        println("Creating applicants...")
        val applicants = applications.groupBy { it.id }
            .map { (id, applications) ->
                Applicant(id, applications)
            }

        return applicants
    }

    suspend fun getApplications(): MutableList<Application> {
        val rawApplications = mutableListOf<Application>()
        bypassDirections { filial, level, budget, form, direction, id ->
            rawApplications.addAll(getApplications(filial, level, budget, form, direction, id))
        }

        println("Parsed ${rawApplications.size} applicants")
        return rawApplications
    }

    suspend fun getDirections(): List<Direction> {
        val directions = mutableMapOf<String, MutableList<String>>()

        bypassDirections { filial, level, budget, form, direction, id ->
            if (level in supportedLevels) {
                val info = getDirectionInfo(id, budget)
                val key = "$filial+$direction+$level+$form"

                // println("$filial; $level; $budget; $form; $direction; ${info.size}; $info")
                if (directions.containsKey(key)) {
                    directions[key]!!.addAll(info)
                } else {
                    directions[key] = info.toMutableList()
                }
            }
        }

        return directions.map { (direction, info) ->
            Direction(
                filial=direction.split("+")[0],
                level=direction.split("+")[2],
                form=direction.split("+")[3],
                name=direction.split("+")[1],
                quotas=info.map { it.split(": ") }.associate { Quota.byLongName(it[0]) to it[1].toInt() }
            )
        }
    }

    private suspend fun getDirectionInfo(id: String, budget: String): List<String> {
        val url = "https://public.mai.ru/priem/rating/data/$id.html"
        val applicantsResponse = client.get(url).bodyAsText()

        val soup = Jsoup.parse(applicantsResponse)
        val elements = soup
            .selectXpath("//*[@id=\"collapseRating\"]/div/article/div/div/ul/li")
            .toList()
            .map { it.text() }
            .toMutableList()

        // println(elements)
        val additionalElement = soup.selectXpath("//section/div[2]/p[2]").toList().firstOrNull()?.text()
        if (additionalElement != null) {
            when (budget) {
                "Бюджет" -> elements.add(additionalElement)
                "Платная" -> elements.add(additionalElement.replace("Количество мест", "Платная"))
                "Платная иностранцы" -> elements.add(additionalElement.replace("Количество мест", "Платная иностранцы"))
                else -> throw RuntimeException("Unknown form: $budget")
            }
        } else throw RuntimeException("Additional element not found: $soup")

        return elements
    }

    private suspend fun bypassDirections(action: suspend (filial: String, level: String, budget: String, form: String, direction: String, id: String) -> Unit) {
        println("Parsing filial...")
        val filial = getFilial()

        println("Parsing levels...")
        val levels = filial.map { (name, id) ->
            name to getEducationLevels(id)
        }.toMap()

        println("Parsing budget types...")
        val budgetType = levels.map { (filial, levels) ->
            filial to levels.map { (level, id) ->
                level to getProfiles(id)
            }.toMap()
        }.toMap()

        println("Parsing forms...")
        val forms = budgetType.map { (filial, levels) ->
            filial to levels.map { (level, profiles) ->
                level to profiles.map { (profile, id) ->
                    profile to getForms(id)
                }.toMap()
            }.toMap()
        }.toMap()

        println("Parsing directions...")
        val directions = forms.map { (filial, levels) ->
            filial to levels.map { (level, profiles) ->
                level to profiles.map { (profile, forms) ->
                    profile to forms.map { (form, id) ->
                        form to getDirection(id)
                    }.toMap()
                }.toMap()
            }.toMap()
        }.toMap()

        println("Parsing applicants...")
        directions.map { (filial, levels) ->
            levels.map { (level, profiles) ->
                profiles.map { (budget, forms) ->
                    forms.map { (form, budgetTypes) ->
                        budgetTypes.map { (direction, id) ->
                            action(filial, level, budget, form, direction, id)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getFilial(): Map<String, String> {
        val url = "https://priem.mai.ru/rating/"
        val filialResponse = client.get(url).bodyAsText()

        // //*[@id="place"]/option
        val filial = Jsoup.parse(filialResponse)
            .selectXpath("//*[@id=\"place\"]/option")
            .toList()
            .filter { it.text() != "---" }
            .associate { it.text() to it.attribute("value").value }

        return filial
    }

    private suspend fun getEducationLevels(filialId: String): Map<String, String> = getListByDataId(filialId)

    private suspend fun getProfiles(educationLevelId: String) = getListByDataId(educationLevelId)

    private suspend fun getForms(profileId: String) = getListByDataId(profileId)

    private suspend fun getDirection(formId: String): Map<String, String> = getListByDataId(formId)

    private suspend fun getListByDataId(id: String): Map<String, String> {
        val url = "https://public.mai.ru/priem/rating/data/$id.html"
        val profilesResponse = client.get(url).bodyAsText()

        val profiles = Jsoup.parse(profilesResponse)
            .selectXpath("//option")
            .toList()
            .filter { it.text() != "---" }
            .associate { it.text() to it.attribute("value").value }

        return profiles
    }

    private suspend fun getApplications(
        filial: String,
        level: String,
        budget: String,
        form: String,
        direction: String,
        directionId: String
    ): MutableList<Application> {
        val url = "https://public.mai.ru/priem/rating/data/$directionId.html"
        val applicantsResponse = client.get(url).bodyAsText()

        val soup = Jsoup.parse(applicantsResponse)
        val elements = soup
            .selectXpath("//tr")
            .toList()

        val applicantElements = elements//.filter { it.children()[0].text().toIntOrNull() != null  }
        val applications = mutableListOf<Application>()

        var i = 0
        while (i < applicantElements.size) {
            var quota = null as String?
            var id = null as String?
            var testScore = null as Int?
            var additionalScore = null as Int?
            var priority = null as Int?
            var requiresDormitory = null as Boolean?
            var hasOriginal = null as String?

            if (level in supportedLevels) {
                requiresDormitory = applicantElements[i].select("[title=\"Нуждаемость в общежитии\"]").text().isNotBlank()

                if (applicantElements.getOrNull(i + 1)?.hasClass("skipcount") == true) {
                    quota = "целевая"
                    id = applicantElements[i].children()[1].text()
                    testScore = applicantElements[i].children()[2].text().toInt()
                    additionalScore = applicantElements[i].children()[6].text().toInt()
                    priority = applicantElements[i].children()[8].text().toInt()
                    hasOriginal = applicantElements[i].children()[7].text()
                    i++
                } else if (applicantElements[i].children()[0].text().toIntOrNull() != null) {
                    id = applicantElements[i].children()[1].text()
                    testScore = applicantElements[i].children()[2].text().toInt()

                    when (budget) {
                        "Бюджет" -> {
                            quota = when(applicantElements[i].childrenSize()) {
                                8 -> "бви"
                                11 -> "особая"
                                12 -> "отдельная"
                                13 -> "общая"
                                else -> throw RuntimeException("Unknown budget type: ${applicantElements[i].childrenSize()}, ${applicantElements[i]}")
                            }
                            additionalScore = if (quota == "бви") applicantElements[i].children()[2].text().toInt()
                            else applicantElements[i].children()[6].text().toInt()

                            priority = when (quota) {
                                "бви" -> { applicantElements[i].children()[4].text().toInt() }
                                "особая" -> { applicantElements[i].children()[8].text().toInt() }
                                "отдельная" -> { applicantElements[i].children()[8].text().toInt() }
                                "общая" -> { applicantElements[i].children()[9].text().toInt() }
                                else -> throw RuntimeException("Unknown quota type: $quota")
                            }

                            hasOriginal = when (quota) {
                                "бви" -> { "бви" }
                                "особая" -> { applicantElements[i].children()[7].text() }
                                "отдельная" -> { applicantElements[i].children()[7].text() }
                                "общая" -> { applicantElements[i].children()[7].text() }
                                else -> throw RuntimeException("Unknown quota type: $quota")
                            }
                        }

                        "Платная" -> {
                            quota = "платная"
                            additionalScore = applicantElements[i].children()[6].text().toInt()
                            priority = applicantElements[i].children()[8].text().toInt()
                            hasOriginal = applicantElements[i].children()[7].text()
                        }

                        "Платная иностранцы" -> {
                            quota = "платная-иностранцы"
                            additionalScore = applicantElements[i].children()[5].text().toInt()
                            priority = applicantElements[i].children()[8].text().toInt()
                            hasOriginal = applicantElements[i].children()[6].text()
                        }
                    }
                }
            }

            if (quota != null) {
                val application = Application(
                    id = id!!,
                    quota = Quota.byName(quota),
                    testScore = testScore!!,
                    additionalScore = additionalScore!!,
                    priority = priority!!,
                    requiresDormitory = requiresDormitory!!,
                    hasOriginal=HasOriginal.byName(hasOriginal!!),

                    filial = filial,
                    level = level,
                    profile = form,
                    form = budget,
                    budgetType = budget,

                    lastUpdate = "reserved",
                    fullName = direction
                )
                applications.add(application)
            }
            i++
        }

        return applications
    }
}
