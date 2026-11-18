package projet01_helpdesk.app

import projet01_helpdesk.incidents.Incident
import projet01_helpdesk.incidents.IncidentLogiciel
import projet01_helpdesk.incidents.IncidentMateriel
import projet01_helpdesk.materiel.Equipement
import projet01_helpdesk.personnes.Employe
import projet01_helpdesk.personnes.TechnicienSupport

fun main() {
    println("===== IT HELPDESK — INCIDENTS =====")
    val employe = Employe("EM001", "Aya")
    val technicien = TechnicienSupport("TECH001", "Sara", "matériel et réseaux")
    listOf(employe, technicien).forEach {
        it.afficherInformations()
        it.travailler()
    }
    val pc = Equipement("PC001", "PC portable d'Aya", "PC")
    pc.declarerPanne()
    pc.afficherInformations()
    val incidents: List<Incident> = listOf(
        IncidentMateriel("Le PC ne démarre plus."),
        IncidentLogiciel("Impossible de se connecter.", "ERP")
    )
    incidents.forEach { println("${it.description} : ${it.diagnostiquer()}") }
}
