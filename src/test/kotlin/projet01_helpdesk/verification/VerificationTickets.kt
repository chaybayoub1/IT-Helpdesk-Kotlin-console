package projet01_helpdesk.verification

import projet01_helpdesk.incidents.*
import projet01_helpdesk.materiel.Equipement
import projet01_helpdesk.personnes.Employe
import projet01_helpdesk.personnes.TechnicienSupport

internal fun verifierTickets() {
    val aya = Employe("EM001", "Aya")
    val sara = TechnicienSupport("TECH001", "Sara", "Support")
    fun nouveau(logiciel: Boolean = false) = Ticket(
        "INC001", aya, Equipement("PC001", "Portable", "PC"),
        if (logiciel) IncidentLogiciel("Erreur", "ERP") else IncidentMateriel("Écran noir")
    )
    scenario("Cycle complet et auteur de l'intervention") {
        val ticket = nouveau()
        check(ticket.statut == Ticket.OUVERT && ticket.technicien == null)
        ticket.equipement.declarerPanne()
        ticket.assignerTechnicien(sara)
        check(ticket.statut == Ticket.EN_COURS && ticket.technicien === sara)
        ticket.ajouterIntervention("Chargeur remplacé")
        check(ticket.interventions.single().technicien === sara)
        ticket.equipement.reparer()
        ticket.confirmerTraitement()
        ticket.resoudre()
        check(ticket.statut == Ticket.RESOLU)
        ticket.cloturer()
        check(ticket.statut == Ticket.CLOTURE && ticket.historique.size == 6)
    }
    scenario("Opérations interdites à l'ouverture sans modification") {
        val ticket = nouveau()
        val avant = ticket.historique
        refuser<IllegalStateException> { ticket.ajouterIntervention("Action") }
        refuser<IllegalStateException> { ticket.confirmerTraitement() }
        refuser<IllegalStateException> { ticket.resoudre() }
        refuser<IllegalStateException> { ticket.cloturer() }
        check(ticket.historique == avant && ticket.statut == Ticket.OUVERT)
    }
    scenario("Réaffectation, résolution prématurée et panne persistante refusées") {
        val ticket = nouveau()
        ticket.assignerTechnicien(sara)
        refuser<IllegalStateException> { ticket.assignerTechnicien(sara) }
        refuser<IllegalStateException> { ticket.confirmerTraitement() }
        refuser<IllegalStateException> { ticket.resoudre() }
        refuser<IllegalStateException> { ticket.cloturer() }
        ticket.ajouterIntervention("Diagnostic")
        ticket.equipement.declarerPanne()
        refuser<IllegalStateException> { ticket.confirmerTraitement() }
        ticket.equipement.reparer()
        refuser<IllegalStateException> { ticket.resoudre() }
        ticket.confirmerTraitement()
        refuser<IllegalStateException> { ticket.confirmerTraitement() }
        ticket.equipement.declarerPanne()
        refuser<IllegalStateException> { ticket.resoudre() }
        check(ticket.statut == Ticket.EN_COURS)
    }
    scenario("Nouvelle intervention : confirmation à renouveler") {
        val ticket = nouveau(true)
        ticket.assignerTechnicien(sara)
        ticket.ajouterIntervention("Configuration corrigée")
        ticket.confirmerTraitement()
        ticket.ajouterIntervention("Test supplémentaire")
        check(!ticket.problemeTraite)
        refuser<IllegalStateException> { ticket.resoudre() }
        ticket.confirmerTraitement()
        ticket.resoudre()
    }
    scenario("Incident logiciel indépendant d'une panne matérielle distincte") {
        val ticket = nouveau(true)
        ticket.equipement.declarerPanne()
        ticket.assignerTechnicien(sara)
        ticket.ajouterIntervention("Compte débloqué")
        ticket.confirmerTraitement()
        ticket.resoudre()
        check(ticket.equipement.enPanne)
    }
    scenario("Ticket résolu puis clôturé : transitions verrouillées") {
        val ticket = nouveau()
        ticket.assignerTechnicien(sara)
        ticket.ajouterIntervention("Connectique vérifiée")
        ticket.confirmerTraitement()
        ticket.resoudre()
        for (cloture in listOf(false, true)) {
            if (cloture) ticket.cloturer()
            val avant = ticket.historique
            refuser<IllegalStateException> { ticket.assignerTechnicien(sara) }
            refuser<IllegalStateException> { ticket.ajouterIntervention("Action tardive") }
            refuser<IllegalStateException> { ticket.confirmerTraitement() }
            refuser<IllegalStateException> { ticket.resoudre() }
            if (cloture) refuser<IllegalStateException> { ticket.cloturer() }
            check(ticket.historique == avant)
        }
    }
    scenario("Numéro et intervention non vides ; refus sans effets") {
        for (vide in listOf("", " \t\n")) {
            refuser<IllegalArgumentException> { Ticket(vide, aya, nouveau().equipement, IncidentMateriel("Panne")) }
            refuser<IllegalArgumentException> { Intervention(vide, sara) }
            val ticket = nouveau()
            ticket.assignerTechnicien(sara)
            val avant = ticket.historique
            refuser<IllegalArgumentException> { ticket.ajouterIntervention(vide) }
            check(ticket.interventions.isEmpty() && ticket.historique == avant)
        }
    }
    scenario("Historique et interventions exposés par copies") {
        val ticket = nouveau()
        val historiqueInitial = ticket.historique
        val interventionsInitiales = ticket.interventions
        ticket.assignerTechnicien(sara)
        ticket.ajouterIntervention("Diagnostic")
        ticket.ajouterIntervention("Réparation")
        check(historiqueInitial.size == 1 && interventionsInitiales.isEmpty())
        (ticket.interventions as MutableList).clear()
        (ticket.historique as MutableList).clear()
        check(ticket.interventions.size == 2 && ticket.historique.size == 4)
    }
}
