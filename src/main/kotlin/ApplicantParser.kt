package ru.lavafrai.mai.applicantsparser

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jsoup.Jsoup

object ApplicantParser {
    private val client = HttpClient(CIO)

    suspend fun parse(): List<Applicant> {
        println("Parsing filial...")
        val filial = getFilial()

        println("Parsing levels...")
        val levels = filial.map {
            (name, id) -> name to getEducationLevels(id)
        }.toMap()

        println("Parsing profiles...")
        val profiles = levels.map {
            (filial, levels) -> filial to levels.map {
                (level, id) -> level to getProfiles(id)
            }.toMap()
        }.toMap()

        println("Parsing forms...")
        val forms = profiles.map {
            (filial, levels) -> filial to levels.map {
                (level, profiles) -> level to profiles.map {
                    (profile, id) -> profile to getForms(id)
                }.toMap()
            }.toMap()
        }.toMap()

        println("Parsing budget types...")
        val budgetTypes = forms.map {
            (filial, levels) -> filial to levels.map {
                (level, profiles) -> level to profiles.map {
                    (profile, forms) -> profile to forms.map {
                        (form, id) -> form to getBudgetType(id)
                    }.toMap()
                }.toMap()
            }.toMap()
        }.toMap()

        println("Parsing applicants...")
        val rawApplicants = mutableListOf<RawApplicant>()
        budgetTypes.map {
            (filial, levels) -> levels.map {
                (level, profiles) -> profiles.map {
                    (profile, forms) -> forms.map {
                        (form, budgetTypes) -> budgetTypes.map {
                            (budgetType, id) -> rawApplicants.addAll(getApplicants(filial, level, profile, form, budgetType, id))
                        }
                    }
                }
            }
        }

        println("Creating applicants...")
        val applicants = mutableListOf<Applicant>()
        rawApplicants.bypass { _, _, _, _, _, raw ->
            if (applicants.any { it.id == raw.id }) { return@bypass }

            applicants.add(Applicant.create(rawApplicants, raw.id))
        }

        println("Parsed ${applicants.size} applicants")
        return applicants
    }

    suspend fun getFilial(): Map<String, String> {
        val url = "https://priem.mai.ru/list/"
        val filialResponse = client.get(url).bodyAsText()

        // //*[@id="place"]/option
        val filial = Jsoup.parse(filialResponse)
            .selectXpath("//*[@id=\"place\"]/option")
            .toList()
            .filter { it.text() != "---" }
            .associate { it.text() to it.attribute("value").value }

        return filial
    }

    suspend fun getEducationLevels(filialId: String): Map<String, String> = getListByDataId(filialId)

    suspend fun getProfiles(educationLevelId: String) = getListByDataId(educationLevelId)

    suspend fun getForms(profileId: String) = getListByDataId(profileId)

    suspend fun getBudgetType(formId: String): Map<String, String> = getListByDataId(formId)

    private suspend fun getListByDataId(id: String): Map<String, String> {
        val url = "https://public.mai.ru/priem/list/data/$id.html"
        val profilesResponse = client.get(url).bodyAsText()

        val profiles = Jsoup.parse(profilesResponse)
            .selectXpath("//option")
            .toList()
            .filter { it.text() != "---" }
            .associate { it.text() to it.attribute("value").value }

        return profiles
    }

    suspend fun getApplicants(
        filial: String,
        level: String,
        profile: String,
        form: String,
        budgetType: String,
        budgetTypeId: String
    ): List<RawApplicant> {
        val url = "https://public.mai.ru/priem/list/data/$budgetTypeId.html"
        val applicantsResponse = client.get(url).bodyAsText()

        val soup =  Jsoup.parse(applicantsResponse)
        val applicants = soup
            .selectXpath("//tr")
            .toList()
            .filter { it.text() != "№ ФИО Вступительные испытания Сумма баллов за ИД" }
            .map { RawApplicant(
                id=it.children()[1].text(),
                testType=it.children()[2].text(),
                testScore=it.children()[3].text(),
                listNumber=it.children()[0].text().toInt(),

                filial=filial,
                level=level,
                profile=profile,
                form=form,
                budgetType=budgetType,

                fullName = soup.selectXpath("//p")[0].text(),
                lastUpdate = soup.selectXpath("//p")[1].text()
            ) }

        return applicants
    }
}
