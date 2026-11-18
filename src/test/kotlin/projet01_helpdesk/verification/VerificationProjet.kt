package projet01_helpdesk.verification

import projet01_helpdesk.incidents.Incident
import projet01_helpdesk.incidents.IncidentLogiciel
import projet01_helpdesk.incidents.IncidentMateriel
import projet01_helpdesk.verification.etape1.main as verifierEtape1

fun main() {
    verifierEtape1()
    verifierIncidents()
    terminerVerifications()
}

private var reussites = 0
private val echecs = mutableListOf<String>()

internal fun scenario(nom: String, action: () -> Unit) {
    try {
        action()
        reussites++
        println("OK — $nom")
    } catch (erreur: Exception) {
        echecs.add("$nom : ${erreur.message}")
        System.err.println("ÉCHEC — ${echecs.last()}")
    }
}

internal inline fun <reified T : Exception> refuser(action: () -> Unit) {
    val erreur = runCatching(action).exceptionOrNull()
    check(erreur is T) {
        "Erreur attendue : ${T::class.simpleName}, reçue : ${erreur?.javaClass?.simpleName ?: "aucune"}"
    }
}

internal fun terminerVerifications() {
    check(echecs.isEmpty()) { echecs.joinToString("\n") }
    println("$reussites scénarios réussis, plus les vérifications de l'étape 1.")
}

private fun verifierIncidents() {
    scenario("Diagnostics polymorphes matériel et logiciel") {
        val incidents: List<Incident> = listOf(
            IncidentMateriel("Écran noir"), IncidentLogiciel("Connexion refusée", "ERP")
        )
        check(incidents[0].diagnostiquer().contains("matériel"))
        check(incidents[1].diagnostiquer().contains("ERP"))
        check(incidents[0].description == "Écran noir")
    }
    scenario("Descriptions et logiciel obligatoires") {
        for (vide in listOf("", " \t\n")) {
            refuser<IllegalArgumentException> { IncidentMateriel(vide) }
            refuser<IllegalArgumentException> { IncidentLogiciel(vide, "ERP") }
            refuser<IllegalArgumentException> { IncidentLogiciel("Erreur", vide) }
        }
    }
}
