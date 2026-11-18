package projet01_helpdesk.materiel

class Equipement(val reference: String, val nom: String, val type: String) {
    var enPanne: Boolean = false
        private set

    var nombrePannes: Int = 0
        private set

    init {
        require(reference.isNotBlank()) { "La référence est obligatoire." }
        require(nom.isNotBlank()) { "Le nom de l'équipement est obligatoire." }
        require(type.isNotBlank()) { "Le type de l'équipement est obligatoire." }
    }

    fun declarerPanne() {
        check(!enPanne) { "L'équipement $reference est déjà en panne." }
        enPanne = true
        nombrePannes++
    }

    fun reparer() {
        check(enPanne) { "L'équipement $reference fonctionne déjà." }
        enPanne = false
    }

    fun afficherInformations() {
        val etat = if (enPanne) "En panne" else "Opérationnel"
        println("Équipement : $reference | $nom | $type | $etat | Pannes : $nombrePannes")
    }
}
