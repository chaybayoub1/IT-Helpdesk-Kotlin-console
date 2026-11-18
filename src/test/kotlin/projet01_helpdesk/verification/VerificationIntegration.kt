package projet01_helpdesk.verification

import projet01_helpdesk.app.ConsoleHelpdesk
import projet01_helpdesk.app.executerDemonstration
import projet01_helpdesk.incidents.Ticket
import projet01_helpdesk.services.ServiceSupport

internal fun verifierIntegration() {
    scenario("Démonstration : deux tickets clôturés, une panne et douze notifications") {
        val notifications = NotificationsMemoire()
        val service = executerDemonstration(notifications)
        check(service.consulterTickets().size == 2)
        check(service.consulterTickets().all { it.statut == Ticket.CLOTURE && it.interventions.size == 1 })
        check(service.rechercherEquipement("PC001")!!.nombrePannes == 1)
        check(!service.rechercherEquipement("PC001")!!.enPanne)
        check(notifications.messages.size == 12)
    }
    scenario("Menu : parcours matériel complet depuis un service vide") {
        val service = ServiceSupport(NotificationsMemoire())
        val saisies = listOf(
            "1", "EM1", "Aya",
            "2", "TECH1", "Sara", "Support",
            "3", "PC1", "Portable", "PC",
            "4", "5", "PC1",
            "7", "INC1", "EM1", "PC1", "1", "Écran noir",
            "8", "INC1", "TECH1",
            "9", "INC1", "Chargeur remplacé",
            "6", "PC1", "10", "INC1", "oui",
            "11", "INC1", "12", "INC1",
            "13", "14", "INC1", "15", "0"
        )
        val sortie = jouerConsole(service, saisies)
        check(service.rechercherTicket("INC1")!!.statut == Ticket.CLOTURE)
        check(service.rechercherEquipement("PC1")!!.nombrePannes == 1)
        check(sortie.any { it.contains("Chargeur remplacé") })
        check(sortie.none { it.startsWith("Erreur") })
        check(sortie.last() == "Session terminée.")
    }
    scenario("Menu : parcours logiciel et refus de clôture prématurée") {
        val service = serviceExemple()
        val sortie = jouerConsole(service, listOf(
            "7", "I1", "EM001", "PC001", "2", "Connexion refusée", "ERP",
            "12", "I1", "8", "I1", "TECH001",
            "9", "I1", "Compte débloqué",
            "10", "I1", "non", "11", "I1",
            "10", "I1", "oui", "11", "I1", "12", "I1", "0"
        ))
        check(sortie.count { it.startsWith("Erreur") } == 2)
        check(service.rechercherTicket("I1")!!.statut == Ticket.CLOTURE)
        check(service.rechercherEquipement("PC001")!!.nombrePannes == 0)
    }
    scenario("Menu : saisies invalides, doublon, recherche absente et poursuite") {
        val service = serviceExemple()
        val sortie = jouerConsole(service, listOf(
            "inconnu", "1", " ", "1", "EM001", "Doublon",
            "7", "I1", "EM001", "PC001", "3",
            "14", "absent", "8", "absent", "TECH001", "13", "0"
        ))
        check(sortie.any { it.contains("Choix inconnu") })
        check(sortie.count { it.startsWith("Erreur") } == 4)
        check(sortie.any { it == "Ticket introuvable." })
        check(service.consulterTickets().isEmpty() && service.consulterEmployes().size == 2)
    }
    scenario("Fin de saisie propre au menu et au milieu d'une opération") {
        val service = serviceExemple()
        check(jouerConsole(service, emptyList()).last() == "Session terminée.")
        jouerConsole(service, listOf("1", "NOUVEAU"))
        jouerConsole(service, listOf("7", "I1", "EM001", "PC001", "2", "Erreur"))
        check(service.rechercherEmploye("NOUVEAU") == null && service.consulterTickets().isEmpty())
    }
}

private fun jouerConsole(service: ServiceSupport, lignes: List<String>): List<String> {
    val iterateur = lignes.iterator()
    val sortie = mutableListOf<String>()
    ConsoleHelpdesk(
        service,
        lire = { if (iterateur.hasNext()) iterateur.next() else null },
        afficher = { sortie.add(it) }
    ).demarrer()
    return sortie
}
