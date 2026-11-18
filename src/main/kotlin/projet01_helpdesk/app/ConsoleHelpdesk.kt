package projet01_helpdesk.app

import projet01_helpdesk.incidents.IncidentLogiciel
import projet01_helpdesk.incidents.IncidentMateriel
import projet01_helpdesk.materiel.Equipement
import projet01_helpdesk.personnes.Employe
import projet01_helpdesk.personnes.TechnicienSupport
import projet01_helpdesk.services.ServiceSupport

class ConsoleHelpdesk(
    private val service: ServiceSupport,
    private val lire: () -> String? = ::readlnOrNull,
    private val afficher: (String) -> Unit = ::println
) {
    fun demarrer() {
        afficher("IT Helpdesk — les données restent en mémoire pendant cette session.")
        while (true) {
            afficher(MENU)
            val choix = lire()?.trim() ?: break
            if (choix == "0") break
            try {
                executer(choix)
            } catch (_: FinSaisie) {
                break
            } catch (erreur: IllegalArgumentException) {
                afficher("Erreur : ${erreur.message}")
            } catch (erreur: IllegalStateException) {
                afficher("Erreur : ${erreur.message}")
            }
        }
        afficher("Session terminée.")
    }

    private fun executer(choix: String) {
        when (choix) {
            "1" -> {
                val matricule = demander("Matricule")
                val nom = demander("Nom")
                service.ajouterEmploye(Employe(matricule, nom))
                afficher("Employé enregistré.")
            }
            "2" -> {
                val matricule = demander("Matricule")
                val nom = demander("Nom")
                val specialite = demander("Spécialité")
                service.ajouterEmploye(TechnicienSupport(matricule, nom, specialite))
                afficher("Technicien enregistré.")
            }
            "3" -> {
                val reference = demander("Référence")
                val nom = demander("Nom")
                val type = demander("Type : PC, Imprimante, Serveur ou autre")
                service.ajouterEquipement(Equipement(reference, nom, type))
                afficher("Équipement enregistré.")
            }
            "4" -> afficherAnnuaire()
            "5" -> {
                service.declarerPanne(demander("Référence de l'équipement"))
                afficher("Panne déclarée.")
            }
            "6" -> {
                service.reparerEquipement(demander("Référence de l'équipement"))
                afficher("Équipement remis en service.")
            }
            "7" -> ouvrirTicket()
            "8" -> {
                val numero = demander("Numéro du ticket")
                val matricule = demander("Matricule du technicien")
                service.affecterTechnicien(numero, matricule)
                afficher("Technicien affecté.")
            }
            "9" -> {
                val numero = demander("Numéro du ticket")
                val description = demander("Description de l'intervention")
                service.ajouterIntervention(numero, description)
                afficher("Intervention enregistrée.")
            }
            "10" -> {
                val numero = demander("Numéro du ticket")
                val confirmation = demander("Le problème est-il effectivement traité ? oui/non")
                if (confirmation.equals("oui", ignoreCase = true)) {
                    service.confirmerTraitement(numero)
                    afficher("Traitement confirmé.")
                } else {
                    afficher("Traitement non confirmé ; ticket inchangé.")
                }
            }
            "11" -> {
                service.resoudreTicket(demander("Numéro du ticket"))
                afficher("Ticket résolu.")
            }
            "12" -> {
                service.cloturerTicket(demander("Numéro du ticket"))
                afficher("Ticket clôturé.")
            }
            "13" -> {
                val tickets = service.consulterTickets()
                if (tickets.isEmpty()) afficher("Aucun ticket enregistré.")
                tickets.forEach { afficher("${it.numero} | ${it.statut} | ${it.incident.description}") }
            }
            "14" -> afficherTicket()
            "15" -> {
                val erreurs = service.erreursNotification
                if (erreurs.isEmpty()) afficher("Aucun échec de notification.")
                erreurs.forEach(afficher)
            }
            else -> afficher("Choix inconnu. Saisir un numéro du menu.")
        }
    }

    private fun demander(libelle: String): String {
        afficher("$libelle :")
        val valeur = lire()?.trim() ?: throw FinSaisie()
        require(valeur.isNotBlank()) { "$libelle obligatoire." }
        return valeur
    }

    private fun ouvrirTicket() {
        val numero = demander("Numéro du ticket")
        val matricule = demander("Matricule du déclarant")
        val reference = demander("Référence de l'équipement")
        val nature = demander("Nature : 1 = matériel, 2 = logiciel")
        require(nature == "1" || nature == "2") { "La nature doit être 1 ou 2." }
        val description = demander("Description de l'incident")
        val incident = if (nature == "1") {
            IncidentMateriel(description)
        } else {
            IncidentLogiciel(description, demander("Logiciel concerné"))
        }
        val ticket = service.ouvrirTicket(numero, matricule, reference, incident)
        afficher("Ticket ${ticket.numero} ouvert. ${incident.diagnostiquer()}")
    }

    private fun afficherAnnuaire() {
        afficher("Employés et techniciens :")
        service.consulterEmployes().forEach {
            val role = if (it is TechnicienSupport) "Technicien : ${it.specialite}" else "Employé"
            afficher("${it.matricule} | ${it.nom} | $role")
        }
        afficher("Équipements :")
        service.consulterEquipements().forEach {
            afficher("${it.reference} | ${it.nom} | ${it.type} | ${if (it.enPanne) "En panne" else "Opérationnel"} | Pannes : ${it.nombrePannes}")
        }
    }

    private fun afficherTicket() {
        val ticket = service.rechercherTicket(demander("Numéro du ticket"))
        if (ticket == null) {
            afficher("Ticket introuvable.")
            return
        }
        afficher("${ticket.numero} | ${ticket.statut} | Déclarant : ${ticket.declarant.nom}")
        afficher("Équipement : ${ticket.equipement.reference} | Technicien : ${ticket.technicien?.nom ?: "Non affecté"}")
        afficher("Incident : ${ticket.incident.description}")
        afficher("Diagnostic : ${ticket.incident.diagnostiquer()}")
        ticket.historique.forEach(afficher)
    }

    private class FinSaisie : RuntimeException()

    companion object {
        private val MENU = """
            |--- IT Helpdesk ---
            |1. Enregistrer un employé       2. Enregistrer un technicien
            |3. Ajouter un équipement       4. Consulter l'annuaire et le parc
            |5. Déclarer une panne          6. Réparer un équipement
            |7. Ouvrir un ticket            8. Affecter un technicien
            |9. Ajouter une intervention   10. Confirmer le traitement
            |11. Résoudre un ticket        12. Clôturer un ticket
            |13. Lister les tickets        14. Rechercher un ticket et son historique
            |15. Voir les échecs de notification
            |0. Quitter
            |Votre choix :
        """.trimMargin()
    }
}
