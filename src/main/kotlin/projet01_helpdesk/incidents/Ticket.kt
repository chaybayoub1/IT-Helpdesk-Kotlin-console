package projet01_helpdesk.incidents

import java.time.LocalDateTime
import projet01_helpdesk.materiel.Equipement
import projet01_helpdesk.personnes.Employe
import projet01_helpdesk.personnes.TechnicienSupport

class Ticket(
    val numero: String,
    val declarant: Employe,
    val equipement: Equipement,
    val incident: Incident
) {
    var statut: String = OUVERT
        private set
    var technicien: TechnicienSupport? = null
        private set
    var problemeTraite: Boolean = false
        private set

    private val listeInterventions = mutableListOf<Intervention>()
    private val evenements = mutableListOf<String>()

    // Des copies empêchent de modifier les collections internes depuis l'extérieur.
    val interventions: List<Intervention> get() = listeInterventions.toList()
    val historique: List<String> get() = evenements.toList()

    init {
        require(numero.isNotBlank()) { "Le numéro du ticket est obligatoire." }
        enregistrer("Ticket ouvert par ${declarant.nom}.")
    }

    fun assignerTechnicien(technicien: TechnicienSupport) {
        check(statut == OUVERT) { "Seul un ticket ouvert peut être affecté." }
        this.technicien = technicien
        statut = EN_COURS
        enregistrer("Ticket affecté à ${technicien.nom} : $EN_COURS.")
    }

    fun ajouterIntervention(description: String) {
        check(statut == EN_COURS) { "Une intervention exige un ticket en cours." }
        val auteur = checkNotNull(technicien) { "Aucun technicien affecté." }
        val intervention = Intervention(description, auteur)
        listeInterventions.add(intervention)
        // Une nouvelle action nécessite une nouvelle confirmation du résultat.
        problemeTraite = false
        enregistrer("Intervention de ${auteur.nom} : $description")
    }

    fun confirmerTraitement() {
        verifierTraitementPossible()
        check(!problemeTraite) { "Le traitement est déjà confirmé." }
        problemeTraite = true
        enregistrer("Le technicien confirme que le problème est traité.")
    }

    fun resoudre() {
        verifierTraitementPossible()
        check(problemeTraite) { "Le traitement du problème doit être confirmé avant résolution." }
        statut = RESOLU
        enregistrer("Ticket résolu.")
    }

    fun cloturer() {
        check(statut == RESOLU) { "Impossible de clôturer un ticket non résolu." }
        statut = CLOTURE
        enregistrer("Ticket clôturé.")
    }

    private fun verifierTraitementPossible() {
        check(statut == EN_COURS) { "Le ticket doit être en cours." }
        check(listeInterventions.isNotEmpty()) { "Une intervention est nécessaire avant résolution." }
        if (incident is IncidentMateriel) {
            check(!equipement.enPanne) { "L'équipement doit être réparé avant résolution." }
        }
    }

    private fun enregistrer(message: String) {
        evenements.add("${LocalDateTime.now()} — $message")
    }

    fun afficherInformations() {
        println("$numero | $statut | ${declarant.nom} | ${equipement.reference}")
        println("Incident : ${incident.description}")
        println("Technicien : ${technicien?.nom ?: "Non affecté"} | Interventions : ${listeInterventions.size}")
    }

    fun afficherHistorique() {
        evenements.forEach(::println)
    }

    companion object {
        const val OUVERT = "Ouvert"
        const val EN_COURS = "En cours"
        const val RESOLU = "Résolu"
        const val CLOTURE = "Clôturé"
    }
}
