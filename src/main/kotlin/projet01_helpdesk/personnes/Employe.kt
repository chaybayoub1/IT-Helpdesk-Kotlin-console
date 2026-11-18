package projet01_helpdesk.personnes

open class Employe(val matricule: String, val nom: String) {
    init {
        require(matricule.isNotBlank()) { "Le matricule est obligatoire." }
        require(nom.isNotBlank()) { "Le nom de l'employé est obligatoire." }
    }

    open fun afficherInformations() {
        println("Employé : $matricule | $nom")
    }

    open fun travailler() {
        println("$nom effectue son travail.")
    }
}
