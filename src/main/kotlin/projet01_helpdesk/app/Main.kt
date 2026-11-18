package projet01_helpdesk.app

import projet01_helpdesk.materiel.Equipement
import projet01_helpdesk.personnes.Employe
import projet01_helpdesk.personnes.TechnicienSupport

fun main() {
    println("===== IT HELPDESK — ÉTAPE 1 =====")
    val employe = Employe("EM001", "Aya")
    val technicien = TechnicienSupport("TECH001", "Sara", "matériel et réseaux")

    // Le type commun Employe permet d'observer le polymorphisme.
    val equipe: List<Employe> = listOf(employe, technicien)
    for (personne in equipe) {
        personne.afficherInformations()
        personne.travailler()
    }
    technicien.diagnostiquer("Le PC portable ne démarre plus.")

    println("\n===== CYCLE DES PANNES =====")
    val pc = Equipement("PC001", "PC portable d'Aya", "PC")
    val imprimante = Equipement("IMP001", "Imprimante du bureau", "Imprimante")
    val serveur = Equipement("SRV001", "Serveur de fichiers", "Serveur")
    listOf(pc, imprimante, serveur).forEach { it.afficherInformations() }
    check(!pc.enPanne && pc.nombrePannes == 0)

    verifierEtatInvalide("Réparation sans panne") { pc.reparer() }
    check(!pc.enPanne && pc.nombrePannes == 0)

    pc.declarerPanne()
    check(pc.enPanne && pc.nombrePannes == 1)
    pc.afficherInformations()

    verifierEtatInvalide("Double déclaration de panne") { pc.declarerPanne() }
    check(pc.enPanne && pc.nombrePannes == 1)

    pc.reparer()
    check(!pc.enPanne && pc.nombrePannes == 1)
    verifierEtatInvalide("Double réparation") { pc.reparer() }
    check(!pc.enPanne && pc.nombrePannes == 1)

    pc.declarerPanne()
    check(pc.enPanne && pc.nombrePannes == 2)
    pc.reparer()
    check(!pc.enPanne && pc.nombrePannes == 2)
    check(imprimante.nombrePannes == 0 && serveur.nombrePannes == 0)
    pc.afficherInformations()

    println("\n===== VALIDATION DES INFORMATIONS =====")
    // Chaque champ obligatoire doit refuser une chaîne vide ou composée d'espaces.
    for (valeurInvalide in listOf("", " \t\n")) {
        verifierArgumentInvalide("Matricule") { Employe(valeurInvalide, "Aya") }
        verifierArgumentInvalide("Nom de l'employé") { Employe("EM002", valeurInvalide) }
        verifierArgumentInvalide("Matricule du technicien") {
            TechnicienSupport(valeurInvalide, "Sara", "Réseaux")
        }
        verifierArgumentInvalide("Nom du technicien") {
            TechnicienSupport("TECH002", valeurInvalide, "Réseaux")
        }
        verifierArgumentInvalide("Spécialité") {
            TechnicienSupport("TECH002", "Sara", valeurInvalide)
        }
        verifierArgumentInvalide("Référence") { Equipement(valeurInvalide, "PC", "PC") }
        verifierArgumentInvalide("Nom de l'équipement") { Equipement("PC002", valeurInvalide, "PC") }
        verifierArgumentInvalide("Type") { Equipement("PC002", "PC", valeurInvalide) }
        verifierArgumentInvalide("Description") { technicien.diagnostiquer(valeurInvalide) }
    }
    println("\nToutes les vérifications de l'étape 1 ont réussi.")
}

private fun verifierArgumentInvalide(libelle: String, action: () -> Unit) {
    val erreur = runCatching(action).exceptionOrNull()
    check(erreur is IllegalArgumentException) { "$libelle : une erreur de validation était attendue." }
    println("Refus attendu — $libelle : ${erreur.message}")
}

private fun verifierEtatInvalide(libelle: String, action: () -> Unit) {
    val erreur = runCatching(action).exceptionOrNull()
    check(erreur is IllegalStateException) { "$libelle : une erreur d'état était attendue." }
    println("Refus attendu — $libelle : ${erreur.message}")
}
