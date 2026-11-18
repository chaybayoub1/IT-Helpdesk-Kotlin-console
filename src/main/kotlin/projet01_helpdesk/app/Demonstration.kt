package projet01_helpdesk.app

import projet01_helpdesk.incidents.IncidentLogiciel
import projet01_helpdesk.incidents.IncidentMateriel
import projet01_helpdesk.materiel.Equipement
import projet01_helpdesk.notifications.Notificateur
import projet01_helpdesk.personnes.Employe
import projet01_helpdesk.personnes.TechnicienSupport
import projet01_helpdesk.services.ServiceSupport

fun executerDemonstration(notificateur: Notificateur): ServiceSupport {
    println("===== IT HELPDESK — DÉMONSTRATION COMPLÈTE =====")
    val service = ServiceSupport(notificateur)
    service.ajouterEmploye(Employe("EM001", "Aya"))
    service.ajouterEmploye(TechnicienSupport("TECH001", "Sara", "Matériel et logiciels"))
    service.ajouterEquipement(Equipement("PC001", "PC portable d'Aya", "PC"))
    service.ajouterEquipement(Equipement("IMP001", "Imprimante du bureau", "Imprimante"))
    service.ajouterEquipement(Equipement("SRV001", "Serveur de fichiers", "Serveur"))

    service.declarerPanne("PC001")
    val materiel = service.ouvrirTicket("INC001", "EM001", "PC001", IncidentMateriel("Le PC ne démarre plus"))
    println(materiel.incident.diagnostiquer())
    service.affecterTechnicien("INC001", "TECH001")
    service.ajouterIntervention("INC001", "Chargeur défectueux remplacé et démarrage testé")
    service.reparerEquipement("PC001")
    service.confirmerTraitement("INC001")
    service.resoudreTicket("INC001")
    service.cloturerTicket("INC001")

    val logiciel = service.ouvrirTicket("INC002", "EM001", "PC001", IncidentLogiciel("Connexion refusée", "ERP"))
    println(logiciel.incident.diagnostiquer())
    service.affecterTechnicien("INC002", "TECH001")
    service.ajouterIntervention("INC002", "Compte débloqué et connexion à l'ERP vérifiée")
    service.confirmerTraitement("INC002")
    service.resoudreTicket("INC002")
    service.cloturerTicket("INC002")

    println("\n===== BILAN ET HISTORIQUES =====")
    service.afficherTickets()
    service.consulterTickets().forEach {
        println("\nHistorique de ${it.numero}")
        it.afficherHistorique()
    }
    service.consulterEquipements().forEach { it.afficherInformations() }
    return service
}
