package projet01_helpdesk.personnes

class TechnicienSupport(
    matricule: String,
    nom: String,
    val specialite: String
) : Employe(matricule, nom) {
    init {
        require(specialite.isNotBlank()) { "La spécialité est obligatoire." }
    }

    override fun afficherInformations() {
        super.afficherInformations()
        println("Spécialité : $specialite")
    }

    override fun travailler() {
        println("$nom traite les demandes de support en $specialite.")
    }

    fun diagnostiquer(description: String) {
        require(description.isNotBlank()) { "La description du problème est obligatoire." }
        println("$nom analyse : $description")
        println("Diagnostic initial : vérifier les connexions et le fonctionnement de l'équipement.")
    }
}
