package projet01_helpdesk.verification

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import projet01_helpdesk.incidents.*
import projet01_helpdesk.materiel.Equipement
import projet01_helpdesk.notifications.*
import projet01_helpdesk.personnes.*
import projet01_helpdesk.services.ServiceSupport

internal class NotificationsMemoire : Notificateur {
    val messages = mutableListOf<String>()
    override fun envoyer(message: String) { messages.add(message) }
}

internal fun serviceExemple(notificateur: Notificateur = NotificationsMemoire()): ServiceSupport =
    ServiceSupport(notificateur).apply {
        ajouterEmploye(Employe("EM001", "Aya"))
        ajouterEmploye(TechnicienSupport("TECH001", "Sara", "Support"))
        ajouterEquipement(Equipement("PC001", "Portable", "PC"))
    }

internal fun verifierService() {
    scenario("Enregistrement, recherche normalisée et doublons") {
        val service = serviceExemple()
        val employe = service.rechercherEmploye(" em001 ")
        val equipement = service.rechercherEquipement(" pc001 ")
        refuser<IllegalArgumentException> { service.ajouterEmploye(Employe(" Em001 ", "Autre")) }
        refuser<IllegalArgumentException> { service.ajouterEquipement(Equipement(" pc001 ", "Autre", "PC")) }
        val ticket = service.ouvrirTicket("INC001", "em001", "pc001", IncidentMateriel("Panne"))
        refuser<IllegalArgumentException> {
            service.ouvrirTicket(" inc001 ", "EM001", "PC001", IncidentMateriel("Autre"))
        }
        check(service.rechercherEmploye("EM001") === employe)
        check(service.rechercherEquipement("PC001") === equipement)
        check(service.rechercherTicket(" inc001 ") === ticket)
        check(service.consulterTickets().size == 1 && service.consulterEmployes().size == 2)
        check(service.rechercherTicket("INC999") == null)
    }
    scenario("Parcours service complet et notifications uniquement après succès") {
        val notifications = NotificationsMemoire()
        val service = serviceExemple(notifications)
        service.declarerPanne("PC001")
        val ticket = service.ouvrirTicket("INC001", "EM001", "PC001", IncidentMateriel("Panne"))
        refuser<IllegalStateException> { service.cloturerTicket("INC001") }
        refuser<IllegalArgumentException> { service.affecterTechnicien("INC001", "EM001") }
        check(notifications.messages.size == 1 && ticket.statut == Ticket.OUVERT)
        service.affecterTechnicien("INC001", "TECH001")
        service.ajouterIntervention("INC001", "Chargeur remplacé")
        refuser<IllegalStateException> { service.confirmerTraitement("INC001") }
        service.reparerEquipement("PC001")
        service.confirmerTraitement("INC001")
        service.resoudreTicket("INC001")
        service.cloturerTicket("INC001")
        check(ticket.statut == Ticket.CLOTURE)
        check(notifications.messages.size == 6 && notifications.messages.last().contains("clôturé"))
        check(service.erreursNotification.isEmpty())
    }
    scenario("Références inconnues ou vides refusées sans ticket créé") {
        val service = serviceExemple()
        refuser<IllegalArgumentException> { service.ouvrirTicket("I1", "absent", "PC001", IncidentMateriel("Panne")) }
        refuser<IllegalArgumentException> { service.ouvrirTicket("I1", "EM001", "absent", IncidentMateriel("Panne")) }
        refuser<IllegalArgumentException> { service.affecterTechnicien("absent", "TECH001") }
        refuser<IllegalArgumentException> { service.ajouterIntervention("absent", "Action") }
        refuser<IllegalArgumentException> { service.confirmerTraitement("absent") }
        refuser<IllegalArgumentException> { service.resoudreTicket("absent") }
        refuser<IllegalArgumentException> { service.cloturerTicket("absent") }
        refuser<IllegalArgumentException> { service.declarerPanne("absent") }
        refuser<IllegalArgumentException> { service.reparerEquipement("absent") }
        refuser<IllegalArgumentException> { service.rechercherTicket(" ") }
        check(service.consulterTickets().isEmpty())
    }
    scenario("Objets étrangers et ticket déjà affecté refusés à l'enregistrement") {
        val service = serviceExemple()
        val employe = service.rechercherEmploye("EM001")!!
        val equipement = service.rechercherEquipement("PC001")!!
        refuser<IllegalArgumentException> {
            service.ajouterTicket(Ticket("I1", Employe("EM001", "Imposteur"), equipement, IncidentMateriel("Panne")))
        }
        refuser<IllegalArgumentException> {
            service.ajouterTicket(Ticket("I1", employe, Equipement("PC001", "Autre", "PC"), IncidentMateriel("Panne")))
        }
        val ticket = Ticket("I1", employe, equipement, IncidentMateriel("Panne"))
        ticket.assignerTechnicien(TechnicienSupport("EXT", "Externe", "Support"))
        refuser<IllegalArgumentException> { service.ajouterTicket(ticket) }
        check(service.consulterTickets().isEmpty())
    }
    scenario("Les listes du service sont des copies") {
        val service = serviceExemple()
        val avant = service.consulterTickets()
        service.ouvrirTicket("I1", "EM001", "PC001", IncidentMateriel("Panne"))
        service.ouvrirTicket("I2", "EM001", "PC001", IncidentLogiciel("Erreur", "ERP"))
        (service.consulterTickets() as MutableList).clear()
        (service.consulterEmployes() as MutableList).clear()
        check(avant.isEmpty() && service.consulterTickets().size == 2 && service.consulterEmployes().size == 2)
    }
    scenario("Échec du canal signalé sans annuler l'ouverture") {
        val service = serviceExemple(object : Notificateur {
            override fun envoyer(message: String) { error("Canal indisponible") }
        })
        val ticket = service.ouvrirTicket("I1", "EM001", "PC001", IncidentMateriel("Panne"))
        check(service.rechercherTicket("I1") === ticket)
        check(service.erreursNotification.single().contains("Canal indisponible"))
    }
    scenario("Canaux Email et SMS simulés, messages vides refusés") {
        val sortie = ByteArrayOutputStream()
        val original = System.out
        try {
            System.setOut(PrintStream(sortie, true, Charsets.UTF_8))
            NotificationEmail().envoyer("Message A")
            NotificationSMS().envoyer("Message B")
        } finally { System.setOut(original) }
        val texte = sortie.toString(Charsets.UTF_8)
        check(texte.contains("[Email simulé] Message A"))
        check(texte.contains("[SMS simulé] Message B"))
        refuser<IllegalArgumentException> { NotificationEmail().envoyer(" ") }
        refuser<IllegalArgumentException> { NotificationSMS().envoyer("") }
    }
}
