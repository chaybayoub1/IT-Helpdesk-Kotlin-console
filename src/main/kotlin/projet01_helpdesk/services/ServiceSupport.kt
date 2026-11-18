package projet01_helpdesk.services

import java.util.Locale
import projet01_helpdesk.incidents.Incident
import projet01_helpdesk.incidents.Ticket
import projet01_helpdesk.materiel.Equipement
import projet01_helpdesk.notifications.Notificateur
import projet01_helpdesk.personnes.Employe
import projet01_helpdesk.personnes.TechnicienSupport

class ServiceSupport(private val notificateur: Notificateur) {
    private val employes = linkedMapOf<String, Employe>()
    private val equipements = linkedMapOf<String, Equipement>()
    private val tickets = linkedMapOf<String, Ticket>()
    private val notificationsEnEchec = mutableListOf<String>()

    val erreursNotification: List<String> get() = notificationsEnEchec.toList()

    // Identifiants insensibles à la casse et aux espaces de début/fin.
    private fun cle(identifiant: String): String {
        require(identifiant.isNotBlank()) { "L'identifiant est obligatoire." }
        return identifiant.trim().uppercase(Locale.ROOT)
    }

    fun ajouterEmploye(employe: Employe) {
        val id = cle(employe.matricule)
        require(id !in employes) { "Le matricule ${employe.matricule} existe déjà." }
        employes[id] = employe
    }

    fun ajouterEquipement(equipement: Equipement) {
        val id = cle(equipement.reference)
        require(id !in equipements) { "La référence ${equipement.reference} existe déjà." }
        equipements[id] = equipement
    }

    fun ajouterTicket(ticket: Ticket) {
        val id = cle(ticket.numero)
        require(id !in tickets) { "Le ticket ${ticket.numero} existe déjà." }
        require(rechercherEmploye(ticket.declarant.matricule) === ticket.declarant) {
            "Le déclarant doit être l'employé enregistré dans ce service."
        }
        require(rechercherEquipement(ticket.equipement.reference) === ticket.equipement) {
            "L'équipement doit être celui enregistré dans ce service."
        }
        require(ticket.statut == Ticket.OUVERT) { "Seul un nouveau ticket ouvert peut être enregistré." }
        tickets[id] = ticket
        notifier(ticket, "ouvert")
    }

    fun ouvrirTicket(numero: String, matricule: String, reference: String, incident: Incident): Ticket {
        val employe = requireNotNull(rechercherEmploye(matricule)) { "Employé introuvable : $matricule." }
        val equipement = equipementExistant(reference)
        val ticket = Ticket(numero, employe, equipement, incident)
        ajouterTicket(ticket)
        return ticket
    }

    fun rechercherEmploye(matricule: String): Employe? = employes[cle(matricule)]
    fun rechercherEquipement(reference: String): Equipement? = equipements[cle(reference)]
    fun rechercherTicket(numero: String): Ticket? = tickets[cle(numero)]

    fun consulterEmployes(): List<Employe> = employes.values.toList()
    fun consulterEquipements(): List<Equipement> = equipements.values.toList()
    fun consulterTickets(): List<Ticket> = tickets.values.toList()

    fun declarerPanne(reference: String) = equipementExistant(reference).declarerPanne()
    fun reparerEquipement(reference: String) = equipementExistant(reference).reparer()

    fun affecterTechnicien(numero: String, matricule: String) {
        val ticket = ticketExistant(numero)
        val technicien = rechercherEmploye(matricule)
        require(technicien is TechnicienSupport) { "Un technicien enregistré est nécessaire." }
        ticket.assignerTechnicien(technicien)
        notifier(ticket, "affecté à ${technicien.nom}")
    }

    fun ajouterIntervention(numero: String, description: String) {
        val ticket = ticketExistant(numero)
        ticket.ajouterIntervention(description)
        notifier(ticket, "nouvelle intervention : $description")
    }

    fun confirmerTraitement(numero: String) {
        val ticket = ticketExistant(numero)
        ticket.confirmerTraitement()
        notifier(ticket, "traitement confirmé")
    }

    fun resoudreTicket(numero: String) {
        val ticket = ticketExistant(numero)
        ticket.resoudre()
        notifier(ticket, "résolu")
    }

    fun cloturerTicket(numero: String) {
        val ticket = ticketExistant(numero)
        ticket.cloturer()
        notifier(ticket, "clôturé")
    }

    fun afficherTickets() {
        if (tickets.isEmpty()) println("Aucun ticket enregistré.")
        tickets.values.forEach { it.afficherInformations() }
    }

    private fun ticketExistant(numero: String): Ticket =
        requireNotNull(rechercherTicket(numero)) { "Ticket introuvable : $numero." }

    private fun equipementExistant(reference: String): Equipement =
        requireNotNull(rechercherEquipement(reference)) { "Équipement introuvable : $reference." }

    private fun notifier(ticket: Ticket, action: String) {
        val message = "Ticket ${ticket.numero} : $action."
        // L'opération métier est déjà enregistrée ; un échec d'envoi ne l'annule pas.
        try {
            notificateur.envoyer(message)
        } catch (erreur: RuntimeException) {
            val avertissement = "$message Notification non envoyée : ${erreur.message}"
            notificationsEnEchec.add(avertissement)
            println("Avertissement : $avertissement")
        }
    }
}
